package com.vilt.talentos.controller;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.service.GroupService;
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
@RequestMapping("/api/v1/admin/groups")
@RequiredArgsConstructor
@Tag(name = "Admin Groups", description = "Gerenciamento de grupos — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminGroupController {

    private final GroupService groupService;

    @GetMapping("/active")
    @Operation(summary = "Listar grupos ativos (Admin)")
    public Page<GroupResponse> listActive(@PageableDefault(size = 20) Pageable pageable) {
        return groupService.findAllActive(pageable);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar grupos inativos (Admin)")
    public Page<GroupResponse> listInactive(@PageableDefault(size = 20) Pageable pageable) {
        return groupService.findAllInactive(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar grupo por ID")
    public GroupResponse getById(@PathVariable UUID id) {
        return groupService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo grupo")
    public GroupResponse create(@RequestBody @Valid GroupRequest request) {
        return groupService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar grupo existente")
    public GroupResponse update(@PathVariable UUID id, @RequestBody @Valid GroupRequest request) {
        return groupService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativar grupo")
    public void activate(@PathVariable UUID id) {
        groupService.setActiveStatus(id, true);
    }

    @PatchMapping("/{id}/inactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Inativar grupo")
    public void inactivate(@PathVariable UUID id) {
        groupService.setActiveStatus(id, false);
    }
}
