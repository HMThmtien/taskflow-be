package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.storage.FileStorageService;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.module.issue.dto.IssueAttachmentDtos;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import com.taskflow.taskflow_be.module.issue.repository.IssueAttachmentRepository;
import com.taskflow.taskflow_be.module.issue.repository.IssueRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueAttachmentServiceImpl implements IssueAttachmentService {

    private static final int MAX_FILE_NAME_LENGTH = 180;

    private final IssueRepository issueRepo;
    private final IssueAttachmentRepository attachmentRepo;
    private final UserRepository userRepo;
    private final ProjectPermissionService permission;
    private final IssueActivityLogService activityLogService;
    private final StorageProperties storageProperties;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<IssueAttachmentDtos.AttachmentRes> list(UUID issueId) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireMember(issue.getProject().getId(), me);

        return attachmentRepo.findByIssue_IdOrderByCreatedAtDesc(issueId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IssueAttachmentDtos.AttachmentRes upload(UUID issueId, MultipartFile file) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireWrite(issue.getProject().getId(), me);

        var user = userRepo.findById(me)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "file must not be empty");
        }

        validateFile(file);

        String originalName = sanitizeOriginalName(file.getOriginalFilename());

        String safeName = UUID.randomUUID() + "_" + originalName;
        String objectKey = Paths.get("issues", issueId.toString(), safeName).toString().replace('\\', '/');
        FileStorageService.StoredFile storedFile = fileStorageService.store(objectKey, file);

        var entity = IssueAttachmentEntity.builder()
                .issue(issue)
                .uploadedBy(user)
                .fileName(originalName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(storedFile.legacyPath() == null ? storedFile.key() : storedFile.legacyPath())
                .storageProvider(storedFile.provider())
                .storageBucket(storedFile.bucket())
                .storageKey(storedFile.key())
                .build();

        entity = attachmentRepo.save(entity);

        activityLogService.log(
                me,
                issue,
                IssueActivityType.ISSUE_UPDATED,
                java.util.Map.of(
                        "action", "attachment_added",
                        "fileName", entity.getFileName()
                )
        );

        return toResponse(entity);
    }

    @Override
    public void delete(UUID issueId, UUID attachmentId) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));

        permission.requireWrite(issue.getProject().getId(), me);

        var attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, "Attachment not found"));

        if (!attachment.getIssue().getId().equals(issueId)) {
            throw new NotFoundException(ErrorCode.NOT_FOUND, "Attachment not found");
        }

        if (attachment.getStorageKey() != null && !attachment.getStorageKey().isBlank()) {
            fileStorageService.delete(attachment);
        } else {
            deleteLegacyLocalFile(issueId, attachment);
        }

        attachmentRepo.delete(attachment);

        activityLogService.log(
                me,
                issue,
                IssueActivityType.ISSUE_UPDATED,
                java.util.Map.of(
                        "action", "attachment_deleted",
                        "fileName", attachment.getFileName()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String resolveDownloadUrl(UUID issueId, UUID attachmentId) {
        UUID me = SecurityUtils.currentUserId();

        var issue = issueRepo.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ISSUE_NOT_FOUND, "Issue not found"));
        permission.requireMember(issue.getProject().getId(), me);

        var attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, "Attachment not found"));
        if (!attachment.getIssue().getId().equals(issueId)) {
            throw new NotFoundException(ErrorCode.NOT_FOUND, "Attachment not found");
        }

        if (attachment.getStorageKey() != null && !attachment.getStorageKey().isBlank()) {
            return fileStorageService.createAccessUrl(attachment);
        }

        return attachment.getStoragePath();
    }

    private IssueAttachmentDtos.AttachmentRes toResponse(IssueAttachmentEntity e) {
        return IssueAttachmentDtos.AttachmentRes.builder()
                .id(e.getId())
                .issueId(e.getIssue().getId())
                .uploadedBy(e.getUploadedBy().getId())
                .uploadedByUsername(e.getUploadedBy().getUsername())
                .fileName(e.getFileName())
                .fileType(e.getFileType())
                .fileSize(e.getFileSize())
                .storagePath("/api/issues/" + e.getIssue().getId() + "/attachments/" + e.getId() + "/download")
                .createdAt(e.getCreatedAt())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "File is too large");
        }

        String contentType = file.getContentType();
        Set<String> allowedContentTypes = new HashSet<>(storageProperties.getAllowedContentTypes());
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "File type is not allowed");
        }
    }

    private String sanitizeOriginalName(String originalFilename) {
        String baseName = originalFilename == null
                ? "file"
                : Paths.get(originalFilename).getFileName().toString();
        String sanitized = baseName
                .replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("[^a-zA-Z0-9._ -]", "_")
                .trim();

        if (sanitized.isBlank()) {
            sanitized = "file";
        }

        if (sanitized.length() > MAX_FILE_NAME_LENGTH) {
            int extensionIndex = sanitized.lastIndexOf('.');
            if (extensionIndex > 0 && extensionIndex < sanitized.length() - 1) {
                String extension = sanitized.substring(extensionIndex);
                int baseLength = Math.max(1, MAX_FILE_NAME_LENGTH - extension.length());
                sanitized = sanitized.substring(0, baseLength) + extension;
            } else {
                sanitized = sanitized.substring(0, MAX_FILE_NAME_LENGTH);
            }
        }

        return sanitized;
    }

    private void deleteLegacyLocalFile(UUID issueId, IssueAttachmentEntity attachment) {
        String prefix = "/uploads/issues/" + issueId + "/";
        String fileName = attachment.getStoragePath().startsWith(prefix)
                ? attachment.getStoragePath().substring(prefix.length())
                : null;

        if (fileName == null || fileName.isBlank()) {
            return;
        }

        Path realPath = Paths.get(storageProperties.getUploadDir(), "issues", issueId.toString(), fileName)
                .toAbsolutePath()
                .normalize();
        try {
            Files.deleteIfExists(realPath);
        } catch (IOException ignored) {
        }
    }
}
