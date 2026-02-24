package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;

import java.util.List;
import java.util.UUID;

public interface IssueService {
    List<IssueDtos.IssueResponse> list(UUID projectId, String q, IssueStatus status, IssuePriority priority);
    IssueDtos.IssueResponse create(UUID projectId, IssueDtos.CreateIssueRequest req);
    IssueDtos.IssueResponse update(UUID projectId, UUID issueId, IssueDtos.UpdateIssueRequest req);
    IssueDtos.IssueResponse move(UUID projectId, UUID issueId, IssueDtos.MoveIssueRequest req);
}