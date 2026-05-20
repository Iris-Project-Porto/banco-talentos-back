package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record FormUpdateRequest(@NotBlank(message = "O id do gruopo é obrigatório")
                                UUID id,
                                UUID groupId,
                                String title,
                                Integer version,
                                String elements,
                                Boolean active) {
}
