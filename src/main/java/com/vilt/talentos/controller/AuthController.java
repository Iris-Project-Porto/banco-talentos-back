package com.vilt.talentos.controller;

import com.vilt.talentos.dto.AuthRequest;
import com.vilt.talentos.dto.AuthResponse;
import com.vilt.talentos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação — obtém o JWT para usar nas demais rotas")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Retorna o token JWT. Use-o no botão Authorize acima (Bearer <token>).")
    public AuthResponse login(@Valid @RequestBody AuthRequest req) {
        return authService.login(req);
    }

    @GetMapping("/hash")
    public String hash() {
        return passwordEncoder.encode("senha");
    }
}