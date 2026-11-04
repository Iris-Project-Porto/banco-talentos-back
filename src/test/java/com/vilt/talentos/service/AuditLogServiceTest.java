package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.repository.AuditLogRepository;
import com.vilt.talentos.repository.ProfileRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogService auditLogService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listForCurrentUser_AdminReturnsAllAuditLogs() {
        User admin = User.builder().id(userId).role(UserRole.ADMIN).build();
        AuditLog auditLog = AuditLog.builder().user(admin).build();
        List<AuditLog> auditLogs = List.of(auditLog);
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(auditLogs);

        List<AuditLogResponse> result = auditLogService.listForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).userId());

        verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
        verify(profileRepository, never()).findProfileIdByUserId(userId);
    }

    @Test
    void listForCurrentUser_ResourceReturnsLogsForOwnProfile() {
        User resource = User.builder().id(userId).role(UserRole.RESOURCE).build();
        UUID auditEntityId = UUID.randomUUID();
        List<AuditLog> auditLogs = List.of(AuditLog.builder().entityId(auditEntityId).build());
        when(userRepository.findById(userId)).thenReturn(Optional.of(resource));
        when(auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(userId)).thenReturn(auditLogs);

        List<AuditLogResponse> result = auditLogService.listForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(auditEntityId, result.get(0).entityId());

        verify(auditLogRepository).findByUserAffectedIdOrderByCreatedAtDesc(userId);
        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc();
        verify(profileRepository, never()).findProfileIdByUserId(userId);
    }

    @Test
    void listForCurrentUser_ResourceWithoutProfileReturnsEmptyList() {
        User resource = User.builder().id(userId).role(UserRole.RESOURCE).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(resource));
        when(auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        assertEquals(List.of(), auditLogService.listForCurrentUser());

        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void list_AdminFiltersByAffectedUser() {
        UUID affectedUserId = UUID.randomUUID();
        User admin = User.builder().id(userId).role(UserRole.ADMIN).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        when(auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(affectedUserId))
                .thenReturn(List.of());

        auditLogService.list(affectedUserId);

        verify(auditLogRepository).findByUserAffectedIdOrderByCreatedAtDesc(affectedUserId);
        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void list_ResourceIgnoresRequestedAffectedUserAndReturnsOwnAudits() {
        UUID otherUserId = UUID.randomUUID();
        User resource = User.builder().id(userId).role(UserRole.RESOURCE).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(resource));
        when(auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        auditLogService.list(otherUserId);

        verify(auditLogRepository).findByUserAffectedIdOrderByCreatedAtDesc(userId);
        verify(auditLogRepository, never())
                .findByUserAffectedIdOrderByCreatedAtDesc(otherUserId);
    }

    @Test
    void recordForAffectedUsersCreatesOneLineForEachUniqueAffectedUser() {
        UUID firstAffectedUserId = UUID.randomUUID();
        UUID secondAffectedUserId = UUID.randomUUID();
        User actor = User.builder().id(userId).role(UserRole.ADMIN).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(actor));
        when(userRepository.getReferenceById(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return User.builder().id(id).build();
        });

        auditLogService.recordForAffectedUsers(
                AuditOperation.UPDATE,
                "PROFILE",
                UUID.randomUUID(),
                List.of(firstAffectedUserId, secondAffectedUserId, firstAffectedUserId),
                Map.of("Nivel", 1),
                Map.of("Nivel", 2));

        verify(auditLogRepository, times(2)).save(any(AuditLog.class));
    }
}
