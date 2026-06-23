package com.vilt.talentos.dto;

import com.vilt.talentos.entity.ExperienceLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(description = "KPIs do Dashboard")
public record DashboardKpisResponse(
    @Schema(description = "Total de usuários cadastrados")
    long total,
    
    @Schema(description = "Usuários com perfil ATIVO")
    long active,
    
    @Schema(description = "Usuários com perfil PENDENTE")
    long pending,
    
    @Schema(description = "Top skills por proficiência (habilidade)")
    List<SkillKpi> topSkillsByProficiency,

    @Schema(description = "Distribuição por nível de experiência")
    Map<ExperienceLevel, Long> levelCount
) {
    public record SkillKpi(
        @Schema(description = "Nome da skill")
        String name,
        
        @Schema(description = "Pontuação calculada")
        Long score
    ) {}
}
