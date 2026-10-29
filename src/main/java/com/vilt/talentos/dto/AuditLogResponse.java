package com.vilt.talentos.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        AuditOperation operation,
        String entityType,
        UUID entityId,
        JsonNode oldValue,
        JsonNode newValue,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser() != null ? auditLog.getUser().getId() : null,
                auditLog.getOperation(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getCreatedAt()
        );
    }
}
