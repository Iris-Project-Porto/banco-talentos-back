package com.vilt.talentos.dto;

import java.util.List;

public record ProfileRequest(
    // Identificação
    String photoUrl,
    String cargo,

    // Perfil Técnico
    String area,
    String skills_text,
    String sobre,
    String prontidaoStack,

    // Alocação e Potencial
    String alocacaoStatus,
    Integer nivelMentoria,
    String autonomia,
    String trilhaCarreira,
    String certificacoesCount,
    String nivelAcompanhamento,

    // Code Review (Matriz de Conhecimentos)
    String codeReviewAtuacao,

    // Extras
    Integer experienceYears,
    Integer projectsCount,
    String availability,
    String certifications,
    String linkedinUrl,
    String githubUrl,

    String registrationNumber,
    String registrationStatus,

    List<SkillEntry> skills
) {
}
