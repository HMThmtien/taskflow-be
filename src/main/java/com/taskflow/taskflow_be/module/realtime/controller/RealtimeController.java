package com.taskflow.taskflow_be.module.realtime.controller;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.realtime.service.RealtimeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/realtime")
public class RealtimeController {

    private final RealtimeEventService realtimeEventService;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        var userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthenticated");
        }
        return realtimeEventService.subscribe(userId);
    }
}
