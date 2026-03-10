package com.taskflow.taskflow_be.module.workspace.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.issue.entity.IssueActivityType;
import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceActivityDtos;
import com.taskflow.taskflow_be.module.workspace.service.WorkspaceActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
public class WorkspaceActivityController {

    private final WorkspaceActivityService workspaceActivityService;

    public WorkspaceActivityController(WorkspaceActivityService workspaceActivityService) {
        this.workspaceActivityService = workspaceActivityService;
    }

    @GetMapping
    public ApiResponse<WorkspaceActivityDtos.ActivityPageResponse> getActivities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) IssueActivityType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);

        return ApiResponse.ok(
                workspaceActivityService.getActivities(q, projectId, actor, type, safePage, safePageSize)
        );
    }
}