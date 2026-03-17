package com.taskflow.taskflow_be.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuditLogService {

    public void log(String action, UUID actorId, String entityType, UUID entityId, Map<String, Object> details) {
        log.info(
                "audit action={} actorId={} entityType={} entityId={} details={}",
                action,
                actorId,
                entityType,
                entityId,
                details == null ? Map.of() : details
        );
    }
}
