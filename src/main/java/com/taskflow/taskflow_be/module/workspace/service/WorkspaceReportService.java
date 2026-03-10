package com.taskflow.taskflow_be.module.workspace.service;

import com.taskflow.taskflow_be.module.workspace.dto.WorkspaceReportDtos;

import java.time.LocalDate;
import java.util.UUID;

public interface WorkspaceReportService {
    WorkspaceReportDtos.WorkspaceReportResponse getWorkspaceReport(
            LocalDate from,
            LocalDate to,
            UUID projectId
    );
}