package com.taskflow.taskflow_be.module.project.controller;

import com.taskflow.taskflow_be.module.project.dto.ProjectDtos;
import com.taskflow.taskflow_be.module.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    @GetMapping
    public List<ProjectDtos.ProjectResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ProjectDtos.ProjectResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    public ProjectDtos.ProjectResponse create(@Valid @RequestBody ProjectDtos.CreateProjectRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProjectDtos.ProjectResponse update(@PathVariable UUID id, @Valid @RequestBody ProjectDtos.UpdateProjectRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/archive")
    public ProjectDtos.ProjectResponse archive(@PathVariable UUID id) {
        return service.archive(id);
    }

    @PatchMapping("/{id}/unarchive")
    public ProjectDtos.ProjectResponse unarchive(@PathVariable UUID id) {
        return service.unarchive(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
