package com.taskflow.taskflow_be.module.projectreport.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProjectReportDtos {

    @Getter
    @Builder
    public static class CountItem {
        private String key;
        private long count;
    }

    @Getter
    @Builder
    public static class OverdueIssueItem {
        private UUID issueId;
        private String title;
        private String status;
        private String priority;
        private LocalDate dueDate;
        private String assigneeUsername;
    }

    @Getter
    @Builder
    public static class ActiveSprintSummary {
        private UUID sprintId;
        private String sprintName;
        private long totalIssues;
        private long doneIssues;
        private long inProgressIssues;
        private long todoIssues;
        private long unfinishedIssues;
        private int completionPercent;
    }

    @Getter
    @Builder
    public static class ProjectSummaryResponse {
        private UUID projectId;
        private long totalIssues;
        private long openIssues;
        private long doneIssues;
        private long overdueIssues;
        private List<CountItem> issuesByStatus;
        private List<CountItem> issuesByPriority;
        private List<OverdueIssueItem> overdueItems;
        private ActiveSprintSummary activeSprint;
    }

    @Getter
    @Builder
    public static class WorkloadItem {
        private UUID userId;
        private String username;
        private String fullName;
        private long totalAssigned;
        private long openAssigned;
        private long overdueAssigned;
    }

    @Getter
    @Builder
    public static class WorkloadResponse {
        private UUID projectId;
        private List<WorkloadItem> items;
    }

    @Getter
    @Builder
    public static class SprintProgressResponse {
        private UUID projectId;
        private ActiveSprintSummary activeSprint;
    }
}
