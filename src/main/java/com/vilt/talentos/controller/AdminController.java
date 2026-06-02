package com.vilt.talentos.controller;

import com.vilt.talentos.dto.AdminUpdateRequest;
import com.vilt.talentos.dto.DashboardKpisResponse;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.service.AdminService;
import com.vilt.talentos.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Operações administrativas — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final ProfileService profileService;
    private final AdminService adminService;

    @GetMapping("/profiles")
    @Operation(summary = "Listar todos os perfis")
    public List<Profile> all() {
        return profileService.getAll();
    }

    @GetMapping("/profiles/pending")
    @Operation(summary = "Fila de revisão — perfis com status PENDENTE")
    public List<Profile> pendentes() {
        return profileService.getByStatus("PENDENTE");
    }

    @GetMapping("/profiles/active")
    @Operation(summary = "Banco de talentos — perfis com status ATIVO")
    public List<Profile> ativos() {
        return profileService.getByStatus("ATIVO");
    }

    @GetMapping("/profiles/{id}")
    @Operation(summary = "Buscar perfil por id")
    public Profile getById(@PathVariable UUID id) {
        return profileService.getById(id);
    }

    @PatchMapping("/profiles/{id}")
    @Operation(summary = "Atualizar perfil", description = "Permite alterar status (ATIVO/PENDENTE/INATIVO), nivel_override, área e disponibilidade.")
    public Profile update(@PathVariable UUID id, @RequestBody AdminUpdateRequest req) {
        return profileService.adminUpdate(id, req);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "KPIs do dashboard", description = "Total, ativos, pendentes, top skills, distribuição por nível.")
    public DashboardKpisResponse dashboard() {
        return adminService.getDashboardKpis();
    }

    @GetMapping("/users/pending")
    @Operation(summary = "Listar usuários pendentes", description = "Retorna todos os usuários que aguardam aprovação (Status PENDING).")
    public List<User> getPendingUsers() {
        return adminService.getPendingUsers();
    }

    @PostMapping("/users/{id}/approve")
    @Operation(summary = "Aprovar usuário", description = "Altera o status de um usuário para ACTIVE e registra quem aprovou.")
    public void approveUser(@PathVariable UUID id) {
        String adminIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminService.approveUser(id, UUID.fromString(adminIdStr));
    }

    @PostMapping("/users/{id}/reject")
    @Operation(summary = "Rejeitar usuário", description = "Altera o status de um usuário para INACTIVE.")
    public void rejectUser(@PathVariable UUID id) {
        adminService.rejectUser(id);
    }
}
