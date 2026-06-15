package com.vilt.talentos.dto;

import com.vilt.talentos.entity.SkillType;

public record ProfileSkillResponse(
    String name,
    SkillType type,
    Integer proficiencyLevel
) {}
