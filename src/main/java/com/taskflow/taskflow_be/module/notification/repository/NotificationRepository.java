package com.taskflow.taskflow_be.module.notification.repository;

import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    @Query("""
        select n
        from NotificationEntity n
        where n.user.id = :userId
          and (:unreadOnly = false or n.isRead = false)
        order by n.createdAt desc
    """)
    Page<NotificationEntity> findMyNotifications(UUID userId, boolean unreadOnly, Pageable pageable);

    long countByUserIdAndIsReadFalse(UUID userId);
}