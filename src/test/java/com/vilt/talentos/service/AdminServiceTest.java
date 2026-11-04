package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ProfileRepository profileRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private AppProperties appProperties;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void approveUserAuditsApprovalWithoutRecordingValues() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@vilt-group.com")
                .status(DomainStatus.PENDING)
                .build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.getReferenceById(adminId)).thenReturn(User.builder().id(adminId).build());
        when(appProperties.getUrl()).thenReturn("https://app.example.com");

        adminService.approveUser(userId, adminId);

        verify(auditLogService).recordUserChange(
                AuditOperation.UPDATE,
                userId,
                null,
                Map.of("Aprovação", "alterada"));
    }

    @Test
    void rejectUserAuditsStatusChangeWithoutRecordingValues() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .status(DomainStatus.PENDING)
                .build();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        adminService.rejectUser(userId);

        verify(auditLogService).recordUserChange(
                AuditOperation.UPDATE,
                userId,
                null,
                Map.of("Status", "alterado"));
    }
}
