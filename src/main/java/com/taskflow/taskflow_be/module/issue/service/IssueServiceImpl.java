package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.entity.IssueType;
import com.taskflow.taskflow_be.module.issue.mapper.IssueMapper;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueSpecs;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepo;
    private final ProjectRepository projectRepo;
    private final ProjectPermissionService permission;
    private final UserRepository userRepo;
    private final IssueActivityLogService activityLogService;

    @Override
    @Transactional(readOnly = true)
    public Page<IssueDtos.IssueResponse> list(
            UUID projectId,
            String q,
            IssueStatus status,
            IssuePriority priority,
            UUID assigneeId,
            LocalDate dueFrom,
            LocalDate dueTo,
            String label,
            Pageable pageable
    ) {
        UUID userId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, userId);

        projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        Specification<IssueEntity> spec = IssueSpecs.projectId(projectId);

        if (q != null && !q.trim().isBlank()) spec = spec.and(IssueSpecs.titleContains(q.trim()));
        if (status != null) spec = spec.and(IssueSpecs.status(status));
        if (priority != null) spec = spec.and(IssueSpecs.priority(priority));
        if (assigneeId != null) spec = spec.and(IssueSpecs.assigneeId(assigneeId));
        if (dueFrom != null) spec = spec.and(IssueSpecs.dueFrom(dueFrom));
        if (dueTo != null) spec = spec.and(IssueSpecs.dueTo(dueTo));
        if (label != null && !label.isBlank()) spec = spec.and(IssueSpecs.hasLabel(label.trim()));

        return issueRepo.findAll(spec, pageable).map(IssueMapper::toResponse);
    }

    @Override
    public IssueDtos.IssueResponse create(UUID projectId, IssueDtos.CreateIssueRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        permission.requireWrite(projectId, userId);

        var project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        var reporter = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String title = req.getTitle() == null ? null : req.getTitle().trim();
        if (title == null || title.isBlank()) {
            throw new com.taskflow.taskflow_be.exception.AppException(
                    ErrorCode.BAD_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "title must not be blank"
            );
        }

        IssueStatus statusToUse = (req.getStatus() != null) ? req.getStatus() : IssueStatus.TODO;
        int nextPos = issueRepo.maxPosition(projectId, statusToUse) + 1;

        IssueEntity e = IssueEntity.builder()
                .project(project)
                .title(title)
                .description(req.getDescription())
                .status(statusToUse)
                .priority(req.getPriority() != null ? req.getPriority() : IssuePriority.MEDIUM)
                .position(nextPos)
                .reporter(reporter)
                .assignee(resolveAssignee(projectId, req.getAssigneeId()))
                .dueDate(req.getDueDate())
                .labels(normalizeLabels(req.getLabels()))
                .build();

        IssueEntity saved = issueRepo.save(e);

        activityLogService.log(
                userId,
                saved,
                com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.ISSUE_CREATED,
                java.util.Map.of(
                        "title", saved.getTitle(),
                        "status", saved.getStatus().name(),
                        "priority", saved.getPriority().name()
                )
        );

        return IssueMapper.toResponse(saved);
    }

    @Override
    public IssueDtos.IssueResponse update(UUID projectId, UUID issueId, IssueDtos.UpdateIssueRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        permission.requireWrite(projectId, userId);

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        if (!issue.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }

        IssueStatus oldStatus = issue.getStatus();
        IssuePriority oldPriority = issue.getPriority();
        UUID oldAssigneeId = issue.getAssignee() == null ? null : issue.getAssignee().getId();
        LocalDate oldDueDate = issue.getDueDate();
        List<String> oldLabels = issue.getLabels();

        if (req.getTitle() != null) {
            String title = req.getTitle().trim();
            if (title.isBlank()) {
                throw new com.taskflow.taskflow_be.exception.AppException(
                        ErrorCode.BAD_REQUEST,
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "title must not be blank"
                );
            }
            issue.setTitle(title);
        }

        if (req.getDescription() != null) issue.setDescription(req.getDescription());
        if (req.getPriority() != null) issue.setPriority(req.getPriority());

        if (req.getAssigneeId() != null) {
            issue.setAssignee(resolveAssignee(projectId, req.getAssigneeId()));
        }

        if (req.getDueDate() != null) {
            issue.setDueDate(req.getDueDate());
        }

        if (req.getLabels() != null) {
            issue.setLabels(normalizeLabels(req.getLabels()));
        }

        if (req.getStatus() != null && req.getStatus() != issue.getStatus()) {
            IssueStatus nextStatus = req.getStatus();
            int nextPos = issueRepo.maxPosition(projectId, nextStatus) + 1;
            issue.setStatus(nextStatus);
            issue.setPosition(nextPos);
        }

        issue = issueRepo.save(issue);

        if (oldStatus != issue.getStatus()) {
            activityLogService.log(
                    userId,
                    issue,
                    com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.STATUS_CHANGED,
                    java.util.Map.of("from", oldStatus.name(), "to", issue.getStatus().name())
            );
        }

        if (oldPriority != issue.getPriority()) {
            activityLogService.log(
                    userId,
                    issue,
                    com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.PRIORITY_CHANGED,
                    java.util.Map.of("from", oldPriority.name(), "to", issue.getPriority().name())
            );
        }

        UUID newAssigneeId = issue.getAssignee() == null ? null : issue.getAssignee().getId();
        if (!java.util.Objects.equals(oldAssigneeId, newAssigneeId)) {
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("from", oldAssigneeId == null ? "" : oldAssigneeId.toString());
            payload.put("to", newAssigneeId == null ? "" : newAssigneeId.toString());

            activityLogService.log(
                    userId,
                    issue,
                    com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.ASSIGNEE_CHANGED,
                    payload
            );
        }

        if (!java.util.Objects.equals(oldDueDate, issue.getDueDate())) {
            java.util.Map<String, String> payload = new java.util.HashMap<>();
            payload.put("from", oldDueDate == null ? "" : oldDueDate.toString());
            payload.put("to", issue.getDueDate() == null ? "" : issue.getDueDate().toString());

            activityLogService.log(
                    userId,
                    issue,
                    com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.DUE_DATE_CHANGED,
                    payload
            );
        }

        if (!java.util.Objects.equals(oldLabels, issue.getLabels())) {
            java.util.Map<String, String> payload = new java.util.HashMap<>();

            String oldLabelsStr = oldLabels == null ? "" : String.join(",", oldLabels);
            String newLabelsStr = issue.getLabels() == null ? "" : String.join(",", issue.getLabels());

            payload.put("from", oldLabelsStr);
            payload.put("to", newLabelsStr);

            activityLogService.log(
                    userId,
                    issue,
                    com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.LABELS_CHANGED,
                    payload
            );
        }

        return IssueMapper.toResponse(issue);
    }

    // helper: assignee phải là member project (optional nhưng chuẩn)
    private com.taskflow.taskflow_be.module.auth.entity.UserEntity resolveAssignee(UUID projectId, UUID assigneeId) {
        if (assigneeId == null) return null;
        permission.requireMember(projectId, assigneeId); // đảm bảo assignee nằm trong project
        return userRepo.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "Assignee not found"));
    }

    @Override
    public IssueDtos.IssueResponse move(UUID projectId, UUID issueId, IssueDtos.MoveIssueRequest req) {
        UUID userId = SecurityUtils.currentUserId();
        permission.requireWrite(projectId, userId);

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        if (!issue.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }

        if (req.getStatus() == null) {
            throw new com.taskflow.taskflow_be.exception.AppException(
                    ErrorCode.BAD_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "status is required"
            );
        }

        IssueStatus oldStatus = issue.getStatus();
        IssueStatus nextStatus = req.getStatus();

        if (oldStatus == nextStatus) return IssueMapper.toResponse(issue);

        int nextPos = issueRepo.maxPosition(projectId, nextStatus) + 1;
        issue.setStatus(nextStatus);
        issue.setPosition(nextPos);

        activityLogService.log(userId, issue,
                com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.ISSUE_MOVED,
                java.util.Map.of("from", oldStatus.name(), "to", nextStatus.name()));

        return IssueMapper.toResponse(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueDtos.IssueResponse> listSubtasks(UUID projectId, UUID issueId) {

        UUID userId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, userId);

        var parent = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        if (!parent.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found");
        }

        return issueRepo.findByParentIssue_Id(issueId)
                .stream()
                .map(IssueMapper::toResponse)
                .toList();
    }

    @Override
    public IssueDtos.IssueResponse createSubtask(UUID projectId, UUID parentIssueId, IssueDtos.CreateIssueRequest req) {

        UUID userId = SecurityUtils.currentUserId();
        permission.requireWrite(projectId, userId);

        var project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        var parent = issueRepo.findById(parentIssueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Parent issue not found"));

        if (!parent.getProject().getId().equals(projectId)) {
            throw new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Parent issue not in project");
        }

        var reporter = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String title = req.getTitle() == null ? null : req.getTitle().trim();
        if (title == null || title.isBlank()) {
            throw new com.taskflow.taskflow_be.exception.AppException(
                    ErrorCode.BAD_REQUEST,
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "title must not be blank"
            );
        }

        int nextPos = issueRepo.maxPosition(projectId, IssueStatus.TODO) + 1;

        IssueEntity e = IssueEntity.builder()
                .project(project)
                .title(title)
                .description(req.getDescription())
                .status(IssueStatus.TODO)
                .priority(req.getPriority() != null ? req.getPriority() : IssuePriority.MEDIUM)
                .type(req.getType() != null ? req.getType() : IssueType.TASK)
                .parentIssue(parent)
                .position(nextPos)
                .reporter(reporter)
                .assignee(resolveAssignee(projectId, req.getAssigneeId()))
                .dueDate(req.getDueDate())
                .labels(normalizeLabels(req.getLabels()))
                .build();

        IssueEntity saved = issueRepo.save(e);

        activityLogService.log(
                userId,
                saved,
                com.taskflow.taskflow_be.module.issue.entity.IssueActivityType.ISSUE_CREATED,
                java.util.Map.of(
                        "title", saved.getTitle(),
                        "type", "SUBTASK"
                )
        );

        return IssueMapper.toResponse(saved);
    }


    private List<String> normalizeLabels(List<String> labels) {
        if (labels == null) return List.of();
        return labels.stream()
                .filter(s -> s != null && !s.trim().isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}