package com.vilt.talentos.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}
