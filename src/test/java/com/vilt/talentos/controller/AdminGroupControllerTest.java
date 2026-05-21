package com.vilt.talentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.config.SecurityConfig;
import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.security.JwtService;
import com.vilt.talentos.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminGroupController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class AdminGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserRepository userRepository; // Requerido pelo SecurityConfig

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar grupos ativos com sucesso")
    void listActive_Success() throws Exception {
        GroupResponse response = new GroupResponse(
                UUID.randomUUID(), "Grupo 1", "Desc", true, 
                Instant.now(), null, "Admin", null
        );
        when(groupService.findAllActive()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/groups/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Grupo 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar um grupo com sucesso")
    void create_Success() throws Exception {
        GroupRequest request = new GroupRequest("Novo Grupo", "Descrição");
        GroupResponse response = new GroupResponse(
                UUID.randomUUID(), "Novo Grupo", "Descrição", true,
                Instant.now(), null, "Admin", null
        );
        when(groupService.create(any(GroupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Grupo"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 se o usuário não for ADMIN")
    void accessDenied_ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/groups/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve inativar um grupo com sucesso")
    void inactivate_Success() throws Exception {
        mockMvc.perform(patch("/api/admin/groups/{id}/inactivate", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 400 ao tentar criar grupo com nome inválido")
    void create_BadRequest_InvalidName() throws Exception {
        GroupRequest request = new GroupRequest("", "Descrição");

        mockMvc.perform(post("/api/admin/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
