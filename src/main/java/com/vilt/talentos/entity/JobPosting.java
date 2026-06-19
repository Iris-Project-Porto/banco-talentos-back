package com.vilt.talentos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "job_postings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JobPosting extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vacancy_code", unique = true)
    private String vacancyCode;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "squad_id")
    private Squad squad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    private String description; // Não obrigatório

    @Column(nullable = false)
    private String requirements;

    @Column(nullable = false)
    private String recruiter;

    @Column(name = "estimated_allocation_weeks")
    private Integer estimatedAllocationWeeks; // Não obrigatório

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPostingStatus status = JobPostingStatus.OPEN;

    private String modality;

    private String notes;

    @Column(nullable = false)
    private Instant openingDate;

    @Column(name = "closing_date")
    private Instant closingDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isUrgent = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JobPostingSkill> skills = new ArrayList<>();
}
