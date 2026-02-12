package com.taskflow.taskflow_be.module.auth.dto;

public record AuthResponse(
        String accessToken,
        UserView user
) {
    public record UserView(String username, String role) {}
}
