package com.vilt.talentos.service;

import com.vilt.talentos.dto.*;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.exception.ConflictException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.JobPostingAllocationRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingAllocationService {

    private static final String ALLOCATED_STATUS_LABEL = "Alocado Integral (100%)";

    private final JobPostingRepository jobPostingRepo;
    private final ProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final JobPostingAllocationRepository allocationRepo;
    private final JobPostingMapper jobPostingMapper;

    /**
     * Aloca recurso(s) em uma vaga, vinculando à squad/projeto da vaga com cargo/função,
     * atualizando o status do recurso para ALLOCATED e fechando a vaga como FILLED.
     */
    @Transactional
    public JobPostingAllocationBatchResponse allocate(UUID jobPostingId, JobPostingAllocationBatchRequest request) {
        List<JobPostingAllocationItemRequest> items = request.resolvedAllocations();
        log.info("Iniciando alocação de {} recurso(s) para a vaga ID: {}", items.size(), jobPostingId);

        JobPosting jobPosting = jobPostingRepo.findById(jobPostingId)
                .orElseThrow(() -> {
                    log.warn("Alocação falhou: Vaga ID '{}' não encontrada.", jobPostingId);
                    return new ResourceNotFoundException("Vaga não encontrada");
                });

        validateJobPostingForAllocation(jobPosting);
        ensureNoDuplicateResources(items);

        User currentUser = getCurrentUser();
        List<JobPostingAllocation> savedAllocations = new ArrayList<>();

        for (JobPostingAllocationItemRequest item : items) {
            JobPostingAllocation saved = allocateSingleResource(jobPosting, item);
            savedAllocations.add(saved);
        }

        jobPosting.setStatus(JobPostingStatus.FILLED);
        jobPosting.setUpdatedBy(currentUser);
        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        log.info("Alocação concluída com sucesso para a vaga ID: {}. Status atualizado para FILLED.", jobPostingId);

        return new JobPostingAllocationBatchResponse(
                jobPostingMapper.toResponse(updatedJobPosting),
                savedAllocations.stream().map(this::toResponse).toList());
    }

    /**
     * Lista as alocações registradas para uma vaga específica.
     */
    @Transactional(readOnly = true)
    public List<JobPostingAllocationResponse> listByJobPosting(UUID jobPostingId) {
        if (!jobPostingRepo.existsById(jobPostingId)) {
            throw new ResourceNotFoundException("Vaga não encontrada");
        }
        return allocationRepo.findByJobPostingIdOrderByCreatedAtDesc(jobPostingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateJobPostingForAllocation(JobPosting jobPosting) {
        if (!Boolean.TRUE.equals(jobPosting.getActive())) {
            throw new ConflictException("Não é possível alocar recursos em uma vaga inativa.");
        }
        JobPostingStatus status = jobPosting.getStatus();
        if (status != JobPostingStatus.OPEN
                && status != JobPostingStatus.SCREENING
                && status != JobPostingStatus.ALLOCATING) {
            throw new ConflictException("Não é possível alocar recursos em uma vaga com status " + status + ".");
        }
    }

    private void ensureNoDuplicateResources(List<JobPostingAllocationItemRequest> items) {
        Set<UUID> seen = new HashSet<>();
        for (JobPostingAllocationItemRequest item : items) {
            if (!seen.add(item.resourceId())) {
                throw new BadRequestException(
                        "Recurso duplicado na mesma requisição de alocação: " + item.resourceId());
            }
        }
    }

    private JobPostingAllocation allocateSingleResource(JobPosting jobPosting, JobPostingAllocationItemRequest item) {
        Profile profile = profileRepo.findById(item.resourceId())
                .orElseThrow(() -> {
                    log.warn("Alocação falhou: Recurso (perfil) ID '{}' não encontrado.", item.resourceId());
                    return new ResourceNotFoundException("Recurso não encontrado: " + item.resourceId());
                });

        validateProfileEligibility(profile, jobPosting);

        String trimmedRole = item.role().trim();
        if (jobPosting.getProject() != null) {
            profile.setAllocationProject(jobPosting.getProject());
        }
        if (jobPosting.getSquad() != null) {
            profile.setAllocationSquad(jobPosting.getSquad());
        }
        profile.setJobTitle(trimmedRole);
        profile.setProjectEntryDate(item.startDate());
        profile.setAllocationStatus(ALLOCATED_STATUS_LABEL);
        profile.setResourceStatus(ResourceStatus.ALLOCATED);

        Profile savedProfile = profileRepo.save(profile);

        JobPostingAllocation allocation = JobPostingAllocation.builder()
                .jobPosting(jobPosting)
                .profile(savedProfile)
                .project(jobPosting.getProject())
                .squad(jobPosting.getSquad())
                .startDate(item.startDate())
                .role(trimmedRole)
                .active(true)
                .build();

        return allocationRepo.save(allocation);
    }

    private void validateProfileEligibility(Profile profile, JobPosting jobPosting) {
        String resourceName = resolveProfileName(profile);

        boolean userInactive = profile.getUser() != null && profile.getUser().getStatus() != DomainStatus.ACTIVE;
        if (profile.getStatus() != DomainStatus.ACTIVE || userInactive) {
            throw new ConflictException("O recurso '" + resourceName + "' não está ativo e não pode ser alocado.");
        }

        if (profile.getResourceStatus() == ResourceStatus.ALLOCATED
                || ALLOCATED_STATUS_LABEL.equalsIgnoreCase(profile.getAllocationStatus())) {
            throw new ConflictException("O recurso '" + resourceName + "' já está alocado e indisponível.");
        }

        if (allocationRepo.existsByProfileIdAndActiveTrue(profile.getId())) {
            throw new ConflictException("O recurso '" + resourceName + "' já possui uma alocação ativa.");
        }

        Squad currentSquad = profile.getAllocationSquad();
        Squad targetSquad = jobPosting.getSquad();
        if (currentSquad != null && (targetSquad == null || !currentSquad.getId().equals(targetSquad.getId()))) {
            throw new ConflictException("O recurso '" + resourceName + "' já está alocado na squad '"
                    + currentSquad.getName() + "'.");
        }
    }

    private JobPostingAllocationResponse toResponse(JobPostingAllocation allocation) {
        return new JobPostingAllocationResponse(
                allocation.getId(),
                allocation.getJobPosting().getId(),
                allocation.getProfile().getId(),
                resolveProfileName(allocation.getProfile()),
                allocation.getProject() != null ? allocation.getProject().getId() : null,
                allocation.getProject() != null ? allocation.getProject().getName() : null,
                allocation.getSquad() != null ? allocation.getSquad().getId() : null,
                allocation.getSquad() != null ? allocation.getSquad().getName() : null,
                allocation.getStartDate(),
                allocation.getRole(),
                allocation.isActive(),
                allocation.getCreatedAt(),
                allocation.getCreatedBy() != null ? allocation.getCreatedBy().getName() : null);
    }

    private String resolveProfileName(Profile profile) {
        if (profile.getUser() != null && profile.getUser().getName() != null) {
            return profile.getUser().getName();
        }
        return profile.getId() != null ? profile.getId().toString() : "Recurso";
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
