package com.vilt.talentos.entity;

import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;
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

    @Column(name = "form_definition_id")
    private UUID formDefinitionId;

    @Column(name = "user_id")
    private UUID userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private Map<String, Object> answers;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public FormSubmission(FormSubmissionRequest form,UUID userId) {
        this.formDefinitionId = form.formDefinitionId();
        this.userId = userId;
        this.answers = form.answers();
    }

    public FormSubmission(UUID formDefinitionId, UUID userId, Map<String, Object> answers) {
        this.formDefinitionId = formDefinitionId;
        this.userId = userId;
        this.answers = answers;
    }

    public void atualizarInformacoes(FormSubmissionUpdateRequest dados){
        if(dados.formDefinitionId()!=null){
            this.formDefinitionId=dados.formDefinitionId();
        }if(dados.userId()!=null){
            this.userId=dados.userId();
        }if(dados.answers()!=null){
            this.answers=dados.answers();
        }
    }
}

