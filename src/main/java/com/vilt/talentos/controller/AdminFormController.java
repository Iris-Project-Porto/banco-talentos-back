package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormDefinitionResponse;
import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormUpdateRequest;
import com.vilt.talentos.service.FormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/forms")
@RequiredArgsConstructor
@Tag(name = "Admin Forms", description = "Gerenciamento de formularios — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminFormController {

    private final FormService formService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo formulário")
    public FormDefinitionResponse create(@RequestBody @Valid FormCreateRequest request) {
        return formService.create(request);
    }

    @GetMapping
    @Operation(summary = "Obter lista de formulários")
    public Page<FormListResponse> getFormList(@PageableDefault(size = 10, sort = {"title"}) Pageable pagination) {
        return formService.findAll(pagination);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualizar formulário")
    public void updateForm(@RequestBody @Valid FormUpdateRequest form) {
        formService.update(form);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter dados do formulário cadastrado")
    public FormListResponse detailForm(@PathVariable UUID id) {
        return formService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover formulário")
    public void deleteForm(@PathVariable UUID id) {
        formService.delete(id);
    }
}
