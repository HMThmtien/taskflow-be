package com.taskflow.taskflow_be.module.projectreport.service;

import com.taskflow.taskflow_be.module.projectreport.dto.ProjectReportDtos;

import java.util.UUID;

public interface ProjectReportService {
    ProjectReportDtos.ProjectSummaryResponse getSummary(UUID projectId);

    ProjectReportDtos.WorkloadResponse getWorkload(UUID projectId);

    ProjectReportDtos.SprintProgressResponse getSprintProgress(UUID projectId);
}
