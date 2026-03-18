package com.taskflow.taskflow_be.module.issue.service;

import com.taskflow.taskflow_be.module.issue.dto.IssueAttachmentDtos;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IssueAttachmentService {
    List<IssueAttachmentDtos.AttachmentRes> list(UUID issueId);
    IssueAttachmentDtos.AttachmentRes upload(UUID issueId, MultipartFile file);
    void delete(UUID issueId, UUID attachmentId);
    String resolveDownloadUrl(UUID issueId, UUID attachmentId);
}
