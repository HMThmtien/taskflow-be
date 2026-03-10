package com.taskflow.taskflow_be.module.auth.repository;

import com.taskflow.taskflow_be.module.auth.entity.RefreshTokenEntity;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);

    @Modifying
    @Query("update RefreshTokenEntity r set r.revoked = true where r.user.id = :userId and r.revoked = false")
    void revokeAllActiveTokensByUserId(UUID userId);
}
