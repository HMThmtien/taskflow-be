package com.taskflow.taskflow_be.module.auth.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.auth.dto.AdminUserDtos;
import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.auth.service.UserAdminService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService service;

    // ✅ list users: /api/admin/users?q=hm&role=ADMIN&page=0&size=20&sort=username,asc
    @GetMapping
    public ApiResponse<Page<AdminUserDtos.UserResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) GlobalRole role,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(service.listUsers(q, role, pageable));
    }

    @PatchMapping("/{username}/role")
    public ApiResponse<Void> changeRole(
            @PathVariable String username,
            @RequestParam @NotBlank GlobalRole role
    ) {
        service.changeRole(username, role);
        return ApiResponse.ok(null);
    }
}