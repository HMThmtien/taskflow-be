package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;

public interface NotificationRealtimeService {
    void publishCreated(NotificationEntity entity);
}
