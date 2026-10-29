package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.repository.AuditLogRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listForCurrentUser() {
        User currentUser = getCurrentUser();
        List<AuditLog> auditLogs;

        if (currentUser.getRole() == UserRole.ADMIN) {
            auditLogs = auditLogRepository.findAllByOrderByCreatedAtDesc();
        } else {
            auditLogs = profileRepository.findProfileIdByUserId(currentUser.getId())
                    .map(auditLogRepository::findByEntityIdOrderByCreatedAtDesc)
                    .orElseGet(List::of);
        }

        return auditLogs.stream().map(AuditLogResponse::from).toList();
    }

    @Transactional
    public void record(
            AuditOperation operation,
            String entityType,
            UUID entityId,
            Object oldValue,
            Object newValue
    ) {
        AuditLog auditLog = AuditLog.builder()
                .user(getCurrentUser())
                .operation(operation)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .build();

        auditLogRepository.save(auditLog);
    }

    private JsonNode toJson(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Usuário não autenticado");
        }

        try {
            UUID userId = UUID.fromString(authentication.getName());
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
    }
}
