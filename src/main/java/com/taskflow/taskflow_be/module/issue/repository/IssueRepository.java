package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<IssueEntity, UUID>, JpaSpecificationExecutor<IssueEntity> {


    List<IssueEntity> findByParentIssue_Id(UUID parentIssueId);

    @Query("select coalesce(max(i.position), 0) from IssueEntity i where i.project.id = :projectId and i.status = :status")
    int maxPosition(UUID projectId, IssueStatus status);

}