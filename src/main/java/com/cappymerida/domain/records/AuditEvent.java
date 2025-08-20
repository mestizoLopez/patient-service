package com.cappymerida.domain.records;

import java.time.LocalDateTime;
import java.util.Map;

public record AuditEvent(
        String eventId,
        String auditType,
        String action,
        String resourceId,
        String userId,
        LocalDateTime timestamp,
        Map<String, Object> metadata
) {}
