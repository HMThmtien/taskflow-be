package com.taskflow.taskflow_be.module.projectreport.service;

import com.taskflow.taskflow_be.config.CacheConfig;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.projectreport.dto.ProjectReportDtos;
import com.taskflow.taskflow_be.module.sprint.entity.SprintStatus;
import com.taskflow.taskflow_be.module.sprint.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectReportServiceImpl implements ProjectReportService {

    private final ProjectRepository projectRepository;
    private final ProjectPermissionService permission;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;
    private final Clock clock = Clock.systemUTC();

    @Override
    @Cacheable(cacheNames = CacheConfig.PROJECT_REPORT_SUMMARY_CACHE, key = "#projectId")
    public ProjectReportDtos.ProjectSummaryResponse getSummary(UUID projectId) {
        requireMember(projectId);
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));

        long totalIssues = issueRepository.countByProject_Id(projectId);
        long doneIssues = issueRepository.countByProject_IdAndStatus(projectId, IssueStatus.DONE);
        long overdueIssues = issueRepository.countByProject_IdAndDueDateBeforeAndStatusNot(projectId, today, IssueStatus.DONE);
        long openIssues = Math.max(0, totalIssues - doneIssues);

        List<ProjectReportDtos.CountItem> issuesByStatus = Arrays.stream(IssueStatus.values())
                .map(status -> MapBackedCount.fromStatus(status.name(), issueRepository.countByStatusForProject(projectId)))
                .map(item -> ProjectReportDtos.CountItem.builder().key(item.key()).count(item.count()).build())
                .toList();

        List<ProjectReportDtos.CountItem> issuesByPriority = Arrays.stream(IssuePriority.values())
                .map(priority -> MapBackedCount.fromPriority(priority.name(), issueRepository.countByPriorityForProject(projectId)))
                .map(item -> ProjectReportDtos.CountItem.builder().key(item.key()).count(item.count()).build())
                .toList();

        List<ProjectReportDtos.OverdueIssueItem> overdueItems = issueRepository
                .findOverdueIssuesForProject(projectId, today, IssueStatus.DONE)
                .stream()
                .limit(8)
                .map(issue -> ProjectReportDtos.OverdueIssueItem.builder()
                        .issueId(issue.getId())
                        .title(issue.getTitle())
                        .status(issue.getStatus().name())
                        .priority(issue.getPriority().name())
                        .dueDate(issue.getDueDate())
                        .assigneeUsername(issue.getAssignee() != null ? issue.getAssignee().getUsername() : null)
                        .build())
                .toList();

        return ProjectReportDtos.ProjectSummaryResponse.builder()
                .projectId(projectId)
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .doneIssues(doneIssues)
                .overdueIssues(overdueIssues)
                .issuesByStatus(issuesByStatus)
                .issuesByPriority(issuesByPriority)
                .overdueItems(overdueItems)
                .activeSprint(buildActiveSprintSummary(projectId))
                .build();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PROJECT_REPORT_WORKLOAD_CACHE, key = "#projectId")
    public ProjectReportDtos.WorkloadResponse getWorkload(UUID projectId) {
        requireMember(projectId);
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));

        List<ProjectReportDtos.WorkloadItem> items = issueRepository.getAssigneeWorkload(projectId, today).stream()
                .map(item -> ProjectReportDtos.WorkloadItem.builder()
                        .userId(item.getUserId())
                        .username(item.getUsername())
                        .fullName(item.getFullName())
                        .totalAssigned(item.getTotalAssigned())
                        .openAssigned(item.getOpenAssigned())
                        .overdueAssigned(item.getOverdueAssigned())
                        .build())
                .toList();

        return ProjectReportDtos.WorkloadResponse.builder()
                .projectId(projectId)
                .items(items)
                .build();
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.PROJECT_REPORT_SPRINT_PROGRESS_CACHE, key = "#projectId")
    public ProjectReportDtos.SprintProgressResponse getSprintProgress(UUID projectId) {
        requireMember(projectId);
        return ProjectReportDtos.SprintProgressResponse.builder()
                .projectId(projectId)
                .activeSprint(buildActiveSprintSummary(projectId))
                .build();
    }

    private ProjectReportDtos.ActiveSprintSummary buildActiveSprintSummary(UUID projectId) {
        return sprintRepository.findByProject_IdAndStatus(projectId, SprintStatus.ACTIVE)
                .map(sprint -> {
                    var issues = issueRepository.findByProject_IdAndSprint_IdOrderByUpdatedAtDesc(projectId, sprint.getId());
                    long total = issues.size();
                    long done = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.DONE).count();
                    long inProgress = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS).count();
                    long todo = issues.stream().filter(issue -> issue.getStatus() == IssueStatus.TODO).count();
                    long unfinished = Math.max(0, total - done);
                    int completionPercent = total <= 0 ? 0 : (int) Math.round((done * 100.0) / total);

                    return ProjectReportDtos.ActiveSprintSummary.builder()
                            .sprintId(sprint.getId())
                            .sprintName(sprint.getName())
                            .totalIssues(total)
                            .doneIssues(done)
                            .inProgressIssues(inProgress)
                            .todoIssues(todo)
                            .unfinishedIssues(unfinished)
                            .completionPercent(completionPercent)
                            .build();
                })
                .orElse(null);
    }

    private void requireMember(UUID projectId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, currentUserId);
        projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));
    }

    private record MapBackedCount(String key, long count) {
        static MapBackedCount fromStatus(
                String status,
                List<IssueRepository.StatusCountProjection> items
        ) {
            var byKey = items.stream().collect(Collectors.toMap(IssueRepository.StatusCountProjection::getKey, IssueRepository.StatusCountProjection::getCount));
            return new MapBackedCount(status, byKey.getOrDefault(status, 0L));
        }

        static MapBackedCount fromPriority(
                String priority,
                List<IssueRepository.PriorityCountProjection> items
        ) {
            var byKey = items.stream().collect(Collectors.toMap(IssueRepository.PriorityCountProjection::getKey, IssueRepository.PriorityCountProjection::getCount));
            return new MapBackedCount(priority, byKey.getOrDefault(priority, 0L));
        }
    }
}
