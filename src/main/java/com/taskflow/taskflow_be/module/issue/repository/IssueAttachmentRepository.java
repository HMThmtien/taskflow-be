package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueAttachmentRepository extends JpaRepository<IssueAttachmentEntity, UUID> {
    List<IssueAttachmentEntity> findByIssue_IdOrderByCreatedAtDesc(UUID issueId);
}