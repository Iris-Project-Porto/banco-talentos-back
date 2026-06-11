package com.vilt.talentos.service;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.entity.Group;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.mapper.GroupMapper;
import com.vilt.talentos.repository.GroupRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private GroupMapper mapper;

    @InjectMocks
    private GroupService groupService;

    private User mockUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@vilt.com")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(userId.toString(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Deve criar um grupo com sucesso")
    void create_Success() {
        GroupRequest request = new GroupRequest("Novos Talentos", "Grupo para novos contratados");
        Group group = Group.builder().name(request.name()).description(request.description()).active(true).build();
        GroupResponse expectedResponse = new GroupResponse(UUID.randomUUID(), request.name(), request.description(), true, Instant.now(), null, "Test User", null);

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(mapper.toEntity(request)).thenReturn(group);
        when(groupRepo.save(any(Group.class))).thenReturn(group);
        when(mapper.toResponse(any(Group.class))).thenReturn(expectedResponse);

        GroupResponse response = groupService.create(request);

        assertNotNull(response);
        assertEquals(request.name(), response.name());
        assertEquals("Test User", response.createdBy());
        assertTrue(response.active());
        verify(groupRepo).save(any(Group.class));
    }

    @Test
    @DisplayName("Deve atualizar um grupo com sucesso")
    void update_Success() {
        UUID groupId = UUID.randomUUID();
        Group existingGroup = Group.builder()
                .id(groupId)
                .name("Antigo")
                .description("Desc antiga")
                .active(true)
                .build();

        GroupRequest updateRequest = new GroupRequest("Novo Nome", "Nova Desc");
        GroupResponse expectedResponse = new GroupResponse(groupId, "Novo Nome", "Nova Desc", true, Instant.now(), Instant.now(), "Admin", "Test User");
        
        when(groupRepo.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(groupRepo.save(any(Group.class))).thenReturn(existingGroup);
        when(mapper.toResponse(any(Group.class))).thenReturn(expectedResponse);

        GroupResponse response = groupService.update(groupId, updateRequest);

        assertEquals("Novo Nome", response.name());
        assertEquals("Nova Desc", response.description());
        assertEquals("Test User", response.updatedBy());
        verify(mapper).updateEntity(eq(updateRequest), eq(existingGroup));
    }

    @Test
    @DisplayName("Deve lançar exceção quando buscar grupo inexistente")
    void findById_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(groupRepo.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.findById(randomId));
    }

    @Test
    @DisplayName("Deve alterar o status de ativo com sucesso")
    void setActiveStatus_Success() {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder().id(groupId).active(true).build();
        
        when(groupRepo.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));

        groupService.setActiveStatus(groupId, false);

        assertFalse(group.isActive());
        verify(groupRepo).save(group);
    }

    @Test
    @DisplayName("Deve lançar 404 ao tentar atualizar grupo inexistente")
    void update_NotFound() {
        UUID groupId = UUID.randomUUID();
        GroupRequest request = new GroupRequest("Novo", "Desc");
        when(groupRepo.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.update(groupId, request));
    }

    @Test
    @DisplayName("Deve lançar 401 se usuário do token não existir no banco")
    void getCurrentUser_UserNotFound() {
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        GroupRequest request = new GroupRequest("Novo", "Desc");

        assertThrows(UnauthorizedException.class, () -> groupService.create(request));
    }
}
