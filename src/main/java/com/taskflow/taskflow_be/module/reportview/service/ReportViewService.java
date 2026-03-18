package com.taskflow.taskflow_be.module.reportview.service;

import com.taskflow.taskflow_be.module.reportview.dto.ReportViewDtos;

import java.util.List;
import java.util.UUID;

public interface ReportViewService {
    List<ReportViewDtos.ReportViewResponse> list(String routeKey);
    ReportViewDtos.ReportViewResponse create(ReportViewDtos.CreateReportViewRequest request);
    void delete(UUID reportViewId);
}
