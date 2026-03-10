package com.taskflow.taskflow_be.module.auth.repository;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    boolean existsByEmailIgnoreCase(String email);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);

    // ✅ q + role optional
    @Query("""
        select u from UserEntity u
        where (:q is null or lower(u.username) like lower(concat('%', :q, '%')))
          and (:role is null or u.role = :role)
        """)
    Page<UserEntity> adminSearch(@Param("q") String q,
                                 @Param("role") GlobalRole role,
                                 Pageable pageable);
}