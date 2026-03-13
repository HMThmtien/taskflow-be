package com.taskflow.taskflow_be.module.realtime.service;

import com.taskflow.taskflow_be.module.realtime.dto.RealtimeDtos;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeEventServiceImpl implements RealtimeEventService {

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ex -> removeEmitter(userId, emitter));

        send(userId, emitter, "realtime.connected", Map.of("connected", true));
        return emitter;
    }

    @Override
    public void publishToUser(UUID userId, String type, Object payload) {
        var userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) return;

        for (SseEmitter emitter : List.copyOf(userEmitters)) {
            send(userId, emitter, type, payload);
        }
    }

    @Override
    public void publishToUsers(Collection<UUID> userIds, String type, Object payload) {
        userIds.stream().distinct().forEach(userId -> publishToUser(userId, type, payload));
    }

    @Scheduled(fixedDelay = 20000)
    public void heartbeat() {
        for (var entry : emitters.entrySet()) {
            UUID userId = entry.getKey();
            for (SseEmitter emitter : List.copyOf(entry.getValue())) {
                send(userId, emitter, "realtime.heartbeat", Map.of("ts", Instant.now().toString()));
            }
        }
    }

    private void send(UUID userId, SseEmitter emitter, String type, Object payload) {
        try {
            var envelope = RealtimeDtos.EventEnvelope.builder()
                    .id(UUID.randomUUID())
                    .type(type)
                    .issuedAt(Instant.now())
                    .payload(payload)
                    .build();

            emitter.send(SseEmitter.event()
                    .name(type)
                    .id(envelope.getId().toString())
                    .data(envelope));
        } catch (IOException | IllegalStateException ex) {
            removeEmitter(userId, emitter);
        }
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        var userEmitters = emitters.get(userId);
        if (userEmitters == null) return;
        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
