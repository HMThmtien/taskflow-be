package com.taskflow.taskflow_be.module.project.dto;

import com.taskflow.taskflow_be.module.project.entity.ProjectRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ProjectMemberDtos {

    @Getter @Setter
    public static class AddMemberRequest {
        @NotBlank
        private String username; // add theo username cho tiện test

        @NotNull
        private ProjectRole role; // OWNER/ADMIN/MEMBER/VIEWER (thường không cho set OWNER qua API)
    }

    @Getter @Setter
    public static class UpdateMemberRoleRequest {
        @NotNull
        private ProjectRole role;
    }

    @Builder @Getter
    public static class MemberResponse {
        private UUID userId;
        private String username;
        private ProjectRole role;
    }

    @Builder @Getter
    public static class MyProjectRoleResponse {
        private UUID projectId;
        private UUID userId;
        private ProjectRole role;
    }
}