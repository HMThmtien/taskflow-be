package com.taskflow.taskflow_be.module.auth.controller;

import com.taskflow.taskflow_be.common.response.ApiResponse;
import com.taskflow.taskflow_be.common.util.SecurityUtils;
import com.taskflow.taskflow_be.module.auth.dto.AuthDtos;
import com.taskflow.taskflow_be.module.auth.dto.AuthResponse;
import com.taskflow.taskflow_be.module.auth.dto.LoginRequest;
import com.taskflow.taskflow_be.module.auth.dto.RegisterRequest;
import com.taskflow.taskflow_be.module.auth.service.AuthService;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;
    private final UserRepository userRepo;

    public AuthController(AuthService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
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

    @GetMapping("/me")
    public ApiResponse<AuthDtos.MeResponse> me() {
        UUID userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }

        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return ApiResponse.ok(new AuthDtos.MeResponse(user.getId(), user.getUsername(), user.getRole()));
    }
}