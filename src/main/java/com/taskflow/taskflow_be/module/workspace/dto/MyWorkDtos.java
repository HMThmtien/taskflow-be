package com.taskflow.taskflow_be.module.workspace.dto;

import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MyWorkDtos {

    @Getter
    @Setter
    public static class SimpleUserResponse {
        private UUID id;
        private String username;
        private String fullName;
    }

    @Getter
    @Setter
    public static class IssueSummaryResponse {
        private UUID id;
        private String key;
        private String title;
        private String description;
        private IssueStatus status;
        private IssuePriority priority;
        private LocalDate dueDate;
        private Instant updatedAt;
        private UUID projectId;
        private String projectKey;
        private String projectName;
        private SimpleUserResponse assignee;
        private List<String> labels;
    }
}