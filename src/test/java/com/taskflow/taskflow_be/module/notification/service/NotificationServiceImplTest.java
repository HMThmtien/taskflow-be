package com.taskflow.taskflow_be.module.notification.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.notification.entity.NotificationEntity;
import com.taskflow.taskflow_be.module.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, userRepository);
    }

    @Test
    void getMyNotificationsClampsPageSize() {
        String username = "alice";
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(username);

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUsername).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(notificationRepository.findMyNotifications(eq(userId), eq(false), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));
            when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(0L);

            var response = service.getMyNotifications(false, 1, 999);

            assertEquals(100, response.getPageSize());
        }
    }

    @Test
    void markAllAsReadUsesBulkUpdate() {
        String username = "alice";
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername(username);

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUsername).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

            assertDoesNotThrow(() -> service.markAllAsRead());

            verify(notificationRepository).markAllAsRead(userId);
        }
    }

    @Test
    void markAsReadRejectsForeignNotification() {
        String username = "alice";
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        UserEntity currentUser = new UserEntity();
        currentUser.setId(userId);
        currentUser.setUsername(username);

        UserEntity owner = new UserEntity();
        owner.setId(otherUserId);

        NotificationEntity notification = NotificationEntity.builder()
                .id(notificationId)
                .user(owner)
                .isRead(false)
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUsername).thenReturn(username);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(currentUser));
            when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            assertThrows(AppException.class, () -> service.markAsRead(notificationId));

            verify(notificationRepository, never()).save(any(NotificationEntity.class));
        }
    }
}
