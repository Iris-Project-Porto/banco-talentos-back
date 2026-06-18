package com.vilt.talentos.dto;

import com.vilt.talentos.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "O nome é obrigatório.") String name,
        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") String email,
        @NotBlank(message = "A senha é obrigatória.") 
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$", 
                 message = "A senha deve incluir pelo menos uma letra maiúscula, um número e um caractere especial.")
        String password,
        @NotNull(message = "O papel (role) é obrigatório.") UserRole role,
        @NotNull(message = "O grupo é obrigatório.") UUID groupId
) {
}
