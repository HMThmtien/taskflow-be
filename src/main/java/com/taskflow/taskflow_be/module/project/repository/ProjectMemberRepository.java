package com.taskflow.taskflow_be.module.project.repository;

import com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMemberEntity, UUID> {

    Optional<ProjectMemberEntity> findByProjectIdAndUserId(UUID projectId, UUID userId);
    List<ProjectMemberEntity> findAllByProjectId(UUID projectId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
}