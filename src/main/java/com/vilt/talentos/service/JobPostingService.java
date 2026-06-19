package com.vilt.talentos.service;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.dto.JobPostingSkillRequest;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepo;
    private final ProjectRepository projectRepo;
    private final SquadRepository squadRepo;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;
    private final JobPostingMapper mapper;

    public Page<JobPostingResponse> findAllActive(Pageable pageable){
        Page<JobPostingResponse> page = jobPostingRepo.findByActive(true, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma vaga ativa encontrada");
        }
        return page;
    }

    public Page<JobPostingResponse> findAllInactive(Pageable pageable){
        Page<JobPostingResponse> page = jobPostingRepo.findByActive(false, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma vaga inativa encontrada");
        }
        return page;
    }

    public JobPostingResponse findById(UUID id){
        return jobPostingRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));
    }

    @Transactional
    public JobPostingResponse create(JobPostingRequest request){
        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));

        JobPosting jobPosting = mapper.toEntity(request);
        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setCreatedBy(getCurrentUser());
        
        reconcileSkills(jobPosting, request.skills());

        return mapper.toResponse(jobPostingRepo.save(jobPosting));
    }

    @Transactional
    public JobPostingResponse update(UUID id, JobPostingRequest request){
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));

        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));

        mapper.updateEntity(request, jobPosting);
        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setUpdatedBy(getCurrentUser());

        reconcileSkills(jobPosting, request.skills());

        return mapper.toResponse(jobPostingRepo.save(jobPosting));
    }

    private void reconcileSkills(JobPosting jobPosting, List<JobPostingSkillRequest> skillRequests) {
        if (skillRequests == null || skillRequests.isEmpty()) {
            jobPosting.getSkills().clear();
            return;
        }

        // RN004 - Validação de soma de pesos (100%)
        int totalWeight = skillRequests.stream()
                .mapToInt(JobPostingSkillRequest::importanceWeight)
                .sum();
        
        if (totalWeight != 100) {
            throw new BadRequestException("A soma dos pesos das skills deve totalizar 100%. Soma atual: " + totalWeight + "%");
        }

        Map<String, JobPostingSkillRequest> requested = skillRequests.stream()
                .filter(s -> s.name() != null && !s.name().isBlank())
                .collect(Collectors.toMap(
                        s -> s.name().trim().toUpperCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

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

    public void setActiveStatus(UUID id, boolean active){
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada"));
        jobPosting.setActive(active);
        jobPosting.setUpdatedBy(getCurrentUser());
        jobPostingRepo.save(jobPosting);
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
    }
}
