package com.taskflow.taskflow_be.module.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class NotificationDtos {

    @Getter
    @Setter
    public static class ActorResponse {
        private UUID id;
        private String username;
        private String fullName;
    }

    @Getter
    @Setter
    public static class TargetResponse {
        private String entityType;
        private UUID entityId;
        private String route;
    }

    @Getter
    @Setter
    public static class NotificationResponse {
        private UUID id;
        private String type;
        private String title;
        private String body;
        private boolean isRead;
        private Instant createdAt;
        private ActorResponse actor;
        private TargetResponse target;
    }

    @Getter
    @Setter
    public static class NotificationPageResponse {
        private List<NotificationResponse> items;
        private int page;
        private int pageSize;
        private long total;
        private long unreadCount;
    }
}