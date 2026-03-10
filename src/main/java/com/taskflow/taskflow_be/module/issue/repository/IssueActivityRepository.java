package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IssueActivityRepository extends JpaRepository<IssueActivityEntity, UUID> {

    List<IssueActivityEntity> findAllByIssue_IdOrderByCreatedAtAsc(UUID issueId);

    Page<IssueActivityEntity> findByIssue_IdOrderByCreatedAtDesc(UUID issueId, Pageable pageable);
}