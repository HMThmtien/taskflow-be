package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.module.auth.dto.AuthResponse;
import com.taskflow.taskflow_be.module.auth.dto.LoginRequest;
import com.taskflow.taskflow_be.module.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refresh(String refreshToken);
}
