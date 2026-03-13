package com.taskflow.taskflow_be.module.realtime.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class RealtimeDtos {

    @Getter
    @Builder
    public static class EventEnvelope {
        private UUID id;
        private String type;
        private Instant issuedAt;
        private Object payload;
    }

    @Getter
    @Builder
    public static class IssueCommentEventPayload {
        private UUID issueId;
        private Object comment;
    }

    @Getter
    @Builder
    public static class IssueActivityEventPayload {
        private UUID issueId;
        private Object activity;
    }

    @Getter
    @Builder
    public static class NotificationEventPayload {
        private Object notification;
    }
}
