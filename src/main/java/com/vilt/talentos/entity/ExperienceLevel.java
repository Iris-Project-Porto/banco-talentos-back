package com.vilt.talentos.entity;

import lombok.Getter;

@Getter
public enum ExperienceLevel {
    JUNIOR("Júnior"),
    PLENO("Pleno"),
    SENIOR("Sênior"),
    ESPECIALISTA("Especialista");

    private final String description;

    ExperienceLevel(String description) {
        this.description = description;
    }

    public static ExperienceLevel fromValue(String value) {
        if (value == null) return null;
        return switch (value) {
            case "Jr", "JUNIOR", "Júnior" -> JUNIOR;
            case "Pleno", "PLENO" -> PLENO;
            case "Sr", "SENIOR", "Sênior" -> SENIOR;
            case "Especialista", "ESPECIALISTA" -> ESPECIALISTA;
            default -> null;
        };
    }
}
