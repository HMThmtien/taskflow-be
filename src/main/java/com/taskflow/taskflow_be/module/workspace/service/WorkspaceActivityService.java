package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceActivityDtos;

import java.util.UUID;

public interface WorkspaceActivityService {
    WorkspaceActivityDtos.ActivityPageResponse getActivities(
            String q,
            UUID projectId,
            String actor,
            IssueActivityType type,
            int page,
            int pageSize
    );
}