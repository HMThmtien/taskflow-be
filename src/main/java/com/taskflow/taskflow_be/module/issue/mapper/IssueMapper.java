package com.taskflow.taskflow_be.module.issue.mapper;

import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;

public class IssueMapper {
    public static IssueDtos.IssueResponse toResponse(IssueEntity e) {
        return IssueDtos.IssueResponse.builder()
                .id(e.getId())
                .projectId(e.getProject().getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .status(e.getStatus())
                .priority(e.getPriority())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}