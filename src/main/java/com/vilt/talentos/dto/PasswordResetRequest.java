package com.vilt.talentos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
    @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
    @NotBlank(message = "O token é obrigatório.") String token,
    @NotBlank(message = "A nova senha é obrigatória.") @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.") String newPassword
) {
}
