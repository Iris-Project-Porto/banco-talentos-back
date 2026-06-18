package com.vilt.talentos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
    @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
    @NotBlank(message = "O token é obrigatório.") String token,
    @NotBlank(message = "A nova senha é obrigatória.") 
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", 
             message = "A senha deve incluir pelo menos uma letra maiúscula, um número e um caractere especial.")
    String newPassword
) {
}
