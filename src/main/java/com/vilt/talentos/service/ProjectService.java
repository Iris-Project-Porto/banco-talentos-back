package com.vilt.talentos.service;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.ProjectMapper;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final ProjectMapper mapper;

    public Page<ProjectResponse> findAllActive(Pageable pageable) {
        Page<ProjectResponse> page = projectRepo.findByActive(true, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum projeto ativo encontrado");
        }
        return page;
    }

    public Page<ProjectResponse> findAllInactive(Pageable pageable) {
        Page<ProjectResponse> page = projectRepo.findByActive(false, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum projeto inativo encontrado");
        }
        return page;
    }

    public ProjectResponse findById(UUID id) {
        return projectRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }

    public ProjectResponse create(ProjectRequest request) {
        User currentUser = getCurrentUser();
        Project project = mapper.toEntity(request);
        project.setCreatedBy(currentUser);
        return mapper.toResponse(projectRepo.save(project));
    }

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        
        mapper.updateEntity(request, project);
        project.setUpdatedBy(getCurrentUser());
        
        return mapper.toResponse(projectRepo.save(project));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Project project = projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
        project.setActive(active);
        project.setUpdatedBy(getCurrentUser());
        projectRepo.save(project);
    }

    private User getCurrentUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
    }
}
