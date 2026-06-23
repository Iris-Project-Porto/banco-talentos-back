package com.vilt.talentos.dto;

import com.vilt.talentos.entity.SkillCategory;
import com.vilt.talentos.entity.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SkillRequest(
    @NotBlank(message = "O nome da skill é obrigatório")
    String name,
    
    @NotNull(message = "O tipo da skill é obrigatório")
    SkillType type,
    
    String description,
    
    SkillCategory category
) {}
