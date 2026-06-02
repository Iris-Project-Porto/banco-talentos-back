package com.vilt.talentos.service;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.ProjectRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public List<ProjectResponse> findAllActive() {
        List<ProjectResponse> list = projectRepo.findByActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum projeto ativo encontrado");
        }
        return list;
    }

    public List<ProjectResponse> findAllInactive() {
        List<ProjectResponse> list = projectRepo.findByActive(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum projeto inativo encontrado");
        }
        return list;
    }

    public ProjectResponse findById(UUID id) {
        return projectRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
    }

    public ProjectResponse create(ProjectRequest request) {
        User currentUser = getCurrentUser();
        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .active(true)
                .createdBy(currentUser)
                .build();
        return mapToResponse(projectRepo.save(project));
    }

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        
        project.setName(request.name());
        project.setDescription(request.description());
        project.setUpdatedBy(getCurrentUser());
        
        return mapToResponse(projectRepo.save(project));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
        project.setActive(active);
        project.setUpdatedBy(getCurrentUser());
        projectRepo.save(project);
    }

    private ProjectResponse mapToResponse(Project p) {
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getCreatedBy() != null ? p.getCreatedBy().getName() : "Sistema",
                p.getUpdatedBy() != null ? p.getUpdatedBy().getName() : null
        );
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }
}
