package com.vilt.talentos.service;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.entity.Squad;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SquadAuditServiceTest {

    @Mock SquadRepository squadRepo;
    @Mock ProfileRepository profileRepo;
    @Mock JobPostingRepository jobPostingRepo;
    @Mock UserRepository userRepo;
    @Mock SquadMapper mapper;
    @Mock AuditLogService auditLogService;

    @InjectMocks SquadService service;

    private UUID userId;

    @BeforeEach
    void authenticate() {
        userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null));
        lenient().when(userRepo.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        SquadResponse mockResponse = mock(SquadResponse.class);
        lenient().when(mockResponse.withRecursos(any())).thenReturn(mockResponse);
        lenient().when(mapper.toResponse(any())).thenReturn(mockResponse);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuditSquadCreate() {
        SquadRequest request = new SquadRequest("Squad A", "Descricao", "Coordenador", "Gerente");
        Squad squad = Squad.builder().name("Squad A").description("Descricao").active(true).build();
        Squad saved = Squad.builder().id(UUID.randomUUID()).name("Squad A").description("Descricao").active(true).build();
        when(mapper.toEntity(request)).thenReturn(squad);
        when(squadRepo.save(squad)).thenReturn(saved);

        service.create(request);

        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.CREATE),
                eq("SQUAD"), eq(saved.getId()), isNull(), any());
    }

    @Test
    void shouldAuditSquadUpdate() {
        UUID id = UUID.randomUUID();
        SquadRequest request = new SquadRequest("Squad B", "Nova descricao", null, null);
        Squad squad = Squad.builder().id(id).name("Squad A").description("Antiga").active(true).build();
        when(squadRepo.findById(id)).thenReturn(Optional.of(squad));
        org.mockito.Mockito.doAnswer(invocation -> {
            squad.setName(request.name());
            squad.setDescription(request.description());
            return null;
        }).when(mapper).updateEntity(request, squad);
        when(squadRepo.save(squad)).thenReturn(squad);

        service.update(id, request);

        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.UPDATE),
                eq("SQUAD"), eq(id), any(), any());
    }

    @Test
    void shouldAuditSquadDelete() {
        UUID id = UUID.randomUUID();
        Squad squad = Squad.builder().id(id).name("Squad A").description("Descricao").active(true).build();
        when(squadRepo.findById(id)).thenReturn(Optional.of(squad));

        service.deleteSquad(id);

        verify(squadRepo).delete(squad);
        verify(auditLogService).record(eq(com.vilt.talentos.entity.AuditOperation.DELETE),
                eq("SQUAD"), eq(id), any(), isNull());
    }
}
