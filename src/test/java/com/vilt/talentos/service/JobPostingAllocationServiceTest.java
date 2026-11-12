package com.vilt.talentos.service;

import com.vilt.talentos.dto.*;
import com.vilt.talentos.entity.*;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.exception.ConflictException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.JobPostingMapper;
import com.vilt.talentos.repository.JobPostingAllocationRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPostingAllocationServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepo;

    @Mock
    private ProfileRepository profileRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private JobPostingAllocationRepository allocationRepo;

    @Mock
    private JobPostingMapper jobPostingMapper;

    @InjectMocks
    private JobPostingAllocationService allocationService;

    private UUID adminUserId;
    private User adminUser;
    private UUID jobPostingId;
    private Project project;
    private Squad squad;
    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        adminUserId = UUID.randomUUID();
        adminUser = User.builder()
                .id(adminUserId)
                .name("Admin Teste")
                .email("admin@vilt.com")
                .role(UserRole.ADMIN)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUserId.toString(), null)
        );

        project = Project.builder().id(UUID.randomUUID()).name("Projeto Iris").build();
        squad = Squad.builder().id(UUID.randomUUID()).name("Squad Conversão").project(project).build();
        jobPostingId = UUID.randomUUID();

        jobPosting = JobPosting.builder()
                .id(jobPostingId)
                .vacancyCode("VAG-001")
                .title("Tech Lead Frontend")
                .project(project)
                .squad(squad)
                .experienceLevel(ExperienceLevel.SENIOR)
                .recruiter("Ana Recrutadora")
                .status(JobPostingStatus.OPEN)
                .openingDate(Instant.now())
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve alocar recurso com lista allocations, vincular à squad/projeto com jobTitle e marcar vaga como FILLED")
    void allocate_Success_WithAllocationsList() {
        UUID profileId = UUID.randomUUID();
        UUID resourceUserId = UUID.randomUUID();
        User resourceUser = User.builder().id(resourceUserId).name("Diogo Carrasco").email("diogo@vilt.com").build();
        Profile profile = Profile.builder()
                .id(profileId)
                .user(resourceUser)
                .status(DomainStatus.ACTIVE)
                .resourceStatus(ResourceStatus.AVAILABLE)
                .allocationStatus("Disponível (Bench)")
                .build();

        LocalDate startDate = LocalDate.of(2026, 11, 11);
        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(List.of(
                new JobPostingAllocationItemRequest(profileId, startDate, "Tech Lead Frontend")
        ));

        JobPostingResponse filledResponse = new JobPostingResponse(
                jobPostingId, "VAG-001", "Tech Lead Frontend", project.getName(), project.getId(),
                squad.getName(), squad.getId(), ExperienceLevel.SENIOR, "Sênior", null,
                "Ana Recrutadora", 8, JobPostingStatus.FILLED, "REMOTO", null,
                jobPosting.getOpeningDate(), null, false, true, List.of(), Instant.now(), Instant.now(), "Admin", "Admin Teste"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(profile));
        when(allocationRepo.existsByProfileIdAndActiveTrue(profileId)).thenReturn(false);
        when(profileRepo.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(allocationRepo.save(any(JobPostingAllocation.class))).thenAnswer(inv -> {
            JobPostingAllocation entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            entity.setCreatedAt(Instant.now());
            return entity;
        });
        when(jobPostingRepo.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobPostingMapper.toResponse(jobPosting)).thenReturn(filledResponse);

        JobPostingAllocationBatchResponse response = allocationService.allocate(jobPostingId, request);

        assertNotNull(response);
        assertEquals(JobPostingStatus.FILLED, jobPosting.getStatus());
        assertEquals(JobPostingStatus.FILLED, response.jobPosting().status());
        assertEquals(1, response.allocations().size());

        JobPostingAllocationResponse itemResp = response.allocations().get(0);
        assertEquals(profileId, itemResp.resourceId());
        assertEquals("Diogo Carrasco", itemResp.resourceName());
        assertEquals(project.getId(), itemResp.projectId());
        assertEquals(squad.getId(), itemResp.squadId());
        assertEquals(startDate, itemResp.startDate());
        assertEquals("Tech Lead Frontend", itemResp.role());
        assertTrue(itemResp.active());

        assertEquals(project, profile.getAllocationProject());
        assertEquals(squad, profile.getAllocationSquad());
        assertEquals("Tech Lead Frontend", profile.getJobTitle());
        assertEquals(startDate, profile.getProjectEntryDate());
        assertEquals("Alocado Integral (100%)", profile.getAllocationStatus());
        assertEquals(ResourceStatus.ALLOCATED, profile.getResourceStatus());
    }

    @Test
    @DisplayName("Deve alocar recurso em payload plano (único candidato) para vaga em SCREENING ou ALLOCATING")
    void allocate_Success_SingleFlatPayloadOnAllocatingVacancy() {
        jobPosting.setStatus(JobPostingStatus.ALLOCATING);
        UUID profileId = UUID.randomUUID();
        User resourceUser = User.builder().id(UUID.randomUUID()).name("Maria Dev").build();
        Profile profile = Profile.builder()
                .id(profileId)
                .user(resourceUser)
                .status(DomainStatus.ACTIVE)
                .resourceStatus(ResourceStatus.AVAILABLE)
                .build();

        LocalDate startDate = LocalDate.of(2026, 11, 18);
        JobPostingAllocationBatchRequest flatRequest = new JobPostingAllocationBatchRequest(
                profileId, startDate, "Dev Frontend"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(profile));
        when(allocationRepo.existsByProfileIdAndActiveTrue(profileId)).thenReturn(false);
        when(profileRepo.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(allocationRepo.save(any(JobPostingAllocation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobPostingRepo.save(any(JobPosting.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobPostingMapper.toResponse(jobPosting)).thenReturn(mock(JobPostingResponse.class));

        JobPostingAllocationBatchResponse response = allocationService.allocate(jobPostingId, flatRequest);

        assertEquals(1, response.allocations().size());
        assertEquals("Alocado Integral (100%)", profile.getAllocationStatus());
        assertEquals(ResourceStatus.ALLOCATED, profile.getResourceStatus());
        assertEquals("Dev Frontend", profile.getJobTitle());
        assertEquals(JobPostingStatus.FILLED, jobPosting.getStatus());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando a vaga já estiver FILLED, CLOSED ou CANCELLED")
    void allocate_ThrowsConflict_WhenJobPostingClosedOrFilled() {
        for (JobPostingStatus blocked : List.of(JobPostingStatus.FILLED, JobPostingStatus.CLOSED, JobPostingStatus.CANCELLED)) {
            jobPosting.setStatus(blocked);
            JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                    UUID.randomUUID(), LocalDate.now(), "Dev"
            );

            when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));

            assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        }
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando a vaga estiver inativa")
    void allocate_ThrowsConflict_WhenJobPostingInactive() {
        jobPosting.setActive(false);
        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                UUID.randomUUID(), LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));

        assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando o mesmo recurso for enviado duplicado na requisição")
    void allocate_ThrowsBadRequest_WhenDuplicateResourceInBatch() {
        UUID sameId = UUID.randomUUID();
        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(List.of(
                new JobPostingAllocationItemRequest(sameId, LocalDate.now(), "Dev 1"),
                new JobPostingAllocationItemRequest(sameId, LocalDate.now(), "Dev 2")
        ));

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));

        assertThrows(BadRequestException.class, () -> allocationService.allocate(jobPostingId, request));
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando o perfil ou usuário estiver inativo")
    void allocate_ThrowsConflict_WhenProfileOrUserInactive() {
        UUID profileId = UUID.randomUUID();
        Profile inactiveProfile = Profile.builder()
                .id(profileId)
                .user(User.builder().name("Inativo").status(DomainStatus.ACTIVE).build())
                .status(DomainStatus.INACTIVE)
                .build();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                profileId, LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(inactiveProfile));

        assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando o recurso já estiver com status ALLOCATED (indisponível)")
    void allocate_ThrowsConflict_WhenResourceUnavailableAllocated() {
        UUID profileId = UUID.randomUUID();
        Profile allocatedProfile = Profile.builder()
                .id(profileId)
                .user(User.builder().name("Ocupado").build())
                .status(DomainStatus.ACTIVE)
                .resourceStatus(ResourceStatus.ALLOCATED)
                .build();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                profileId, LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(allocatedProfile));

        assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando o recurso já possuir uma alocação ativa")
    void allocate_ThrowsConflict_WhenResourceAlreadyHasActiveAllocation() {
        UUID profileId = UUID.randomUUID();
        Profile profile = Profile.builder()
                .id(profileId)
                .user(User.builder().name("João").build())
                .status(DomainStatus.ACTIVE)
                .resourceStatus(ResourceStatus.AVAILABLE)
                .build();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                profileId, LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(profile));
        when(allocationRepo.existsByProfileIdAndActiveTrue(profileId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        verify(allocationRepo, never()).save(any());
        verify(jobPostingRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando o recurso já estiver alocado em outra squad")
    void allocate_ThrowsConflict_WhenResourceAllocatedInAnotherSquad() {
        UUID profileId = UUID.randomUUID();
        Squad otherSquad = Squad.builder().id(UUID.randomUUID()).name("Squad Pagamentos").build();
        Profile profile = Profile.builder()
                .id(profileId)
                .user(User.builder().name("Carlos").build())
                .status(DomainStatus.ACTIVE)
                .resourceStatus(ResourceStatus.AVAILABLE)
                .allocationSquad(otherSquad)
                .build();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                profileId, LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(jobPostingId)).thenReturn(Optional.of(jobPosting));
        when(userRepo.findById(adminUserId)).thenReturn(Optional.of(adminUser));
        when(profileRepo.findById(profileId)).thenReturn(Optional.of(profile));
        when(allocationRepo.existsByProfileIdAndActiveTrue(profileId)).thenReturn(false);

        ConflictException ex = assertThrows(ConflictException.class, () -> allocationService.allocate(jobPostingId, request));
        assertTrue(ex.getMessage().contains("Squad Pagamentos"));
        verify(allocationRepo, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException se a vaga ou recurso não existir")
    void shouldThrowNotFoundWhenJobPostingOrProfileMissing() {
        UUID missingId = UUID.randomUUID();
        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                UUID.randomUUID(), LocalDate.now(), "Dev"
        );

        when(jobPostingRepo.findById(missingId)).thenReturn(Optional.empty());
        when(jobPostingRepo.existsById(missingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> allocationService.allocate(missingId, request));
        assertThrows(ResourceNotFoundException.class, () -> allocationService.listByJobPosting(missingId));
    }
}
