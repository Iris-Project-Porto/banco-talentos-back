package com.vilt.talentos.controller;

import com.vilt.talentos.dto.JobPostingCandidateMatchResponse;
import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/job-postings")
@RequiredArgsConstructor
@Tag(name = "Admin Job Posting", description = "Gerenciamento de vagas — requer role ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AdminJobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping("/active")
    @Operation(summary = "Listar vagas ativas")
    public Page<JobPostingResponse> listActive(@PageableDefault(size = 20) Pageable pageable) {
        return jobPostingService.findAllActive(pageable);
    }

    @GetMapping("/inactive")
    @Operation(summary = "Listar vagas inativas")
    public Page<JobPostingResponse> listInactive(@PageableDefault(size = 20) Pageable pageable) {
        return jobPostingService.findAllInactive(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter vaga por ID")
    public JobPostingResponse getById(@PathVariable UUID id) {
        return jobPostingService.findById(id);
    }

    @GetMapping("/{id}/matches")
    @Operation(summary = "Ranking de candidatos compatíveis (Match)", description = "Calcula a compatibilidade dos colaboradores com a vaga e retorna o ranking com score, skills e resumo.")
    public Page<JobPostingCandidateMatchResponse> getCandidateMatches(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return jobPostingService.findCandidatesForJobPosting(id, minScore, availability, search, pageable);
    }

    @PostMapping
    @Operation(summary = "Criar nova vaga")
    @ResponseStatus(HttpStatus.CREATED)
    public JobPostingResponse create(@RequestBody @Valid JobPostingRequest request) {
        return jobPostingService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar vaga existente")
    public JobPostingResponse update(@PathVariable UUID id, @RequestBody @Valid JobPostingRequest request) {
        return jobPostingService.update(id, request);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ativar vaga")
    public void activate(@PathVariable UUID id) {
        jobPostingService.setActiveStatus(id, true);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desativar vaga")
    public void deactivate(@PathVariable UUID id) {
        jobPostingService.setActiveStatus(id, false);
    }

    @DeleteMapping("cleaning_delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Excluir vaga")
    public void delete(@PathVariable UUID id) {
        jobPostingService.deleteJobPosting(id);
    }
}
