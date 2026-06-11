package com.vilt.talentos.dto;

import com.vilt.talentos.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório.") String name,
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
        @NotBlank(message = "A senha é obrigatória.") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.") String password,
        @NotNull(message = "O papel (role) é obrigatório.") UserRole role,
        @NotNull(message = "O grupo é obrigatório.") UUID groupId
) {
}
