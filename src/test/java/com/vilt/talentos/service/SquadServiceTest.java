package com.vilt.talentos.service;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.dto.SquadResourceInput;
import com.vilt.talentos.dto.SquadResourceResponse;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.Squad;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.SquadMapper;
import com.vilt.talentos.repository.JobPostingAllocationRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.SquadRepository;
import com.vilt.talentos.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SquadServiceTest {

    @Mock
    private SquadRepository squadRepo;

    @Mock
    private ProfileRepository profileRepo;

    @Mock
    private JobPostingRepository jobPostingRepo;

    @Mock
    private JobPostingAllocationRepository allocationRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SquadMapper mapper;

    @InjectMocks
    private SquadService squadService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .name("Admin User")
                .email("admin@vilt.com")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Deve criar squad e associar recursos informados")
    void create_Success_WithRecursos() {
        UUID profile1Id = UUID.randomUUID();
        UUID profile2Id = UUID.randomUUID();
        List<UUID> recursoIds = List.of(profile1Id, profile2Id);

        SquadRequest request = new SquadRequest("Alpha Squad", "Squad descrição", "Coord", "GP", recursoIds);

        UUID squadId = UUID.randomUUID();
        Squad squad = Squad.builder()
                .id(squadId)
                .name(request.name())
                .description(request.description())
                .portoCoordinator(request.portoCoordinator())
                .projectManager(request.projectManager())
                .active(true)
                .build();

        SquadResponse expectedResponse = new SquadResponse(
                squadId, request.name(), request.description(), request.portoCoordinator(),
                request.projectManager(), null, null, true, Instant.now(), null, "Admin User", null
        );

        Profile p1 = Profile.builder().id(profile1Id).build();
        Profile p2 = Profile.builder().id(profile2Id).build();
        List<Profile> profiles = new ArrayList<>(List.of(p1, p2));

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toEntity(request)).thenReturn(squad);
        when(squadRepo.save(any(Squad.class))).thenReturn(squad);
        when(profileRepo.findAllById(recursoIds)).thenReturn(profiles);
        when(mapper.toResponse(squad)).thenReturn(expectedResponse);

        SquadResponse response = squadService.create(request);

        assertNotNull(response);
        assertEquals("Alpha Squad", response.name());

        verify(profileRepo).clearAllocationSquad(squadId);
        verify(profileRepo).findAllById(recursoIds);
        assertEquals(squad, p1.getAllocationSquad());
        assertEquals(squad, p2.getAllocationSquad());
        verify(profileRepo).saveAll(profiles);
    }

    @Test
    @DisplayName("Deve criar squad sem alterar recursos quando recursoIds for nulo")
    void create_Success_WithNullRecursos() {
        SquadRequest request = new SquadRequest("Beta Squad", "Descrição", "Coord", "GP", null);

        UUID squadId = UUID.randomUUID();
        Squad squad = Squad.builder().id(squadId).name("Beta Squad").build();
        SquadResponse expectedResponse = new SquadResponse(
                squadId, "Beta Squad", "Descrição", "Coord", "GP", null, null, true, Instant.now(), null, "Admin User", null
        );

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toEntity(request)).thenReturn(squad);
        when(squadRepo.save(any(Squad.class))).thenReturn(squad);
        when(mapper.toResponse(squad)).thenReturn(expectedResponse);

        SquadResponse response = squadService.create(request);

        assertNotNull(response);
        verify(profileRepo, never()).clearAllocationSquad(any());
        verify(profileRepo, never()).findAllById(any());
        verify(profileRepo, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve desvincular recursos quando recursoIds for lista vazia")
    void create_Success_WithEmptyRecursos() {
        SquadRequest request = new SquadRequest("Gamma Squad", "Descrição", "Coord", "GP", List.of());

        UUID squadId = UUID.randomUUID();
        Squad squad = Squad.builder().id(squadId).name("Gamma Squad").build();
        SquadResponse expectedResponse = new SquadResponse(
                squadId, "Gamma Squad", "Descrição", "Coord", "GP", null, null, true, Instant.now(), null, "Admin User", null
        );

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toEntity(request)).thenReturn(squad);
        when(squadRepo.save(any(Squad.class))).thenReturn(squad);
        when(mapper.toResponse(squad)).thenReturn(expectedResponse);

        SquadResponse response = squadService.create(request);

        assertNotNull(response);
        verify(profileRepo).clearAllocationSquad(squadId);
        verify(profileRepo, never()).findAllById(any());
        verify(profileRepo, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve atualizar squad e sincronizar recursos com sucesso")
    void update_Success_WithRecursos() {
        UUID squadId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        List<UUID> recursoIds = List.of(profileId);

        SquadRequest request = new SquadRequest("Squad Atualizada", "Nova Desc", "Coord 2", "GP 2", recursoIds);

        Squad existingSquad = Squad.builder()
                .id(squadId)
                .name("Squad Antiga")
                .build();

        Profile profile = Profile.builder().id(profileId).build();
        List<Profile> profiles = new ArrayList<>(List.of(profile));

        SquadResponse expectedResponse = new SquadResponse(
                squadId, "Squad Atualizada", "Nova Desc", "Coord 2", "GP 2", null, null, true, Instant.now(), null, "Admin User", null
        );

        when(squadRepo.findById(squadId)).thenReturn(Optional.of(existingSquad));
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(squadRepo.save(any(Squad.class))).thenReturn(existingSquad);
        when(profileRepo.findAllById(recursoIds)).thenReturn(profiles);
        when(mapper.toResponse(existingSquad)).thenReturn(expectedResponse);

        SquadResponse response = squadService.update(squadId, request);

        assertNotNull(response);
        assertEquals("Squad Atualizada", response.name());

        verify(mapper).updateEntity(request, existingSquad);
        verify(profileRepo).clearAllocationSquad(squadId);
        verify(profileRepo).findAllById(recursoIds);
        assertEquals(existingSquad, profile.getAllocationSquad());
        verify(profileRepo).saveAll(profiles);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar squad inexistente")
    void update_NotFound() {
        UUID squadId = UUID.randomUUID();
        SquadRequest request = new SquadRequest("Squad", "Desc", "Coord", "GP", null);

        when(squadRepo.findById(squadId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> squadService.update(squadId, request));
        verify(squadRepo, never()).save(any());
        verify(profileRepo, never()).clearAllocationSquad(any());
    }

    @Test
    @DisplayName("Deve deletar squad e limpar referências em profiles e job_postings")
    void deleteSquad_Success() {
        UUID squadId = UUID.randomUUID();
        Squad squad = Squad.builder().id(squadId).name("Squad Delete").build();

        when(squadRepo.findById(squadId)).thenReturn(Optional.of(squad));

        squadService.deleteSquad(squadId);

        verify(profileRepo).clearAllocationSquad(squadId);
        verify(jobPostingRepo).clearSquad(squadId);
        verify(squadRepo).delete(squad);
    }

    @Test
    @DisplayName("Deve buscar squad por ID e retornar seus recursos vinculados")
    void findById_Success_WithRecursos() {
        UUID squadId = UUID.randomUUID();
        Squad squad = Squad.builder().id(squadId).name("Squad Detalhe").build();

        SquadResponse mappedResponse = new SquadResponse(
                squadId, "Squad Detalhe", "Desc", "Coord", "GP", null, null, true, Instant.now(), null, "Admin", null
        );

        UUID profileId = UUID.randomUUID();
        User profileUser = User.builder().name("Desenvolvedor").email("dev@empresa.com").build();
        Profile profile = Profile.builder()
                .id(profileId)
                .user(profileUser)
                .jobTitle("Tech Lead")
                .photoUrl("https://foto.png")
                .build();

        when(squadRepo.findById(squadId)).thenReturn(Optional.of(squad));
        when(mapper.toResponse(squad)).thenReturn(mappedResponse);
        when(profileRepo.findAllByAllocationSquadId(squadId)).thenReturn(List.of(profile));

        SquadResponse response = squadService.findById(squadId);

        assertNotNull(response);
        assertEquals("Squad Detalhe", response.name());
        assertNotNull(response.recursos());
        assertEquals(1, response.recursos().size());

        SquadResourceResponse recursoResp = response.recursos().get(0);
        assertEquals(profileId, recursoResp.id());
        assertEquals("Desenvolvedor", recursoResp.name());
        assertEquals("dev@empresa.com", recursoResp.email());
        assertEquals("Tech Lead", recursoResp.jobTitle());
        assertEquals("https://foto.png", recursoResp.photoUrl());
    }

    @Test
    @DisplayName("Deve atualizar squad e sincronizar recursos com novo cargo/função")
    void update_Success_WithRecursosAndJobTitle() {
        UUID squadId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        List<SquadResourceInput> recursosInput = List.of(new SquadResourceInput(profileId, "Tech Lead"));

        SquadRequest request = new SquadRequest("Squad Atualizada", "Desc", "Coord", "GP", null, recursosInput);

        Squad existingSquad = Squad.builder().id(squadId).name("Squad Antiga").build();
        SquadResponse expectedResponse = new SquadResponse(
                squadId, "Squad Atualizada", "Desc", "Coord", "GP", null, null, true, Instant.now(), null, "Admin", null
        );

        Profile profile = Profile.builder().id(profileId).jobTitle("Desenvolvedor Junior").build();
        List<Profile> profiles = new ArrayList<>(List.of(profile));

        when(squadRepo.findById(squadId)).thenReturn(Optional.of(existingSquad));
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(squadRepo.save(any(Squad.class))).thenReturn(existingSquad);
        when(profileRepo.findAllById(List.of(profileId))).thenReturn(profiles);
        when(mapper.toResponse(existingSquad)).thenReturn(expectedResponse);

        SquadResponse response = squadService.update(squadId, request);

        assertNotNull(response);
        verify(profileRepo).clearAllocationSquad(squadId);
        verify(profileRepo).findAllById(List.of(profileId));
        assertEquals(existingSquad, profile.getAllocationSquad());
        assertEquals("Tech Lead", profile.getJobTitle());
        verify(profileRepo).saveAll(profiles);
    }
}

