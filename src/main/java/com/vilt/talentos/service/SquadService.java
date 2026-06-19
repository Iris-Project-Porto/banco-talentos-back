package com.vilt.talentos.service;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SquadService {

    private final SquadRepository squadRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final SquadMapper mapper;

    public Page<SquadResponse> findAllActive(Pageable pageable) {
        Page<SquadResponse> page = squadRepo.findByActive(true, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma squad ativa encontrada.");
        }
        return page;
    }

    public Page<SquadResponse> findAllInactive(Pageable pageable) {
        Page<SquadResponse> page = squadRepo.findByActive(false, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma squad inativa encontrada.");
        }
        return page;
    }

    public SquadResponse findById(UUID id) {
        return squadRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));
    }

    public SquadResponse create(SquadRequest request) {
        User currentUser = getCurrentUser();
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepo.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        }

        Squad squad = mapper.toEntity(request);
        squad.setProject(project);
        squad.setActive(true);
        squad.setCreatedBy(currentUser);
        
        return mapper.toResponse(squadRepo.save(squad));
    }

    public SquadResponse update(UUID id, SquadRequest request) {
        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));
        
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepo.findById(request.projectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        }

        mapper.updateEntity(request, squad);
        squad.setProject(project);
        squad.setUpdatedBy(getCurrentUser());
        
        return mapper.toResponse(squadRepo.save(squad));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada"));
        squad.setActive(active);
        squad.setUpdatedBy(getCurrentUser());
        squadRepo.save(squad);
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
    }
}
