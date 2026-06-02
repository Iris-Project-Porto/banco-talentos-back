package com.vilt.talentos.service;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.repository.*;
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
public class SquadService {

    private final SquadRepository squadRepo;
    private final ProjectRepository projectRepo;
    private final SkillRepository skillRepo;
    private final UserRepository userRepo;

    public List<SquadResponse> findAllActive() {
        List<SquadResponse> list = squadRepo.findByActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma squad ativa encontrada");
        }
        return list;
    }

    public List<SquadResponse> findAllInactive() {
        List<SquadResponse> list = squadRepo.findByActive(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma squad inativa encontrada");
        }
        return list;
    }

    public SquadResponse findById(UUID id) {
        return squadRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Squad não encontrada"));
    }

    public SquadResponse create(SquadRequest request) {
        User currentUser = getCurrentUser();
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepo.findById(request.projectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        }

        List<Skill> skills = List.of();
        if (request.skillIds() != null && !request.skillIds().isEmpty()) {
            skills = skillRepo.findAllById(request.skillIds());
        }

        Squad squad = Squad.builder()
                .name(request.name())
                .description(request.description())
                .portoCoordinator(request.portoCoordinator())
                .projectManager(request.projectManager())
                .project(project)
                .skills(skills)
                .active(true)
                .createdBy(currentUser)
                .build();
        
        return mapToResponse(squadRepo.save(squad));
    }

    public SquadResponse update(UUID id, SquadRequest request) {
        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Squad não encontrada"));
        
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepo.findById(request.projectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        }

        List<Skill> skills = List.of();
        if (request.skillIds() != null && !request.skillIds().isEmpty()) {
            skills = skillRepo.findAllById(request.skillIds());
        }

        squad.setName(request.name());
        squad.setDescription(request.description());
        squad.setPortoCoordinator(request.portoCoordinator());
        squad.setProjectManager(request.projectManager());
        squad.setProject(project);
        squad.setSkills(skills);
        squad.setUpdatedBy(getCurrentUser());
        
        return mapToResponse(squadRepo.save(squad));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Squad squad = squadRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Squad não encontrada"));
        squad.setActive(active);
        squad.setUpdatedBy(getCurrentUser());
        squadRepo.save(squad);
    }

    private SquadResponse mapToResponse(Squad s) {
        return new SquadResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getPortoCoordinator(),
                s.getProjectManager(),
                s.getProject() != null ? s.getProject().getName() : null,
                s.getProject() != null ? s.getProject().getId() : null,
                s.getSkills().stream().map(Skill::getName).collect(Collectors.toList()),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt(),
                s.getCreatedBy() != null ? s.getCreatedBy().getName() : "Sistema",
                s.getUpdatedBy() != null ? s.getUpdatedBy().getName() : null
        );
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }
}
