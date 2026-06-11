package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "squads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Squad extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(name = "porto_coordinator")
    private String portoCoordinator;

    @Column(name = "project_manager") // GP
    private String projectManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToMany
    @JoinTable(
        name = "squad_skills",
        joinColumns = @JoinColumn(name = "squad_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
