package com.vilt.talentos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SkillCategory {
    FRONTEND("Frontend"),
    BACKEND("Backend"),
    DESIGN("Design"),
    QA("QA"),
    MOBILE("Mobile"),
    DATA_SCIENCE("Data Science"),
    DEVOPS("DevOps"),
    MANAGEMENT("Gestão / Agilidade");

    private final String displayName;
}
