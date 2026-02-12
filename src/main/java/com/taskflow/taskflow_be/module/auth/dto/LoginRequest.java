package com.taskflow.taskflow_be.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 20, message = "username must be 3-20 chars")
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9._-]{2,19}$",
                message = "username must start with a letter and contain only letters, numbers, . _ -")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 6, max = 72, message = "password must be 6-72 chars")
        String password
) {}
