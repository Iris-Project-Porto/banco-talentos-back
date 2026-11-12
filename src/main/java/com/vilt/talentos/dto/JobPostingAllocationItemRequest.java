package com.vilt.talentos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JobPostingAllocationItemRequest(
    @NotNull(message = "O resourceId é obrigatório")
    UUID resourceId,

    @NotNull(message = "A data de início é obrigatória")
    LocalDate startDate,

    @NotBlank(message = "O papel/função na vaga é obrigatório")
    String role
) {}
