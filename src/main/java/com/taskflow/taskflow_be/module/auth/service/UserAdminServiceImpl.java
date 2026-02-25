package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.dto.AdminUserDtos;
import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepo;

    @Override
    public void changeRole(String username, GlobalRole role) {
        String target = username.trim();

        UserEntity user = userRepo.findByUsername(target)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        // ✅ chặn tự demote chính mình (tuỳ rule, nhưng production nên có)
        String me = SecurityUtils.currentUsername();
        if (me != null && me.equalsIgnoreCase(user.getUsername()) && role == GlobalRole.USER) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "You cannot demote yourself");
        }

        user.setRole(role);
        userRepo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDtos.UserResponse> listUsers(String q, GlobalRole role, Pageable pageable) {
        Page<UserEntity> page = userRepo.adminSearch(
                (q == null || q.isBlank()) ? null : q.trim(),
                role,
                pageable
        );

        return page.map(u -> AdminUserDtos.UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build());
    }
}