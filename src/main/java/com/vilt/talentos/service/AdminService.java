package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.DashboardKpisResponse;
import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.ExperienceLevel;
import com.vilt.talentos.entity.SkillType;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var ativos = all.stream().filter(p -> DomainStatus.ACTIVE == p.getStatus()).count();
        var pendentes = all.stream().filter(p -> DomainStatus.PENDING == p.getStatus()).count();

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
            .map(p -> ExperienceLevel.fromValue(p.getLevelOverride() != null ? p.getLevelOverride() : p.getLevel()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new DashboardKpisResponse(
            total,
            ativos,
            pendentes,
            mapToSkillKpiList(skillsByProficiency),
            mapToSkillKpiList(skillsByImportance),
            nivelCount
        );
    }

    private List<DashboardKpisResponse.SkillKpi> mapToSkillKpiList(Map<String, Long> skillMap) {
        return skillMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(8)
            .map(e -> new DashboardKpisResponse.SkillKpi(
                e.getKey(),
                e.getValue()))
            .toList();
    }

    public List<User> getPendingUsers() {
        org.springframework.data.domain.Page<User> users = userRepo.findAllByRoleAndStatus(UserRole.ADMIN, DomainStatus.PENDING, Pageable.unpaged());
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum usuário pendente de aprovação");
        }
        return users.getContent();
    }

    @Transactional
    public void approveUser(UUID userId, UUID adminId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getStatus() != DomainStatus.PENDING) {
            throw new BadRequestException("Usuário não está pendente de aprovação.");
        }

        User admin = userRepo.getReferenceById(adminId);

        user.setStatus(DomainStatus.ACTIVE);
        user.setApprovedBy(admin);
        user.setApprovedAt(Instant.now());
        
        userRepo.save(user);

        emailService.sendAdminApprovalConfirmedEmail(user.getEmail(), user.getName(), appProperties.getUrl());
    }

    @Transactional
    public void rejectUser(UUID userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        user.setStatus(DomainStatus.INACTIVE);
        userRepo.save(user);
    }
}
