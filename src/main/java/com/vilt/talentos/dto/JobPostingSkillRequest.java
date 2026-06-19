package com.vilt.talentos.dto;

import com.vilt.talentos.entity.RequirementType;
import com.vilt.talentos.entity.SkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobPostingSkillRequest(
    @NotBlank(message = "O nome da skill é obrigatório")
    String name,

    @NotNull(message = "O tipo de necessidade da skill é obrigatório")
    RequirementType type,

    @NotNull(message = "O nível mínimo da skill é obrigatório")
    SkillLevel minLevel,
    
    @NotNull(message = "O peso da skill é obrigatório")
    Integer importanceWeight,

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    String description
) {}
