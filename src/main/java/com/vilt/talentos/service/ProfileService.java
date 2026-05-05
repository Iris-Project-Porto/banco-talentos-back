package com.vilt.talentos.service;

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
    private final TalentEvaluationService evaluationService;

    @Transactional
    public Profile createOrUpdate(UUID userId, ProfileRequest req) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var evaluation = evaluationService.evaluate(req);

        var profile = profileRepo.findByUserId(userId).orElseGet(() -> Profile.builder()
                .user(user).status("PENDENTE").build());

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

        profile.getSkills().clear();
        profile = profileRepo.saveAndFlush(profile);

        if (req.skills() != null) {
            for (var s : req.skills()) {
                if (s.name() == null || s.name().isBlank()) continue;
                var skill = skillRepo.findByName(s.name().trim())
                        .orElseGet(() -> skillRepo.save(Skill.builder().name(s.name().trim()).build()));
                profile.getSkills().add(ProfileSkill.builder()
                        .profile(profile).skill(skill).level(s.level()).build());
            }
            profile = profileRepo.save(profile);
        }

        return profile;
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

        if (req.skills() != null) {
            profile.getSkills().clear();
            profile = profileRepo.saveAndFlush(profile);
            for (var s : req.skills()) {
                if (s.name() == null || s.name().isBlank()) continue;
                var skill = skillRepo.findByName(s.name().trim())
                        .orElseGet(() -> skillRepo.save(Skill.builder().name(s.name().trim()).build()));
                profile.getSkills().add(ProfileSkill.builder()
                        .profile(profile).skill(skill).level(s.level()).build());
            }
        }

        return profileRepo.save(profile);
    }
}
