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

    interface StatusCountProjection {
        String getKey();
        long getCount();
    }

    interface PriorityCountProjection {
        String getKey();
        long getCount();
    }

    interface AssigneeWorkloadProjection {
        UUID getUserId();
        String getUsername();
        String getFullName();
        long getTotalAssigned();
        long getOpenAssigned();
        long getOverdueAssigned();
    }


    List<IssueEntity> findByParentIssue_Id(UUID parentIssueId);
    List<IssueEntity> findByProject_IdAndSprint_IdOrderByUpdatedAtDesc(UUID projectId, UUID sprintId);
    List<IssueEntity> findByProject_IdAndSprintIsNullOrderByUpdatedAtDesc(UUID projectId);
    long countByProject_IdAndSprint_Id(UUID projectId, UUID sprintId);
    long countByProject_Id(UUID projectId);
    long countByProject_IdAndStatus(UUID projectId, IssueStatus status);
    long countByProject_IdAndDueDateBeforeAndStatusNot(UUID projectId, java.time.LocalDate dueDate, IssueStatus status);

    @Query("""
        select i.status as key, count(i) as count
        from IssueEntity i
        where i.project.id = :projectId
        group by i.status
        """)
    List<StatusCountProjection> countByStatusForProject(@Param("projectId") UUID projectId);

    @Query("""
        select i.priority as key, count(i) as count
        from IssueEntity i
        where i.project.id = :projectId
        group by i.priority
        """)
    List<PriorityCountProjection> countByPriorityForProject(@Param("projectId") UUID projectId);

    @Query("""
        select i from IssueEntity i
        left join fetch i.assignee
        where i.project.id = :projectId
          and i.status <> :doneStatus
          and i.dueDate is not null
          and i.dueDate < :today
        order by i.dueDate asc, i.updatedAt desc
        """)
    List<IssueEntity> findOverdueIssuesForProject(
            @Param("projectId") UUID projectId,
            @Param("today") java.time.LocalDate today,
            @Param("doneStatus") IssueStatus doneStatus
    );

    @Query("""
        select a.id as userId,
               a.username as username,
               a.fullName as fullName,
               count(i) as totalAssigned,
               sum(case when i.status <> com.taskflow.taskflow_be.module.issue.entity.IssueStatus.DONE then 1 else 0 end) as openAssigned,
               sum(case when i.status <> com.taskflow.taskflow_be.module.issue.entity.IssueStatus.DONE and i.dueDate is not null and i.dueDate < :today then 1 else 0 end) as overdueAssigned
        from IssueEntity i
        join i.assignee a
        where i.project.id = :projectId
        group by a.id, a.username, a.fullName
        order by sum(case when i.status <> com.taskflow.taskflow_be.module.issue.entity.IssueStatus.DONE then 1 else 0 end) desc, a.username asc
        """)
    List<AssigneeWorkloadProjection> getAssigneeWorkload(
            @Param("projectId") UUID projectId,
            @Param("today") java.time.LocalDate today
    );

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
