package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IssueService {
    Page<IssueDtos.IssueResponse> list(
            UUID projectId,
            String q,
            IssueStatus status,
            IssuePriority priority,
            UUID assigneeId,
            UUID sprintId,
            LocalDate dueFrom,
            LocalDate dueTo,
            String label,
            Pageable pageable
    );
    IssueDtos.IssueResponse create(UUID projectId, IssueDtos.CreateIssueRequest req);
    IssueDtos.IssueResponse update(UUID projectId, UUID issueId, IssueDtos.UpdateIssueRequest req);
    IssueDtos.IssueResponse move(UUID projectId, UUID issueId, IssueDtos.MoveIssueRequest req);
    List<IssueDtos.IssueResponse> bulkUpdate(UUID projectId, IssueDtos.BulkUpdateIssueRequest req);

    List<IssueDtos.IssueResponse> listSubtasks(UUID projectId, UUID issueId);

    IssueDtos.IssueResponse createSubtask(UUID projectId, UUID parentIssueId, IssueDtos.CreateIssueRequest req);
}
