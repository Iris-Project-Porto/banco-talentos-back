package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequest(
    @NotBlank(message = "O nome do projeto é obrigatório")
    String name,
    String description
) {}
