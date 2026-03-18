package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.issue.entity.IssueEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueAttachmentRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueAttachmentServiceImplTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private IssueAttachmentRepository attachmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectPermissionService permissionService;

    @Mock
    private IssueActivityLogService activityLogService;

    private StorageProperties storageProperties;
    private IssueAttachmentServiceImpl service;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.setUploadDir("uploads-test");
        storageProperties.setMaxFileSizeBytes(10);
        storageProperties.setAllowedContentTypes(List.of("image/png"));

        service = new IssueAttachmentServiceImpl(
                issueRepository,
                attachmentRepository,
                userRepository,
                permissionService,
                activityLogService,
                storageProperties
        );
    }

    @Test
    void uploadRejectsFileTooLarge() {
        UUID issueId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        IssueEntity issue = issueFor(issueId);
        UserEntity user = new UserEntity();
        user.setId(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "this is too large".getBytes()
        );

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUserId).thenReturn(userId);
            when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doNothing().when(permissionService).requireWrite(issue.getProject().getId(), userId);

            assertThrows(AppException.class, () -> service.upload(issueId, file));
        }
    }

    @Test
    void uploadRejectsDisallowedContentType() {
        UUID issueId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        IssueEntity issue = issueFor(issueId);
        UserEntity user = new UserEntity();
        user.setId(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.exe",
                "application/octet-stream",
                "12345".getBytes()
        );

        try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::currentUserId).thenReturn(userId);
            when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doNothing().when(permissionService).requireWrite(issue.getProject().getId(), userId);

            assertThrows(AppException.class, () -> service.upload(issueId, file));
        }
    }

    private IssueEntity issueFor(UUID issueId) {
        ProjectEntity project = ProjectEntity.builder()
                .id(UUID.randomUUID())
                .key("TEST")
                .name("Test")
                .build();

        return IssueEntity.builder()
                .id(issueId)
                .project(project)
                .title("Issue")
                .build();
    }
}
