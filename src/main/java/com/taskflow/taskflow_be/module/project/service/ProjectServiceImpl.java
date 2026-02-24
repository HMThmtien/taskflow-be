package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.project.dto.ProjectDtos;
import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import com.taskflow.taskflow_be.module.project.mapper.ProjectMapper;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repo;
    private final ProjectMemberRepository projectMemberRepo;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDtos.ProjectResponse> list() {
        return repo.findAll().stream().map(ProjectMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDtos.ProjectResponse get(UUID id) {
        var p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));
        return ProjectMapper.toResponse(p);
    }

    @Override
    public ProjectDtos.ProjectResponse create(ProjectDtos.CreateProjectRequest req) {

        UUID userId = SecurityUtils.currentUserId();

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found"));

        ProjectEntity project = ProjectEntity.builder()
                .key(req.getKey().trim())
                .name(req.getName().trim())
                .description(req.getDescription())
                .build();

        repo.save(project);

        projectMemberRepo.save(ProjectMemberEntity.builder()
                .project(project)
                .user(user)
                .role(ProjectRole.OWNER)
                .build());

        return ProjectMapper.toResponse(project);
    }

    @Override
    public ProjectDtos.ProjectResponse update(UUID id, ProjectDtos.UpdateProjectRequest req) {
        var p = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());

        return ProjectMapper.toResponse(p);
    }
}