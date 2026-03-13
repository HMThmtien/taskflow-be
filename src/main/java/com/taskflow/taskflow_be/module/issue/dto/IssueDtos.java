package com.taskflow.taskflow_be.module.issue.dto;

import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.entity.IssueType;
import com.taskflow.taskflow_be.module.sprint.entity.SprintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class IssueDtos {

    @Getter @Setter
    public static class CreateIssueRequest {
        @NotBlank @Size(max = 300)
        private String title;

        @Size(max = 5000)
        private String description;

        private IssueStatus status;     // optional
        private IssuePriority priority; // optional

        // ✅ NEW
        private UUID assigneeId;        // optional
        private LocalDate dueDate;      // optional
        private List<String> labels;    // optional
        private IssueType type;
        private UUID parentIssueId;
    }

    @Getter @Setter
    public static class UpdateIssueRequest {
        @Size(max = 300)
        private String title;

        @Size(max = 5000)
        private String description;

        private IssueStatus status;
        private IssuePriority priority;

        // ✅ NEW
        private UUID assigneeId;
        private LocalDate dueDate;
        private List<String> labels;
        private IssueType type;
        private UUID parentIssueId;
    }

    @Getter @Setter
    public static class MoveIssueRequest {
        private IssueStatus status; // required
    }

    @Builder @Getter
    public static class IssueResponse {
        private UUID id;
        private UUID projectId;
        private String title;
        private String description;
        private IssueStatus status;
        private IssuePriority priority;
        private IssueType type;
        private UUID parentIssueId;
        private String parentIssueTitle;

        // ✅ NEW
        private Integer position;
        private UUID reporterId;
        private String reporterUsername;
        private UUID assigneeId;
        private String assigneeUsername;
        private UUID sprintId;
        private String sprintName;
        private SprintStatus sprintStatus;
        private LocalDate dueDate;
        private List<String> labels;

        private Instant createdAt;
        private Instant updatedAt;
    }
}
