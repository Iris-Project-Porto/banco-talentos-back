package com.vilt.talentos.dto;

import java.util.List;
import java.util.UUID;

public record AdminUpdateRequest(
    String status,
    String nivelOverride,
    // campos editáveis pelo admin
    String cargo,
    String area,
    String sobre,
    String prontidaoStack,
    String alocacaoStatus,
    Integer nivelMentoria,
    String autonomia,
    String trilhaCarreira,
    String certificacoesCount,
    String nivelAcompanhamento,
    String linkedinUrl,
    String githubUrl,
    String availability,
    String codeReviewAtuacao,
    UUID groupId,
    List<SkillEntry> skills
) {
    public record SkillEntry(String name, String level) {}
}
