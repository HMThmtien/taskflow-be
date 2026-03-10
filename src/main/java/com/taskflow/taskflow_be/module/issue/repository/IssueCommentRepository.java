package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueCommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueCommentRepository extends JpaRepository<IssueCommentEntity, UUID> {
    Page<IssueCommentEntity> findByIssue_IdOrderByCreatedAtDesc(
            UUID issueId,
            Pageable pageable
    );
}