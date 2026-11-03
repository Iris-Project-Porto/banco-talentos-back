package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.exception.ResourceNotFoundException;
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
import java.util.Collection;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listForCurrentUser() {
        return list(null);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> list(UUID userAffectedId) {
        User currentUser = getCurrentUser();
        List<AuditLog> auditLogs;

        if (currentUser.getRole() == UserRole.ADMIN) {
            auditLogs = userAffectedId == null
                    ? auditLogRepository.findAllByOrderByCreatedAtDesc()
                    : auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(userAffectedId);
        } else {
            auditLogs = auditLogRepository.findByUserAffectedIdOrderByCreatedAtDesc(currentUser.getId());
        }

        return auditLogs.stream().map(AuditLogResponse::summary).toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getById(UUID id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de auditoria não encontrado"));

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != UserRole.ADMIN
                && (auditLog.getUserAffected() == null
                || !currentUser.getId().equals(auditLog.getUserAffected().getId()))) {
            throw new ResourceNotFoundException("Registro de auditoria não encontrado");
        }

        return AuditLogResponse.from(auditLog);
    }

    @Transactional
    public void record(
            AuditOperation operation,
            String entityType,
            UUID entityId,
            Object oldValue,
            Object newValue
    ) {
        record(operation, entityType, entityId, null, oldValue, newValue);
    }

    @Transactional
    public void record(
            AuditOperation operation,
            String entityType,
            UUID entityId,
            UUID userAffectedId,
            Object oldValue,
            Object newValue
    ) {
        AuditLog auditLog = AuditLog.builder()
                .user(getCurrentUser())
                .userAffected(userAffectedId == null ? null : userRepository.getReferenceById(userAffectedId))
                .operation(operation)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .build();

        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void recordForAffectedUsers(
            AuditOperation operation,
            String entityType,
            UUID entityId,
            Collection<UUID> userAffectedIds,
            Object oldValue,
            Object newValue
    ) {
        User actor = getCurrentUser();

        new LinkedHashSet<>(userAffectedIds).stream()
                .filter(java.util.Objects::nonNull)
                .forEach(userAffectedId -> auditLogRepository.save(AuditLog.builder()
                        .user(actor)
                        .userAffected(userRepository.getReferenceById(userAffectedId))
                        .operation(operation)
                        .entityType(entityType)
                        .entityId(entityId)
                        .oldValue(toJson(oldValue))
                        .newValue(toJson(newValue))
                        .build()));
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
