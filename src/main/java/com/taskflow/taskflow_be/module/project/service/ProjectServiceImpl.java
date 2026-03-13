package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.project.dto.ProjectDtos;
import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import com.taskflow.taskflow_be.module.project.mapper.ProjectMapper;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repo;
    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepository;
    private final ProjectPermissionService permission;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDtos.ProjectResponse> list() {
        UUID userId = SecurityUtils.currentUserId();

        return projectMemberRepo.findAllByUserId(userId).stream()
                .map(ProjectMemberEntity::getProject)
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDtos.ProjectResponse get(UUID id) {
        UUID userId = SecurityUtils.currentUserId();

        var member = projectMemberRepo.findByProjectIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED,"Access denied"));

        return ProjectMapper.toResponse(member.getProject());
    }

    @Override
    public ProjectDtos.ProjectResponse create(ProjectDtos.CreateProjectRequest req) {

        UUID userId = SecurityUtils.currentUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String normalizedKey = normalizeKey(req.getKey());
        if (repo.existsByKey(normalizedKey)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.CONFLICT, "Project key already exists");
        }

        ProjectEntity project = ProjectEntity.builder()
                .key(normalizedKey)
                .name(req.getName().trim())
                .description(normalizeDescription(req.getDescription()))
                .build();

        ProjectEntity e = repo.save(project);

        projectMemberRepo.save(ProjectMemberEntity.builder()
                .project(e)
                .user(user)
                .role(ProjectRole.OWNER)
                .build());

        return ProjectMapper.toResponse(e);
    }

    @Override
    public ProjectDtos.ProjectResponse update(UUID id, ProjectDtos.UpdateProjectRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(id, currentUserId);

        var p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        String normalizedKey = normalizeKey(req.getKey());
        if (repo.existsByKeyAndIdNot(normalizedKey, id)) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.CONFLICT, "Project key already exists");
        }

        p.setKey(normalizedKey);
        p.setName(req.getName().trim());
        p.setDescription(normalizeDescription(req.getDescription()));

        return ProjectMapper.toResponse(repo.save(p));
    }

    @Override
    public ProjectDtos.ProjectResponse archive(UUID id) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(id, currentUserId);

        var project = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isArchived()) {
            project.setArchived(true);
            project.setArchivedAt(Instant.now());
        }

        return ProjectMapper.toResponse(repo.save(project));
    }

    @Override
    public ProjectDtos.ProjectResponse unarchive(UUID id) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(id, currentUserId);

        var project = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        if (project.isArchived()) {
            project.setArchived(false);
            project.setArchivedAt(null);
        }

        return ProjectMapper.toResponse(repo.save(project));
    }

    @Override
    public void delete(UUID id) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireOwner(id, currentUserId);

        var project = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        repo.delete(project);
    }

    private String normalizeKey(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase();
        if (value.isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Project key is required");
        }
        return value;
    }

    private String normalizeDescription(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        return value.isBlank() ? null : value;
    }
}
