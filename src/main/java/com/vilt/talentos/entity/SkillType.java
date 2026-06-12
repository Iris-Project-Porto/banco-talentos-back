package com.vilt.talentos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillType {
    HARD("Hard Skill"),
    SOFT("Soft Skill");

    private final String description;
}
