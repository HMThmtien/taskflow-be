package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepo;

    @Override
    public void changeRole(String username, String role) {
        String newRole = role.trim().toUpperCase();

        // validate role hợp lệ (tối thiểu USER/ADMIN)
        if (!newRole.equals("USER") && !newRole.equals("ADMIN")) {
            throw new AppException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "Invalid role");
        }

        UserEntity user = userRepo.findByUsername(username.trim())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "User not found"));

        user.setRole(newRole);
        userRepo.save(user);
    }
}