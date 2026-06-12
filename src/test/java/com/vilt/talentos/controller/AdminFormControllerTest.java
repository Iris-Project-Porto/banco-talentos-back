package com.vilt.talentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.repository.FormDefinitionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminFormController.class)
class AdminFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FormDefinitionRepository repository;

    @Test
    @DisplayName("Deve retornar 201 ao criar formulário válido")
    @WithMockUser
    void criarDefinicaoCenario01() throws Exception {

        String body = """
        {
            "groupId":"550e8400-e29b-41d4-a716-446655440000",
            "title":"Formulário Teste",
            "version":1,
            "elements":{"campo":"valor"},
            "active":true
        }
        """;

        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/admin/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar formulário inválido")
    @WithMockUser
    void criarDefinicaoCenario02() throws Exception {

        String body = """
        {
            "groupId":null,
            "title":"",
            "version":null,
            "elements":{},
            "active":null
        }
        """;

        mockMvc.perform(post("/api/admin/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar formulários")
    @WithMockUser
    void listarFormularios() throws Exception {

        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/admin/forms"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar formulário existente")
    @WithMockUser
    void detalharFormularioExistente() throws Exception {

        UUID id = UUID.randomUUID();

        FormDefinition form = mock(FormDefinition.class);

        when(repository.findById(id))
                .thenReturn(Optional.of(form));

        mockMvc.perform(get("/api/admin/forms/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar formulário inexistente")
    @WithMockUser
    void detalharFormularioInexistente() throws Exception {

        UUID id = UUID.randomUUID();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/forms/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar formulário")
    @WithMockUser
    void atualizarFormulario() throws Exception {

        UUID id = UUID.randomUUID();

        FormDefinition form = mock(FormDefinition.class);

        when(repository.getReferenceById(id))
                .thenReturn(form);

        String body = """
        {
            "id":"%s",
            "title":"Novo Título"
        }
        """.formatted(id);

        mockMvc.perform(put("/api/admin/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(form, times(1))
                .atualizarInformacoes(any());
    }

    @Test
    @DisplayName("Deve remover formulário")
    @WithMockUser
    void removerFormulario() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/forms/{id}", id))
                .andExpect(status().isOk());

        verify(repository).deleteById(id);
    }
}