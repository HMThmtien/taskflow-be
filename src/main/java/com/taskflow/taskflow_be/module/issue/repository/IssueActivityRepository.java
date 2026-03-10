package com.taskflow.taskflow_be.module.issue.repository;

import com.taskflow.taskflow_be.module.issue.entity.IssueActivityEntity;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IssueActivityRepository extends JpaRepository<IssueActivityEntity, UUID> {

    List<IssueActivityEntity> findAllByIssue_IdOrderByCreatedAtAsc(UUID issueId);

    Page<IssueActivityEntity> findByIssue_IdOrderByCreatedAtDesc(UUID issueId, Pageable pageable);

    @Query("""
        select ia
        from IssueActivityEntity ia
        join ia.issue i
        join i.project p
        where p.id in (
            select pm.project.id
            from ProjectMemberEntity pm
            where pm.user.id = :userId
        )
          and (:projectId is null or p.id = :projectId)
          and (:type is null or ia.type = :type)
        order by ia.createdAt desc
    """)
    Page<IssueActivityEntity> findWorkspaceActivities(
            UUID userId,
            UUID projectId,
            IssueActivityType type,
            Pageable pageable
    );

    @Query("""
        select ia
        from IssueActivityEntity ia
        join ia.issue i
        join i.project p
        join ia.actor a
        where p.id in (
            select pm.project.id
            from ProjectMemberEntity pm
            where pm.user.id = :userId
        )
          and (:projectId is null or p.id = :projectId)
          and (:type is null or ia.type = :type)
          and lower(a.username) like lower(concat('%', :actor, '%'))
        order by ia.createdAt desc
    """)
    Page<IssueActivityEntity> findWorkspaceActivitiesByActor(
            UUID userId,
            UUID projectId,
            String actor,
            IssueActivityType type,
            Pageable pageable
    );
}