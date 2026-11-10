package com.vilt.talentos.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SquadResponse(
    UUID id,
    String name,
    String description,
    String portoCoordinator,
    String projectManager,
    String projectName,
    UUID projectId,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy,
    List<SquadResourceResponse> recursos
) {
    public SquadResponse {
        if (recursos == null) {
            recursos = List.of();
        }
    }

    public SquadResponse(
        UUID id,
        String name,
        String description,
        String portoCoordinator,
        String projectManager,
        String projectName,
        UUID projectId,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
    ) {
        this(id, name, description, portoCoordinator, projectManager, projectName, projectId, active, createdAt, updatedAt, createdBy, updatedBy, List.of());
    }

    public SquadResponse withRecursos(List<SquadResourceResponse> recursos) {
        return new SquadResponse(
            id, name, description, portoCoordinator, projectManager,
            projectName, projectId, active, createdAt, updatedAt,
            createdBy, updatedBy, recursos
        );
    }
}
