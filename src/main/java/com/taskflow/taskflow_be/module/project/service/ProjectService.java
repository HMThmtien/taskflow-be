package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.module.project.dto.ProjectDtos;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    List<ProjectDtos.ProjectResponse> list();
    ProjectDtos.ProjectResponse get(UUID id);
    ProjectDtos.ProjectResponse create(ProjectDtos.CreateProjectRequest req);
    ProjectDtos.ProjectResponse update(UUID id, ProjectDtos.UpdateProjectRequest req);
}