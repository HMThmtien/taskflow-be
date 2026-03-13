package com.taskflow.taskflow_be.module.realtime.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.UUID;

public interface RealtimeEventService {
    SseEmitter subscribe(UUID userId);

    void publishToUser(UUID userId, String type, Object payload);

    void publishToUsers(Collection<UUID> userIds, String type, Object payload);
}
