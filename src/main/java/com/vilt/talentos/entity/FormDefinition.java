package com.vilt.talentos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Table(name = "form_definitions")
@Entity(name = "FormDefinition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of="id")
public class FormDefinition extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "version")
    private int version;

    @Column(name = "title")
    private String title;

    @Column(name = "active")
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "elements", columnDefinition = "jsonb")
    private Map<String, Object> elements;
}

