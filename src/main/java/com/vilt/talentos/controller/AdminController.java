package com.vilt.talentos.controller;

import com.vilt.talentos.dto.AdminUpdateRequest;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Operações administrativas — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final ProfileService profileService;
    private final UserRepository userRepo;

    @GetMapping("/profiles")
    @Operation(summary = "Listar todos os perfis")
    public List<Profile> all() {
        return profileService.getAll();
    }

    @GetMapping("/profiles/pendentes")
    @Operation(summary = "Fila de revisão — perfis com status PENDENTE")
    public List<Profile> pendentes() {
        return profileService.getByStatus("PENDENTE");
    }

    @GetMapping("/profiles/ativos")
    @Operation(summary = "Banco de talentos — perfis com status ATIVO")
    public List<Profile> ativos() {
        return profileService.getByStatus("ATIVO");
    }

    @GetMapping("/profiles/{id}")
    @Operation(summary = "Buscar perfil por id")
    public Profile getById(@PathVariable UUID id) {
        return profileService.getAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PatchMapping("/profiles/{id}")
    @Operation(summary = "Atualizar perfil", description = "Permite alterar status (ATIVO/PENDENTE/INATIVO), nivel_override, área e disponibilidade.")
    public Profile update(@PathVariable UUID id, @RequestBody AdminUpdateRequest req) {
        return profileService.adminUpdate(id, req);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "KPIs do dashboard", description = "Total, ativos, pendentes, top skills, distribuição por nível.")
    public Map<String, Object> dashboard() {
        var all = profileService.getAll();
        var total = all.size();
        var ativos = all.stream().filter(p -> "ATIVO".equals(p.getStatus())).count();
        var pendentes = all.stream().filter(p -> "PENDENTE".equals(p.getStatus())).count();

        var skillCount = all.stream()
            .flatMap(p -> p.getSkills().stream())
            .collect(Collectors.groupingBy(ps -> ps.getSkill().getName(), Collectors.counting()));

        var nivelCount = Map.of(
            "Jr", all.stream().filter(p -> "Jr".equals(p.getNivelOverride() != null ? p.getNivelOverride() : p.getNivel())).count(),
            "Pleno", all.stream().filter(p -> "Pleno".equals(p.getNivelOverride() != null ? p.getNivelOverride() : p.getNivel())).count(),
            "Sr", all.stream().filter(p -> "Sr".equals(p.getNivelOverride() != null ? p.getNivelOverride() : p.getNivel())).count()
        );

        return Map.of(
            "total", total,
            "ativos", ativos,
            "pendentes", pendentes,
            "skillsCount", skillCount.size(),
            "topSkills", skillCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> Map.of("name", e.getKey(), "count", e.getValue()))
                .toList(),
            "nivelCount", nivelCount
        );
    }

    @GetMapping("/users/pending")
    @Operation(summary = "Listar usuários pendentes", description = "Retorna todos os usuários que aguardam aprovação (Status PENDING).")
    public List<User> getPendingUsers() {
        return userRepo.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.PENDING);
    }

    @PostMapping("/users/{id}/approve")
    @Operation(summary = "Aprovar usuário", description = "Altera o status de um usuário para ACTIVE e registra quem aprovou.")
    public void approveUser(@PathVariable UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (user.getStatus() != User.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não está pendente de aprovação.");
        }

        String adminIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID adminId = UUID.fromString(adminIdStr);

        user.setStatus(User.Status.ACTIVE);
        user.setApprovedBy(adminId);
        user.setApprovedAt(Instant.now());
        
        userRepo.save(user);
    }

    @PostMapping("/users/{id}/reject")
    @Operation(summary = "Rejeitar usuário", description = "Altera o status de um usuário para INACTIVE.")
    public void rejectUser(@PathVariable UUID id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        user.setStatus(User.Status.INACTIVE);
        userRepo.save(user);
    }
}
