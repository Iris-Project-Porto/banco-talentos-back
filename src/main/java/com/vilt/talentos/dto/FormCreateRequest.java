package com.vilt.talentos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FormCreateRequest(

        @NotNull(message = "O id do grupo é obrigatório")
        UUID groupId,

        @NotBlank(message = "O título do formulário é obrigatório")
        String title,

        @NotNull(message = "A versão do formulário é obrigatória")
        Integer version,

        @NotBlank(message = "Os elementos do formulário são obrigatórios")
        String elements,

        @NotNull(message = "O status é obrigatório")
        Boolean active
) {}
