package com.taskflow.taskflow_be.module.reportview.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.reportview.dto.ReportViewDtos;
import com.taskflow.taskflow_be.module.reportview.service.ReportViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report-views")
public class ReportViewController {

    private final ReportViewService reportViewService;

    @GetMapping
    public ApiResponse<List<ReportViewDtos.ReportViewResponse>> list(
            @RequestParam String routeKey
    ) {
        return ApiResponse.ok(reportViewService.list(routeKey));
    }

    @PostMapping
    public ApiResponse<ReportViewDtos.ReportViewResponse> create(
            @Valid @RequestBody ReportViewDtos.CreateReportViewRequest request
    ) {
        return ApiResponse.ok(reportViewService.create(request));
    }

    @DeleteMapping("/{reportViewId}")
    public ApiResponse<Map<String, String>> delete(
            @PathVariable UUID reportViewId
    ) {
        reportViewService.delete(reportViewId);
        return ApiResponse.ok(Map.of("message", "Saved view deleted"));
    }
}
