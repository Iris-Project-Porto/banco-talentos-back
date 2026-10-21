package com.vilt.talentos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Resposta de candidato compatível com a vaga (Match de Talentos)")
public record JobPostingCandidateMatchResponse(
    @Schema(description = "ID do perfil do colaborador", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Nome completo do colaborador", example = "João Silva")
    String name,

    @Schema(description = "Cargo ou descrição do perfil", example = "Engenheiro de Software Pleno")
    String description,

    @Schema(description = "URL da foto do colaborador", example = "https://...")
    String photoUrl,

    @Schema(description = "Nome da squad atual", example = "Squad Pagamentos")
    String squadName,

    @Schema(description = "Disponibilidade do recurso", example = "available")
    String availability,

    @Schema(description = "Score de compatibilidade com a vaga (0 a 100)", example = "92")
    int matchScore,

    @Schema(description = "Posição geral no ranking de aderência à vaga (1, 2, 3...)", example = "1")
    int rankingScore,

    @Schema(description = "Principais skills que mais contribuíram para o score de match")
    List<MainSkillContribution> mainSkills,

    @Schema(description = "Resumo dos requisitos obrigatórios e desejáveis atendidos")
    CandidateMatchSummary summary
) {
    @Schema(description = "Contribuição de cada skill para a pontuação de match")
    public record MainSkillContribution(
        @Schema(description = "Nome da skill", example = "React")
        String name,

        @Schema(description = "Percentual que essa skill agregou ao score do candidato", example = "40")
        int contributionPercent
    ) {}

    @Schema(description = "Resumo do atendimento aos requisitos da vaga")
    public record CandidateMatchSummary(
        @Schema(description = "Quantidade de skills obrigatórias atendidas", example = "3")
        int requiredSkillsMatched,

        @Schema(description = "Total de skills obrigatórias exigidas na vaga", example = "3")
        int requiredSkillsTotal,

        @Schema(description = "Quantidade de skills desejáveis atendidas", example = "2")
        int desirableSkillsMatched,

        @Schema(description = "Total de skills desejáveis cadastradas na vaga", example = "4")
        int desirableSkillsTotal
    ) {}
}
