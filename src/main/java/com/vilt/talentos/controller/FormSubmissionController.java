package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionResponse;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.entity.FormSubmission;
import com.vilt.talentos.repository.FormDefinitionRepository;
import com.vilt.talentos.repository.FormSubmissionRepository;
import com.vilt.talentos.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@Tag(name = "Formulary submission", description = "Gerenciamento de submissões dos formulários")
@SecurityRequirement(name = "bearerAuth")
public class FormSubmissionController {

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @Autowired
    private FormDefinitionRepository formDefinitionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-group")
    @Operation(summary = "Obter formulários vinculados ao grupo do usuário")
    public ResponseEntity<List<FormListResponse>> getFormsByGroup(Authentication authentication){

        UUID userId = UUID.fromString(authentication.getName());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        UUID groupId = user.getGroup().getId();

        List<FormDefinition> forms = formDefinitionRepository.findAllByGroupId(groupId);

        var response = forms.stream()
                .map(FormListResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/submissions")
    @Transactional
    @Operation(summary = "Cadatrar submissão do formulário")
    public ResponseEntity<FormSubmissionResponse> create(
            @RequestBody @Valid FormSubmissionRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        formDefinitionRepository.findById(request.formDefinitionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Formulário não encontrado."
                ));

        var formSubmission = new FormSubmission(
                request.formDefinitionId(),
                userId,
                request.answers()
        );

        formSubmissionRepository.save(formSubmission);

        var uri = uriBuilder
                .path("/api/forms/submissions/{id}")
                .buildAndExpand(formSubmission.getId())
                .toUri();

        return ResponseEntity.created(uri)
                .body(new FormSubmissionResponse(formSubmission));
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Obter submissão de formulário por ID")
    public ResponseEntity<FormSubmissionResponse> getSubmissionById(@PathVariable UUID id){

        var submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Submissão de respostas não encontrada com o id informado."
                ));

        return ResponseEntity.ok(new FormSubmissionResponse(submission));
    }
}
