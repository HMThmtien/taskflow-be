package com.taskflow.taskflow_be.module.auth.dto;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;

import java.util.UUID;


public class AuthDtos {
    public record MeResponse(UUID id, String username, GlobalRole role) {}
}
