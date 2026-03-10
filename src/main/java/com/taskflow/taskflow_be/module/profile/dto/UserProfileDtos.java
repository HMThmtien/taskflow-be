package com.taskflow.taskflow_be.module.profile.dto;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public class UserProfileDtos {

    @Getter
    @Setter
    public static class MeResponse {
        private UUID id;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String bio;
        private String jobTitle;
        private String timezone;
        private String locale;
        private GlobalRole role;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastLoginAt;
    }

    @Getter
    @Setter
    public static class UpdateProfileRequest {
        @NotBlank
        @Size(max = 120)
        private String fullName;

        @Email
        @Size(max = 255)
        private String email;

        @Size(max = 120)
        private String jobTitle;

        @Size(max = 5000)
        private String bio;

        @Size(max = 80)
        private String timezone;

        @Size(max = 20)
        private String locale;
    }
}