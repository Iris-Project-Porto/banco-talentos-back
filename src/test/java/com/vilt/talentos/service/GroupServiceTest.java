package com.vilt.talentos.service;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.entity.Group;
import com.vilt.talentos.entity.User;
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
import org.springframework.web.server.ResponseStatusException;

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
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(groupRepo.save(any(Group.class))).thenAnswer(i -> {
            Group g = i.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

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
        
        when(groupRepo.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(groupRepo.save(any(Group.class))).thenReturn(existingGroup);

        GroupResponse response = groupService.update(groupId, updateRequest);

        assertEquals("Novo Nome", response.name());
        assertEquals("Nova Desc", response.description());
        assertEquals("Test User", response.updatedBy());
    }

    @Test
    @DisplayName("Deve lançar exceção quando buscar grupo inexistente")
    void findById_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(groupRepo.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> groupService.findById(randomId));
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

        assertThrows(ResponseStatusException.class, () -> groupService.update(groupId, request));
    }

    @Test
    @DisplayName("Deve lançar 401 se usuário do token não existir no banco")
    void getCurrentUser_UserNotFound() {
        when(userRepo.findById(userId)).thenReturn(Optional.empty());
        GroupRequest request = new GroupRequest("Novo", "Desc");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> groupService.create(request));
        assertEquals(401, ex.getStatusCode().value());
    }
}
