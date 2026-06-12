package com.vilt.talentos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.entity.FormSubmission;
import com.vilt.talentos.entity.Group;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.FormDefinitionRepository;
import com.vilt.talentos.repository.FormSubmissionRepository;
import com.vilt.talentos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FormSubmissionController.class)
class FormSubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FormSubmissionRepository formSubmissionRepository;

    @MockBean
    private FormDefinitionRepository formDefinitionRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve retornar formulários do grupo do usuário")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getFormsByGroupCenario01() throws Exception {

        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        UUID groupId = UUID.randomUUID();

        Group group = mock(Group.class);
        when(group.getId()).thenReturn(groupId);

        User user = mock(User.class);
        when(user.getGroup()).thenReturn(group);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(formDefinitionRepository.findAllByGroupId(groupId))
                .thenReturn(List.of(mock(FormDefinition.class)));

        mockMvc.perform(get("/api/forms/my-group"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 404 quando usuário não existir")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void getFormsByGroupCenario02() throws Exception {

        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forms/my-group"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve criar submissão com sucesso")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void createSubmissionCenario01() throws Exception {

        UUID formId = UUID.randomUUID();

        String body = """
            {
              "formDefinitionId":"%s",
              "answers":{
                "nome":"Caio",
                "cargo":"Backend"
              }
            }
            """.formatted(formId);

        when(formDefinitionRepository.findById(formId))
                .thenReturn(Optional.of(mock(FormDefinition.class)));

        when(formSubmissionRepository.save(any(FormSubmission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/forms/submissions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 404 quando formulário não existir")
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void createSubmissionCenario02() throws Exception {

        UUID formId = UUID.randomUUID();

        String body = """
            {
              "formDefinitionId":"%s",
              "answers":{
                "nome":"Caio"
              }
            }
            """.formatted(formId);

        when(formDefinitionRepository.findById(formId))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/forms/submissions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar submissão existente")
    @WithMockUser
    void getSubmissionByIdCenario01() throws Exception {

        UUID submissionId = UUID.randomUUID();

        FormSubmission submission = mock(FormSubmission.class);

        when(formSubmissionRepository.findById(submissionId))
                .thenReturn(Optional.of(submission));

        mockMvc.perform(get("/api/forms/submissions/{id}", submissionId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 404 para submissão inexistente")
    @WithMockUser
    void getSubmissionByIdCenario02() throws Exception {

        UUID submissionId = UUID.randomUUID();

        when(formSubmissionRepository.findById(submissionId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forms/submissions/{id}", submissionId))
                .andExpect(status().isNotFound());
    }
}