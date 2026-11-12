package com.vilt.talentos.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditLogResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void summaryIncludesActorAndAffectedUserNames() {
        User actor = User.builder().id(UUID.randomUUID()).name("Admin").build();
        User affected = User.builder().id(UUID.randomUUID()).name("Recurso").build();
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .user(actor)
                .userAffected(affected)
                .operation(AuditOperation.UPDATE)
                .entityType("PROFILE")
                .entityId(UUID.randomUUID())
                .build();

        AuditLogResponse response = AuditLogResponse.summary(log);

        assertEquals("Admin", response.userName());
        assertEquals("Recurso", response.userAffectedName());
        assertNull(response.oldValue());
        assertNull(response.newValue());
    }

    @Test
    void summaryUsesSnapshotNameWhenAffectedUserWasDeleted() {
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .operation(AuditOperation.DELETE)
                .entityType("PROFILE")
                .entityId(UUID.randomUUID())
                .oldValue(objectMapper.valueToTree(Map.of("Nome do usuário", "Recurso removido")))
                .build();

        AuditLogResponse response = AuditLogResponse.summary(log);

        assertNull(response.userAffectedId());
        assertEquals("Recurso removido", response.userAffectedName());
    }
}
