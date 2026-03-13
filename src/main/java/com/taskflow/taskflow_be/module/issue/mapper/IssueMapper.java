package com.taskflow.taskflow_be.module.issue.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflow_be.module.issue.dto.IssueDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;

import java.util.Collections;
import java.util.List;

public class IssueMapper {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_STR = new TypeReference<>() {};

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

                // ✅ NEW: reporter
                .reporterId(e.getReporter() != null ? e.getReporter().getId() : null)
                .reporterUsername(e.getReporter() != null ? e.getReporter().getUsername() : null)

                // ✅ NEW: assignee
                .assigneeId(e.getAssignee() != null ? e.getAssignee().getId() : null)
                .assigneeUsername(e.getAssignee() != null ? e.getAssignee().getUsername() : null)

                // ✅ NEW: due/labels
                .dueDate(e.getDueDate())
                .labels(e.getLabels() != null ? e.getLabels() : Collections.emptyList())

                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private static List<String> parseLabels(String labelsJson) {
        if (labelsJson == null || labelsJson.isBlank()) return Collections.emptyList();
        try {
            return OM.readValue(labelsJson, LIST_STR);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}