package com.taskflow.taskflow_be.module.issue.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public class IssueAttachmentDtos {

    @Getter
    @Setter
    @Builder
    public static class AttachmentRes {
        private UUID id;
        private UUID issueId;
        private UUID uploadedBy;
        private String uploadedByUsername;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String storagePath;
        private Instant createdAt;
    }
}