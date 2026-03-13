package com.taskflow.taskflow_be.module.issue.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.issue.dto.IssueAttachmentDtos;
import com.taskflow.taskflow_be.module.issue.service.IssueAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/issues/{issueId}/attachments")
public class IssueAttachmentController {

    private final IssueAttachmentService issueAttachmentService;

    @GetMapping
    public ApiResponse<List<IssueAttachmentDtos.AttachmentRes>> list(
            @PathVariable UUID issueId
    ) {
        return ApiResponse.ok(issueAttachmentService.list(issueId));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<IssueAttachmentDtos.AttachmentRes> upload(
            @PathVariable UUID issueId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(issueAttachmentService.upload(issueId, file));
    }

    @DeleteMapping("/{attachmentId}")
    public ApiResponse<Map<String, String>> delete(
            @PathVariable UUID issueId,
            @PathVariable UUID attachmentId
    ) {
        issueAttachmentService.delete(issueId, attachmentId);
        return ApiResponse.ok(Map.of("message", "Attachment deleted"));
    }
}