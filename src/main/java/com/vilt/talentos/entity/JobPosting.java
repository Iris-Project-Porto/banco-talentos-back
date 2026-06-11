package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_postings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobPosting extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
    private DomainStatus status;

    private String notes;

    @Column(nullable = false)
    private Instant openingDate;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isUrgent = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
