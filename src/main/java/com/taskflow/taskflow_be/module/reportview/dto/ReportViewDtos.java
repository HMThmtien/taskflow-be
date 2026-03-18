package com.taskflow.taskflow_be.module.reportview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public class ReportViewDtos {

    @Getter
    @Setter
    public static class CreateReportViewRequest {
        @NotBlank
        private String routeKey;

        @NotBlank
        private String name;

        private UUID projectId;
        private boolean isDefault;
    }

    @Getter
    @Builder
    public static class ReportViewResponse {
        private UUID id;
        private String routeKey;
        private String name;
        private UUID projectId;
        private boolean isDefault;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
