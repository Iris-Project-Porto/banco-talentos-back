package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.AdminUpdateRequest;
import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.dto.SkillEntry;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.ProfileMapper;
import com.vilt.talentos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepo;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final TalentEvaluationService evaluationService;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final ProfileMapper profileMapper;

    @Transactional
    public Profile createOrUpdate(UUID userId, ProfileRequest req) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        var evaluation = evaluationService.evaluate(req);

        var profile = profileRepo.findByUserId(userId).orElseGet(() -> Profile.builder()
                .user(user).status(DomainStatus.PENDING).build());

        // Toda submissão pelo recurso deve passar por revisão (status PENDENTE)
        boolean wasActive = DomainStatus.ACTIVE == profile.getStatus();
        profile.setStatus(DomainStatus.PENDING);

        profileMapper.updateEntity(req, profile);
        
        // Lógica de Matrícula para o Recurso: envia para revisão administrativa
        if (req.registrationNumber() != null && !req.registrationNumber().isBlank()) {
            profile.setRegistrationNumber(req.registrationNumber());
            profile.setRegistrationStatus(RegistrationStatus.AWAITING_APPROVAL);
        }

        profile.setLevel(evaluation.nivel().name());
        profile.setLevelScore(evaluation.score());
        profile.setLevelJustification(evaluation.justificativa());

        // Reconciliação de skills HARD (recursos só mexem em HARD)
        reconcileSkills(profile, req.skills(), SkillType.HARD);

        Profile saved = profileRepo.save(profile);

        // Notificar admins se o perfil foi submetido (se estava ATIVO ou se é novo)
        if (wasActive || profileRepo.findByUserId(userId).isEmpty()) {
            List<String> adminEmails = userRepo.findAllByRoleAndStatus(UserRole.ADMIN, DomainStatus.ACTIVE, Pageable.unpaged())
                    .getContent().stream().map(User::getEmail).toList();
            if (!adminEmails.isEmpty()) {
                emailService.sendAdminNewProfileSubmissionEmail(
                    adminEmails, user.getName(), saved.getJobTitle(), saved.getLevel(), appProperties.getUrl()
                );
            }
        }

        return saved;
    }

    public Profile getByUserId(UUID userId) {
        Profile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado para o usuário"));
        
        return sanitizeProfileForResource(profile);
    }

    private Profile sanitizeProfileForResource(Profile profile) {
        if (profile.getRegistrationStatus() == RegistrationStatus.REJECTED) {
            profile.setRegistrationStatus(null);
        }
        // Soft skills são visíveis apenas para admins
        profile.getSkills().removeIf(ps -> ps.getSkill().getType() == SkillType.SOFT);
        return profile;
    }

    public Profile getById(UUID id) {
        return profileRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));
    }

    public Page<Profile> getByStatus(DomainStatus status, Pageable pageable) {
        Page<Profile> page = profileRepo.findByStatus(status, pageable);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("No profiles found with status: " + status);
        }
        return page;
    }

    public Page<Profile> getAll(Pageable pageable) {
        Page<Profile> page = profileRepo.findAll(pageable);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("No profiles found");
        }
        return page;
    }

    @Transactional
    public Profile adminUpdate(UUID profileId, AdminUpdateRequest req) {
        var profile = profileRepo.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));
        
        DomainStatus oldStatus = profile.getStatus();
        
        if (req.status() != null) {
            try {
                profile.setStatus(DomainStatus.valueOf(req.status()));
            } catch (IllegalArgumentException e) {
                // Keep old status or handle error
            }
        }

        profileMapper.updateEntityFromAdmin(req, profile);

        if (req.registrationStatus() != null) {
            try {
                profile.setRegistrationStatus(RegistrationStatus.valueOf(req.registrationStatus()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid enum
            }
        }

        if (req.groupId() != null) {
            var group = groupRepo.findById(req.groupId())
                    .orElseThrow(() -> new BadRequestException("Group not found"));
            profile.getUser().setGroup(group);
            userRepo.save(profile.getUser());
        }

        // Reconciliação de skills HARD
        if (req.skills() != null) {
            reconcileSkills(profile, req.skills(), SkillType.HARD);
        }

        // Reconciliação de skills SOFT (exclusivo Admin)
        if (req.softSkills() != null) {
            reconcileSkills(profile, req.softSkills(), SkillType.SOFT);
        }

        Profile saved = profileRepo.save(profile);


        // Se o status mudou para ATIVO, notifica o colaborador
        if (DomainStatus.ACTIVE == saved.getStatus() && DomainStatus.ACTIVE != oldStatus) {
            emailService.sendResourceProfileApprovedEmail(
                saved.getUser().getEmail(), saved.getUser().getName(), appProperties.getUrl()
            );
        }

        return saved;
    }

    private void reconcileSkills(Profile profile, List<SkillEntry> entries, SkillType type) {
        if (entries == null) return;

        var requestSkills = entries.stream()
                .filter(s -> s.name() != null && !s.name().isBlank())
                .collect(Collectors.toMap(
                        s -> s.name().trim().toUpperCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        // 1. Remove as que não estão mais no request (apenas do tipo especificado)
        profile.getSkills().removeIf(ps -> 
            ps.getSkill().getType() == type &&
            !requestSkills.containsKey(ps.getSkill().getName().trim().toUpperCase())
        );

        // 2. Atualiza níveis das existentes ou Adiciona novas
        for (var entry : requestSkills.values()) {
            String name = entry.name().trim().toUpperCase();
            Integer level = entry.proficiencyLevel();

            var existing = profile.getSkills().stream()
                    .filter(ps -> ps.getSkill().getName().equalsIgnoreCase(name))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().setProficiencyLevel(level);
                // Garantir que o tipo está correto se a skill já existia no banco
                if (existing.get().getSkill().getType() != type) {
                    existing.get().getSkill().setType(type);
                    skillRepo.save(existing.get().getSkill());
                }
            } else {
                var skill = skillRepo.findByName(name)
                        .orElseGet(() -> skillRepo.save(Skill.builder().name(name).type(type).build()));
                
                if (skill.getType() != type) {
                    skill.setType(type);
                    skillRepo.save(skill);
                }

                profile.getSkills().add(ProfileSkill.builder()
                        .profile(profile)
                        .skill(skill)
                        .proficiencyLevel(level)
                        .build());
            }
        }
    }
}
