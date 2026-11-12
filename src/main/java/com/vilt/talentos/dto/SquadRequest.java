package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SquadRequest(
    @NotBlank(message = "O nome da squad é obrigatório")
    String name,
    String description,
    String portoCoordinator,
    String projectManager,
    List<UUID> recursoIds,
    List<SquadResourceInput> recursos
) {
    public SquadRequest(String name, String description, String portoCoordinator, String projectManager) {
        this(name, description, portoCoordinator, projectManager, null, null);
    }

    public SquadRequest(String name, String description, String portoCoordinator, String projectManager, List<UUID> recursoIds) {
        this(name, description, portoCoordinator, projectManager, recursoIds, null);
    }
}
