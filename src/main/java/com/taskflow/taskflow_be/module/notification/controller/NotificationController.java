package com.taskflow.taskflow_be.module.notification.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.notification.dto.NotificationDtos;
import com.taskflow.taskflow_be.module.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<NotificationDtos.NotificationPageResponse> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        ensureAuthenticated();

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        return ApiResponse.ok(
                notificationService.getMyNotifications(unreadOnly, safePage, safePageSize)
        );
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Map<String, String>> markRead(@PathVariable UUID id) {
        ensureAuthenticated();

        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification id is required");
        }

        return ApiResponse.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Map<String, String>> markReadAll() {
        ensureAuthenticated();
        return ApiResponse.ok(notificationService.markAllAsRead());
    }

    private void ensureAuthenticated() {
        String username = SecurityUtils.currentUsername();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
    }
}