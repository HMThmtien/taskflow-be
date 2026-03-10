package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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

    public static Specification<IssueEntity> assigneeId(UUID assigneeId) {
        return (root, q, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<IssueEntity> dueFrom(LocalDate from) {
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), from);
    }

    public static Specification<IssueEntity> dueTo(LocalDate to) {
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), to);
    }

    public static Specification<IssueEntity> hasLabel(String label) {
        return (root, q, cb) ->
                cb.isTrue(cb.function(
                        "jsonb_exists",
                        Boolean.class,
                        root.get("labels"),
                        cb.literal(label)
                ));
    }
}