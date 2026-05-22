package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionUpdateRequest;
import com.vilt.talentos.entity.FormSubmission;
import com.vilt.talentos.repository.FormSubmissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@Tag(name = "Formulary submission", description = "Gerenciamento de submissões dos formulários")
@SecurityRequirement(name = "bearerAuth")
public class FormSubmissionController {

    @Autowired
    private FormSubmissionRepository repository;

    @GetMapping("/my-group")
    @Operation(summary = "Obter formulários vinculados ao grupo do usuário")
    public ResponseEntity getFormsByGroup(){
        var form = repository.getReferenceById(id);
        return  ResponseEntity.ok(new FormListResponse(form));
    }

    @PostMapping("/{id}/submissions")
    @Transactional
    @Operation(summary = "Cadatrar submissão do formulário")
    public ResponseEntity create(@PathVariable UUID id, @RequestBody FormSubmissionRequest request, UriComponentsBuilder uriBuilder) {
        var formSubmission = new FormSubmission(request);
        repository.save(formSubmission);
        var uri = uriBuilder.path("/{id}").buildAndExpand(formSubmission.getId()).toUri();
        return ResponseEntity.created(uri).body(new FormSubmissionUpdateRequest(formSubmission));
    }
}
