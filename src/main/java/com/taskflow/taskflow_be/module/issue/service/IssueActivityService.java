package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueActivityDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IssueActivityService {
    void log(IssueEntity issue, UUID actorId, IssueActivityType type, Map<String, String> payload);
    List<IssueActivityDtos.ActivityRes> list(UUID issueId);
}