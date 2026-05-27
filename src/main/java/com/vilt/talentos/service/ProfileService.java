package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.AdminUpdateRequest;
import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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

    @Transactional
    public Profile createOrUpdate(UUID userId, ProfileRequest req) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var evaluation = evaluationService.evaluate(req);

        var profile = profileRepo.findByUserId(userId).orElseGet(() -> Profile.builder()
                .user(user).status("PENDENTE").build());

        // Se o perfil era novo ou estava inativo, e agora está sendo submetido, garantimos status PENDENTE
        boolean isNewSubmission = !"ATIVO".equals(profile.getStatus());
        if (isNewSubmission) {
            profile.setStatus("PENDENTE");
        }

        profile.setPhotoUrl(req.photoUrl());
        profile.setCargo(req.cargo());
        profile.setArea(req.area());
        profile.setSobre(req.sobre());
        profile.setProntidaoStack(req.prontidaoStack());
        profile.setAlocacaoStatus(req.alocacaoStatus());
        profile.setNivelMentoria(req.nivelMentoria());
        profile.setAutonomia(req.autonomia());
        profile.setTrilhaCarreira(req.trilhaCarreira());
        profile.setCertificacoesCount(req.certificacoesCount());
        profile.setNivelAcompanhamento(req.nivelAcompanhamento());
        profile.setExperienceYears(req.experienceYears());
        profile.setProjectsCount(req.projectsCount());
        profile.setAvailability(req.availability());
        profile.setCertifications(req.certifications());
        profile.setLinkedinUrl(req.linkedinUrl());
        profile.setGithubUrl(req.githubUrl());
        profile.setCodeReviewAtuacao(req.codeReviewAtuacao());
        profile.setNivel(evaluation.nivel());
        profile.setNivelScore(evaluation.score());
        profile.setNivelJustificativa(evaluation.justificativa());

        // Reconciliação de skills: evita deletar e reinserir a mesma skill, o que causa erro de Unique Constraint
        if (req.skills() != null) {
            var requestSkills = req.skills().stream()
                    .filter(s -> s.name() != null && !s.name().isBlank())
                    .collect(java.util.stream.Collectors.toMap(
                            s -> s.name().trim().toUpperCase(),
                            s -> s,
                            (existing, replacement) -> existing
                    ));

            // 1. Remove as que não estão mais no request
            profile.getSkills().removeIf(ps -> 
                !requestSkills.containsKey(ps.getSkill().getName().trim().toUpperCase())
            );

            // 2. Atualiza níveis das existentes ou Adiciona novas
            for (var entry : requestSkills.values()) {
                String name = entry.name().trim().toUpperCase();
                String level = entry.level();

                var existing = profile.getSkills().stream()
                        .filter(ps -> ps.getSkill().getName().equalsIgnoreCase(name))
                        .findFirst();

                if (existing.isPresent()) {
                    existing.get().setLevel(level);
                } else {
                    var skill = skillRepo.findByName(name)
                            .orElseGet(() -> skillRepo.save(Skill.builder().name(name).build()));                    
                    profile.getSkills().add(ProfileSkill.builder()
                            .profile(profile)
                            .skill(skill)
                            .level(level)
                            .build());
                }
            }
        } else {
            profile.getSkills().clear();
        }

        Profile saved = profileRepo.save(profile);

        // Notificar admins se for uma submissão pendente
        if (isNewSubmission) {
            List<String> adminEmails = userRepo.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ACTIVE)
                    .stream().map(User::getEmail).toList();
            if (!adminEmails.isEmpty()) {
                emailService.sendAdminNewProfileSubmissionEmail(
                    adminEmails, user.getName(), saved.getCargo(), saved.getNivel(), appProperties.getUrl()
                );
            }
        }

        return saved;
    }

    public Profile getByUserId(UUID userId) {
        return profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Profile> getByStatus(String status) {
        return profileRepo.findByStatus(status);
    }

    public List<Profile> getAll() {
        return profileRepo.findAll();
    }

    @Transactional
    public Profile adminUpdate(UUID profileId, AdminUpdateRequest req) {
        var profile = profileRepo.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        String oldStatus = profile.getStatus();
        
        if (req.status() != null) profile.setStatus(req.status());
        if (req.nivelOverride() != null) profile.setNivelOverride("".equals(req.nivelOverride()) ? null : req.nivelOverride());
        if (req.cargo() != null) profile.setCargo(req.cargo());
        if (req.area() != null) profile.setArea(req.area());
        if (req.sobre() != null) profile.setSobre(req.sobre());
        if (req.prontidaoStack() != null) profile.setProntidaoStack(req.prontidaoStack());
        if (req.alocacaoStatus() != null) profile.setAlocacaoStatus(req.alocacaoStatus());
        if (req.nivelMentoria() != null) profile.setNivelMentoria(req.nivelMentoria());
        if (req.autonomia() != null) profile.setAutonomia(req.autonomia());
        if (req.trilhaCarreira() != null) profile.setTrilhaCarreira(req.trilhaCarreira());
        if (req.certificacoesCount() != null) profile.setCertificacoesCount(req.certificacoesCount());
        if (req.nivelAcompanhamento() != null) profile.setNivelAcompanhamento(req.nivelAcompanhamento());
        if (req.linkedinUrl() != null) profile.setLinkedinUrl(req.linkedinUrl());
        if (req.githubUrl() != null) profile.setGithubUrl(req.githubUrl());
        if (req.availability() != null) profile.setAvailability(req.availability());
        if (req.codeReviewAtuacao() != null) profile.setCodeReviewAtuacao(req.codeReviewAtuacao());

        if (req.groupId() != null) {
            var group = groupRepo.findById(req.groupId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group not found"));
            profile.getUser().setGroup(group);
            userRepo.save(profile.getUser());
        }

        if (req.skills() != null) {
            var requestSkills = req.skills().stream()
                    .filter(s -> s.name() != null && !s.name().isBlank())
                    .collect(java.util.stream.Collectors.toMap(
                            s -> s.name().trim().toUpperCase(),
                            s -> s,
                            (existing, replacement) -> existing
                    ));

            profile.getSkills().removeIf(ps -> 
                !requestSkills.containsKey(ps.getSkill().getName().trim().toUpperCase())
            );

            for (var entry : requestSkills.values()) {
                String name = entry.name().trim().toUpperCase();
                String level = entry.level();
                
                var existing = profile.getSkills().stream()
                        .filter(ps -> ps.getSkill().getName().equalsIgnoreCase(name))
                        .findFirst();

                if (existing.isPresent()) {
                    existing.get().setLevel(level);
                } else {
                    var skill = skillRepo.findByName(name)
                            .orElseGet(() -> skillRepo.save(Skill.builder().name(name).build()));
                    profile.getSkills().add(ProfileSkill.builder()
                            .profile(profile).skill(skill).level(level).build());
                }
            }
        }

        Profile saved = profileRepo.save(profile);

        // Se o status mudou para ATIVO, notifica o colaborador
        if ("ATIVO".equals(saved.getStatus()) && !"ATIVO".equals(oldStatus)) {
            emailService.sendResourceProfileApprovedEmail(
                saved.getUser().getEmail(), saved.getUser().getName(), appProperties.getUrl()
            );
        }

        return saved;
    }
}
