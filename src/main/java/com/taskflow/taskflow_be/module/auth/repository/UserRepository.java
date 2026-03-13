package com.taskflow.taskflow_be.module.auth.repository;

import com.taskflow.taskflow_be.module.auth.entity.GlobalRole;
import com.taskflow.taskflow_be.module.auth.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
        select u from UserEntity u
        where (
            lower(u.username) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%'))
        )
        order by u.username asc
        """)
    List<UserEntity> searchAllUsers(@Param("q") String q, Pageable pageable);

    @Query("""
        select u from UserEntity u
        where u.id = :currentUserId
          and (
            lower(u.username) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%'))
          )
        order by u.username asc
        """)
    List<UserEntity> searchSelf(@Param("currentUserId") UUID currentUserId,
                                @Param("q") String q,
                                Pageable pageable);

    @Query("""
        select u from UserEntity u
        where (
            lower(u.username) like lower(concat('%', :q, '%'))
            or lower(coalesce(u.fullName, '')) like lower(concat('%', :q, '%'))
        )
          and (
            u.id = :currentUserId
            or exists (
                select 1 from ProjectMemberEntity pm
                where pm.user.id = u.id
                  and pm.project.id in :projectIds
            )
          )
        order by u.username asc
        """)
    List<UserEntity> searchWorkspaceUsers(@Param("currentUserId") UUID currentUserId,
                                          @Param("projectIds") List<UUID> projectIds,
                                          @Param("q") String q,
                                          Pageable pageable);
}
