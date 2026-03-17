package com.taskflow.taskflow_be.module.sprint.dto;

import com.taskflow.taskflow_be.module.sprint.entity.SprintStatus;
import com.taskflow.taskflow_be.module.sprint.entity.SprintCompletionAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SprintDtos {

    @Getter
    @Setter
    public static class CreateSprintRequest {
        @NotBlank
        @Size(max = 200)
        private String name;

        @Size(max = 1000)
        private String goal;

        @Size(max = 4000)
        private String description;

        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Getter
    @Setter
    public static class UpdateSprintRequest {
        @NotBlank
        @Size(max = 200)
        private String name;

        @Size(max = 1000)
        private String goal;

        @Size(max = 4000)
        private String description;

        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Getter
    @Setter
    public static class AssignIssueSprintRequest {
        private UUID sprintId;
    }

    @Getter
    @Setter
    public static class CompleteSprintRequest {
        @NotNull
        private SprintCompletionAction completionAction;

        private UUID targetSprintId;
    }

    @Getter
    @Builder
    public static class SprintMetricsResponse {
        private UUID sprintId;
        private long totalIssues;
        private long doneIssues;
        private long inProgressIssues;
        private long todoIssues;
        private long unfinishedIssues;
        private Integer completionPercent;
        private SprintCompletionAction completionAction;
        private UUID targetSprintId;
    }

    @Getter
    @Builder
    public static class SprintResponse {
        private UUID id;
        private UUID projectId;
        private String name;
        private String goal;
        private String description;
        private SprintStatus status;
        private LocalDate startDate;
        private LocalDate endDate;
        private Instant completedAt;
        private SprintCompletionAction completionAction;
        private UUID completedTargetSprintId;
        private Integer position;
        private long issueCount;
        private Long completedTotalIssues;
        private Long completedDoneIssues;
        private Long completedInProgressIssues;
        private Long completedTodoIssues;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
