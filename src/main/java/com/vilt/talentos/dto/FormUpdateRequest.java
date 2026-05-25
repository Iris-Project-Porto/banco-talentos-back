package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record FormUpdateRequest(@NotNull(message = "O id do grupo é obrigatório")
                                UUID id,
                                UUID groupId,
                                String title,
                                Integer version,
                                Map<String, Object> elements,
                                Boolean active) {
}
