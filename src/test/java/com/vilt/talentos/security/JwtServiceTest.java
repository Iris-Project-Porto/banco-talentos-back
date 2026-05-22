package com.vilt.talentos.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "minha-chave-secreta-bem-longa-para-hmac-sha256-test";
    private static final long EXPIRATION_MS = 3_600_000L; // 1h

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void generate_deveRetornarTokenNaoNulo() {
        String token = jwtService.generate("user-123", Map.of("role", "RECURSO"));
        assertThat(token).isNotBlank();
    }

    @Test
    void parse_deveRetornarClaimsCorretos() {
        String token = jwtService.generate("user-123", Map.of("role", "ADMIN", "email", "test@vilt-group.com"));

        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user-123");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("email", String.class)).isEqualTo("test@vilt-group.com");
    }

    @Test
    void isValid_deveRetornarTrueParaTokenValido() {
        String token = jwtService.generate("user-123", Map.of());
        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_deveRetornarFalseParaTokenAlterado() {
        String token = jwtService.generate("user-123", Map.of()) + "invalido";
        assertThat(jwtService.isValid(token)).isFalse();
    }

    @Test
    void isValid_deveRetornarFalseParaTokenVazio() {
        assertThat(jwtService.isValid("")).isFalse();
    }

    @Test
    void isValid_deveRetornarFalseParaTokenExpirado() {
        JwtService expiredService = new JwtService();
        ReflectionTestUtils.setField(expiredService, "secret", SECRET);
        ReflectionTestUtils.setField(expiredService, "expirationMs", -1000L); // já expirado

        String token = expiredService.generate("user-123", Map.of());
        assertThat(expiredService.isValid(token)).isFalse();
    }

    @Test
    void parse_deveLancarExcecaoParaTokenInvalido() {
        assertThatThrownBy(() -> jwtService.parse("token.invalido.aqui"))
                .isInstanceOf(Exception.class);
    }
}
