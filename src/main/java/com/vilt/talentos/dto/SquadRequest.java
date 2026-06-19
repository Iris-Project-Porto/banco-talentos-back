package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record SquadRequest(
    @NotBlank(message = "O nome da squad é obrigatório")
    String name,
    String description,
    String portoCoordinator,
    String projectManager,
    UUID projectId
) {}
