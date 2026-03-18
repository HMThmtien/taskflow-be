package com.taskflow.taskflow_be.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "taskflow.storage")
public class StorageProperties {

    private String uploadDir = "uploads";
    private long maxFileSizeBytes = 10 * 1024 * 1024;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf",
            "text/plain"
    );

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
