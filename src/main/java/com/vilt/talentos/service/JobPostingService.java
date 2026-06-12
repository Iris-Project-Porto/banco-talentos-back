package com.vilt.talentos.service;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.SquadRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobPostingService {
    private final JobPostingRepository jobPostingRepo;
    private final ProjectRepository projectRepo;
    private final SquadRepository squadRepo;
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

    public JobPostingResponse create(JobPostingRequest request){
        Project project = projectRepo.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        Squad squad = squadRepo.findById(request.squadId())
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));

        JobPosting jobPosting = mapper.toEntity(request);
        jobPosting.setProject(project);
        jobPosting.setSquad(squad);
        jobPosting.setCreatedBy(getCurrentUser());

        return mapper.toResponse(jobPostingRepo.save(jobPosting));
    }

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

        return mapper.toResponse(jobPostingRepo.save(jobPosting));
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
