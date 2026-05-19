package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormDefinitionResponse;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.repository.FormDefinitonRepository;
import com.vilt.talentos.service.FormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/forms")
@RequiredArgsConstructor
@Tag(name = "Admin Forms", description = "Gerenciamento de formularios — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminFormController {

    @Autowired
    private FormService formService;

    @Autowired
    private FormDefinitonRepository repository;

    @PostMapping
    @Transactional
    @Operation(summary = "Criar novo formulario")
    public ResponseEntity create(@RequestBody @Valid FormCreateRequest request, UriComponentsBuilder uriBuilder) {
        var formDefinition = new FormDefinition(request);
        repository.save(formDefinition);
        var uri = uriBuilder.path("/{id}").buildAndExpand(formDefinition.getId()).toUri();
        return ResponseEntity.created(uri).body(new FormDefinitionResponse(formDefinition));
    }



}
