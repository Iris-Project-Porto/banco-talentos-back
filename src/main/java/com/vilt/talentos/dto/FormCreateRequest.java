package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record FormCreateRequest(
        @NotBlank(message = "O id do gruopo é obrigatório")
        UUID groupId,
        @NotBlank(message = "O título do formulário é obrigatório")
        String title,
        @NotBlank(message = "A versão do formulário é obrigatória")
        int version,
        @NotBlank(message = "Os elementos do formulário são obrigatórios")
        String elements,
        @NotBlank(message = "O status de ativação do formulário é obrigatório")
        boolean active) {
}
