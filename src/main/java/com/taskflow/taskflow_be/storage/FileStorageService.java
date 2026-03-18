package com.taskflow.taskflow_be.storage;

import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile store(String objectKey, MultipartFile file);
    void delete(IssueAttachmentEntity attachment);
    String createAccessUrl(IssueAttachmentEntity attachment);

    record StoredFile(String provider, String bucket, String key, String legacyPath) {
    }
}
