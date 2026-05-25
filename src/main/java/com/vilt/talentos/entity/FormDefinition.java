package com.vilt.talentos.entity;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Table(name = "form_definitions")
@Entity(name = "FormDefinition")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class FormDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "version")
    private int version;

    @Column(name = "title")
    private String title;

    @Column(name = "active")
    private boolean active;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "elements", columnDefinition = "jsonb")
    private Map<String, Object> elements;

    public FormDefinition(FormCreateRequest form) {
        this.groupId = form.groupId();
        this.version = form.version();
        this.title = form.title();
        this.active = form.active();
        this.elements = form.elements();
    }

    public void atualizarInformacoes(FormUpdateRequest dados){
        if(dados.groupId()!=null){
            this.groupId=dados.groupId();
        }if(dados.version()!=null){
            this.version=dados.version();
        }if(dados.title()!=null){
            this.title=dados.title();
        }if(dados.elements()!=null){
            this.elements=dados.elements();
        }if(dados.active()!=null){
            this.active=dados.active();
        }
    }
}

