package com.taskflow.taskflow_be.module.projectreport.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.projectreport.dto.ProjectReportDtos;
import com.taskflow.taskflow_be.module.projectreport.service.ProjectReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/reports")
public class ProjectReportController {

    private final ProjectReportService projectReportService;

    @GetMapping("/summary")
    public ApiResponse<ProjectReportDtos.ProjectSummaryResponse> summary(@PathVariable UUID projectId) {
        return ApiResponse.ok(projectReportService.getSummary(projectId));
    }

    @GetMapping("/workload")
    public ApiResponse<ProjectReportDtos.WorkloadResponse> workload(@PathVariable UUID projectId) {
        return ApiResponse.ok(projectReportService.getWorkload(projectId));
    }

    @GetMapping("/sprint-progress")
    public ApiResponse<ProjectReportDtos.SprintProgressResponse> sprintProgress(@PathVariable UUID projectId) {
        return ApiResponse.ok(projectReportService.getSprintProgress(projectId));
    }
}
