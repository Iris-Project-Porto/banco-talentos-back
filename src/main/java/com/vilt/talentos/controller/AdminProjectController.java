package com.vilt.talentos.controller;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.service.ProjectService;
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
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
@Tag(name = "Admin Projects", description = "Gerenciamento de projetos — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminProjectController {

    private final ProjectService projectService;

    @GetMapping("/active")
    @Operation(summary = "Listar projetos ativos")
    public Page<ProjectResponse> listActive(@PageableDefault(size = 20) Pageable pageable) {
        return projectService.findAllActive(pageable);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar projetos inativos")
    public Page<ProjectResponse> listInactive(@PageableDefault(size = 20) Pageable pageable) {
        return projectService.findAllInactive(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo projeto")
    public ProjectResponse create(@RequestBody @Valid ProjectRequest request) {
        return projectService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto existente")
    public ProjectResponse update(@PathVariable UUID id, @RequestBody @Valid ProjectRequest request) {
        return projectService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativar projeto")
    public void activate(@PathVariable UUID id) {
        projectService.setActiveStatus(id, true);
    }

    @PatchMapping("/{id}/inactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Inativar projeto")
    public void inactivate(@PathVariable UUID id) {
        projectService.setActiveStatus(id, false);
    }
}
