package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.workspace.dto.MyWorkDtos;

import java.util.List;
import java.util.UUID;

public interface MyWorkService {
    List<MyWorkDtos.IssueSummaryResponse> getMyWork(
            String type,
            String q,
            UUID projectId,
            IssueStatus status,
            IssuePriority priority
    );
}