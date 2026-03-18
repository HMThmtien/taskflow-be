package com.taskflow.taskflow_be.storage;

import com.taskflow.taskflow_be.config.StorageProperties;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.issue.entity.IssueAttachmentEntity;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(prefix = "taskflow.storage", name = "type", havingValue = "s3")
public class MinioFileStorageService implements FileStorageService {

    private final StorageProperties storageProperties;
    private final MinioClient minioClient;

    public MinioFileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        this.minioClient = MinioClient.builder()
                .endpoint(storageProperties.getS3().getEndpoint())
                .credentials(storageProperties.getS3().getAccessKey(), storageProperties.getS3().getSecretKey())
                .build();
    }

    @Override
    public StoredFile store(String objectKey, MultipartFile file) {
        ensureBucketExists();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store file");
        }

        return new StoredFile("s3", storageProperties.getS3().getBucket(), objectKey, objectKey);
    }

    @Override
    public void delete(IssueAttachmentEntity attachment) {
        if (attachment.getStorageKey() == null || attachment.getStorageKey().isBlank()) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(resolveBucket(attachment))
                            .object(attachment.getStorageKey())
                            .build()
            );
        } catch (Exception ignored) {
        }
    }

    @Override
    public String createAccessUrl(IssueAttachmentEntity attachment) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(resolveBucket(attachment))
                            .object(attachment.getStorageKey())
                            .expiry((int) storageProperties.getSignedUrlExpiryMinutes(), TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR, "Cannot create signed url");
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(storageProperties.getS3().getBucket())
                                .build()
                );
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR, "Cannot prepare object storage bucket");
        }
    }

    private String resolveBucket(IssueAttachmentEntity attachment) {
        return attachment.getStorageBucket() == null || attachment.getStorageBucket().isBlank()
                ? storageProperties.getS3().getBucket()
                : attachment.getStorageBucket();
    }
}
