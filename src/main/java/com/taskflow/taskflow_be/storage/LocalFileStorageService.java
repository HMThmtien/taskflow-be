package com.taskflow.taskflow_be.storage;

import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(prefix = "taskflow.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

    private final StorageProperties storageProperties;

    public LocalFileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public StoredFile store(String objectKey, MultipartFile file) {
        Path baseDir = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve(objectKey).normalize();
        if (!targetPath.startsWith(baseDir)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store file");
        }

        return new StoredFile("local", null, objectKey, "/uploads/" + objectKey);
    }

    @Override
    public void delete(IssueAttachmentEntity attachment) {
        String key = attachment.getStorageKey();
        if (key == null || key.isBlank()) {
            return;
        }

        Path baseDir = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetPath = baseDir.resolve(key).normalize();
        if (!targetPath.startsWith(baseDir)) {
            return;
        }

        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
        }
    }

    @Override
    public String createAccessUrl(IssueAttachmentEntity attachment) {
        if (attachment.getStorageKey() != null && !attachment.getStorageKey().isBlank()) {
            return "/uploads/" + attachment.getStorageKey();
        }
        return attachment.getStoragePath();
    }
}
