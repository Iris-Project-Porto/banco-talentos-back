package com.vilt.talentos.dto;

import java.util.Map;
import java.util.UUID;

public record FormDefinitionResponse(
        UUID id,
        Map<String, Object> elements,
        boolean active,
        String title,
        UUID groupId) {
}
