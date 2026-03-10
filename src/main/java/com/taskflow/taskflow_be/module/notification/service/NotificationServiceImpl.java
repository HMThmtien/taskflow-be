package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.notification.dto.NotificationDtos;
import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;
import com.taskflow.taskflow_be.module.notification.repository.NotificationRepository;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public NotificationDtos.NotificationPageResponse getMyNotifications(boolean unreadOnly, int page, int pageSize) {
        var username = SecurityUtils.currentUsername();

        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        var currentUserId = currentUser.getId();
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        var pageable = PageRequest.of(
                safePage - 1,
                safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var result = notificationRepository.findMyNotifications(currentUserId, unreadOnly, pageable);

        var response = new NotificationDtos.NotificationPageResponse();
        response.setItems(result.getContent().stream().map(this::toResponse).toList());
        response.setPage(safePage);
        response.setPageSize(safePageSize);
        response.setTotal(result.getTotalElements());
        response.setUnreadCount(notificationRepository.countByUserIdAndIsReadFalse(currentUserId));
        return response;
    }

    @Override
    @Transactional
    public Map<String, String> markAsRead(UUID id) {
        var username = SecurityUtils.currentUsername();

        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        var currentUserId = currentUser.getId();
        var entity = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!entity.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Access denied");
        }

        entity.setRead(true);
        notificationRepository.save(entity);
        return Map.of("message", "Marked as read");
    }

    @Override
    @Transactional
    public Map<String, String> markAllAsRead() {
        var username = SecurityUtils.currentUsername();

        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        var currentUserId = currentUser.getId();
        var all = notificationRepository.findMyNotifications(
                currentUserId,
                true,
                PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        all.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(all);

        return Map.of("message", "Marked all as read");
    }

    private NotificationDtos.NotificationResponse toResponse(NotificationEntity entity) {
        var dto = new NotificationDtos.NotificationResponse();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setBody(entity.getBody());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getActor() != null) {
            var actor = new NotificationDtos.ActorResponse();
            actor.setId(entity.getActor().getId());
            actor.setUsername(entity.getActor().getUsername());
            actor.setFullName(entity.getActor().getFullName());
            dto.setActor(actor);
        }

        if (entity.getEntityType() != null || entity.getEntityId() != null || entity.getRoute() != null) {
            var target = new NotificationDtos.TargetResponse();
            target.setEntityType(entity.getEntityType());
            target.setEntityId(entity.getEntityId());
            target.setRoute(entity.getRoute());
            dto.setTarget(target);
        }

        return dto;
    }
}