package com.taskflow.taskflow_be.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskflow_be.common.audit.entity.AuditLogEntity;
import com.taskflow.taskflow_be.common.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void log(String action, UUID actorId, String entityType, UUID entityId, Map<String, Object> details) {
        Map<String, Object> safeDetails = details == null ? Map.of() : details;
        String requestId = MDC.get("requestId");

        try {
            auditLogRepository.save(AuditLogEntity.builder()
                    .action(action)
                    .actorId(actorId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .requestId(requestId)
                    .detailsJson(serializeDetails(safeDetails))
                    .build());
        } catch (Exception ex) {
            log.warn("audit.persist_failed action={} entityType={} entityId={}", action, entityType, entityId, ex);
        }

        log.info(
                "audit action={} actorId={} entityType={} entityId={} requestId={} details={}",
                action,
                actorId,
                entityType,
                entityId,
                requestId,
                safeDetails
        );
    }

    private String serializeDetails(Map<String, Object> details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException ex) {
            log.warn("audit.details_serialize_failed", ex);
            return "{\"serializationError\":true}";
        }
    }
}
