package com.taskflow.taskflow_be.module.workspace.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class WorkspaceReportDtos {

    @Getter
    @Setter
    public static class Totals {
        private long totalProjects;
        private long totalIssues;
        private long openIssues;
        private long doneIssues;
        private long overdueIssues;
    }

    @Getter
    @Setter
    public static class StatusCount {
        private String status;
        private long count;
    }

    @Getter
    @Setter
    public static class PriorityCount {
        private String priority;
        private long count;
    }

    @Getter
    @Setter
    public static class UserWorkload {
        private UUID userId;
        private String username;
        private String fullName;
        private long assignedOpenIssues;
        private long doneThisWeek;
    }

    @Getter
    @Setter
    public static class WorkspaceReportResponse {
        private LocalDate from;
        private LocalDate to;
        private Totals totals;
        private List<StatusCount> issuesByStatus;
        private List<PriorityCount> issuesByPriority;
        private List<UserWorkload> workloadByUser;
    }
}