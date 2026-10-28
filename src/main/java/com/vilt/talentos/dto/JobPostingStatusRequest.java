package com.vilt.talentos.dto;

import com.vilt.talentos.entity.JobPostingStatus;
import jakarta.validation.constraints.NotNull;

public record JobPostingStatusRequest(
    @NotNull(message = "Status é obrigatório.")
    JobPostingStatus status
) {
}