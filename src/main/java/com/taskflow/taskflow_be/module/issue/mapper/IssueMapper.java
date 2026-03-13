package com.taskflow.taskflow_be.module.issue.mapper;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;

import java.util.Collections;

public class IssueMapper {

    public static IssueDtos.IssueResponse toResponse(IssueEntity e) {
        return IssueDtos.IssueResponse.builder()
                .id(e.getId())
                .projectId(e.getProject().getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus())
                .priority(e.getPriority())
                .position(e.getPosition())
                .type(e.getType())
                .parentIssueId(e.getParentIssue() != null ? e.getParentIssue().getId() : null)
                .parentIssueTitle(e.getParentIssue() != null ? e.getParentIssue().getTitle() : null)
                .reporterId(e.getReporter() != null ? e.getReporter().getId() : null)
                .reporterUsername(e.getReporter() != null ? e.getReporter().getUsername() : null)
                .assigneeId(e.getAssignee() != null ? e.getAssignee().getId() : null)
                .assigneeUsername(e.getAssignee() != null ? e.getAssignee().getUsername() : null)
                .sprintId(e.getSprint() != null ? e.getSprint().getId() : null)
                .sprintName(e.getSprint() != null ? e.getSprint().getName() : null)
                .sprintStatus(e.getSprint() != null ? e.getSprint().getStatus() : null)
                .dueDate(e.getDueDate())
                .labels(e.getLabels() != null ? e.getLabels() : Collections.emptyList())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
