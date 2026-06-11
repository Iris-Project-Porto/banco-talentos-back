package com.vilt.talentos.controller;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.ExperienceLevel;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar vagas ativas com sucesso")
    void listActive_Success() throws Exception {
        JobPostingResponse response = new JobPostingResponse(UUID.randomUUID(), "Projeto", UUID.randomUUID(), "Squad", UUID.randomUUID(), ExperienceLevel.SENIOR, "Senior", "Desc", "Reqs", "Recruiter", 4, DomainStatus.ACTIVE, "Notes", Instant.now(), false, true, Instant.now(), null, "admin", null);
        when(jobPostingService.findAllActive(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/admin/job-postings/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].projectName").value("Projeto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve listar vagas inativas com sucesso")
    void listInactive_Success() throws Exception {
        JobPostingResponse response = new JobPostingResponse(UUID.randomUUID(), "Projeto", UUID.randomUUID(), "Squad", UUID.randomUUID(), ExperienceLevel.SENIOR, "Senior", "Desc", "Reqs", "Recruiter", 4, DomainStatus.INACTIVE, "Notes", Instant.now(), false, false, Instant.now(), null, "admin", null);
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
        JobPostingResponse response = new JobPostingResponse(id, "Projeto", UUID.randomUUID(), "Squad", UUID.randomUUID(), ExperienceLevel.SENIOR, "Senior", "Desc", "Reqs", "Recruiter", 4, DomainStatus.ACTIVE, "Notes", Instant.now(), false, true, Instant.now(), null, "admin", null);
        when(jobPostingService.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/job-postings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve criar nova vaga com sucesso")
    void create_Success() throws Exception {
        JobPostingRequest request = new JobPostingRequest(UUID.randomUUID(), UUID.randomUUID(), ExperienceLevel.PLENO, "Desc", "Reqs", "Recruiter", 4, DomainStatus.ACTIVE.name(), "Notes", Instant.now(), false);
        JobPostingResponse response = new JobPostingResponse(UUID.randomUUID(), "Projeto", request.projectId(), "Squad", request.squadId(), request.experienceLevel(), "Pleno", request.description(), request.requirements(), request.recruiter(), request.estimatedAllocationWeeks(), DomainStatus.ACTIVE, request.notes(), request.openingDate(), request.isUrgent(), true, Instant.now(), null, "admin", null);
        
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
        JobPostingRequest request = new JobPostingRequest(UUID.randomUUID(), UUID.randomUUID(), ExperienceLevel.PLENO, "Desc Updated", "Reqs", "Recruiter", 4, DomainStatus.ACTIVE.name(), "Notes", Instant.now(), false);
        JobPostingResponse response = new JobPostingResponse(id, "Projeto", request.projectId(), "Squad", request.squadId(), request.experienceLevel(), "Pleno", request.description(), request.requirements(), request.recruiter(), request.estimatedAllocationWeeks(), DomainStatus.ACTIVE, request.notes(), request.openingDate(), request.isUrgent(), true, Instant.now(), null, "admin", null);

        when(jobPostingService.update(eq(id), any(JobPostingRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/job-postings/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Desc Updated"));
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
}
