package com.taskflow.taskflow_be.module.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class ProjectDtos {

    @Getter @Setter
    public static class CreateProjectRequest {
        @NotBlank @Size(max = 20)
        private String key;

        @NotBlank @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;
    }

    @Getter @Setter
    public static class UpdateProjectRequest {
        @NotBlank @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;
    }

    @Builder @Getter
    public static class ProjectResponse {
        private UUID id;
        private String key;
        private String name;
        private String description;
        private Instant createdAt;
    }
}