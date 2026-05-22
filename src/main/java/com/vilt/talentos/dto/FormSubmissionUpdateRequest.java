package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormSubmission;

import java.time.Instant;
import java.util.UUID;

public record FormSubmissionUpdateRequest(UUID id, UUID formDefinitionId, UUID userId, String answers, Instant updatedAt) {

    public FormSubmissionUpdateRequest(FormSubmission formSubmission) {
        this(formSubmission.getId(),formSubmission.getFormDefinitionId(),formSubmission.getUserId(),formSubmission.getAnswers(),formSubmission.getUpdatedAt());
    }

}
