package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vagas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Senioridade senioridade;

    @Column(name = "time", nullable = false)
    private String time;

    @Column(nullable = false)
    private String solicitante;

    @Column(name = "tempo_contratacao")
    private String tempoContratacao;

    @Column(name = "numero_vagas", nullable = false)
    @Builder.Default
    private Integer numeroVagas = 1;

    private String area;

    @Column(columnDefinition = "text[]")
    private String[] skills;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusVaga status = StatusVaga.ABERTA;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Prioridade prioridade = Prioridade.MEDIA;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Senioridade {
        Jr, Pleno, Sr;

        public String label() { return this.name(); }
    }

    public enum StatusVaga {
        ABERTA("Aberta"),
        EM_ANDAMENTO("Em andamento"),
        FECHADA("Fechada"),
        CANCELADA("Cancelada");

        private final String label;
        StatusVaga(String label) { this.label = label; }
        public String getLabel() { return label; }

        public static StatusVaga fromLabel(String label) {
            for (StatusVaga s : values()) {
                if (s.label.equalsIgnoreCase(label)) return s;
            }
            throw new IllegalArgumentException("Status inválido: " + label);
        }
    }

    public enum Prioridade {
        BAIXA("Baixa"),
        MEDIA("Media"),
        ALTA("Alta"),
        URGENTE("Urgente");

        private final String label;
        Prioridade(String label) { this.label = label; }
        public String getLabel() { return label; }

        public static Prioridade fromLabel(String label) {
            for (Prioridade p : values()) {
                if (p.label.equalsIgnoreCase(label)) return p;
            }
            throw new IllegalArgumentException("Prioridade inválida: " + label);
        }
    }
}
