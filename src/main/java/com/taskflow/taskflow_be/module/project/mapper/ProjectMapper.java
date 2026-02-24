package com.taskflow.taskflow_be.module.project.mapper;

import com.taskflow.taskflow_be.module.project.dto.ProjectDtos;
import com.taskflow.taskflow_be.module.project.entity.ProjectEntity;

public class ProjectMapper {
    public static ProjectDtos.ProjectResponse toResponse(ProjectEntity e) {
        return ProjectDtos.ProjectResponse.builder()
                .id(e.getId())
                .key(e.getKey())
                .name(e.getName())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }
}