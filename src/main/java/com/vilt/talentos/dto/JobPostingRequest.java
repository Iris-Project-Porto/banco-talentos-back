package com.vilt.talentos.dto;

import com.vilt.talentos.entity.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JobPostingRequest(
        String vacancyCode,

        @NotBlank(message = "O título da vaga é obrigatório")
        String title,

        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId,

        @NotNull(message = "O ID da squad é obrigatório")
        UUID squadId,

        @NotNull(message = "O nível de experiência é obrigatório")
        ExperienceLevel experienceLevel,

        String description,

        @NotBlank(message = "O recrutador é obrigatório")
        String recruiter,

        Integer estimatedAllocationWeeks,

        @NotBlank(message = "O status é obrigatório")
        String status,

        String modality,

        String notes,

        @NotNull(message = "A data de abertura é obrigatória")
        Instant openingDate,

        Instant closingDate,

        Boolean isUrgent,

        List<JobPostingSkillRequest> skills
) {}
