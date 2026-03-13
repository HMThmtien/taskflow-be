package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueAttachmentServiceImpl implements IssueAttachmentService {

    private final IssueRepository issueRepo;
    private final IssueAttachmentRepository attachmentRepo;
    private final UserRepository userRepo;
    private final ProjectPermissionService permission;
    private final IssueActivityLogService activityLogService;
    private final StorageProperties storageProperties;

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

        String originalName = file.getOriginalFilename() == null
                ? "file"
                : Paths.get(file.getOriginalFilename()).getFileName().toString();

        String safeName = UUID.randomUUID() + "_" + originalName;

        Path baseDir = Paths.get(storageProperties.getUploadDir(), "issues", issueId.toString());
        Path targetPath = baseDir.resolve(safeName);

        try {
            Files.createDirectories(baseDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store file");
        }

        String publicUrl = "/uploads/issues/" + issueId + "/" + safeName;

        var entity = IssueAttachmentEntity.builder()
                .issue(issue)
                .uploadedBy(user)
                .fileName(originalName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(publicUrl)
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

        String prefix = "/uploads/issues/" + issueId + "/";
        String fileName = attachment.getStoragePath().startsWith(prefix)
                ? attachment.getStoragePath().substring(prefix.length())
                : null;

        if (fileName != null && !fileName.isBlank()) {
            Path realPath = Paths.get(storageProperties.getUploadDir(), "issues", issueId.toString(), fileName);
            try {
                Files.deleteIfExists(realPath);
            } catch (IOException ignored) {
            }
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

    private IssueAttachmentDtos.AttachmentRes toResponse(IssueAttachmentEntity e) {
        return IssueAttachmentDtos.AttachmentRes.builder()
                .id(e.getId())
                .issueId(e.getIssue().getId())
                .uploadedBy(e.getUploadedBy().getId())
                .uploadedByUsername(e.getUploadedBy().getUsername())
                .fileName(e.getFileName())
                .fileType(e.getFileType())
                .fileSize(e.getFileSize())
                .storagePath(e.getStoragePath())
                .createdAt(e.getCreatedAt())
                .build();
    }
}