package com.taskflow.taskflow_be.module.sprint.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.sprint.dto.SprintDtos;

import java.util.List;
import java.util.UUID;

public interface SprintService {
    List<SprintDtos.SprintResponse> list(UUID projectId);

    SprintDtos.SprintResponse create(UUID projectId, SprintDtos.CreateSprintRequest req);

    SprintDtos.SprintResponse update(UUID projectId, UUID sprintId, SprintDtos.UpdateSprintRequest req);

    SprintDtos.SprintResponse start(UUID projectId, UUID sprintId);

    SprintDtos.SprintResponse complete(UUID projectId, UUID sprintId, SprintDtos.CompleteSprintRequest req);

    SprintDtos.SprintMetricsResponse getMetrics(UUID projectId, UUID sprintId);

    List<IssueDtos.IssueResponse> listBacklogIssues(UUID projectId);

    IssueDtos.IssueResponse assignIssue(UUID projectId, UUID issueId, UUID sprintId);
}
