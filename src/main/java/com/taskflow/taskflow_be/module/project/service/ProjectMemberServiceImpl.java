package com.taskflow.taskflow_be.module.project.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.exception.NotFoundException;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.module.project.dto.ProjectMemberDtos;
import com.taskflow.taskflow_be.module.project.entity.ProjectMemberEntity;
import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import com.taskflow.taskflow_be.module.project.repository.ProjectMemberRepository;
import com.taskflow.taskflow_be.module.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepo;
    private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final ProjectPermissionService permission;

    @Override
    public ProjectMemberDtos.MemberResponse addMember(UUID projectId, ProjectMemberDtos.AddMemberRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        var project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.PROJECT_NOT_FOUND));

        UserEntity user = userRepo.findByUsername(req.getUsername().trim())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        // (tuỳ bạn) chặn set OWNER qua API
        if (req.getRole() == ProjectRole.OWNER) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Cannot assign OWNER via API");
        }

        // upsert: nếu đã là member thì update role
        var existing = memberRepo.findByProjectIdAndUserId(projectId, user.getId());
        ProjectMemberEntity member = existing.orElseGet(() -> ProjectMemberEntity.builder()
                .project(project)
                .user(user)
                .build());

        member.setRole(req.getRole());
        memberRepo.save(member);

        return ProjectMemberDtos.MemberResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(member.getRole())
                .build();
    }

    @Override
    public ProjectMemberDtos.MemberResponse updateRole(UUID projectId, UUID userId, ProjectMemberDtos.UpdateMemberRoleRequest req) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        var member = memberRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND, "Member not found"));

        // (tuỳ bạn) chặn đổi role OWNER qua API
        if (req.getRole() == ProjectRole.OWNER) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Cannot assign OWNER via API");
        }

        member.setRole(req.getRole());
        memberRepo.save(member);

        return ProjectMemberDtos.MemberResponse.builder()
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .role(member.getRole())
                .build();
    }

    @Override
    public void removeMember(UUID projectId, UUID userId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireAdmin(projectId, currentUserId);

        var member = memberRepo.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND, HttpStatus.NOT_FOUND, "Member not found"));

        // (tuỳ bạn) không cho xoá OWNER
        if (member.getRole() == ProjectRole.OWNER) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Cannot remove OWNER");
        }

        memberRepo.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberDtos.MemberResponse> listMembers(UUID projectId) {
        UUID currentUserId = SecurityUtils.currentUserId();
        permission.requireMember(projectId, currentUserId);

        return memberRepo.findAllByProjectId(projectId).stream()
                .map(m -> ProjectMemberDtos.MemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .role(m.getRole())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMemberDtos.MyProjectRoleResponse myRole(UUID projectId) {
        UUID currentUserId = SecurityUtils.currentUserId();

        var member = memberRepo.findByProjectIdAndUserId(projectId, currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, "Not a project member"));

        return ProjectMemberDtos.MyProjectRoleResponse.builder()
                .projectId(projectId)
                .userId(currentUserId)
                .role(member.getRole())
                .build();
    }
}