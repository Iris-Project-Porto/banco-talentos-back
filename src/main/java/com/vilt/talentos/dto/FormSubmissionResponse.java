package com.vilt.talentos.dto;

import java.util.Map;
import java.util.UUID;
import java.time.Instant;

public record FormSubmissionResponse(
        UUID id,
        UUID formDefinitionId,
        UUID userId,
        Map<String, Object> answers,
        Instant updatedAt
) {
}
