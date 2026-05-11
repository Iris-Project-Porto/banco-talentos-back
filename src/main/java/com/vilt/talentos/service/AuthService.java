package com.vilt.talentos.service;

import com.vilt.talentos.dto.AuthRequest;
import com.vilt.talentos.dto.AuthResponse;
import com.vilt.talentos.dto.RegisterRequest;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.repository.UserRepository;
import com.vilt.talentos.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest req) {
        log.info("Tentando login para: {}", req.email());

        var user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", req.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        log.info("Usuário encontrado: {} | role: {}", user.getEmail(), user.getRole());
        log.info("Hash no banco: {}", user.getPassword());

        boolean matches = passwordEncoder.matches(req.password(), user.getPassword());
        log.info("Senha confere: {}", matches);

        if (!matches) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generate(user.getId().toString(), Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public void register(RegisterRequest request){
        if (!request.email().endsWith("@vilt-group.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail deve ser, obrigatoriamente, do domínio 'vilt-group.com'.");
        }

        if (userRepo.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já em uso.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.RECURSO)
                .build();

        userRepo.save(user);
    }
}
