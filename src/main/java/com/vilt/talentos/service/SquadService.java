package com.vilt.talentos.service;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.dto.SquadResourceInput;
import com.vilt.talentos.dto.SquadResourceResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SquadService {

    private static final String FULLY_ALLOCATED_STATUS = "Alocado Integral (100%)";
    private static final String BENCH_STATUS = "Disponível (Bench)";

    private final SquadRepository squadRepo;
    private final ProfileRepository profileRepo;
    private final JobPostingRepository jobPostingRepo;
    private final JobPostingAllocationRepository allocationRepo;
    private final UserRepository userRepo;
    private final SquadMapper mapper;
    private final AuditLogService auditLogService;

    public Page<SquadResponse> findAllActive(String search, Pageable pageable) {
        Page<Squad> squadPage;
        if (search != null && !search.trim().isEmpty()) {
            squadPage = squadRepo.findByActiveAndNameContainingIgnoreCase(true, search.trim(), pageable);
        } else {
            squadPage = squadRepo.findByActive(true, pageable);
        }

        Page<SquadResponse> page = squadPage.map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por squads ativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma squad ativa encontrada.");
        }

        return page;
    }

    public Page<SquadResponse> findAllInactive(String search, Pageable pageable) {
        Page<Squad> squadPage;
        if (search != null && !search.trim().isEmpty()) {
            squadPage = squadRepo.findByActiveAndNameContainingIgnoreCase(false, search.trim(), pageable);
        } else {
            squadPage = squadRepo.findByActive(false, pageable);
        }

        Page<SquadResponse> page = squadPage.map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por squads inativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma squad inativa encontrada.");
        }

        return page;
    }

    public List<SquadResponse> findAllUnlinked() {
        log.info("Buscando squads ativas sem vínculo com projeto.");
        List<SquadResponse> result = squadRepo.findByActiveAndProjectIsNull(true)
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.info("Total de squads sem vínculo encontradas: {}", result.size());
        return result;
    }

    public SquadResponse findById(UUID id) {
        return squadRepo.findById(id)
                .map(squad -> mapper.toResponse(squad).withRecursos(getSquadRecursos(id)))
                .orElseThrow(() -> {
                    log.warn("Squad ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Squad não encontrada");
                });
    }

    @Transactional
    public SquadResponse create(SquadRequest request) {
        log.info("Iniciando criação de uma nova squad.");

        User currentUser = getCurrentUser();

        Squad squad = mapper.toEntity(request);
        squad.setActive(true);
        squad.setCreatedBy(currentUser);

        Squad savedSquad = squadRepo.save(squad);
        log.info("Squad criada com sucesso. ID gerado: {}", savedSquad.getId());
        
        syncRecursos(savedSquad, request.recursoIds(), request.recursos());

        auditLogService.record(
                AuditOperation.CREATE,
                "SQUAD",
                savedSquad.getId(),
                null,
                squadAuditSnapshot(savedSquad));

        return mapper.toResponse(savedSquad).withRecursos(getSquadRecursos(savedSquad.getId()));
    }

    @Transactional
    public SquadResponse update(UUID id, SquadRequest request) {
        log.info("Iniciando atualização da squad ID: {}", id);

        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar: Squad ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Squad não encontrada");
                });
        Map<String, Object> oldValue = squadAuditSnapshot(squad);

        mapper.updateEntity(request, squad);
        squad.setUpdatedBy(getCurrentUser());

        Squad updatedSquad = squadRepo.save(squad);
        log.info("Squad ID: {} atualizada com sucesso.", id);
        
        syncRecursos(updatedSquad, request.recursoIds(), request.recursos());

        Map<String, Object> newValue = squadAuditSnapshot(updatedSquad);
        if (!Objects.equals(oldValue, newValue)) {
            auditLogService.record(
                    AuditOperation.UPDATE,
                    "SQUAD",
                    updatedSquad.getId(),
                    oldValue,
                    newValue);
        }

        return mapper.toResponse(updatedSquad).withRecursos(getSquadRecursos(updatedSquad.getId()));
    }

    private List<SquadResourceResponse> getSquadRecursos(UUID squadId) {
        if (squadId == null)
            return List.of();
        return profileRepo.findAllByAllocationSquadId(squadId)
                .stream()
                .map(p -> new SquadResourceResponse(
                        p.getId(),
                        p.getUser() != null ? p.getUser().getName() : "",
                        p.getUser() != null ? p.getUser().getEmail() : "",
                        p.getJobTitle() != null ? p.getJobTitle() : "",
                        p.getPhotoUrl()))
                .toList();
    }

    private void syncRecursos(Squad squad, List<UUID> recursoIds, List<SquadResourceInput> recursos) {
        if (recursos == null && recursoIds == null) {
            return;
        }

        List<UUID> targetIds = recursos != null
                ? recursos.stream().map(SquadResourceInput::id).filter(Objects::nonNull).toList()
                : recursoIds == null ? List.of() : recursoIds;
        List<Profile> currentProfiles = profileRepo.findAllByAllocationSquadId(squad.getId());
        var targetIdSet = new HashSet<>(targetIds);
        Map<UUID, Profile> profilesToSave = new LinkedHashMap<>();

        if (allocationRepo != null) {
            if (targetIds == null || targetIds.isEmpty()) {
                allocationRepo.deactivateBySquadId(squad.getId());
            } else {
                allocationRepo.deactivateBySquadIdAndProfileIdNotIn(squad.getId(), targetIds);
            }
        }

        for (Profile profile : currentProfiles) {
            if (!targetIdSet.contains(profile.getId())) {
                Map<String, Object> oldProfile = profileAuditSnapshot(profile);
                profile.setAllocationSquad(null);
                updateResourceStatus(profile);
                recordProfileChange(profile, oldProfile);
                profilesToSave.put(profile.getId(), profile);
            }
        }

        if (!targetIds.isEmpty()) {
            List<Profile> profiles = profileRepo.findAllById(targetIds);
            Map<UUID, String> jobTitleMap = recursos == null
                    ? Map.of()
                    : recursos.stream()
                            .filter(r -> r.id() != null && r.jobTitle() != null)
                            .collect(Collectors.toMap(SquadResourceInput::id, SquadResourceInput::jobTitle, (a, b) -> b));

            for (Profile profile : profiles) {
                Map<String, Object> oldProfile = profileAuditSnapshot(profile);
                profile.setAllocationSquad(squad);
                profile.setResourceStatus(ResourceStatus.ALLOCATED);
                if (jobTitleMap.containsKey(profile.getId())) {
                    profile.setJobTitle(jobTitleMap.get(profile.getId()));
                }

                recordProfileChange(profile, oldProfile);
                profilesToSave.put(profile.getId(), profile);
            }
        }

        if (!profilesToSave.isEmpty()) {
            profileRepo.saveAll(List.copyOf(profilesToSave.values()));
        }
    }

    private void updateResourceStatus(Profile profile) {
        boolean hasActiveAllocation = allocationRepo != null
                && profile.getId() != null
                && allocationRepo.existsByProfileIdAndActiveTrue(profile.getId());
        boolean allocatedElsewhere = profile.getAllocationProject() != null || hasActiveAllocation;
        profile.setResourceStatus(allocatedElsewhere ? ResourceStatus.ALLOCATED : ResourceStatus.AVAILABLE);
        if (!allocatedElsewhere && FULLY_ALLOCATED_STATUS.equalsIgnoreCase(profile.getAllocationStatus())) {
            profile.setAllocationStatus(BENCH_STATUS);
        }
    }

    private void recordProfileChange(Profile profile, Map<String, Object> oldProfile) {
        Map<String, Object> newProfile = profileAuditSnapshot(profile);
        if (!Objects.equals(oldProfile, newProfile)) {
            auditLogService.record(AuditOperation.UPDATE, "PROFILE", profile.getId(),
                    profile.getUser() != null ? profile.getUser().getId() : null,
                    oldProfile, newProfile);
        }
    }

    private Map<String, Object> profileAuditSnapshot(Profile profile) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ID", profile.getId());
        snapshot.put("Cargo", profile.getJobTitle());
        snapshot.put("Squad",
                profile.getAllocationSquad() != null ? profile.getAllocationSquad().getName() : "Nenhuma");
        snapshot.put("Status do recurso", profile.getResourceStatus());
        snapshot.put("Status da alocação", profile.getAllocationStatus());
        return snapshot;
    }

    @Transactional
    public void setActiveStatus(UUID id, boolean active) {
        log.info("Alterando status da squad ID: {} para ativo={}", id, active);

        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao alterar status: Squad ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Squad não encontrada");
                });
        Map<String, Object> oldValue = squadAuditSnapshot(squad);

        if (squad.isActive() == active) {
            return;
        }

        squad.setActive(active);
        squad.setUpdatedBy(getCurrentUser());
        squadRepo.save(squad);

        log.info("Status da squad ID: {} atualizado com sucesso.", id);
        auditLogService.record(
                AuditOperation.UPDATE,
                "SQUAD",
                squad.getId(),
                oldValue,
                squadAuditSnapshot(squad));
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> {
                    log.error("Falha de segurança: Token válido, mas usuário ID: {} não encontrado no banco.",
                            userIdStr);
                    return new UnauthorizedException("Usuário não autenticado");
                });
    }

    @Transactional
    public void deleteSquad(UUID id) {
        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao deletar: Squad ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Squad não encontrada");
                });
        if (allocationRepo != null) {
            allocationRepo.deactivateBySquadId(id);
        }
        Map<String, Object> oldValue = squadAuditSnapshot(squad);
        List<Profile> profiles = profileRepo.findAllByAllocationSquadId(id);
        for (Profile profile : profiles) {
            Map<String, Object> oldProfile = profileAuditSnapshot(profile);
            profile.setAllocationSquad(null);
            updateResourceStatus(profile);
            recordProfileChange(profile, oldProfile);
        }
        if (!profiles.isEmpty()) {
            profileRepo.saveAll(profiles);
        }
        jobPostingRepo.clearSquad(id);
        squadRepo.delete(squad);
        auditLogService.record(
                AuditOperation.DELETE,
                "SQUAD",
                id,
                oldValue,
                null);
    }

    private Map<String, Object> squadAuditSnapshot(Squad squad) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ID", squad.getId());
        snapshot.put("Nome", squad.getName());
        snapshot.put("Descrição", squad.getDescription());
        snapshot.put("Coordenador Porto", squad.getPortoCoordinator());
        snapshot.put("Gerente de Projeto", squad.getProjectManager());
        snapshot.put("Projeto", squad.getProject() == null ? null : squad.getProject().getName());
        snapshot.put("ID do projeto", squad.getProject() == null ? null : squad.getProject().getId());
        snapshot.put("Ativa", squad.isActive());

        List<String> nomesRecursos = profileRepo.findAllByAllocationSquadId(squad.getId())
                .stream()
                .map(p -> p.getUser() != null ? p.getUser().getName() : "Sem Nome")
                .sorted()
                .toList();
        snapshot.put("Recursos Alocados", nomesRecursos);

        return snapshot;
    }
}