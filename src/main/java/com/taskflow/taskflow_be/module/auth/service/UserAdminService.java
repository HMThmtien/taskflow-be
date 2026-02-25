package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.module.auth.dto.AdminUserDtos;
import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {
    void changeRole(String username, GlobalRole role);

    Page<AdminUserDtos.UserResponse> listUsers(String q, GlobalRole role, Pageable pageable);
}