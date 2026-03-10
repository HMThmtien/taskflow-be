package com.taskflow.taskflow_be.module.issue.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class IssueActivityDtos {

    @Builder @Getter
    public static class ActivityRes {
        private UUID id;
        private UUID issueId;
        private UUID actorId;
        private String actorUsername;
        private String type;
        private Map<String, Object> payload;
        private Instant createdAt;
    }
}