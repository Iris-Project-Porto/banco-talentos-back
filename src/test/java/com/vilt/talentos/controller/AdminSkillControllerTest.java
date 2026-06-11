package com.vilt.talentos.controller;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.entity.SkillType;
import com.vilt.talentos.service.SkillService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminSkillController.class)
class AdminSkillControllerTest extends BaseControllerTest {

    @MockBean
    private SkillService skillService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar skills ativas com sucesso")
    void listActive_Success() throws Exception {
        SkillResponse response = new SkillResponse(UUID.randomUUID(), "JAVA", SkillType.HARD, true, 1);
        when(skillService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/skills/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("JAVA"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar skills inativas com sucesso")
    void listInactive_Success() throws Exception {
        SkillResponse response = new SkillResponse(UUID.randomUUID(), "COBOL", SkillType.HARD, false, 1);
        when(skillService.findAllInactive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/skills/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("COBOL"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve buscar skill por ID com sucesso")
    void getById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        SkillResponse response = new SkillResponse(id, "JAVA", SkillType.HARD, true, 1);
        when(skillService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/skills/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar skill com sucesso")
    void update_Success() throws Exception {
        UUID id = UUID.randomUUID();
        SkillRequest request = new SkillRequest("JAVA 21", SkillType.HARD, 2);
        SkillResponse response = new SkillResponse(id, "JAVA 21", SkillType.HARD, true, 2);

        when(skillService.update(eq(id), any(SkillRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/skills/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JAVA 21"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve ativar skill com sucesso")
    void activate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/admin/skills/{id}/activate", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(skillService).setActiveStatus(id, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve inativar skill com sucesso")
    void inactivate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/admin/skills/{id}/inactivate", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(skillService).setActiveStatus(id, false);
    }
}
