package com.vilt.talentos.entity;

import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Table(name = "form_submissions")
@Entity(name = "FormSubmission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of="id")
public class FormSubmission extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_definition_id")
    private FormDefinition formDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb")
    private Map<String, Object> answers;

    public FormSubmission(FormDefinition formDefinition, User user, Map<String, Object> answers) {
        this.formDefinition = formDefinition;
        this.user = user;
        this.answers = answers;
    }

    public void updateInformation(FormSubmissionUpdateRequest data, FormDefinition formDefinition, User user){
        if(formDefinition != null){
            this.formDefinition = formDefinition;
        }
        if(user != null){
            this.user = user;
        }
        if(data.answers() != null){
            this.answers = data.answers();
        }
    }
}

