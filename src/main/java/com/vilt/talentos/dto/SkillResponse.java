package com.vilt.talentos.dto;

import com.vilt.talentos.entity.SkillType;
import java.util.UUID;

public record SkillResponse(
    UUID id,
    String name,
    SkillType type,
    boolean active,
    Integer importanceWeight
) {}
