package com.taskflow.taskflow_be.module.reportview.repository;

import com.taskflow.taskflow_be.module.reportview.entity.ReportViewEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportViewRepository extends JpaRepository<ReportViewEntity, UUID> {

    List<ReportViewEntity> findAllByUser_IdAndRouteKeyOrderByIsDefaultDescUpdatedAtDesc(UUID userId, String routeKey);

    Optional<ReportViewEntity> findByIdAndUser_Id(UUID id, UUID userId);

    @Modifying
    @Transactional
    @Query("""
        update ReportViewEntity rv
        set rv.isDefault = false
        where rv.user.id = :userId
          and rv.routeKey = :routeKey
          and rv.id <> :excludeId
    """)
    int clearOtherDefaults(UUID userId, String routeKey, UUID excludeId);
}
