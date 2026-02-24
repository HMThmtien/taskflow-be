package com.taskflow.taskflow_be.module.auth.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.module.auth.dto.AuthResponse;
import com.taskflow.taskflow_be.module.auth.dto.LoginRequest;
import com.taskflow.taskflow_be.module.auth.dto.RegisterRequest;
import com.taskflow.taskflow_be.module.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(service.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(service.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestParam String refreshToken) {
        return ApiResponse.ok(service.refresh(refreshToken));
    }
}