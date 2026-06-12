package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionResponse;
import com.vilt.talentos.service.FormSubmissionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FormSubmissionController.class)
class FormSubmissionControllerTest extends BaseControllerTest {

    @MockBean
    private FormSubmissionService service;

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void getFormsByGroup_Authenticated_ReturnsFormsList() throws Exception {
        UUID groupId = UUID.randomUUID();
        FormListResponse form = new FormListResponse(UUID.randomUUID(), groupId, "Title", 1, Map.of(), true);
        
        when(service.getFormsByUserGroup(any(UUID.class))).thenReturn(List.of(form));

        mockMvc.perform(get("/api/v1/forms/my-group"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void create_ValidRequest_ReturnsCreatedSubmission() throws Exception {
        UUID formId = UUID.randomUUID();
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        FormSubmissionRequest req = new FormSubmissionRequest(formId, Map.of("q1", "a1"));
        FormSubmissionResponse res = new FormSubmissionResponse(UUID.randomUUID(), formId, userId, Map.of("q1", "a1"), Instant.now());

        when(service.createSubmission(eq(userId), any(FormSubmissionRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/forms/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formDefinitionId").value(formId.toString()));
    }

    @Test
    @WithMockUser
    void getSubmissionById_ExistingId_ReturnsSubmission() throws Exception {
        UUID id = UUID.randomUUID();
        FormSubmissionResponse res = new FormSubmissionResponse(id, UUID.randomUUID(), UUID.randomUUID(), Map.of(), Instant.now());

        when(service.getSubmissionById(id)).thenReturn(res);

        mockMvc.perform(get("/api/v1/forms/submissions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}
