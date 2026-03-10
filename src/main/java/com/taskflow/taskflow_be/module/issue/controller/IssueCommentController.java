package com.taskflow.taskflow_be.module.issue.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.issue.dto.IssueActivityDtos;
import com.taskflow.taskflow_be.module.issue.service.IssueActivityService;
import com.taskflow.taskflow_be.module.issue.dto.IssueCommentDtos;
import com.taskflow.taskflow_be.module.issue.service.IssueCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues/{issueId}")
@RequiredArgsConstructor
public class IssueCommentController {

    private final IssueCommentService commentSvc;
    private final IssueActivityService activitySvc;

    @GetMapping("/comments")
    public ApiResponse<Page<IssueCommentDtos.CommentRes>> list(
            @PathVariable UUID issueId,
            Pageable pageable
    ) {
        return ApiResponse.ok(commentSvc.list(issueId, pageable));
    }

    @PostMapping("/comments")
    public ApiResponse<IssueCommentDtos.CommentRes> create(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueCommentDtos.CreateReq req
    ) {
        return ApiResponse.ok(commentSvc.create(issueId, req));
    }

    @GetMapping("/activities")
    public ApiResponse<List<IssueActivityDtos.ActivityRes>> activities(@PathVariable UUID issueId) {
        return ApiResponse.ok(activitySvc.list(issueId));
    }
}