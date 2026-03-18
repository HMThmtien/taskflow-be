package com.taskflow.taskflow_be.module.reportview.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import com.taskflow.taskflow_be.module.project.service.ProjectPermissionService;
import com.taskflow.taskflow_be.module.reportview.dto.ReportViewDtos;
import com.taskflow.taskflow_be.module.reportview.entity.ReportViewEntity;
import com.taskflow.taskflow_be.module.reportview.repository.ReportViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportViewServiceImpl implements ReportViewService {

    private final ReportViewRepository reportViewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectPermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public List<ReportViewDtos.ReportViewResponse> list(String routeKey) {
        UUID currentUserId = SecurityUtils.currentUserId();
        String normalizedRouteKey = normalizeRouteKey(routeKey);
        return reportViewRepository.findAllByUser_IdAndRouteKeyOrderByIsDefaultDescUpdatedAtDesc(currentUserId, normalizedRouteKey)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReportViewDtos.ReportViewResponse create(ReportViewDtos.CreateReportViewRequest request) {
        UUID currentUserId = SecurityUtils.currentUserId();
        String normalizedRouteKey = normalizeRouteKey(request.getRouteKey());
        String normalizedName = normalizeName(request.getName());
        UUID projectId = request.getProjectId();
        if (projectId == null) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Project is required");
        }

        permissionService.requireMember(projectId, currentUserId);

        var user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND, "Project not found"));

        ReportViewEntity saved = reportViewRepository.save(ReportViewEntity.builder()
                .user(user)
                .project(project)
                .routeKey(normalizedRouteKey)
                .name(normalizedName)
                .isDefault(request.isDefault())
                .build());

        if (saved.isDefault()) {
            reportViewRepository.clearOtherDefaults(currentUserId, normalizedRouteKey, saved.getId());
        }

        return toResponse(saved);
    }

    @Override
    public void delete(UUID reportViewId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        ReportViewEntity view = reportViewRepository.findByIdAndUser_Id(reportViewId, currentUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND, "Saved view not found"));
        reportViewRepository.delete(view);
    }

    private String normalizeRouteKey(String value) {
        String routeKey = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (!List.of("dashboard", "reports").contains(routeKey)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Route key must be dashboard or reports");
        }
        return routeKey;
    }

    private String normalizeName(String value) {
        String name = value == null ? "" : value.trim();
        if (name.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (name.length() > 120) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Name must be 120 characters or fewer");
        }
        return name;
    }

    private ReportViewDtos.ReportViewResponse toResponse(ReportViewEntity entity) {
        return ReportViewDtos.ReportViewResponse.builder()
                .id(entity.getId())
                .routeKey(entity.getRouteKey())
                .name(entity.getName())
                .projectId(entity.getProject().getId())
                .isDefault(entity.isDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
