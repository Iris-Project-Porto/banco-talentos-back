package com.vilt.talentos.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "job_posting_skills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_posting_id", "skill_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JobPostingSkill extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequirementType type = RequirementType.MANDATORY;

    @Enumerated(EnumType.STRING)
    @Column(name = "min_level", nullable = false)
    @Builder.Default
    private SkillLevel minLevel = SkillLevel.BASIC;

    @Column(name = "importance_weight", nullable = false)
    @Builder.Default
    private Integer importanceWeight = 1;

    @Column(length = 500)
    private String description;
}
