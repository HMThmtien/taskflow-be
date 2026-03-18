package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.notification.dto.NotificationDtos;
import com.taskflow.taskflow_be.module.notification.mapper.NotificationMapper;
import com.taskflow.taskflow_be.module.notification.repository.NotificationRepository;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

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
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        var currentUserId = currentUser.getId();
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));

        var pageable = PageRequest.of(
                safePage - 1,
                safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        var result = notificationRepository.findMyNotifications(currentUserId, unreadOnly, pageable);

        var response = new NotificationDtos.NotificationPageResponse();
        response.setItems(result.getContent().stream().map(NotificationMapper::toResponse).toList());
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
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        var currentUserId = currentUser.getId();
        var entity = notificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Notification not found"));

        if (!entity.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, "Access denied");
        }

        if (!entity.isRead()) {
            entity.setRead(true);
            notificationRepository.save(entity);
        }
        return Map.of("message", "Marked as read");
    }

    @Override
    @Transactional
    public Map<String, String> markAllAsRead() {
        var username = SecurityUtils.currentUsername();

        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        var currentUserId = currentUser.getId();
        notificationRepository.markAllAsRead(currentUserId);

        return Map.of("message", "Marked all as read");
    }
}
