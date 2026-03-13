package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.module.notification.mapper.NotificationMapper;
import com.taskflow.taskflow_be.module.realtime.dto.RealtimeDtos;
import com.taskflow.taskflow_be.module.realtime.service.RealtimeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationRealtimeServiceImpl implements NotificationRealtimeService {

    private final RealtimeEventService realtimeEventService;

    @Override
    public void publishCreated(com.taskflow.taskflow_be.module.notification.entity.NotificationEntity entity) {
        realtimeEventService.publishToUser(
                entity.getUser().getId(),
                "notification.created",
                RealtimeDtos.NotificationEventPayload.builder()
                        .notification(NotificationMapper.toResponse(entity))
                        .build()
        );
    }
}
