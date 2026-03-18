package com.taskflow.taskflow_be.common.audit.repository;

import com.taskflow.taskflow_be.common.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {
}
