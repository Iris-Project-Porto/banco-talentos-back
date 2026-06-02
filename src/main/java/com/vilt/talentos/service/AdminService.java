package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.DashboardKpisResponse;
import com.vilt.talentos.entity.ExperienceLevel;
import com.vilt.talentos.entity.SkillType;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;
    private final AppProperties appProperties;

    public DashboardKpisResponse getDashboardKpis() {
        var all = profileRepo.findAll();
        var total = all.size();
        var ativos = all.stream().filter(p -> "ATIVO".equals(p.getStatus())).count();
        var pendentes = all.stream().filter(p -> "PENDENTE".equals(p.getStatus())).count();

        // Visão 1: Skills mais dominadas pelos recursos (Soma dos níveis de proficiência)
        var skillsByProficiency = all.stream()
            .flatMap(p -> p.getSkills().stream())
            .filter(ps -> ps.getSkill().getType() == SkillType.HARD)
            .collect(Collectors.groupingBy(
                ps -> ps.getSkill().getName(),
                Collectors.summingLong(ps -> ps.getProficiencyLevel() != null ? ps.getProficiencyLevel() : 0)
            ));

        // Visão 2: Skills mais estratégicas (Soma dos pesos de importância definidos pelos admins)
        var skillsByImportance = all.stream()
            .flatMap(p -> p.getSkills().stream())
            .filter(ps -> ps.getSkill().getType() == SkillType.HARD)
            .collect(Collectors.groupingBy(
                ps -> ps.getSkill().getName(),
                Collectors.summingLong(ps -> ps.getSkill().getImportanceWeight() != null ? ps.getSkill().getImportanceWeight() : 1)
            ));

        var nivelCount = all.stream()
            .map(p -> ExperienceLevel.fromValue(p.getNivelOverride() != null ? p.getNivelOverride() : p.getNivel()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return DashboardKpisResponse.builder()
            .total(total)
            .ativos(ativos)
            .pendentes(pendentes)
            .topSkillsByProficiency(mapToSkillKpiList(skillsByProficiency))
            .topSkillsByImportance(mapToSkillKpiList(skillsByImportance))
            .nivelCount(nivelCount)
            .build();
    }

    private List<DashboardKpisResponse.SkillKpi> mapToSkillKpiList(Map<String, Long> skillMap) {
        return skillMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(8)
            .map(e -> DashboardKpisResponse.SkillKpi.builder()
                .name(e.getKey())
                .score(e.getValue())
                .build())
            .toList();
    }

    public List<User> getPendingUsers() {
        List<User> users = userRepo.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.PENDING);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum usuário pendente de aprovação");
        }
        return users;
    }

    @Transactional
    public void approveUser(UUID userId, UUID adminId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (user.getStatus() != User.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não está pendente de aprovação.");
        }

        user.setStatus(User.Status.ACTIVE);
        user.setApprovedBy(adminId);
        user.setApprovedAt(Instant.now());
        
        userRepo.save(user);

        emailService.sendAdminApprovalConfirmedEmail(user.getEmail(), user.getName(), appProperties.getUrl());
    }

    @Transactional
    public void rejectUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        user.setStatus(User.Status.INACTIVE);
        userRepo.save(user);
    }
}
