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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SquadService {

    private final SquadRepository squadRepo;
    private final ProfileRepository profileRepo;
    private final JobPostingRepository jobPostingRepo;
    private final JobPostingAllocationRepository allocationRepo;
    private final UserRepository userRepo;
    private final SquadMapper mapper;

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

        mapper.updateEntity(request, squad);
        squad.setUpdatedBy(getCurrentUser());

        Squad updatedSquad = squadRepo.save(squad);
        log.info("Squad ID: {} atualizada com sucesso.", id);

        syncRecursos(updatedSquad, request.recursoIds(), request.recursos());

        return mapper.toResponse(updatedSquad).withRecursos(getSquadRecursos(updatedSquad.getId()));
    }

    private List<SquadResourceResponse> getSquadRecursos(UUID squadId) {
        if (squadId == null) return List.of();
        return profileRepo.findAllByAllocationSquadId(squadId)
                .stream()
                .map(p -> new SquadResourceResponse(
                        p.getId(),
                        p.getUser() != null ? p.getUser().getName() : "",
                        p.getUser() != null ? p.getUser().getEmail() : "",
                        p.getJobTitle() != null ? p.getJobTitle() : "",
                        p.getPhotoUrl()
                ))
                .toList();
    }

    private void syncRecursos(Squad squad, List<UUID> recursoIds, List<SquadResourceInput> recursos) {
        if (recursos == null && recursoIds == null) {
            return;
        }

        List<UUID> targetIds = recursos != null
                ? recursos.stream().map(SquadResourceInput::id).toList()
                : recursoIds;

        profileRepo.clearAllocationSquad(squad.getId());
        if (allocationRepo != null) {
            if (targetIds.isEmpty()) {
                allocationRepo.deactivateBySquadId(squad.getId());
            } else {
                allocationRepo.deactivateBySquadIdAndProfileIdNotIn(squad.getId(), targetIds);
            }
        }

        if (targetIds.isEmpty()) {
            return;
        }

        List<Profile> profiles = profileRepo.findAllById(targetIds);
        Map<UUID, String> jobTitleMap = recursos != null
                ? recursos.stream()
                        .filter(r -> r.id() != null && r.jobTitle() != null)
                        .collect(Collectors.toMap(SquadResourceInput::id, SquadResourceInput::jobTitle, (a, b) -> b))
                : Map.of();

        for (Profile p : profiles) {
            p.setAllocationSquad(squad);
            if (jobTitleMap.containsKey(p.getId())) {
                p.setJobTitle(jobTitleMap.get(p.getId()));
            }
        }
        profileRepo.saveAll(profiles);
    }

    public void setActiveStatus(UUID id, boolean active) {
        log.info("Alterando status da squad ID: {} para ativo={}", id, active);

        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao alterar status: Squad ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Squad não encontrada");
                });

        squad.setActive(active);
        squad.setUpdatedBy(getCurrentUser());
        squadRepo.save(squad);

        log.info("Status da squad ID: {} atualizado com sucesso.", id);
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
        profileRepo.clearAllocationSquad(id);
        jobPostingRepo.clearSquad(id);
        squadRepo.delete(squad);
    }
}
