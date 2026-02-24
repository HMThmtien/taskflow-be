package com.taskflow.taskflow_be.module.issue.dto;

import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
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
    }

    @Getter @Setter
    public static class UpdateIssueRequest {
        @NotBlank @Size(max = 300)
        private String title;

        @Size(max = 5000)
        private String description;

        private IssueStatus status;
        private IssuePriority priority;
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
        private Instant createdAt;
        private Instant updatedAt;
    }
}