package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.dto.AuthResponse;
import com.taskflow.taskflow_be.module.auth.dto.LoginRequest;
import com.taskflow.taskflow_be.module.auth.dto.RegisterRequest;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.security.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();
        if (repo.existsByUsername(username)) {
            throw new AppException(ErrorCode.USERNAME_TAKEN, HttpStatus.CONFLICT, "Username already taken");
        }

        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole("USER");
        repo.save(u);

        String token = jwtService.generateAccessToken(u.getUsername(), u.getRole());
        return new AuthResponse(token, new AuthResponse.UserView(u.getUsername(), u.getRole()));
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        UserEntity u = repo.findByUsername(req.username().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateAccessToken(u.getUsername(), u.getRole());
        return new AuthResponse(token, new AuthResponse.UserView(u.getUsername(), u.getRole()));
    }
}
