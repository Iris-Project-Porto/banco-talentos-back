package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupRequest(
    @NotBlank(message = "O nome do grupo é obrigatório")
    String name,
    String description
) {}
