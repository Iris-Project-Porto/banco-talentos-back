package com.vilt.talentos.controller;

import com.vilt.talentos.dto.VagaRequest;
import com.vilt.talentos.dto.VagaResponse;
import com.vilt.talentos.service.VagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vagas")
@RequiredArgsConstructor
@Tag(name = "Vagas", description = "Gestão de vagas abertas — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class VagaController {

    private final VagaService vagaService;

    @GetMapping
    @Operation(summary = "Listar todas as vagas", description = "Retorna todas as vagas ordenadas por data de abertura decrescente.")
    public List<VagaResponse> listar() {
        return vagaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vaga por id")
    public VagaResponse buscar(@PathVariable UUID id) {
        return vagaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova vaga")
    public VagaResponse criar(@Valid @RequestBody VagaRequest req) {
        return vagaService.criar(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar vaga existente")
    public VagaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody VagaRequest req) {
        return vagaService.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover vaga")
    public void deletar(@PathVariable UUID id) {
        vagaService.deletar(id);
    }
}
