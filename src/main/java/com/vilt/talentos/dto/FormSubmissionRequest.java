package com.vilt.talentos.dto;

import java.util.Map;
import java.util.UUID;

public record FormSubmissionRequest(UUID formDefinitionId, Map<String, Object> answers) {
}
