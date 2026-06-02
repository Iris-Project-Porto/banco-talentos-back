package com.vilt.talentos.dto;

import com.vilt.talentos.entity.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpisResponse {
    private long total;
    private long ativos;
    private long pendentes;
    private List<SkillKpi> topSkillsByProficiency;
    private List<SkillKpi> topSkillsByImportance;
    private Map<ExperienceLevel, Long> nivelCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillKpi {
        private String name;
        private Long score;
    }
}
