package com.taskflow.taskflow_be.storage;

import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile store(String objectKey, MultipartFile file);
    void delete(String provider, String bucket, String key, String legacyPath);
    String createAccessUrl(String provider, String bucket, String key, String legacyPath);

    default void delete(IssueAttachmentEntity attachment) {
        delete(
                attachment.getStorageProvider(),
                attachment.getStorageBucket(),
                attachment.getStorageKey(),
                attachment.getStoragePath()
        );
    }

    default String createAccessUrl(IssueAttachmentEntity attachment) {
        return createAccessUrl(
                attachment.getStorageProvider(),
                attachment.getStorageBucket(),
                attachment.getStorageKey(),
                attachment.getStoragePath()
        );
    }

    record StoredFile(String provider, String bucket, String key, String legacyPath) {
    }
}
