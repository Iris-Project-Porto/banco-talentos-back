package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditEntityContext;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.repository.AuditLogRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.ResourceEquipmentRepository;
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
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogEntityNameTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock ProfileRepository profileRepository;
    @Mock UserRepository userRepository;
    @Mock ProjectRepository projectRepository;
    @Mock SquadRepository squadRepository;
    @Mock JobPostingRepository jobPostingRepository;
    @Mock ResourceEquipmentRepository resourceEquipmentRepository;

    @InjectMocks AuditLogService auditLogService;

    private UUID adminId;

    @BeforeEach
    void authenticateAdmin() {
        adminId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminId.toString(), null, AuthorityUtils.NO_AUTHORITIES));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(
                User.builder().id(adminId).name("Administrador").role(UserRole.ADMIN).build()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listAddsNamesForEveryAuditedEntityWithBatchedQueriesAndKeepsOrder() {
        UUID projectId = UUID.randomUUID();
        UUID secondProjectId = UUID.randomUUID();
        UUID squadId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID profileSkillId = UUID.randomUUID();
        UUID equipmentId = UUID.randomUUID();
        User actor = User.builder().id(adminId).name("Administrador").build();
        User affected = User.builder().id(UUID.randomUUID()).name("Ana Recurso").build();

        List<AuditLog> logs = List.of(
                audit(projectId, "PROJECT", actor, affected, 1),
                audit(secondProjectId, "PROJECT", actor, affected, 2),
                audit(squadId, "SQUAD", actor, affected, 3),
                audit(jobId, "JOB_POSTING", actor, affected, 4),
                audit(profileId, "PROFILE", actor, affected, 5),
                audit(profileSkillId, "PROFILE_SKILL", actor, affected, 6),
                audit(equipmentId, "RESOURCE_EQUIPMENT", actor, affected, 7));
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(logs);
        when(projectRepository.findAuditContextsByIdIn(List.of(projectId, secondProjectId)))
                .thenReturn(List.of(
                        new AuditEntityContext(projectId, "Projeto Atlas"),
                        new AuditEntityContext(secondProjectId, "Projeto Boreal")));
        when(squadRepository.findAuditContextsByIdIn(List.of(squadId)))
                .thenReturn(List.of(new AuditEntityContext(squadId, "Squad de Dados")));
        when(jobPostingRepository.findAuditContextsByIdIn(List.of(jobId)))
                .thenReturn(List.of(new AuditEntityContext(jobId, "Engenheira Java")));
        when(profileRepository.findAuditContextsByProfileIdIn(List.of(profileId)))
                .thenReturn(List.of(new AuditEntityContext(profileId, "Ana Recurso")));
        when(profileRepository.findAuditContextsByProfileSkillIdIn(List.of(profileSkillId)))
                .thenReturn(List.of(new AuditEntityContext(profileSkillId, "Java")));
        when(resourceEquipmentRepository.findAuditContextsByIdIn(List.of(equipmentId)))
                .thenReturn(List.of(new AuditEntityContext(equipmentId, "laptop-ana")));

        List<AuditLogResponse> result = auditLogService.list(null);

        assertEquals(List.of(
                "Projeto Atlas", "Projeto Boreal", "Squad de Dados", "Engenheira Java",
                "Ana Recurso", "Java", "laptop-ana"),
                result.stream().map(AuditLogResponse::entityName).toList());
        assertEquals("Administrador", result.get(0).userName());
        assertEquals("Ana Recurso", result.get(0).userAffectedName());
        assertEquals(logs.stream().map(AuditLog::getId).toList(),
                result.stream().map(AuditLogResponse::id).toList());
        result.forEach(response -> {
            assertNull(response.oldValue());
            assertNull(response.newValue());
        });

        verify(projectRepository).findAuditContextsByIdIn(List.of(projectId, secondProjectId));
        verify(squadRepository).findAuditContextsByIdIn(List.of(squadId));
        verify(jobPostingRepository).findAuditContextsByIdIn(List.of(jobId));
        verify(profileRepository).findAuditContextsByProfileIdIn(List.of(profileId));
        verify(profileRepository).findAuditContextsByProfileSkillIdIn(List.of(profileSkillId));
        verify(resourceEquipmentRepository).findAuditContextsByIdIn(List.of(equipmentId));
    }

    @Test
    void listUsesSnapshotOrSafeFallbackWhenEntityIsMissing() {
        UUID removedProjectId = UUID.randomUUID();
        UUID historicalProjectId = UUID.randomUUID();
        AuditLog removed = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityId(removedProjectId)
                .entityType("PROJECT")
                .oldValue(new ObjectMapper().valueToTree(Map.of("Nome", "Projeto removido")))
                .createdAt(Instant.parse("2026-11-09T12:00:00Z"))
                .build();
        AuditLog historical = audit(historicalProjectId, "PROJECT", null, null, 2);
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(removed, historical));
        when(projectRepository.findAuditContextsByIdIn(List.of(removedProjectId, historicalProjectId)))
                .thenReturn(List.of());

        List<AuditLogResponse> result = auditLogService.list(null);

        assertEquals("Projeto removido", result.get(0).entityName());
        assertEquals("Projeto (contexto indisponível)", result.get(1).entityName());
        assertEquals(removed.getId(), result.get(0).id());
        assertEquals(historical.getId(), result.get(1).id());
    }

    @Test
    void listIncludesProjectLinkEventDescription() {
        UUID squadId = UUID.randomUUID();
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityId(squadId)
                .entityType("SQUAD")
                .newValue(new ObjectMapper().valueToTree(Map.of(
                        "Evento", "Vínculo de projeto",
                        "Projeto relacionado", "Projeto Atlas")))
                .build();
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(log));
        when(squadRepository.findAuditContextsByIdIn(List.of(squadId)))
                .thenReturn(List.of(new AuditEntityContext(squadId, "Squad de Dados")));

        List<AuditLogResponse> result = auditLogService.list(null);

        assertEquals("Vínculo de projeto: Projeto Atlas", result.get(0).description());
        assertEquals("Squad de Dados", result.get(0).entityName());
        assertNull(result.get(0).oldValue());
        assertNull(result.get(0).newValue());
    }

    @Test
    void listIncludesProjectSquadChangeDescription() {
        UUID projectId = UUID.randomUUID();
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityId(projectId)
                .entityType("PROJECT")
                .newValue(new ObjectMapper().valueToTree(Map.of(
                        "Evento", "Squad removida",
                        "Squad relacionada", "Squad de Dados")))
                .build();
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(log));
        when(projectRepository.findAuditContextsByIdIn(List.of(projectId)))
                .thenReturn(List.of(new AuditEntityContext(projectId, "Projeto Atlas")));

        List<AuditLogResponse> result = auditLogService.list(null);

        assertEquals("Projeto Atlas", result.get(0).entityName());
        assertEquals("Squad removida: Squad de Dados", result.get(0).description());
        assertNull(result.get(0).oldValue());
        assertNull(result.get(0).newValue());
    }

    private AuditLog audit(UUID entityId, String entityType, User actor, User affected, int second) {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .user(actor)
                .userAffected(affected)
                .entityId(entityId)
                .entityType(entityType)
                .createdAt(Instant.parse("2026-11-09T12:00:00Z").minusSeconds(second))
                .build();
    }
}
