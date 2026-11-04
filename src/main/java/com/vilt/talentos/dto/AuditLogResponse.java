package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        UUID userAffectedId,
        AuditOperation operation,
        String entityType,
        UUID entityId,
        @JsonInclude(JsonInclude.Include.NON_NULL) JsonNode oldValue,
        @JsonInclude(JsonInclude.Include.NON_NULL) JsonNode newValue,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser() != null ? auditLog.getUser().getId() : null,
                auditLog.getUserAffected() != null ? auditLog.getUserAffected().getId() : null,
                auditLog.getOperation(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getCreatedAt()
        );
    }

    public static AuditLogResponse summary(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser() != null ? auditLog.getUser().getId() : null,
                auditLog.getUserAffected() != null ? auditLog.getUserAffected().getId() : null,
                auditLog.getOperation(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                null,
                null,
                auditLog.getCreatedAt()
        );
    }
}
