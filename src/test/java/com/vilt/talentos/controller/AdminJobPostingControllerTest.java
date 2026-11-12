package com.vilt.talentos.controller;

import com.vilt.talentos.dto.JobPostingAllocationBatchRequest;
import com.vilt.talentos.dto.JobPostingAllocationBatchResponse;
import com.vilt.talentos.dto.JobPostingAllocationItemRequest;
import com.vilt.talentos.dto.JobPostingAllocationResponse;
import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.entity.ExperienceLevel;
import com.vilt.talentos.entity.JobPostingStatus;
import com.vilt.talentos.service.JobPostingAllocationService;
import com.vilt.talentos.service.JobPostingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminJobPostingController.class)
class AdminJobPostingControllerTest extends BaseControllerTest {

    @MockBean
    private JobPostingService jobPostingService;

    @MockBean
    private JobPostingAllocationService jobPostingAllocationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar vagas ativas com sucesso")
    void listActive_Success() throws Exception {
        JobPostingResponse response = createDummyResponse(UUID.randomUUID(), "Projeto", "Squad", true);
        when(jobPostingService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/job-postings/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].projectName").value("Projeto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar vagas inativas com sucesso")
    void listInactive_Success() throws Exception {
        JobPostingResponse response = createDummyResponse(UUID.randomUUID(), "Projeto", "Squad", false);
        when(jobPostingService.findAllInactive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/job-postings/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].projectName").value("Projeto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve buscar vaga por ID com sucesso")
    void getById_Success() throws Exception {
        UUID id = UUID.randomUUID();
        JobPostingResponse response = createDummyResponse(id, "Projeto", "Squad", true);
        when(jobPostingService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/job-postings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar nova vaga com sucesso")
    void create_Success() throws Exception {
        JobPostingRequest request = createDummyRequest(UUID.randomUUID(), UUID.randomUUID());
        JobPostingResponse response = createDummyResponse(UUID.randomUUID(), "Projeto", "Squad", true);
        
        when(jobPostingService.create(any(JobPostingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/job-postings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recruiter").value("Recruiter"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve atualizar vaga com sucesso")
    void update_Success() throws Exception {
        UUID id = UUID.randomUUID();
        JobPostingRequest request = createDummyRequest(UUID.randomUUID(), UUID.randomUUID());
        JobPostingResponse response = createDummyResponse(id, "Projeto", "Squad", true);

        when(jobPostingService.update(eq(id), any(JobPostingRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/job-postings/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Description"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve ativar vaga com sucesso")
    void activate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/admin/job-postings/{id}/activate", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(jobPostingService).setActiveStatus(id, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve desativar vaga com sucesso")
    void deactivate_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/admin/job-postings/{id}/deactivate", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(jobPostingService).setActiveStatus(id, false);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve alocar recurso na vaga com lista allocations e retornar 201 Created")
    void allocate_Success() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(List.of(
                new JobPostingAllocationItemRequest(resourceId, LocalDate.of(2026, 11, 11), "Tech Lead Frontend")
        ));

        JobPostingResponse filledResponse = new JobPostingResponse(
                jobId, "VAC-001", "Title", "Projeto", UUID.randomUUID(), "Squad", UUID.randomUUID(),
                ExperienceLevel.SENIOR, "Senior", "Description", "Recruiter", 4,
                JobPostingStatus.FILLED, "Remote", "Notes", Instant.now(), null, false, true,
                List.of(), Instant.now(), Instant.now(), "admin", "admin"
        );

        JobPostingAllocationResponse allocationResponse = new JobPostingAllocationResponse(
                UUID.randomUUID(), jobId, resourceId, "Diogo", filledResponse.projectId(), "Projeto",
                filledResponse.squadId(), "Squad", LocalDate.of(2026, 11, 11),
                "Tech Lead Frontend", true, Instant.now(), "admin"
        );

        when(jobPostingAllocationService.allocate(eq(jobId), any(JobPostingAllocationBatchRequest.class)))
                .thenReturn(new JobPostingAllocationBatchResponse(filledResponse, List.of(allocationResponse)));

        mockMvc.perform(post("/api/v1/admin/job-postings/{id}/allocations", jobId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobPosting.status").value("FILLED"))
                .andExpect(jsonPath("$.allocations[0].resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.allocations[0].role").value("Tech Lead Frontend"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve alocar recurso em payload plano único e retornar 201 Created")
    void allocate_SingleFlatPayload_Success() throws Exception {
        UUID jobId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(
                resourceId, LocalDate.of(2026, 11, 11), "Dev Frontend"
        );

        JobPostingResponse filledResponse = new JobPostingResponse(
                jobId, "VAC-001", "Title", "Projeto", UUID.randomUUID(), "Squad", UUID.randomUUID(),
                ExperienceLevel.SENIOR, "Senior", "Description", "Recruiter", 4,
                JobPostingStatus.FILLED, "Remote", "Notes", Instant.now(), null, false, true,
                List.of(), Instant.now(), Instant.now(), "admin", "admin"
        );

        JobPostingAllocationResponse allocationResponse = new JobPostingAllocationResponse(
                UUID.randomUUID(), jobId, resourceId, "Diogo", filledResponse.projectId(), "Projeto",
                filledResponse.squadId(), "Squad", LocalDate.of(2026, 11, 11),
                "Dev Frontend", true, Instant.now(), "admin"
        );

        when(jobPostingAllocationService.allocate(eq(jobId), any(JobPostingAllocationBatchRequest.class)))
                .thenReturn(new JobPostingAllocationBatchResponse(filledResponse, List.of(allocationResponse)));

        mockMvc.perform(post("/api/v1/admin/job-postings/{id}/allocations", jobId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobPosting.status").value("FILLED"))
                .andExpect(jsonPath("$.allocations[0].role").value("Dev Frontend"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar 400 Bad Request quando lista de alocações estiver vazia")
    void allocate_EmptyList_ReturnsBadRequest() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobPostingAllocationBatchRequest request = new JobPostingAllocationBatchRequest(List.of());

        mockMvc.perform(post("/api/v1/admin/job-postings/{id}/allocations", jobId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar alocações da vaga com sucesso")
    void listAllocations_Success() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobPostingAllocationResponse allocationResponse = new JobPostingAllocationResponse(
                UUID.randomUUID(), jobId, UUID.randomUUID(), "Diogo", UUID.randomUUID(), "Projeto",
                UUID.randomUUID(), "Squad", LocalDate.of(2026, 11, 11),
                "Tech Lead Frontend", true, Instant.now(), "admin"
        );

        when(jobPostingAllocationService.listByJobPosting(jobId)).thenReturn(List.of(allocationResponse));

        mockMvc.perform(get("/api/v1/admin/job-postings/{id}/allocations", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("Tech Lead Frontend"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    private JobPostingRequest createDummyRequest(UUID projectId, UUID squadId) {
        return new JobPostingRequest(
                "VAC-001",
                "Title",
                projectId,
                squadId,
                ExperienceLevel.PLENO,
                "Description",
                "Recruiter",
                4,
                JobPostingStatus.OPEN.name(),
                "Remote",
                "Notes",
                Instant.now(),
                null,
                false,
                List.of()
        );
    }

    private JobPostingResponse createDummyResponse(UUID id, String projectName, String squadName, boolean active) {
        return new JobPostingResponse(
                id,
                "VAC-001",
                "Title",
                projectName,
                UUID.randomUUID(),
                squadName,
                UUID.randomUUID(),
                ExperienceLevel.SENIOR,
                "Senior",
                "Description",
                "Recruiter",
                4,
                JobPostingStatus.OPEN,
                "Remote",
                "Notes",
                Instant.now(),
                null,
                false,
                active,
                List.of(),
                Instant.now(),
                null,
                "admin",
                null
        );
    }
}
