package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.*;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .name("João Delgado")
                .email("joao@vilt-group.com")
                .password("hashed-password")
                .role(User.Role.RECURSO)
                .status(User.Status.ACTIVE)
                .emailVerified(true)
                .build();
    }

    // ─── login ───────────────────────────────────────────────────────────────

    @Test
    void login_comCredenciaisValidas_deveRetornarToken() {
        when(userRepo.findByEmail("joao@vilt-group.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("senha123", "hashed-password")).thenReturn(true);
        when(jwtService.generate(anyString(), anyMap())).thenReturn("jwt-token");

        AuthResponse resp = authService.login(new AuthRequest("joao@vilt-group.com", "senha123"));

        assertThat(resp.token()).isEqualTo("jwt-token");
        assertThat(resp.email()).isEqualTo("joao@vilt-group.com");
        assertThat(resp.role()).isEqualTo("RECURSO");
    }

    @Test
    void login_usuarioNaoEncontrado_deveLancar401() {
        when(userRepo.findByEmail("nao@vilt-group.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthRequest("nao@vilt-group.com", "senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_emailNaoVerificado_deveLancar403() {
        activeUser.setEmailVerified(false);
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(new AuthRequest(activeUser.getEmail(), "senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_usuarioPendente_deveLancar403() {
        activeUser.setStatus(User.Status.PENDING);
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(new AuthRequest(activeUser.getEmail(), "senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_usuarioInativo_deveLancar403() {
        activeUser.setStatus(User.Status.INACTIVE);
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(new AuthRequest(activeUser.getEmail(), "senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void login_senhaErrada_deveLancar401() {
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("senha-errada", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthRequest(activeUser.getEmail(), "senha-errada")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    void register_emailDominioErrado_deveLancar400() {
        var req = new RegisterRequest("João", "joao@gmail.com", "senha123", User.Role.RECURSO);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(userRepo, never()).save(any());
    }

    @Test
    void register_emailJaEmUso_deveLancar400() {
        var req = new RegisterRequest("João", "joao@vilt-group.com", "senha123", User.Role.RECURSO);
        when(userRepo.findByEmail("joao@vilt-group.com")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_recursoValido_deveSalvarComStatusActive() {
        var req = new RegisterRequest("Maria", "maria@vilt-group.com", "senha123", User.Role.RECURSO);
        when(userRepo.findByEmail("maria@vilt-group.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hash-maria");

        authService.register(req);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(User.Status.ACTIVE);
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.getPassword()).isEqualTo("hash-maria");
        verify(emailService).sendTemplatedEmail(anyList(), anyString(), anyString(), anyMap());
    }

    @Test
    void register_adminValido_deveSalvarComStatusPending() {
        var req = new RegisterRequest("Admin", "admin@vilt-group.com", "senha123", User.Role.ADMIN);
        when(userRepo.findByEmail("admin@vilt-group.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash-admin");

        authService.register(req);

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(User.Status.PENDING);
    }

    // ─── verifyEmail ─────────────────────────────────────────────────────────

    @Test
    void verifyEmail_codigoCorreto_deveAtivarEmailVerified() {
        activeUser.setEmailVerified(false);
        activeUser.setVerificationCode("123456");
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        authService.verifyEmail(new VerificationRequest(activeUser.getEmail(), "123456"));

        assertThat(activeUser.isEmailVerified()).isTrue();
        assertThat(activeUser.getVerificationCode()).isNull();
        verify(userRepo).save(activeUser);
    }

    @Test
    void verifyEmail_codigoErrado_deveLancar400() {
        activeUser.setVerificationCode("123456");
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.verifyEmail(new VerificationRequest(activeUser.getEmail(), "999999")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void verifyEmail_adminVerificado_deveNotificarAdminsAtivos() {
        User adminPendente = User.builder()
                .id(UUID.randomUUID())
                .name("Novo Admin")
                .email("novoadmin@vilt-group.com")
                .password("hash")
                .role(User.Role.ADMIN)
                .status(User.Status.PENDING)
                .emailVerified(false)
                .verificationCode("654321")
                .build();

        User adminAtivo = User.builder()
                .id(UUID.randomUUID())
                .name("Admin Ativo")
                .email("ativo@vilt-group.com")
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .build();

        when(userRepo.findByEmail("novoadmin@vilt-group.com")).thenReturn(Optional.of(adminPendente));
        when(userRepo.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ACTIVE)).thenReturn(List.of(adminAtivo));
        when(appProperties.getUrl()).thenReturn("https://app.vilt.com");

        authService.verifyEmail(new VerificationRequest("novoadmin@vilt-group.com", "654321"));

        verify(emailService, times(1)).sendTemplatedEmail(anyList(), anyString(), anyString(), anyMap());
    }

    @Test
    void verifyEmail_usuarioNaoEncontrado_deveLancar404() {
        when(userRepo.findByEmail("nao@vilt-group.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail(new VerificationRequest("nao@vilt-group.com", "000000")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── forgotPassword ──────────────────────────────────────────────────────

    @Test
    void forgotPassword_usuarioNaoExiste_deveRetornarSilenciosamente() {
        when(userRepo.findByEmail("fantasma@vilt-group.com")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.forgotPassword("fantasma@vilt-group.com"))
                .doesNotThrowAnyException();

        verify(emailService, never()).sendTemplatedEmail(anyList(), anyString(), anyString(), anyMap());
    }

    @Test
    void forgotPassword_usuarioExiste_deveSalvarTokenEEnviarEmail() {
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(appProperties.getUrl()).thenReturn("https://app.vilt.com");

        authService.forgotPassword(activeUser.getEmail());

        assertThat(activeUser.getResetToken()).isNotBlank();
        assertThat(activeUser.getResetTokenExpires()).isAfter(Instant.now());
        verify(userRepo).save(activeUser);
        verify(emailService).sendTemplatedEmail(anyList(), anyString(), anyString(), anyMap());
    }

    // ─── resetPassword ───────────────────────────────────────────────────────

    @Test
    void resetPassword_tokenValido_deveAtualizarSenha() {
        activeUser.setResetToken("token-valido");
        activeUser.setResetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS));
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("nova-senha")).thenReturn("nova-hash");

        authService.resetPassword(new PasswordResetRequest(activeUser.getEmail(), "token-valido", "nova-senha"));

        assertThat(activeUser.getPassword()).isEqualTo("nova-hash");
        assertThat(activeUser.getResetToken()).isNull();
        assertThat(activeUser.getResetTokenExpires()).isNull();
        verify(userRepo).save(activeUser);
    }

    @Test
    void resetPassword_tokenExpirado_deveLancar400() {
        activeUser.setResetToken("token-expirado");
        activeUser.setResetTokenExpires(Instant.now().minus(1, ChronoUnit.HOURS));
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.resetPassword(
                new PasswordResetRequest(activeUser.getEmail(), "token-expirado", "nova-senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resetPassword_tokenErrado_deveLancar400() {
        activeUser.setResetToken("token-correto");
        activeUser.setResetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS));
        when(userRepo.findByEmail(activeUser.getEmail())).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.resetPassword(
                new PasswordResetRequest(activeUser.getEmail(), "token-errado", "nova-senha")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
