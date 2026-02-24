package com.taskflow.taskflow_be.module.auth.service;

import com.taskflow.taskflow_be.exception.AppException;
import com.taskflow.taskflow_be.exception.ErrorCode;
import com.taskflow.taskflow_be.module.auth.dto.AuthResponse;
import com.taskflow.taskflow_be.module.auth.dto.LoginRequest;
import com.taskflow.taskflow_be.module.auth.dto.RegisterRequest;
import com.taskflow.taskflow_be.module.auth.entity.RefreshTokenEntity;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import com.taskflow.taskflow_be.module.auth.repository.RefreshTokenRepository;
import com.taskflow.taskflow_be.module.auth.repository.UserRepository;
import com.taskflow.taskflow_be.security.jwt.JwtProperties;
import com.taskflow.taskflow_be.security.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(
            UserRepository repo,
            RefreshTokenRepository refreshRepo,
            PasswordEncoder encoder,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.repo = repo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();

        if (repo.existsByUsername(username)) {
            throw new AppException(ErrorCode.USERNAME_TAKEN, HttpStatus.CONFLICT, "Username already taken");
        }

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole("USER");
        repo.save(user);

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        String username = req.username().trim();

        UserEntity user = repo.findByUsername(username)
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_CREDENTIALS,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(
                    ErrorCode.INVALID_CREDENTIALS,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshTokenEntity old = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(
                        ErrorCode.INVALID_TOKEN,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"
                ));

        if (old.isRevoked() || old.getExpiryDate().isBefore(Instant.now())) {
            throw new AppException(
                    ErrorCode.INVALID_TOKEN,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token"
            );
        }

        UserEntity user = old.getUser();

        // rotation: revoke old token
        old.setRevoked(true);
        refreshRepo.save(old);

        // issue new refresh token
        String newRefreshToken = createAndSaveRefreshToken(user);

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole()
        );
    }

    private String createAndSaveRefreshToken(UserEntity user) {
        String token = UUID.randomUUID().toString();

        refreshRepo.save(RefreshTokenEntity.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTtl()))
                .revoked(false)
                .build());

        return token;
    }
}