package com.vilt.talentos.dto;

import com.vilt.talentos.entity.RequirementType;
import com.vilt.talentos.entity.SkillLevel;

public record JobPostingSkillResponse(
    String name, 
    RequirementType type, 
    SkillLevel minLevel,
    Integer importanceWeight,
    String description
) {}
