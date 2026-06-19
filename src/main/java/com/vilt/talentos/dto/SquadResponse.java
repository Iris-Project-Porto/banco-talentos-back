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
    String updatedBy
) {}
