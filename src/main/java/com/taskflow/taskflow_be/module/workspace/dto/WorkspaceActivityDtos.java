package com.taskflow.taskflow_be.module.workspace.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class WorkspaceActivityDtos {

    @Getter
    @Setter
    public static class ActorResponse {
        private UUID id;
        private String username;
        private String fullName;
    }

    @Getter
    @Setter
    public static class ProjectResponse {
        private UUID id;
        private String key;
        private String name;
    }

    @Getter
    @Setter
    public static class TargetResponse {
        private String entityType;
        private UUID entityId;
        private String label;
        private String route;
    }

    @Getter
    @Setter
    public static class ActivityResponse {
        private UUID id;
        private String type;
        private String message;
        private Instant createdAt;
        private ActorResponse actor;
        private ProjectResponse project;
        private TargetResponse target;
    }

    @Getter
    @Setter
    public static class ActivityPageResponse {
        private List<ActivityResponse> items;
        private int page;
        private int pageSize;
        private long total;
    }
}