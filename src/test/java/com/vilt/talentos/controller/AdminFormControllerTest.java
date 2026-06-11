package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormDefinitionResponse;
import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormUpdateRequest;
import com.vilt.talentos.service.FormService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFormController.class)
class AdminFormControllerTest extends BaseControllerTest {

    @MockBean
    private FormService formService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar formulário com sucesso")
    void create_Success() throws Exception {
        FormCreateRequest request = new FormCreateRequest(UUID.randomUUID(), "Título", 1, Map.of("q1", "text"), true);
        FormDefinitionResponse response = new FormDefinitionResponse(UUID.randomUUID(), request.elements(), true, request.title(), request.groupId());
        
        when(formService.create(any(FormCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/forms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Título"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar formulários com sucesso")
    void getFormList_Success() throws Exception {
        FormListResponse form = new FormListResponse(UUID.randomUUID(), UUID.randomUUID(), "Título", 1, Map.of(), true);
        Page<FormListResponse> page = new PageImpl<>(List.of(form));
        
        when(formService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Título"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar formulário com sucesso")
    void updateForm_Success() throws Exception {
        FormUpdateRequest request = new FormUpdateRequest(UUID.randomUUID(), UUID.randomUUID(), "Novo Título", 2, Map.of(), true);

        mockMvc.perform(put("/api/v1/admin/forms")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk());

        verify(formService).update(any(FormUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve detalhar formulário com sucesso")
    void detailForm_Success() throws Exception {
        UUID id = UUID.randomUUID();
        FormListResponse response = new FormListResponse(id, UUID.randomUUID(), "Título", 1, Map.of(), true);
        
        when(formService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/forms/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve remover formulário com sucesso")
    void deleteForm_Success() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/admin/forms/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(formService).delete(id);
    }
}
