package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormDefinitionResponse;
import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormUpdateRequest;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.repository.FormDefinitionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/forms")
@RequiredArgsConstructor
@Tag(name = "Admin Forms", description = "Gerenciamento de formularios — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminFormController {

    @Autowired
    private FormDefinitionRepository repository;

    @PostMapping
    @Transactional
    @Operation(summary = "Criar novo formulário")
    public ResponseEntity<FormDefinitionResponse> create(@RequestBody @Valid FormCreateRequest request, UriComponentsBuilder uriBuilder) {
        var formDefinition = new FormDefinition(request);
        repository.save(formDefinition);
        var uri = uriBuilder.path("/api/admin/forms/{id}").buildAndExpand(formDefinition.getId()).toUri();
        return ResponseEntity.created(uri).body(new FormDefinitionResponse(formDefinition));
    }

    @GetMapping
    @Operation(summary = "Obter lista de formulários")
    public Page<FormListResponse> getFormList(@PageableDefault(size = 10, sort = {"title"}) Pageable paginacao){
        return repository.findAll(paginacao).map(FormListResponse::new);
    }

    @PutMapping
    @Transactional
    @Operation(summary = "Atualizar formulário")
    public ResponseEntity<Object> updateForm(@RequestBody @Valid FormUpdateRequest form){
        var formDefinition = repository.getReferenceById(form.getId());
        formDefinition.atualizarInformacoes(form);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter dados do formulário cadastrado")
    public ResponseEntity<FormListResponse> detailForm(@PathVariable UUID id){
        var form = repository.findById(id);
        return form.map(formDefinition -> ResponseEntity.ok(new FormListResponse(formDefinition))).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Remover formulário")
    public ResponseEntity<Object> deleteForm(@PathVariable UUID id){
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }



}
