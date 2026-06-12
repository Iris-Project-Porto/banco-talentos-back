package com.vilt.talentos.controller;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.service.SkillService;
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

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Endpoints públicos/autenticados para consulta de skills")
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    @Operation(summary = "Listar skills ativas", description = "Retorna a lista de skills ativas para seleção.")
    public Page<SkillResponse> getAllActive(@PageableDefault(size = 50) Pageable pageable) {
        return skillService.findAllActive(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Criar nova skill", description = "Permite que qualquer usuário autenticado crie uma skill (será salva em CAPSLOCK).")
    public SkillResponse create(@RequestBody @Valid SkillRequest request) {
        return skillService.create(request);
    }
}
