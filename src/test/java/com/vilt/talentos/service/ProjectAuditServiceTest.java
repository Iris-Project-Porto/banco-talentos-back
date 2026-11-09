package com.vilt.talentos.service;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.mapper.ProjectMapper;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.SquadRepository;
import com.vilt.talentos.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAuditServiceTest {

    @Mock ProjectRepository projectRepo;
    @Mock JobPostingRepository jobPostingRepo;
    @Mock ProfileRepository profileRepo;
    @Mock UserRepository userRepo;
    @Mock SquadRepository squadRepo;
    @Mock ProjectMapper mapper;
    @Mock SquadMapper squadMapper;
    @Mock AuditLogService auditLogService;

    @InjectMocks ProjectService service;

    private UUID userId;

    @BeforeEach
    void authenticate() {
        userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null));
        lenient().when(userRepo.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuditProjectCreate() {
        ProjectRequest request = new ProjectRequest("Projeto A", "Descricao", null, null);
        Project project = Project.builder().name("Projeto A").description("Descricao").active(true).build();
        Project saved = Project.builder().id(UUID.randomUUID()).name("Projeto A").description("Descricao").active(true).build();
        when(mapper.toEntity(request)).thenReturn(project);
        when(projectRepo.save(project)).thenReturn(saved);
        when(projectRepo.findWithSquadsById(saved.getId())).thenReturn(Optional.of(saved));
        when(mapper.toResponse(saved, squadMapper)).thenReturn(anyResponse(saved.getId()));

        service.create(request);

        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.CREATE),
                eq("PROJECT"), eq(saved.getId()), isNull(), any());
    }

    @Test
    void shouldAuditProjectUpdate() {
        UUID id = UUID.randomUUID();
        ProjectRequest request = new ProjectRequest("Projeto B", "Nova descricao", null, null);
        Project project = Project.builder().id(id).name("Projeto A").description("Antiga").active(true).build();
        when(projectRepo.findWithSquadsById(id)).thenReturn(Optional.of(project));
        org.mockito.Mockito.doAnswer(invocation -> {
            project.setName(request.name());
            project.setDescription(request.description());
            return null;
        }).when(mapper).updateEntity(request, project);
        when(projectRepo.save(project)).thenReturn(project);
        when(mapper.toResponse(project, squadMapper)).thenReturn(anyResponse(id));

        service.update(id, request);

        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.UPDATE),
                eq("PROJECT"), eq(id), any(), any());
    }

    @Test
    void shouldAuditProjectDelete() {
        UUID id = UUID.randomUUID();
        Project project = Project.builder().id(id).name("Projeto A").description("Descricao").active(true).build();
        when(projectRepo.findById(id)).thenReturn(Optional.of(project));

        service.deleteProject(id);

        verify(projectRepo).delete(project);
        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.DELETE),
                eq("PROJECT"), eq(id), any(), isNull());
    }

    private ProjectResponse anyResponse(UUID id) {
        return new ProjectResponse(id, "Projeto", "Descricao", true, null, null, null, null, null);
    }
}
