package com.vilt.talentos.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        log.debug("JWT Filter — {} {} | Authorization header: {}", request.getMethod(), request.getRequestURI(),
                header != null ? header.substring(0, Math.min(30, header.length())) + "..." : "null");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            boolean valid = jwtService.isValid(token);
            log.debug("Token valid: {}", valid);
            if (valid) {
                Claims claims = jwtService.parse(token);
                String role = claims.get("role", String.class);
                log.debug("Claims — sub={} role={}", claims.getSubject(), role);
                var auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authentication set: {}", auth.getAuthorities());
            }
        }
        chain.doFilter(request, response);
    }
}
