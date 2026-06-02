package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Identificação
    @Column(name = "photo_url")
    private String photoUrl;

    @Column
    private String cargo;

    // Perfil Técnico
    private String area;

    @Column(columnDefinition = "TEXT")
    private String sobre;

    @Column(name = "prontidao_stack")
    private String prontidaoStack;

    // Alocação e Potencial
    @Column(name = "alocacao_status")
    private String alocacaoStatus;

    @Column(name = "nivel_mentoria")
    private Integer nivelMentoria;

    @Column
    private String autonomia;

    @Column(name = "trilha_carreira")
    private String trilhaCarreira;

    @Column(name = "certificacoes_count")
    private String certificacoesCount;

    @Column(name = "nivel_acompanhamento")
    private String nivelAcompanhamento;

    // Avaliação IA
    private String nivel;

    @Column(name = "nivel_override")
    private String nivelOverride;

    @Column(name = "nivel_score")
    private Integer nivelScore;

    @Column(name = "nivel_justificativa", columnDefinition = "TEXT")
    private String nivelJustificativa;

    // Outros
    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "projects_count")
    private Integer projectsCount;

    private String availability;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "code_review_atuacao")
    private String codeReviewAtuacao;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status")
    @Builder.Default
    private RegistrationStatus registrationStatus = RegistrationStatus.NOT_REQUESTED;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDENTE";

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProfileSkill> skills = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
