package com.vilt.talentos.entity;

import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Table(name = "form_submissions")
@Entity(name = "FormSubmission")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class FormSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID formDefinitionId;
    private UUID userId;
    private String answers;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public FormSubmission(FormSubmissionRequest form) {
        this.formDefinitionId = form.formDefinitionId();
        this.userId = form.userId();
        this.answers = form.answers();
    }

    public void atualizarInformacoes(FormSubmissionUpdateRequest dados){
        if(dados.formDefinitionId()!=null){
            this.formDefinitionId=dados.formDefinitionId();
        }if(dados.userId()!=null){
            this.userId=dados.userId();
        }if(dados.answers()!=null){
            this.answers=dados.answers();
        }if(dados.updatedAt()!=null){
            this.updatedAt=dados.updatedAt();
        }
    }
}

