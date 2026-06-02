package com.vilt.talentos.dto;

import com.vilt.talentos.entity.ExperienceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record JobPostingRequest(
        @NotNull(message = "O ID do projeto é obrigatório")
        UUID projectId,

        @NotNull(message = "O ID da squad é obrigatório")
        UUID squadId,

        @NotNull(message = "O nível de experiência é obrigatório")
        ExperienceLevel experienceLevel,

        String description,

        @NotBlank(message = "Os requisitos são obrigatórios")
        String requirements,

        @NotBlank(message = "O recrutador é obrigatório")
        String recruiter,

        int estimatedAllocationWeeks,

        @NotBlank(message = "O status é obrigatório")
        String status,

        String notes,

        @NotNull(message = "A data de abertura é obrigatória")
        Instant openingDate,

        Boolean isUrgent
) {}
