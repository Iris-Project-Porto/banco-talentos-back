package com.vilt.talentos.dto;

import java.util.UUID;

public record AuditEntityContext(UUID entityId, String entityName) {
}
