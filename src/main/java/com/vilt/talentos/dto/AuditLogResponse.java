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
        String userName,
        UUID userAffectedId,
        String userAffectedName,
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
                userId(auditLog),
                userName(auditLog),
                userAffectedId(auditLog),
                userAffectedName(auditLog),
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
                userId(auditLog),
                userName(auditLog),
                userAffectedId(auditLog),
                userAffectedName(auditLog),
                auditLog.getOperation(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                null,
                null,
                auditLog.getCreatedAt()
        );
    }

    private static UUID userId(AuditLog auditLog) {
        return auditLog.getUser() == null ? null : auditLog.getUser().getId();
    }

    private static String userName(AuditLog auditLog) {
        return auditLog.getUser() == null ? null : auditLog.getUser().getName();
    }

    private static UUID userAffectedId(AuditLog auditLog) {
        return auditLog.getUserAffected() == null ? null : auditLog.getUserAffected().getId();
    }

    private static String userAffectedName(AuditLog auditLog) {
        if (auditLog.getUserAffected() != null) {
            return auditLog.getUserAffected().getName();
        }

        String name = snapshotUserName(auditLog.getOldValue());
        return name != null ? name : snapshotUserName(auditLog.getNewValue());
    }

    private static String snapshotUserName(JsonNode snapshot) {
        if (snapshot == null) {
            return null;
        }
        JsonNode name = snapshot.path("Nome do usuário");
        return name.isTextual() ? name.asText() : null;
    }
}
