package com.vilt.talentos.service;

import com.vilt.talentos.dto.*;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.GroupRepository;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.security.JwtService;
import com.vilt.talentos.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AppProperties appProperties;

    public AuthResponse login(AuthRequest req) {
        log.info("Tentando login para: {}", req.email());
        validateEmailDomain(req.email());

        var user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));

        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "E-mail não verificado. Verifique seu e-mail para continuar.");
        }

        if (user.getStatus() == User.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário pendente de aprovação por um administrador.");
        }

        if (user.getStatus() == User.Status.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo.");
        }

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        String token = jwtService.generate(user.getId().toString(), Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public void register(RegisterRequest request){
        validateEmailDomain(request.email());

        if (userRepo.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já em uso.");
        }

        var group = groupRepo.findById(request.groupId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupo não encontrado."));

        String verificationCode = String.format("%06d", new Random().nextInt(1000000));

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(request.role() == User.Role.ADMIN ? User.Status.PENDING : User.Status.ACTIVE)
                .verificationCode(verificationCode)
                .verificationCodeExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .emailVerified(false)
                .group(group)
                .build();

        userRepo.save(user);
        
        sendVerificationEmail(user, verificationCode);
    }

    public void verifyEmail(VerificationRequest req) {
        validateEmailDomain(req.email());
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já verificado.");
        }

        if (user.getVerificationCode() != null && 
            user.getVerificationCode().equals(req.code()) &&
            user.getVerificationCodeExpiresAt() != null &&
            user.getVerificationCodeExpiresAt().isAfter(Instant.now())) {
            
            user.setEmailVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepo.save(user);

            if (user.getRole() == User.Role.ADMIN && user.getStatus() == User.Status.PENDING) {
                notifyAdmins(user);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de verificação inválido ou expirado.");
        }
    }

    public void resendVerificationCode(String email) {
        validateEmailDomain(email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já verificado.");
        }

        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        userRepo.save(user);

        sendVerificationEmail(user, verificationCode);
    }

    private void sendVerificationEmail(User user, String verificationCode) {
        emailService.sendTemplatedEmail(
            List.of(user.getEmail()), 
            "Banco de Talentos - Verificação de E-mail", 
            "emails/email-verification", 
            Map.of("userName", user.getName(), "code", verificationCode)
        );
    }

    public void forgotPassword(String email) {
        validateEmailDomain(email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado em nossa base de dados."));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpires(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepo.save(user);

        String resetUrl = appProperties.getUrl() + "/reset-password?token=" + token + "&email=" + email;
        emailService.sendTemplatedEmail(
            List.of(user.getEmail()), 
            "Banco de Talentos - Redefinição de Senha", 
            "emails/password-reset", 
            Map.of("userName", user.getName(), "resetUrl", resetUrl)
        );
    }

    public void resetPassword(PasswordResetRequest req) {
        validateEmailDomain(req.email());
        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado em nossa base de dados."));

        if (user.getResetToken() == null || !user.getResetToken().equals(req.token()) || 
            user.getResetTokenExpires().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado.");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        userRepo.save(user);
    }

    private void validateEmailDomain(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail é obrigatório.");
        }
        if (!email.endsWith("@vilt-group.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail deve ser do domínio 'vilt-group.com'.");
        }
    }

    private void notifyAdmins(User newUser) {
        List<User> activeAdmins = userRepo.findAllByRoleAndStatus(User.Role.ADMIN, User.Status.ACTIVE);
        if (activeAdmins.isEmpty()) return;

        List<String> adminEmails = activeAdmins.stream().map(User::getEmail).toList();
        String portalUrl = appProperties.getUrl() + "/admin/usuarios";
        
        emailService.sendTemplatedEmail(
            adminEmails, 
            "Banco de Talentos - Novo Administrador Pendente de Aprovação", 
            "emails/admin-approval-notification", 
            Map.of("userName", newUser.getName(), "userEmail", newUser.getEmail(), "portalUrl", portalUrl)
        );
    }
}
