package com.vilt.talentos.entity;

import com.vilt.talentos.dto.FormCreateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private UUID groupId;
    private int version;
    private String title;
    private boolean active;
    private String elements;


    public FormDefinition(FormCreateRequest form) {
        this.groupId = form.groupId();
        this.version = form.version();
        this.title = form.title();
        this.active = form.active();
        this.elements = form.elements();
    }

    /*public void atualizarInformacoes(Atualizar dados){
    }*/
}

