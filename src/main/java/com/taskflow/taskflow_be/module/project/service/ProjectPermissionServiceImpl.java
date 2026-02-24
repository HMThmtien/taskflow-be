package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProjectPermissionServiceImpl implements ProjectPermissionService {

    private final ProjectMemberRepository repo;

    public ProjectPermissionServiceImpl(ProjectMemberRepository repo) {
        this.repo = repo;
    }

    @Override
    public void requireMember(UUID projectId, UUID userId) {
        if (!repo.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
    }

    @Override
    public void requireWrite(UUID projectId, UUID userId) {
        var member = repo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "Access denied"));

        if (member.getRole() == ProjectRole.VIEWER) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
    }

    @Override
    public void requireAdmin(UUID projectId, UUID userId) {
        var member = repo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, "Access denied"));

        if (member.getRole() != ProjectRole.ADMIN &&
                member.getRole() != ProjectRole.OWNER) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
    }
}