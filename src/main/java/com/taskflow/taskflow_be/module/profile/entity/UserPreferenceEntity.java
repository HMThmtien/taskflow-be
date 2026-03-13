package com.taskflow.taskflow_be.module.profile.entity;

import com.taskflow.taskflow_be.common.constant.BaseEntity;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceEntity extends BaseEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "theme", nullable = false, length = 20)
    private String theme;

    @Column(name = "density", nullable = false, length = 20)
    private String density;

    @Column(name = "default_start_page", nullable = false, length = 50)
    private String defaultStartPage;

    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications;

    @Column(name = "in_app_notifications", nullable = false)
    private boolean inAppNotifications;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (theme == null) theme = "system";
        if (density == null) density = "comfortable";
        if (defaultStartPage == null) defaultStartPage = "dashboard";
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}