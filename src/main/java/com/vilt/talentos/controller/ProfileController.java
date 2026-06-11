package com.vilt.talentos.controller;

import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Operações do recurso — visualizar e submeter o próprio perfil")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Buscar meu perfil", description = "Retorna os detalhes do perfil do usuário autenticado.")
    public Profile getMyProfile(Authentication auth) {
        return profileService.getByUserId(UUID.fromString(auth.getName()));
    }

    @PostMapping
    @Operation(summary = "Criar ou atualizar perfil", description = "Submete o perfil para análise (status PENDENTE). A IA avalia e classifica o nível automaticamente com base na Matriz Porto Seguro.")
    public Profile submit(@RequestBody @Valid ProfileRequest req, Authentication auth) {
        return profileService.createOrUpdate(UUID.fromString(auth.getName()), req);
    }
}
