package com.vilt.talentos.service;

import com.vilt.talentos.dto.JobPostingCandidateMatchResponse;
import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.dto.JobPostingSkillRequest;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingService {
    private final AuditLogService auditLogService;
    private final JobPostingRepository jobPostingRepo;
    private final ProjectRepository projectRepo;
    private final SquadRepository squadRepo;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;
    private final ProfileRepository profileRepo;
    private final JobPostingMapper mapper;

    public Page<JobPostingResponse> findAllActive(Pageable pageable) {
        Page<JobPostingResponse> page = jobPostingRepo.findByActive(true, pageable)
                .map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por vagas ativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma vaga ativa encontrada");
        }

        return page;
    }

    public Page<JobPostingResponse> findAllInactive(Pageable pageable) {
        Page<JobPostingResponse> page = jobPostingRepo.findByActive(false, pageable)
                .map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por vagas inativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma vaga inativa encontrada");
        }
        return page;
    }

    public JobPostingResponse findById(UUID id) {
        return jobPostingRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Busca falhou: Vaga ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Vaga não encontrada");
                });
    }

    @Transactional
    public JobPostingResponse create(JobPostingRequest request) {
        log.info("Iniciando criação de uma nova vaga para o projeto ID: {}", request.projectId());

        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> {
                    log.warn("Falha ao criar vaga: Projeto ID '{}' não encontrado.", request.projectId());
                    return new ResourceNotFoundException("Projeto não encontrado");
                });

        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> {
                    log.warn("Falha ao criar vaga: Squad ID '{}' não encontrado.", request.squadId());
                    return new ResourceNotFoundException("Squad não encontrada");
                });

        JobPosting jobPosting = mapper.toEntity(request);
        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setCreatedBy(getCurrentUser());

        reconcileSkills(jobPosting, request.skills());

        JobPosting savedJobPosting = jobPostingRepo.save(jobPosting);
        log.info("Vaga criada com sucesso. ID gerado: {}", savedJobPosting.getId());

        return mapper.toResponse(savedJobPosting);
    }

    @Transactional
    public JobPostingResponse update(UUID id, JobPostingRequest request) {
        log.info("Iniciando atualização da vaga ID: {}", id);

        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar: Vaga ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Vaga não encontrada");
                });

        JobPostingResponse oldValue = mapper.toResponse(jobPosting);

        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar vaga: Projeto ID '{}' não encontrado.", request.projectId());
                    return new ResourceNotFoundException("Projeto não encontrado");
                });

        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar vaga: Squad ID '{}' não encontrado.", request.squadId());
                    return new ResourceNotFoundException("Squad não encontrada");
                });

        mapper.updateEntity(request, jobPosting);
        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setUpdatedBy(getCurrentUser());

        reconcileSkills(jobPosting, request.skills());

        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);
        log.info("Vaga ID: {} atualizada com sucesso.", id);

        JobPostingResponse newValue = mapper.toResponse(updatedJobPosting);

        auditLogService.record(
                AuditOperation.UPDATE,
                "JOB_POSTING",
                updatedJobPosting.getId(),
                oldValue,
                newValue);

        return newValue;
    }

    private void reconcileSkills(JobPosting jobPosting, List<JobPostingSkillRequest> skillRequests) {
        if (skillRequests == null || skillRequests.isEmpty()) {
            jobPosting.getSkills().clear();
            log.debug("Lista de skills da vaga enviada vazia. Removendo todas as skills da vaga.");
            return;
        }

        Map<String, JobPostingSkillRequest> requested = skillRequests.stream()
                .filter(s -> s.name() != null && !s.name().isBlank())
                .collect(Collectors.toMap(
                        s -> s.name().trim().toUpperCase(),
                        s -> s,
                        (existing, replacement) -> existing));

        List<JobPostingSkill> toRemove = new ArrayList<>();
        for (JobPostingSkill jps : jobPosting.getSkills()) {
            String name = jps.getSkill().getName().trim().toUpperCase();
            if (requested.containsKey(name)) {
                JobPostingSkillRequest req = requested.remove(name);
                jps.setImportanceWeight(req.importanceWeight());
                jps.setType(req.type());
                jps.setMinLevel(req.minLevel());
                jps.setDescription(req.description());
            } else {
                toRemove.add(jps);
            }
        }

        jobPosting.getSkills().removeAll(toRemove);

        for (var entry : requested.values()) {
            String name = entry.name().trim().toUpperCase();
            var skill = skillRepo.findByName(name)
                    .orElseGet(() -> skillRepo.save(Skill.builder().name(name).type(SkillType.HARD).build()));

            jobPosting.getSkills().add(JobPostingSkill.builder()
                    .jobPosting(jobPosting)
                    .skill(skill)
                    .type(entry.type())
                    .minLevel(entry.minLevel())
                    .importanceWeight(entry.importanceWeight())
                    .description(entry.description())
                    .build());
        }
    }

    public void setActiveStatus(UUID id, boolean active) {
        log.info("Alterando status da vaga ID: {} para ativo={}", id, active);

        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar status da vaga: Vaga ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Vaga não encontrada");
                });

        Boolean oldActive = jobPosting.getActive();

        if (Boolean.valueOf(active).equals(oldActive)) {
            log.info("Vaga ID: {} já possui ativo={}. Nenhuma alteração realizada.", id, active);
            return;
        }

        jobPosting.setActive(active);
        jobPosting.setUpdatedBy(getCurrentUser());
        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        auditLogService.record(
                AuditOperation.UPDATE,
                "JOB_POSTING",
                updatedJobPosting.getId(),
                Map.of("active", oldActive),
                Map.of("active", updatedJobPosting.getActive()));

        log.info("Status da vaga ID: {} atualizado para: {}", id, active);
    }

    @Transactional(readOnly = true)
    public Page<JobPostingCandidateMatchResponse> findCandidatesForJobPosting(
            UUID jobPostingId,
            Integer minScore,
            String availability,
            String search,
            Pageable pageable) {
        log.info("Calculando match de candidatos para a vaga ID: {}", jobPostingId);

        JobPosting jobPosting = jobPostingRepo.findById(jobPostingId)
                .orElseThrow(() -> {
                    log.warn("Busca de candidatos falhou: Vaga ID '{}' não encontrada.", jobPostingId);
                    return new ResourceNotFoundException("Vaga não encontrada");
                });

        List<JobPostingSkill> jobSkills = jobPosting.getSkills() != null ? jobPosting.getSkills() : List.of();

        double totalMaxWeight = 0.0;
        int reqTotal = 0;
        int desTotal = 0;

        for (JobPostingSkill jps : jobSkills) {
            int weight = (jps.getImportanceWeight() != null && jps.getImportanceWeight() > 0)
                    ? jps.getImportanceWeight()
                    : 1;
            double effectiveWeight = (jps.getType() == RequirementType.MANDATORY) ? (weight * 2.0) : (weight * 1.0);
            totalMaxWeight += effectiveWeight;

            if (jps.getType() == RequirementType.MANDATORY) {
                reqTotal++;
            } else {
                desTotal++;
            }
        }

        final double finalTotalMaxWeight = totalMaxWeight;
        final int finalReqTotal = reqTotal;
        final int finalDesTotal = desTotal;

        List<Profile> activeProfiles = profileRepo.findByStatus(DomainStatus.ACTIVE);
        List<JobPostingCandidateMatchResponse> candidateMatches = new ArrayList<>();

        for (Profile profile : activeProfiles) {
            Map<String, Integer> candidateSkills = profile.getSkills().stream()
                    .filter(ps -> ps.getSkill() != null && ps.getSkill().getName() != null)
                    .collect(Collectors.toMap(
                            ps -> ps.getSkill().getName().trim().toUpperCase(),
                            ps -> ps.getProficiencyLevel() != null ? ps.getProficiencyLevel() : 1,
                            Math::max));

            int reqMatched = 0;
            int desMatched = 0;
            double earnedWeight = 0.0;
            List<JobPostingCandidateMatchResponse.MainSkillContribution> contributions = new ArrayList<>();

            for (JobPostingSkill jps : jobSkills) {
                if (jps.getSkill() == null || jps.getSkill().getName() == null)
                    continue;

                String skillNameUpper = jps.getSkill().getName().trim().toUpperCase();
                int weight = (jps.getImportanceWeight() != null && jps.getImportanceWeight() > 0)
                        ? jps.getImportanceWeight()
                        : 1;
                double maxSkillWeight = (jps.getType() == RequirementType.MANDATORY) ? (weight * 2.0) : (weight * 1.0);

                if (candidateSkills.containsKey(skillNameUpper)) {
                    int candidateProf = candidateSkills.get(skillNameUpper);
                    SkillLevel level = jps.getMinLevel() != null ? jps.getMinLevel() : SkillLevel.BASIC;
                    int targetProf = switch (level) {
                        case BASIC -> 3;
                        case INTERMEDIATE -> 6;
                        case ADVANCED -> 8;
                        case SPECIALIST -> 10;
                    };

                    double skillEarned;
                    if (candidateProf >= targetProf) {
                        skillEarned = maxSkillWeight;
                    } else {
                        skillEarned = maxSkillWeight * Math.max(0.10, (double) candidateProf / targetProf);
                    }

                    earnedWeight += skillEarned;

                    int contributionPercent = finalTotalMaxWeight > 0
                            ? (int) Math.round((skillEarned / finalTotalMaxWeight) * 100.0)
                            : 0;

                    if (contributionPercent > 0) {
                        contributions.add(new JobPostingCandidateMatchResponse.MainSkillContribution(
                                jps.getSkill().getName(),
                                contributionPercent));
                    }

                    if (jps.getType() == RequirementType.MANDATORY) {
                        reqMatched++;
                    } else {
                        desMatched++;
                    }
                }
            }

            int matchScore = finalTotalMaxWeight > 0
                    ? (int) Math.min(100, Math.round((earnedWeight / finalTotalMaxWeight) * 100.0))
                    : 0;

            contributions.sort(
                    Comparator.comparingInt(JobPostingCandidateMatchResponse.MainSkillContribution::contributionPercent)
                            .reversed());
            List<JobPostingCandidateMatchResponse.MainSkillContribution> topSkills = contributions.stream()
                    .limit(5)
                    .toList();

            String squadName = profile.getAllocationSquad() != null ? profile.getAllocationSquad().getName() : null;
            String avail = profile.getResourceStatus() != null ? profile.getResourceStatus().name().toLowerCase()
                    : "available";
            String candidateName = profile.getUser() != null ? profile.getUser().getName() : "Sem nome";
            String desc = profile.getJobTitle() != null && !profile.getJobTitle().isBlank()
                    ? profile.getJobTitle()
                    : (profile.getArea() != null && !profile.getArea().isBlank() ? profile.getArea() : "Colaborador");

            var summary = new JobPostingCandidateMatchResponse.CandidateMatchSummary(
                    reqMatched,
                    finalReqTotal,
                    desMatched,
                    finalDesTotal);

            candidateMatches.add(new JobPostingCandidateMatchResponse(
                    profile.getId(),
                    candidateName,
                    desc,
                    profile.getPhotoUrl(),
                    squadName,
                    avail,
                    matchScore,
                    0,
                    topSkills,
                    summary));
        }

        candidateMatches.sort((c1, c2) -> {
            int cmp = Integer.compare(c2.matchScore(), c1.matchScore());
            if (cmp != 0)
                return cmp;
            cmp = Integer.compare(c2.summary().requiredSkillsMatched(), c1.summary().requiredSkillsMatched());
            if (cmp != 0)
                return cmp;
            return c1.name().compareToIgnoreCase(c2.name());
        });

        int minimumScore = (minScore != null) ? minScore : 1;
        List<JobPostingCandidateMatchResponse> eligible = candidateMatches.stream()
                .filter(c -> c.matchScore() >= minimumScore)
                .toList();

        List<JobPostingCandidateMatchResponse> ranked = new ArrayList<>();
        for (int i = 0; i < eligible.size(); i++) {
            var c = eligible.get(i);
            ranked.add(new JobPostingCandidateMatchResponse(
                    c.id(),
                    c.name(),
                    c.description(),
                    c.photoUrl(),
                    c.squadName(),
                    c.availability(),
                    c.matchScore(),
                    i + 1,
                    c.mainSkills(),
                    c.summary()));
        }

        var stream = ranked.stream();
        if (availability != null && !availability.isBlank()) {
            String availFilter = availability.trim().toLowerCase();
            stream = stream.filter(c -> c.availability() != null && c.availability().equalsIgnoreCase(availFilter));
        }
        if (search != null && !search.isBlank()) {
            String s = search.trim().toLowerCase();
            stream = stream.filter(c -> (c.name() != null && c.name().toLowerCase().contains(s))
                    || (c.description() != null && c.description().toLowerCase().contains(s))
                    || (c.squadName() != null && c.squadName().toLowerCase().contains(s)));
        }

        List<JobPostingCandidateMatchResponse> filtered = stream.toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<JobPostingCandidateMatchResponse> pageContent = (start < filtered.size() && start <= end)
                ? filtered.subList(start, end)
                : List.of();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> {
                    log.error("Inconsistência de segurança: Token válido, mas usuário ID: {} não encontrado no banco.",
                            userIdStr);
                    return new UnauthorizedException("Usuário não autenticado");
                });
    }
}
