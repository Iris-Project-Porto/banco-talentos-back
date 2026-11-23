package com.vilt.talentos.service;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.Squad;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.exception.ConflictException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.ProjectMapper;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.JobPostingAllocationRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.SquadRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final JobPostingRepository jobPostingRepo;
    private final JobPostingAllocationRepository allocationRepo;
    private final ProfileRepository profileRepo;
    private final UserRepository userRepo;
    private final SquadRepository squadRepo;
    private final ProjectMapper mapper;
    private final SquadMapper squadMapper;
    private final AuditLogService auditLogService;

    public Page<ProjectResponse> findAllActive(Pageable pageable) {
        Page<ProjectResponse> page = projectRepo.findByActive(true, pageable)
                .map(p -> mapper.toResponse(p, squadMapper));
        if (page.isEmpty()) {
            log.warn("Busca por projetos ativos retornou vazia.");
            throw new ResourceNotFoundException("Nenhum projeto ativo encontrado");
        }
        return page;
    }

    public Page<ProjectResponse> findAllInactive(Pageable pageable) {
        Page<ProjectResponse> page = projectRepo.findByActive(false, pageable)
                .map(p -> mapper.toResponse(p, squadMapper));
        if (page.isEmpty()) {
            log.warn("Busca por projetos inativos retornou vazia.");
            throw new ResourceNotFoundException("Nenhum projeto inativo encontrado");
        }
        return page;
    }

    public ProjectResponse findById(UUID id) {
        return projectRepo.findWithSquadsById(id)
                .map(p -> mapper.toResponse(p, squadMapper))
                .orElseThrow(() -> {
                    log.warn("Busca falhou: Projeto ID '{}' não encontrado.", id);
                    return new ResourceNotFoundException("Projeto não encontrado");
                });
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        log.info("Iniciando criação de um novo projeto.");

        User currentUser = getCurrentUser();
        Project project = mapper.toEntity(request);
        project.setCreatedBy(currentUser);

        Project savedProject = projectRepo.save(project);
        log.info("Projeto criado com sucesso. ID gerado: {}", savedProject.getId());
        auditLogService.record(
                AuditOperation.CREATE,
                "PROJECT",
                savedProject.getId(),
                null,
                projectAuditSnapshot(savedProject));

        if (request.squad() != null) {
            Squad newSquad = squadMapper.toEntity(request.squad());
            newSquad.setProject(savedProject);
            newSquad.setCreatedBy(currentUser);
            Squad savedSquad = squadRepo.save(newSquad);
            log.info("Squad criada e vinculada ao projeto com sucesso.");
            auditLogService.record(
                    AuditOperation.CREATE,
                    "SQUAD",
                    savedSquad.getId(),
                    null,
                    squadProjectSnapshot(savedSquad, "Vínculo de projeto", savedProject));
        }

        if (request.squadIds() != null) {
            reconcileSquads(savedProject, request.squadIds(), currentUser);
        }

        return projectRepo.findWithSquadsById(savedProject.getId())
                .map(p -> mapper.toResponse(p, squadMapper))
                .orElseThrow();
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        log.info("Iniciando atualização do projeto ID: {}", id);

        Project project = projectRepo.findWithSquadsById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar: Projeto ID '{}' não encontrado.", id);
                    return new ResourceNotFoundException("Projeto não encontrado");
                });
        Map<String, Object> oldValue = projectAuditSnapshot(project);

        mapper.updateEntity(request, project);
        project.setUpdatedBy(getCurrentUser());

        Project updatedProject = projectRepo.save(project);
        log.info("Projeto ID: {} atualizado com sucesso.", id);

        if (request.squad() != null) {
            User currentUser = getCurrentUser();
            Squad newSquad = squadMapper.toEntity(request.squad());
            newSquad.setProject(updatedProject);
            newSquad.setCreatedBy(currentUser);
            Squad savedSquad = squadRepo.save(newSquad);
            log.info("Nova Squad criada e vinculada ao projeto ID: {} durante atualização.", id);
            auditLogService.record(
                    AuditOperation.CREATE,
                    "SQUAD",
                    savedSquad.getId(),
                    null,
                    squadProjectSnapshot(savedSquad, "Vínculo de projeto", updatedProject));
            recordProjectSquadChange(updatedProject, savedSquad, true);
        }

        if (request.squadIds() != null) {
            reconcileSquads(updatedProject, request.squadIds(), getCurrentUser());
        }

        Map<String, Object> newValue = projectAuditSnapshot(updatedProject);
        if (!Objects.equals(oldValue, newValue)) {
            auditLogService.record(
                    AuditOperation.UPDATE,
                    "PROJECT",
                    updatedProject.getId(),
                    oldValue,
                    newValue);
        }

        return projectRepo.findWithSquadsById(updatedProject.getId())
                .map(p -> mapper.toResponse(p, squadMapper))
                .orElseThrow();
    }

    @Transactional
    public void setActiveStatus(UUID id, boolean active) {
        log.info("Alterando status do projeto ID: {} para ativo={}", id, active);

        Project project = projectRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao definir status ativo: Projeto ID '{}' não encontrado.", id);
                    return new ResourceNotFoundException("Projeto não encontrado");
                });

        if (project.isActive() == active) {
            return;
        }

        project.setActive(active);
        project.setUpdatedBy(getCurrentUser());
        projectRepo.save(project);
        log.info("Status do projeto ID: {} atualizado com sucesso.", id);
        auditLogService.record(
                AuditOperation.UPDATE,
                "PROJECT",
                project.getId(),
                Map.of("Ativo", !active),
                Map.of("Ativo", active));
    }

    private void reconcileSquads(Project project, List<UUID> desiredIds, User currentUser) {
        List<UUID> currentIds = project.getSquads().stream()
                .map(Squad::getId)
                .toList();

        // Desvincula squads que não estão mais na lista desejada.
        for (UUID squadId : currentIds) {
            if (!desiredIds.contains(squadId)) {
                Squad squad = squadRepo.findById(squadId)
                        .orElseThrow(() -> new ResourceNotFoundException("Squad não encontrada: " + squadId));
                Map<String, Object> oldValue = squadAuditSnapshot(squad);
                squad.setProject(null);
                squad.setUpdatedBy(currentUser);
                Squad savedSquad = squadRepo.save(squad);
                log.info("Squad ID: {} desvinculada do projeto ID: {}.", squadId, project.getId());
                recordSquadProjectChange(savedSquad, project, false, oldValue);
                recordProjectSquadChange(project, savedSquad, false);
            }
        }

        // Vincula squads novas que ainda não estão associadas.
        for (UUID squadId : desiredIds) {
            if (!currentIds.contains(squadId)) {
                Squad squad = squadRepo.findById(squadId)
                        .orElseThrow(() -> {
                            log.warn("Squad ID '{}' não encontrada para vínculo.", squadId);
                            return new ResourceNotFoundException("Squad não encontrada: " + squadId);
                        });

                if (squad.getProject() != null && !squad.getProject().getId().equals(project.getId())) {
                    log.warn("Squad ID '{}' já está vinculada ao projeto ID '{}'.", squadId,
                            squad.getProject().getId());
                    throw new ConflictException("A squad já está vinculada a outro projeto: " + squadId);
                }

                Map<String, Object> oldValue = squadAuditSnapshot(squad);
                squad.setProject(project);
                squad.setUpdatedBy(currentUser);
                Squad savedSquad = squadRepo.save(squad);
                log.info("Squad ID: {} vinculada ao projeto ID: {}.", squadId, project.getId());
                recordSquadProjectChange(savedSquad, project, true, oldValue);
                recordProjectSquadChange(project, savedSquad, true);
            }
        }
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> {
                    log.error("Falha de segurança: Token válido, mas usuário ID: {} não encontrado no banco de dados.",
                            userIdStr);
                    return new UnauthorizedException("Usuário não autenticado");
                });
    }

    @Transactional
    public void deleteProject(UUID id) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao deletar: Projeto ID '{}' não encontrado.", id);
                    return new ResourceNotFoundException("Projeto não encontrado");
                });
        if (allocationRepo != null) {
            allocationRepo.deactivateByProjectId(id);
        }
        Map<String, Object> oldValue = projectAuditSnapshot(project);
        jobPostingRepo.clearProject(id);
        profileRepo.clearAllocationProject(id);
        User currentUser = getCurrentUser();
        for (Squad squad : squadRepo.findByProject_Id(id)) {
            Map<String, Object> oldSquadValue = squadAuditSnapshot(squad);
            squad.setProject(null);
            squad.setUpdatedBy(currentUser);
            Squad savedSquad = squadRepo.save(squad);
            recordSquadProjectChange(savedSquad, project, false, oldSquadValue);
        }
        projectRepo.delete(project);
        auditLogService.record(
                AuditOperation.DELETE,
                "PROJECT",
                id,
                oldValue,
                null);
    }

    private Map<String, Object> projectAuditSnapshot(Project project) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("ID", project.getId());
        snapshot.put("Nome", project.getName());
        snapshot.put("Descrição", project.getDescription());
        snapshot.put("Ativo", project.isActive());
        return snapshot;
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
        return snapshot;
    }

    private Map<String, Object> squadProjectSnapshot(Squad squad, String event, Project project) {
        Map<String, Object> snapshot = squadAuditSnapshot(squad);
        snapshot.put("Evento", event);
        snapshot.put("Projeto relacionado", project.getName());
        return snapshot;
    }

    private void recordSquadProjectChange(
            Squad squad,
            Project project,
            boolean linked,
            Map<String, Object> oldValue) {
        Map<String, Object> newValue = squadProjectSnapshot(
                squad,
                linked ? "Vínculo de projeto" : "Remoção de vínculo de projeto",
                project);
        auditLogService.record(
                AuditOperation.UPDATE,
                "SQUAD",
                squad.getId(),
                oldValue,
                newValue);
    }

    private void recordProjectSquadChange(Project project, Squad squad, boolean added) {
        Map<String, Object> oldValue = projectSquadSnapshot(project, squad);
        Map<String, Object> newValue = projectSquadSnapshot(project, squad);
        newValue.put("Evento", added ? "Squad adicionada" : "Squad removida");

        auditLogService.record(
                AuditOperation.UPDATE,
                "PROJECT",
                project.getId(),
                oldValue,
                newValue);
    }

    private Map<String, Object> projectSquadSnapshot(Project project, Squad squad) {
        Map<String, Object> snapshot = projectAuditSnapshot(project);
        snapshot.put("Squad relacionada", squad.getName());
        snapshot.put("ID da Squad relacionada", squad.getId());
        return snapshot;
    }
}
