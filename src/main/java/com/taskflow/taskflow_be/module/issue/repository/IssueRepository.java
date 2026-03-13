package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IssueRepository extends JpaRepository<IssueEntity, UUID>, JpaSpecificationExecutor<IssueEntity> {


    List<IssueEntity> findByParentIssue_Id(UUID parentIssueId);
    List<IssueEntity> findByProject_IdAndSprint_IdOrderByUpdatedAtDesc(UUID projectId, UUID sprintId);
    List<IssueEntity> findByProject_IdAndSprintIsNullOrderByUpdatedAtDesc(UUID projectId);
    long countByProject_IdAndSprint_Id(UUID projectId, UUID sprintId);

    @Query("select coalesce(max(i.position), 0) from IssueEntity i where i.project.id = :projectId and i.status = :status")
    int maxPosition(UUID projectId, IssueStatus status);

    @Query("""
        select i from IssueEntity i
        join i.project p
        where p.id in :projectIds
          and (
            lower(i.title) like lower(concat('%', :q, '%'))
            or lower(coalesce(i.description, '')) like lower(concat('%', :q, '%'))
            or lower(p.name) like lower(concat('%', :q, '%'))
            or lower(p.key) like lower(concat('%', :q, '%'))
          )
        order by i.updatedAt desc
        """)
    List<IssueEntity> searchVisibleIssues(
            @Param("projectIds") List<UUID> projectIds,
            @Param("q") String q,
            Pageable pageable
    );

}
