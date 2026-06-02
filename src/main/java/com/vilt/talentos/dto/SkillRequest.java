package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
    @NotBlank(message = "O nome da skill é obrigatório")
    String name,
    Integer importanceWeight
) {}
