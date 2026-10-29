package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        UUID profileId = UUID.randomUUID();
        User resource = User.builder().id(userId).role(UserRole.RESOURCE).build();
        List<AuditLog> auditLogs = List.of(AuditLog.builder().entityId(profileId).build());
        when(userRepository.findById(userId)).thenReturn(Optional.of(resource));
        when(profileRepository.findProfileIdByUserId(userId)).thenReturn(Optional.of(profileId));
        when(auditLogRepository.findByEntityIdOrderByCreatedAtDesc(profileId)).thenReturn(auditLogs);

        List<AuditLogResponse> result = auditLogService.listForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(profileId, result.get(0).entityId());

        verify(auditLogRepository).findByEntityIdOrderByCreatedAtDesc(profileId);
        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void listForCurrentUser_ResourceWithoutProfileReturnsEmptyList() {
        User resource = User.builder().id(userId).role(UserRole.RESOURCE).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(resource));
        when(profileRepository.findProfileIdByUserId(userId)).thenReturn(Optional.empty());

        assertEquals(List.of(), auditLogService.listForCurrentUser());

        verify(auditLogRepository, never()).findAllByOrderByCreatedAtDesc();
    }
}
