package com.taskflow.taskflow_be.module.sprint.entity;

import com.taskflow.taskflow_be.common.constant.BaseEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "sprints", indexes = {
        @Index(name = "idx_sprints_project_status", columnList = "project_id,status,position"),
        @Index(name = "idx_sprints_project_completed", columnList = "project_id,completed_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "goal", length = 1000)
    private String goal;

    @Column(name = "description", length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SprintStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = SprintStatus.PLANNED;
        if (position == null) position = 0;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
