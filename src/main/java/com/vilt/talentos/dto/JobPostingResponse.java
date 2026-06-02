package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vilt.talentos.entity.ExperienceLevel;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobPostingResponse(
        UUID id,
        String projectName,
        UUID projectId,
        String squadName,
        UUID squadId,
        ExperienceLevel experienceLevel,
        String experienceLevelDescription,
        String description,
        String requirements,
        String recruiter,
        Integer estimatedAllocationWeeks,

        String status,
        String notes,
        Instant openingDate,
        Boolean isUrgent,
        Boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}
