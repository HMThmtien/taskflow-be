package com.taskflow.taskflow_be.module.project.service;

import java.util.UUID;

public interface ProjectPermissionService {

    void requireMember(UUID projectId, UUID userId);

    void requireWrite(UUID projectId, UUID userId);

    void requireAdmin(UUID projectId, UUID userId);
}