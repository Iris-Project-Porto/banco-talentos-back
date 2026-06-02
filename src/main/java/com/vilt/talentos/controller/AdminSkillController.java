package com.vilt.talentos.controller;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/skills")
@RequiredArgsConstructor
@Tag(name = "Admin Skills", description = "Gerenciamento administrativo de skills — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminSkillController {

    private final SkillService skillService;

    @GetMapping("/active")
    @Operation(summary = "Listar skills ativas (Admin)")
    public List<SkillResponse> listActive() {
        return skillService.findAllActive();
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar skills inativas (Admin)")
    public List<SkillResponse> listInactive() {
        return skillService.findAllInactive();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar skill por ID")
    public SkillResponse getById(@PathVariable UUID id) {
        return skillService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar skill")
    public SkillResponse update(@PathVariable UUID id, @RequestBody @Valid SkillRequest request) {
        return skillService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativar skill")
    public void activate(@PathVariable UUID id) {
        skillService.setActiveStatus(id, true);
    }

    @PatchMapping("/{id}/inactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Inativar skill (Exclusão Lógica)")
    public void inactivate(@PathVariable UUID id) {
        skillService.setActiveStatus(id, false);
    }
}
