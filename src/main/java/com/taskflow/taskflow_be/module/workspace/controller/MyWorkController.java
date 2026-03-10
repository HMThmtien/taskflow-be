package com.taskflow.taskflow_be.module.workspace.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.issue.entity.IssuePriority;
import com.taskflow.taskflow_be.module.issue.entity.IssueStatus;
import com.taskflow.taskflow_be.module.workspace.dto.MyWorkDtos;
import com.taskflow.taskflow_be.module.workspace.service.MyWorkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues/me")
public class MyWorkController {

    private final MyWorkService myWorkService;

    public MyWorkController(MyWorkService myWorkService) {
        this.myWorkService = myWorkService;
    }

    @GetMapping
    public ApiResponse<List<MyWorkDtos.IssueSummaryResponse>> getMyWork(
            @RequestParam String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority
    ) {
        return ApiResponse.ok(myWorkService.getMyWork(type, q, projectId, status, priority));
    }
}