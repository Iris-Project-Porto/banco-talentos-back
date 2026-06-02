package com.vilt.talentos.service;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.SquadRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepo;
    private final ProjectRepository projectRepo;
    private final SquadRepository squadRepo;
    private final UserRepository userRepo;

    public List<JobPostingResponse> findAllActive(){
        List<JobPostingResponse> list = jobPostingRepo.findByActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma vaga ativa encontrada");
        }
        return list;
    }

    public List<JobPostingResponse> findAllInactive(){
        List<JobPostingResponse> list = jobPostingRepo.findByActive(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma vaga inativa encontrada");
        }
        return list;
    }

    public JobPostingResponse findById(UUID id){
        return jobPostingRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada"));
    }

    public JobPostingResponse create(JobPostingRequest request){
        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        
        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Squad não encontrada"));

        JobPosting jobPosting = JobPosting.builder()
                .project(project)
                .squad(squad)
                .experienceLevel(request.experienceLevel())
                .description(request.description())
                .requirements(request.requirements())
                .recruiter(request.recruiter())
                .estimatedAllocationWeeks(request.estimatedAllocationWeeks())
                .status(request.status())
                .notes(request.notes())
                .openingDate(request.openingDate())
                .isUrgent(request.isUrgent())
                .active(true)
                .createdBy(getCurrentUser())
                .build();

        return mapToResponse(jobPostingRepo.save(jobPosting));
    }

    public JobPostingResponse update(UUID id, JobPostingRequest request){
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada"));

        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        
        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Squad não encontrada"));

        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setExperienceLevel(request.experienceLevel());
        jobPosting.setDescription(request.description());
        jobPosting.setRequirements(request.requirements());
        jobPosting.setRecruiter(request.recruiter());
        jobPosting.setEstimatedAllocationWeeks(request.estimatedAllocationWeeks());
        jobPosting.setStatus(request.status());
        jobPosting.setNotes(request.notes());
        jobPosting.setOpeningDate(request.openingDate());
        jobPosting.setIsUrgent(request.isUrgent());
        jobPosting.setUpdatedBy(getCurrentUser());

        return mapToResponse(jobPostingRepo.save(jobPosting));
    }

    public void setActiveStatus(UUID id, boolean active){
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada"));
        jobPosting.setActive(active);
        jobPosting.setUpdatedBy(getCurrentUser());
        jobPostingRepo.save(jobPosting);
    }

    private JobPostingResponse mapToResponse(JobPosting j) {
        return new JobPostingResponse(
                j.getId(),
                j.getProject() != null ? j.getProject().getName() : null,
                j.getProject() != null ? j.getProject().getId() : null,
                j.getSquad() != null ? j.getSquad().getName() : null,
                j.getSquad() != null ? j.getSquad().getId() : null,
                j.getExperienceLevel(),
                j.getExperienceLevel() != null ? j.getExperienceLevel().getDescription() : null,
                j.getDescription(),
                j.getRequirements(),
                j.getRecruiter(),
                j.getEstimatedAllocationWeeks(),
                j.getStatus(),
                j.getNotes(),
                j.getOpeningDate(),
                j.getIsUrgent(),
                j.getActive(),
                j.getCreatedAt(),
                j.getUpdatedAt(),
                j.getCreatedBy() != null ? j.getCreatedBy().getName() : "Sistema",
                j.getUpdatedBy() != null ? j.getUpdatedBy().getName() : null
        );
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }
}
