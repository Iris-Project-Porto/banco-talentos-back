package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionResponse;
import com.vilt.talentos.service.FormSubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "Formulary submission", description = "Gerenciamento de submissões dos formulários")
@SecurityRequirement(name = "bearerAuth")
public class FormSubmissionController {

    private final FormSubmissionService service;

    @GetMapping("/my-group")
    @Operation(summary = "Obter formulários vinculados ao grupo do usuário")
    public List<FormListResponse> getFormsByGroup(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return service.getFormsByUserGroup(userId);
    }

    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar submissão do formulário")
    public FormSubmissionResponse create(@RequestBody @Valid FormSubmissionRequest request, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return service.createSubmission(userId, request);
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Obter submissão de formulário por ID")
    public FormSubmissionResponse getSubmissionById(@PathVariable UUID id) {
        return service.getSubmissionById(id);
    }
}
