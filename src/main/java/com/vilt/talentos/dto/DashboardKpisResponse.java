package com.vilt.talentos.dto;

import com.vilt.talentos.entity.ExperienceLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardKpisResponse {
    private long total;
    private long ativos;
    private long pendentes;
    private List<SkillKpi> topSkillsByProficiency;
    private List<SkillKpi> topSkillsByImportance;
    private Map<ExperienceLevel, Long> nivelCount;

    @Data
    @Builder
    public static class SkillKpi {
        private String name;
        private Long score;
    }
}
