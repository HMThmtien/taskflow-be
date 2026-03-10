package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.module.notification.dto.NotificationDtos;

import java.util.Map;
import java.util.UUID;

public interface NotificationService {
    NotificationDtos.NotificationPageResponse getMyNotifications(boolean unreadOnly, int page, int pageSize);
    Map<String, String> markAsRead(UUID id);
    Map<String, String> markAllAsRead();
}