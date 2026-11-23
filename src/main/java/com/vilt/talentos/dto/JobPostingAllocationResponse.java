package com.vilt.talentos.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobPostingAllocationResponse(
    UUID id,
    UUID jobPostingId,
    UUID resourceId,
    String resourceName,
    UUID projectId,
    String projectName,
    UUID squadId,
    String squadName,
    LocalDate startDate,
    String role,
    boolean active,
    Instant createdAt,
    String createdBy
) {}
