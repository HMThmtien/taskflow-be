package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.module.project.dto.ProjectMemberDtos;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {

    ProjectMemberDtos.MemberResponse addMember(UUID projectId, ProjectMemberDtos.AddMemberRequest req);

    ProjectMemberDtos.MemberResponse updateRole(UUID projectId, UUID userId, ProjectMemberDtos.UpdateMemberRoleRequest req);

    void removeMember(UUID projectId, UUID userId);

    List<ProjectMemberDtos.MemberResponse> listMembers(UUID projectId);

    ProjectMemberDtos.MyProjectRoleResponse myRole(UUID projectId);
}