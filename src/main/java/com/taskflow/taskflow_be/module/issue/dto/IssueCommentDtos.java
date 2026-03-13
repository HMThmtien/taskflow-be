package com.taskflow.taskflow_be.module.issue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class IssueCommentDtos {

    @Getter @Setter
    public static class CreateReq {
        @NotBlank
        private String content;
    }

    @Getter
    @Setter
    @Builder
    public static class MentionedUserResponse {
        private UUID id;
        private String username;
        private String fullName;
    }

    @Builder @Getter
    public static class CommentRes {
        private UUID id;
        private UUID issueId;
        private UUID authorId;
        private String authorUsername;
        private String authorFullName;
        private String content;
        private Instant createdAt;
        private List<MentionedUserResponse> mentions;

    }
}