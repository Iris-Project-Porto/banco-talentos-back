package com.vilt.talentos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "O e-mail é obrigatório.") 
    @Email(message = "E-mail inválido.") 
    String email
) {
}
