package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IssueRepository extends JpaRepository<IssueEntity, UUID>, JpaSpecificationExecutor<IssueEntity> {
    // dùng Specification cho filter
}