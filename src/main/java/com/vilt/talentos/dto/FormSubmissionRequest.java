package com.vilt.talentos.dto;

import java.util.UUID;

public record FormSubmissionRequest(UUID formDefinitionId,UUID userId,String answers) {
}
