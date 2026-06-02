package com.vilt.talentos.dto;

import java.util.UUID;

public record SkillResponse(
    UUID id,
    String name,
    boolean active,
    Integer importanceWeight
) {}
