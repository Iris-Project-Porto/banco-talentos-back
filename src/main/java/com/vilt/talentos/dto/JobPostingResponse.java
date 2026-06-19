package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vilt.talentos.entity.ExperienceLevel;
import com.vilt.talentos.entity.JobPostingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobPostingResponse(
        UUID id,
        String vacancyCode,
        String title,
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

        JobPostingStatus status,
        String modality,
        String notes,
        Instant openingDate,
        Instant closingDate,
        Boolean isUrgent,
        Boolean active,
        List<JobPostingSkillResponse> skills,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}
