package com.taskflow.taskflow_be.module.sprint.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.mapper.IssueMapper;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.sprint.dto.SprintDtos;
import com.taskflow.taskflow_be.module.sprint.entity.SprintCompletionAction;
import com.taskflow.taskflow_be.module.sprint.entity.SprintEntity;
import com.taskflow.taskflow_be.module.sprint.entity.SprintStatus;
import com.taskflow.taskflow_be.module.sprint.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final ProjectPermissionService permission;

    @Override
    @Transactional(readOnly = true)
    public List<SprintDtos.SprintResponse> list(UUID projectId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, currentUserId);
        ensureProject(projectId);

        return sprintRepository.findAllByProject_Id(projectId).stream()
                .sorted(sprintComparator())
                .map(sprint -> toResponse(projectId, sprint))
                .toList();
    }

    @Override
    public SprintDtos.SprintResponse create(UUID projectId, SprintDtos.CreateSprintRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        var project = ensureProject(projectId);
        validateDates(req.getStartDate(), req.getEndDate());

        SprintEntity sprint = SprintEntity.builder()
                .project(project)
                .name(normalizeRequired(req.getName(), "Sprint name is required"))
                .goal(normalizeNullable(req.getGoal()))
                .description(normalizeNullable(req.getDescription()))
                .status(SprintStatus.PLANNED)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .position(sprintRepository.maxPosition(projectId) + 1)
                .build();

        return toResponse(projectId, sprintRepository.save(sprint));
    }

    @Override
    public SprintDtos.SprintResponse update(UUID projectId, UUID sprintId, SprintDtos.UpdateSprintRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        SprintEntity sprint = getSprint(projectId, sprintId);
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Completed sprint cannot be updated");
        }

        validateDates(req.getStartDate(), req.getEndDate());
        sprint.setName(normalizeRequired(req.getName(), "Sprint name is required"));
        sprint.setGoal(normalizeNullable(req.getGoal()));
        sprint.setDescription(normalizeNullable(req.getDescription()));
        sprint.setStartDate(req.getStartDate());
        sprint.setEndDate(req.getEndDate());

        return toResponse(projectId, sprintRepository.save(sprint));
    }

    @Override
    public SprintDtos.SprintResponse start(UUID projectId, UUID sprintId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        SprintEntity sprint = getSprint(projectId, sprintId);
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Completed sprint cannot be started");
        }
        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            return toResponse(projectId, sprint);
        }
        ensureNoOtherActiveSprint(projectId, sprintId);

        if (sprint.getStartDate() == null) {
            sprint.setStartDate(LocalDate.now());
        }
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setCompletedAt(null);
        sprint.setCompletionAction(null);
        sprint.setCompletedTargetSprint(null);
        sprint.setCompletedTotalIssues(null);
        sprint.setCompletedDoneIssues(null);
        sprint.setCompletedInProgressIssues(null);
        sprint.setCompletedTodoIssues(null);

        return toResponse(projectId, sprintRepository.save(sprint));
    }

    @Override
    public SprintDtos.SprintResponse complete(UUID projectId, UUID sprintId, SprintDtos.CompleteSprintRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        SprintEntity sprint = getSprint(projectId, sprintId);
        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Only active sprint can be completed");
        }

        SprintMetricsSnapshot metrics = buildMetrics(projectId, sprint);
        var issues = issueRepository.findByProject_IdAndSprint_IdOrderByUpdatedAtDesc(projectId, sprintId);
        var unfinishedIssues = issues.stream()
                .filter(issue -> issue.getStatus() != IssueStatus.DONE)
                .toList();

        SprintCompletionAction completionAction = req.getCompletionAction();
        if (!unfinishedIssues.isEmpty() && completionAction == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Completion action is required when unfinished issues remain");
        }

        SprintEntity targetSprint = null;
        if (completionAction == SprintCompletionAction.MOVE_TO_SPRINT) {
            if (req.getTargetSprintId() == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Target sprint is required");
            }

            targetSprint = getSprint(projectId, req.getTargetSprintId());
            if (targetSprint.getId().equals(sprintId)) {
                throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Target sprint must be different from the active sprint");
            }
            if (targetSprint.getStatus() != SprintStatus.PLANNED) {
                throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Unfinished issues can only be moved to a planned sprint");
            }
        } else if (completionAction == SprintCompletionAction.MOVE_TO_BACKLOG) {
            if (req.getTargetSprintId() != null) {
                throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Target sprint is not allowed when moving issues to backlog");
            }
        }

        if (unfinishedIssues.isEmpty() && completionAction == null) {
            completionAction = SprintCompletionAction.MOVE_TO_BACKLOG;
        }

        for (var issue : unfinishedIssues) {
            issue.setSprint(targetSprint);
        }
        if (!unfinishedIssues.isEmpty()) {
            issueRepository.saveAll(unfinishedIssues);
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(Instant.now());
        sprint.setCompletionAction(completionAction);
        sprint.setCompletedTargetSprint(targetSprint);
        sprint.setCompletedTotalIssues(metrics.totalIssues());
        sprint.setCompletedDoneIssues(metrics.doneIssues());
        sprint.setCompletedInProgressIssues(metrics.inProgressIssues());
        sprint.setCompletedTodoIssues(metrics.todoIssues());
        return toResponse(projectId, sprintRepository.save(sprint));
    }

    @Override
    @Transactional(readOnly = true)
    public SprintDtos.SprintMetricsResponse getMetrics(UUID projectId, UUID sprintId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, currentUserId);

        SprintEntity sprint = getSprint(projectId, sprintId);
        SprintMetricsSnapshot metrics = buildMetrics(projectId, sprint);

        return SprintDtos.SprintMetricsResponse.builder()
                .sprintId(sprintId)
                .totalIssues(metrics.totalIssues())
                .doneIssues(metrics.doneIssues())
                .inProgressIssues(metrics.inProgressIssues())
                .todoIssues(metrics.todoIssues())
                .unfinishedIssues(metrics.unfinishedIssues())
                .completionPercent(metrics.completionPercent())
                .completionAction(sprint.getCompletionAction())
                .targetSprintId(sprint.getCompletedTargetSprint() != null ? sprint.getCompletedTargetSprint().getId() : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueDtos.IssueResponse> listBacklogIssues(UUID projectId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, currentUserId);
        ensureProject(projectId);

        return issueRepository.findByProject_IdAndSprintIsNullOrderByUpdatedAtDesc(projectId).stream()
                .map(IssueMapper::toResponse)
                .toList();
    }

    @Override
    public IssueDtos.IssueResponse assignIssue(UUID projectId, UUID issueId, UUID sprintId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireWrite(projectId, currentUserId);

        var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));
        if (!issue.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }
        if (issue.getSprint() != null && issue.getSprint().getStatus() == SprintStatus.COMPLETED) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Completed sprint history is read-only");
        }

        if (sprintId == null) {
            issue.setSprint(null);
            return IssueMapper.toResponse(issueRepository.save(issue));
        }

        SprintEntity sprint = getSprint(projectId, sprintId);
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Cannot assign issue to completed sprint");
        }

        issue.setSprint(sprint);
        return IssueMapper.toResponse(issueRepository.save(issue));
    }

    private com.taskflow.taskflow_be.module.project.entity.ProjectEntity ensureProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));
    }

    private SprintEntity getSprint(UUID projectId, UUID sprintId) {
        ensureProject(projectId);
        return sprintRepository.findByIdAndProject_Id(sprintId, projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Sprint not found"));
    }

    private SprintDtos.SprintResponse toResponse(UUID projectId, SprintEntity sprint) {
        long issueCount = sprint.getStatus() == SprintStatus.COMPLETED && sprint.getCompletedTotalIssues() != null
                ? sprint.getCompletedTotalIssues()
                : issueRepository.countByProject_IdAndSprint_Id(projectId, sprint.getId());

        return SprintDtos.SprintResponse.builder()
                .id(sprint.getId())
                .projectId(projectId)
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .description(sprint.getDescription())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .completedAt(sprint.getCompletedAt())
                .completionAction(sprint.getCompletionAction())
                .completedTargetSprintId(sprint.getCompletedTargetSprint() != null ? sprint.getCompletedTargetSprint().getId() : null)
                .position(sprint.getPosition())
                .issueCount(issueCount)
                .completedTotalIssues(sprint.getCompletedTotalIssues())
                .completedDoneIssues(sprint.getCompletedDoneIssues())
                .completedInProgressIssues(sprint.getCompletedInProgressIssues())
                .completedTodoIssues(sprint.getCompletedTodoIssues())
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }

    private void ensureNoOtherActiveSprint(UUID projectId, UUID sprintId) {
        if (sprintRepository.findByProject_IdAndStatus(projectId, SprintStatus.ACTIVE)
                .filter(active -> !active.getId().equals(sprintId))
                .isPresent()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.CONFLICT, "Another sprint is already active");
        }
    }

    private SprintMetricsSnapshot buildMetrics(UUID projectId, SprintEntity sprint) {
        if (sprint.getStatus() == SprintStatus.COMPLETED && sprint.getCompletedTotalIssues() != null) {
            long total = sprint.getCompletedTotalIssues();
            long done = valueOrZero(sprint.getCompletedDoneIssues());
            long inProgress = valueOrZero(sprint.getCompletedInProgressIssues());
            long todo = valueOrZero(sprint.getCompletedTodoIssues());
            return new SprintMetricsSnapshot(total, done, inProgress, todo);
        }

        var issues = issueRepository.findByProject_IdAndSprint_IdOrderByUpdatedAtDesc(projectId, sprint.getId());
        long done = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.DONE).count();
        long inProgress = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS).count();
        long todo = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.TODO).count();
        return new SprintMetricsSnapshot(issues.size(), done, inProgress, todo);
    }

    private long valueOrZero(Long value) {
        return value == null ? 0 : value;
    }

    private Comparator<SprintEntity> sprintComparator() {
        return Comparator
                .comparingInt((SprintEntity sprint) -> switch (sprint.getStatus()) {
                    case ACTIVE -> 0;
                    case PLANNED -> 1;
                    case COMPLETED -> 2;
                })
                .thenComparing(SprintEntity::getPosition, Comparator.reverseOrder())
                .thenComparing(sprint -> sprint.getCompletedAt() == null ? Instant.EPOCH : sprint.getCompletedAt(), Comparator.reverseOrder());
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Sprint end date must be after start date");
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private record SprintMetricsSnapshot(long totalIssues, long doneIssues, long inProgressIssues, long todoIssues) {
        long unfinishedIssues() {
            return totalIssues - doneIssues;
        }

        int completionPercent() {
            if (totalIssues <= 0) return 0;
            return (int) Math.round((doneIssues * 100.0) / totalIssues);
        }
    }
}
