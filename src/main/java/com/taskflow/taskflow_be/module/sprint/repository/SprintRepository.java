package com.taskflow.taskflow_be.module.sprint.repository;

import com.taskflow.taskflow_be.module.sprint.entity.SprintEntity;
import com.taskflow.taskflow_be.module.sprint.entity.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<SprintEntity, UUID> {

    List<SprintEntity> findAllByProject_Id(UUID projectId);

    Optional<SprintEntity> findByIdAndProject_Id(UUID sprintId, UUID projectId);

    Optional<SprintEntity> findByProject_IdAndStatus(UUID projectId, SprintStatus status);

    @Query("select coalesce(max(s.position), 0) from SprintEntity s where s.project.id = :projectId")
    int maxPosition(UUID projectId);
}
