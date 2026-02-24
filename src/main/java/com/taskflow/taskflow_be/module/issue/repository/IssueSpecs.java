package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class IssueSpecs {

    public static Specification<IssueEntity> projectId(UUID projectId) {
        return (root, q, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<IssueEntity> status(IssueStatus status) {
        return (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<IssueEntity> priority(IssuePriority priority) {
        return (root, q, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<IssueEntity> titleContains(String keyword) {
        return (root, q, cb) -> cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }
}