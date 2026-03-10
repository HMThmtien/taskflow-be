package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceReportDtos;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkspaceReportServiceImpl implements WorkspaceReportService {

    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public WorkspaceReportServiceImpl(
            IssueRepository issueRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WorkspaceReportDtos.WorkspaceReportResponse getWorkspaceReport(
            LocalDate from,
            LocalDate to,
            UUID projectId
    ) {
        UUID currentUserId = getCurrentUser().getId();

        LocalDate safeFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate safeTo = to != null ? to : LocalDate.now();

        var memberships = projectMemberRepository.findAllByUserId(currentUserId);
        var visibleProjectIds = memberships.stream()
                .map(m -> m.getProject().getId())
                .collect(Collectors.toSet());

        Specification<IssueEntity> spec = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(root.get("project").get("id").in(visibleProjectIds));

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<IssueEntity> issues = issueRepository.findAll(spec);

        Set<UUID> projectIdsInScope = issues.stream()
                .map(i -> i.getProject().getId())
                .collect(Collectors.toSet());

        var totals = new WorkspaceReportDtos.Totals();
        totals.setTotalProjects(projectIdsInScope.size());
        totals.setTotalIssues(issues.size());
        totals.setOpenIssues(issues.stream().filter(i -> i.getStatus() != IssueStatus.DONE).count());
        totals.setDoneIssues(issues.stream().filter(i -> i.getStatus() == IssueStatus.DONE).count());
        totals.setOverdueIssues(issues.stream()
                .filter(i -> i.getDueDate() != null)
                .filter(i -> i.getDueDate().isBefore(LocalDate.now()))
                .filter(i -> i.getStatus() != IssueStatus.DONE)
                .count());

        List<WorkspaceReportDtos.StatusCount> issuesByStatus =
                Arrays.stream(com.taskflow.taskflow_be.module.issue.entity.IssueStatus.values())
                        .map(status -> {
                            var row = new WorkspaceReportDtos.StatusCount();
                            row.setStatus(status.name());
                            row.setCount(issues.stream().filter(i -> i.getStatus() == status).count());
                            return row;
                        })
                        .toList();

        List<WorkspaceReportDtos.PriorityCount> issuesByPriority =
                Arrays.stream(com.taskflow.taskflow_be.module.issue.entity.IssuePriority.values())
                        .map(priority -> {
                            var row = new WorkspaceReportDtos.PriorityCount();
                            row.setPriority(priority.name());
                            row.setCount(issues.stream().filter(i -> i.getPriority() == priority).count());
                            return row;
                        })
                        .toList();

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        Map<UUID, List<IssueEntity>> groupedByAssignee = issues.stream()
                .filter(i -> i.getAssignee() != null)
                .collect(Collectors.groupingBy(i -> i.getAssignee().getId()));

        List<WorkspaceReportDtos.UserWorkload> workloadByUser = groupedByAssignee.values().stream()
                .map(userIssues -> {
                    var first = userIssues.get(0).getAssignee();

                    var row = new WorkspaceReportDtos.UserWorkload();
                    row.setUserId(first.getId());
                    row.setUsername(first.getUsername());
                    row.setFullName(first.getFullName());
                    row.setAssignedOpenIssues(
                            userIssues.stream()
                                    .filter(i -> i.getStatus() != IssueStatus.DONE)
                                    .count()
                    );
                    row.setDoneThisWeek(
                            userIssues.stream()
                                    .filter(i -> i.getStatus() == IssueStatus.DONE)
                                    .filter(i -> i.getUpdatedAt() != null)
                                    .filter(i -> !i.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(weekStart))
                                    .count()
                    );
                    return row;
                })
                .sorted(Comparator.comparingLong(WorkspaceReportDtos.UserWorkload::getAssignedOpenIssues).reversed())
                .toList();

        var response = new WorkspaceReportDtos.WorkspaceReportResponse();
        response.setFrom(safeFrom);
        response.setTo(safeTo);
        response.setTotals(totals);
        response.setIssuesByStatus(issuesByStatus);
        response.setIssuesByPriority(issuesByPriority);
        response.setWorkloadByUser(workloadByUser);

        return response;
    }

    private UserEntity getCurrentUser() {
        var username = SecurityUtils.currentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}