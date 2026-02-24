package com.taskflow.taskflow_be.module.project.repository;

import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Optional<ProjectEntity> findByKey(String key);
    boolean existsByKey(String key);
}