package com.taskflow.taskflow_be.module.search.dto;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.issue.entity.IssueType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class SearchDtos {

    @Getter
    @Builder
    public static class SearchResponse {
        private List<ProjectResult> projects;
        private List<IssueResult> issues;
        private List<UserResult> users;
    }

    @Getter
    @Builder
    public static class ProjectResult {
        private UUID id;
        private String key;
        private String name;
        private String description;
        private boolean archived;
    }

    @Getter
    @Builder
    public static class IssueResult {
        private UUID id;
        private UUID projectId;
        private String projectKey;
        private String projectName;
        private String title;
        private IssueStatus status;
        private IssuePriority priority;
        private IssueType type;
    }

    @Getter
    @Builder
    public static class UserResult {
        private UUID id;
        private String username;
        private String fullName;
        private GlobalRole role;
    }
}
