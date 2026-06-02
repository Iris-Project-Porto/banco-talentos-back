package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SkillType type = SkillType.HARD;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "importance_weight")
    @Builder.Default
    private Integer importanceWeight = 1;
}
