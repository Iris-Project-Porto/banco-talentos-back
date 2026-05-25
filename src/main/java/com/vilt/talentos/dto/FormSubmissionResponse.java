package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormSubmission;

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

    public FormSubmissionResponse(FormSubmission submission){
        this(
                submission.getId(),
                submission.getFormDefinitionId(),
                submission.getUserId(),
                submission.getAnswers(),
                submission.getUpdatedAt()
        );
    }
}
