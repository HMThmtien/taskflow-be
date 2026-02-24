package com.taskflow.taskflow_be.module.project.controller;

import com.taskflow.taskflow_be.module.project.dto.ProjectMemberDtos;
import com.taskflow.taskflow_be.module.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService service;

    @GetMapping
    public List<ProjectMemberDtos.MemberResponse> list(@PathVariable UUID projectId) {
        return service.listMembers(projectId);
    }

    @GetMapping("/me")
    public ProjectMemberDtos.MyProjectRoleResponse myRole(@PathVariable UUID projectId) {
        return service.myRole(projectId);
    }

    @PostMapping
    public ProjectMemberDtos.MemberResponse add(@PathVariable UUID projectId,
                                                @Valid @RequestBody ProjectMemberDtos.AddMemberRequest req) {
        return service.addMember(projectId, req);
    }

    @PatchMapping("/{userId}")
    public ProjectMemberDtos.MemberResponse updateRole(@PathVariable UUID projectId,
                                                       @PathVariable UUID userId,
                                                       @Valid @RequestBody ProjectMemberDtos.UpdateMemberRoleRequest req) {
        return service.updateRole(projectId, userId, req);
    }

    @DeleteMapping("/{userId}")
    public void remove(@PathVariable UUID projectId, @PathVariable UUID userId) {
        service.removeMember(projectId, userId);
    }
}