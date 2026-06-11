package com.vilt.talentos.controller;

import com.vilt.talentos.dto.AdminUpdateRequest;
import com.vilt.talentos.dto.DashboardKpisResponse;
import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.service.AdminService;
import com.vilt.talentos.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest extends BaseControllerTest {

    @MockBean
    private ProfileService profileService;

    @MockBean
    private AdminService adminService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar todos os perfis com sucesso")
    void all_Success() throws Exception {
        Profile profile = Profile.builder().id(UUID.randomUUID()).status(DomainStatus.ACTIVE).build();
        when(profileService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(profile)));

        mockMvc.perform(get("/api/v1/admin/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar perfis pendentes com sucesso")
    void pendentes_Success() throws Exception {
        Profile profile = Profile.builder().id(UUID.randomUUID()).status(DomainStatus.PENDING).build();
        when(profileService.getByStatus(eq(DomainStatus.PENDING), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(profile)));

        mockMvc.perform(get("/api/v1/admin/profiles/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve buscar perfil por id com sucesso")
    void getById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        Profile profile = Profile.builder().id(id).status(DomainStatus.ACTIVE).build();
        when(profileService.getById(id)).thenReturn(profile);

        mockMvc.perform(get("/api/v1/admin/profiles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar perfil com sucesso")
    void update_Success() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUpdateRequest req = new AdminUpdateRequest("ACTIVE", "SENIOR", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Profile updatedProfile = Profile.builder().id(id).status(DomainStatus.ACTIVE).levelOverride("SENIOR").build();
        
        when(profileService.adminUpdate(eq(id), any(AdminUpdateRequest.class))).thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/v1/admin/profiles/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.levelOverride").value("SENIOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve obter KPIs do dashboard com sucesso")
    void dashboard_Success() throws Exception {
        DashboardKpisResponse response = new DashboardKpisResponse(
                10, 5L, 5L, List.of(), List.of(), java.util.Map.of());
        when(adminService.getDashboardKpis()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar usuários pendentes com sucesso")
    void getPendingUsers_Success() throws Exception {
        User user = User.builder().id(UUID.randomUUID()).name("User Test").status(DomainStatus.PENDING).build();
        when(adminService.getPendingUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("User Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "00000000-0000-0000-0000-000000000001")
    @DisplayName("Deve aprovar usuário com sucesso")
    void approveUser_Success() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(post("/api/v1/admin/users/{id}/approve", id)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(adminService).approveUser(eq(id), any(UUID.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve rejeitar usuário com sucesso")
    void rejectUser_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/admin/users/{id}/reject", id)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(adminService).rejectUser(id);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve negar acesso para role não-ADMIN")
    void accessDenied_NonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/profiles"))
                .andExpect(status().isForbidden());
    }
}
