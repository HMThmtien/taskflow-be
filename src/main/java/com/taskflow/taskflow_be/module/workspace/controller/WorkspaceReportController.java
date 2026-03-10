package com.taskflow.taskflow_be.module.workspace.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceReportDtos;
import com.taskflow.taskflow_be.module.workspace.service.WorkspaceReportService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/workspace")
public class WorkspaceReportController {

    private final WorkspaceReportService workspaceReportService;

    public WorkspaceReportController(WorkspaceReportService workspaceReportService) {
        this.workspaceReportService = workspaceReportService;
    }

    @GetMapping
    public ApiResponse<WorkspaceReportDtos.WorkspaceReportResponse> getWorkspaceReport(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) UUID projectId
    ) {
        return ApiResponse.ok(workspaceReportService.getWorkspaceReport(from, to, projectId));
    }
}