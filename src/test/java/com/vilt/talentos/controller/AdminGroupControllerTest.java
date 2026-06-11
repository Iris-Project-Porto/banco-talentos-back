package com.vilt.talentos.controller;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminGroupController.class)
class AdminGroupControllerTest extends BaseControllerTest {

    @MockBean
    private GroupService groupService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar grupos ativos com sucesso")
    void listActive_Success() throws Exception {
        GroupResponse response = new GroupResponse(
                UUID.randomUUID(), "Grupo 1", "Desc", true, 
                Instant.now(), null, "Admin", null
        );
        when(groupService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/groups/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Grupo 1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar grupos inativos com sucesso")
    void listInactive_Success() throws Exception {
        GroupResponse response = new GroupResponse(
                UUID.randomUUID(), "Grupo Inativo", "Desc", false,
                Instant.now(), null, "Admin", null
        );
        when(groupService.findAllInactive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/groups/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Grupo Inativo"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve buscar grupo por ID com sucesso")
    void getById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        GroupResponse response = new GroupResponse(
                id, "Grupo 1", "Desc", true,
                Instant.now(), null, "Admin", null
        );
        when(groupService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/groups/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
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

        mockMvc.perform(post("/api/v1/admin/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Novo Grupo"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar um grupo com sucesso")
    void update_Success() throws Exception {
        UUID id = UUID.randomUUID();
        GroupRequest request = new GroupRequest("Nome Atualizado", "Desc Atualizada");
        GroupResponse response = new GroupResponse(
                id, "Nome Atualizado", "Desc Atualizada", true,
                Instant.now(), Instant.now(), "Admin", "Admin"
        );
        when(groupService.update(eq(id), any(GroupRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/groups/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome Atualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve ativar um grupo com sucesso")
    void activate_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/groups/{id}/activate", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar 403 se o usuário não for ADMIN")
    void accessDenied_ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/groups/active"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve inativar um grupo com sucesso")
    void inactivate_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/groups/{id}/inactivate", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 400 ao tentar criar grupo com nome inválido")
    void create_BadRequest_InvalidName() throws Exception {
        GroupRequest request = new GroupRequest("", "Descrição");

        mockMvc.perform(post("/api/v1/admin/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }
}
