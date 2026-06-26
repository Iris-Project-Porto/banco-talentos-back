package com.vilt.talentos.service;

import com.vilt.talentos.config.AppProperties;
import com.vilt.talentos.dto.PasswordResetRequest;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.exception.BadRequestException;
import com.vilt.talentos.mapper.UserMapper;
import com.vilt.talentos.repository.GroupRepository;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private GroupRepository groupRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private AppProperties appProperties;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void resetPassword_ValidToken_UpdatesPassword() {
        String token = "valid-token";
        String email = "test@vilt-group.com";
        User user = User.builder()
                .email(email)
                .resetToken(token)
                .resetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        PasswordResetRequest req = new PasswordResetRequest(email, token, "new-password");

        when(userRepo.findByResetToken(token)).thenReturn(Optional.of(user));
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        authService.resetPassword(req);

        verify(passwordEncoder).encode("new-password");
        verify(userRepo).save(user);
        assertEquals("encoded-password", user.getPassword());
    }

    @Test
    void resetPassword_ExpiredToken_ThrowsBadRequestException() {
        String token = "expired-token";
        String email = "test@vilt-group.com";
        User user = User.builder()
                .email(email)
                .resetToken(token)
                .resetTokenExpires(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        PasswordResetRequest req = new PasswordResetRequest(email, token, "new-password");

        when(userRepo.findByResetToken(token)).thenReturn(Optional.of(user));
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");

        assertThrows(BadRequestException.class, () -> authService.resetPassword(req));
    }

    @Test
    void resetPassword_InvalidToken_ThrowsBadRequestException() {
        String token = "invalid-token";
        PasswordResetRequest req = new PasswordResetRequest("test@vilt-group.com", token, "new-password");

        when(userRepo.findByResetToken(token)).thenReturn(Optional.empty());
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");

        assertThrows(BadRequestException.class, () -> authService.resetPassword(req));
    }

    @Test
    void validateResetToken_ValidToken_DoesNotThrow() {
        String token = "valid-token";
        String email = "test@vilt-group.com";
        User user = User.builder()
                .email(email)
                .resetToken(token)
                .resetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        when(userRepo.findByResetToken(token)).thenReturn(Optional.of(user));
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");

        authService.validateResetToken(email, token);
    }

    @Test
    void validateResetToken_ExpiredToken_ThrowsBadRequestException() {
        String token = "expired-token";
        String email = "test@vilt-group.com";
        User user = User.builder()
                .email(email)
                .resetToken(token)
                .resetTokenExpires(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(userRepo.findByResetToken(token)).thenReturn(Optional.of(user));
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");

        assertThrows(BadRequestException.class, () -> authService.validateResetToken(email, token));
    }

    @Test
    void validateResetToken_EmailMismatch_ThrowsBadRequestException() {
        String token = "valid-token";
        User user = User.builder()
                .email("owner@vilt-group.com")
                .resetToken(token)
                .resetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        when(userRepo.findByResetToken(token)).thenReturn(Optional.of(user));
        when(appProperties.getAllowedEmailDomain()).thenReturn("vilt-group.com");

        assertThrows(BadRequestException.class, () -> authService.validateResetToken("other@vilt-group.com", token));
    }
}
