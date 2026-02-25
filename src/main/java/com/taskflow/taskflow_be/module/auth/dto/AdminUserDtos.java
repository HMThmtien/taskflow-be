package com.taskflow.taskflow_be.module.auth.dto;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class AdminUserDtos {

    @Builder
    @Getter
    public static class UserResponse {
        private UUID id;
        private String username;
        private GlobalRole role;
        private Instant createdAt;
    }
}