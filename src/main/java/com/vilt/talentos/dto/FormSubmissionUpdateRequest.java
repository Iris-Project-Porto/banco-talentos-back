package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormSubmission;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FormSubmissionUpdateRequest(UUID id, UUID formDefinitionId, UUID userId, Map<String, Object> answers, Instant updatedAt) {

    public FormSubmissionUpdateRequest(FormSubmission formSubmission) {
        this(formSubmission.getId(), formSubmission.getFormDefinition().getId(), formSubmission.getUser().getId(), formSubmission.getAnswers(), formSubmission.getUpdatedAt());
    }

}
