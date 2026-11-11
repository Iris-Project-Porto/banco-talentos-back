package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.AuditEntityContext;
import com.vilt.talentos.dto.AuditLogResponse;
import com.vilt.talentos.entity.AuditLog;
import com.vilt.talentos.entity.AuditOperation;
import com.vilt.talentos.entity.User;
import com.vilt.talentos.entity.UserRole;
import com.vilt.talentos.exception.UnauthorizedException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.repository.AuditLogRepository;
import com.vilt.talentos.repository.JobPostingRepository;
import com.vilt.talentos.repository.ProfileRepository;
import com.vilt.talentos.repository.ProjectRepository;
import com.vilt.talentos.repository.ResourceEquipmentRepository;
import com.vilt.talentos.repository.SquadRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Map<String, List<String>> SNAPSHOT_ENTITY_NAME_KEYS = Map.of(
            "PROJECT", List.of("Nome"),
            "SQUAD", List.of("Nome"),
            "JOB_POSTING", List.of("Título", "Código da vaga"),
            "PROFILE_SKILL", List.of("Skill"),
            "RESOURCE_EQUIPMENT", List.of(
                    "Nome do dispositivo",
                    "Número do patrimônio",
                    "Etiqueta",
                    "Marca/Sistema operacional",
                    "Processador"));

    private final AuditLogRepository auditLogRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final SquadRepository squadRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResourceEquipmentRepository resourceEquipmentRepository;

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

        Map<String, Map<UUID, AuditEntityContext>> entityContexts = loadEntityContexts(auditLogs);
        return auditLogs.stream()
                .map(auditLog -> AuditLogResponse.summary(
                        auditLog,
                        resolveEntityName(auditLog, entityContexts),
                        eventDescription(auditLog)))
                .toList();
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

        Map<String, Map<UUID, AuditEntityContext>> entityContexts = loadEntityContexts(List.of(auditLog));
        return AuditLogResponse.from(
                auditLog,
                resolveEntityName(auditLog, entityContexts),
                eventDescription(auditLog));
    }

    private String eventDescription(AuditLog auditLog) {
        String event = snapshotValue(auditLog.getOldValue(), auditLog.getNewValue(), "Evento");
        if (isBlank(event)) {
            return null;
        }

        String relatedEntity = "PROJECT".equals(auditLog.getEntityType())
                ? snapshotValue(auditLog.getOldValue(), auditLog.getNewValue(), "Squad relacionada")
                : snapshotValue(auditLog.getOldValue(), auditLog.getNewValue(), "Projeto relacionado");
        return !isBlank(relatedEntity)
                ? event + ": " + relatedEntity
                : event;
    }

    private Map<String, Map<UUID, AuditEntityContext>> loadEntityContexts(List<AuditLog> auditLogs) {
        Map<String, Map<UUID, AuditEntityContext>> contexts = new LinkedHashMap<>();
        Map<String, Set<UUID>> idsByType = new LinkedHashMap<>();
        for (AuditLog auditLog : auditLogs) {
            if (auditLog.getEntityType() == null || auditLog.getEntityId() == null) {
                continue;
            }

            idsByType.computeIfAbsent(auditLog.getEntityType(), type -> new LinkedHashSet<>())
                    .add(auditLog.getEntityId());
        }

        for (Map.Entry<String, Set<UUID>> entry : idsByType.entrySet()) {
            List<AuditEntityContext> entityContexts = findEntityContexts(
                    entry.getKey(), List.copyOf(entry.getValue()));
            if (entityContexts.isEmpty()) {
                continue;
            }

            Map<UUID, AuditEntityContext> contextsById = new LinkedHashMap<>();
            for (AuditEntityContext context : entityContexts) {
                contextsById.putIfAbsent(context.entityId(), context);
            }
            contexts.put(entry.getKey(), contextsById);
        }

        return contexts;
    }

    private List<AuditEntityContext> findEntityContexts(String entityType, Collection<UUID> ids) {
        return switch (entityType) {
            case "PROJECT" -> projectRepository.findAuditContextsByIdIn(ids);
            case "SQUAD" -> squadRepository.findAuditContextsByIdIn(ids);
            case "JOB_POSTING" -> jobPostingRepository.findAuditContextsByIdIn(ids);
            case "PROFILE" -> profileRepository.findAuditContextsByProfileIdIn(ids);
            case "PROFILE_SKILL" -> profileRepository.findAuditContextsByProfileSkillIdIn(ids);
            case "RESOURCE_EQUIPMENT" -> resourceEquipmentRepository.findAuditContextsByIdIn(ids);
            default -> List.of();
        };
    }

    private String resolveEntityName(
            AuditLog auditLog,
            Map<String, Map<UUID, AuditEntityContext>> contexts) {
        Map<UUID, AuditEntityContext> typeContexts = contexts.get(auditLog.getEntityType());
        AuditEntityContext context = typeContexts == null || auditLog.getEntityId() == null
                ? null
                : typeContexts.get(auditLog.getEntityId());
        String entityName = context == null ? null : context.entityName();
        if (isBlank(entityName)) {
            entityName = snapshotEntityName(auditLog);
        }
        return isBlank(entityName) ? entityTypeFallback(auditLog.getEntityType()) : entityName;
    }

    private String snapshotEntityName(AuditLog auditLog) {
        if ("PROFILE".equals(auditLog.getEntityType())) {
            return affectedUserName(auditLog);
        }
        List<String> keys = auditLog.getEntityType() == null
                ? List.of()
                : SNAPSHOT_ENTITY_NAME_KEYS.getOrDefault(auditLog.getEntityType(), List.of());
        return snapshotValue(auditLog.getOldValue(), auditLog.getNewValue(), keys.toArray(String[]::new));
    }

    private String snapshotValue(JsonNode oldValue, JsonNode newValue, String... keys) {
        for (JsonNode snapshot : new JsonNode[]{oldValue, newValue}) {
            if (snapshot == null) {
                continue;
            }
            for (String key : keys) {
                JsonNode value = snapshot.path(key);
                if (value.isTextual() && !isBlank(value.asText())) {
                    return value.asText();
                }
            }
        }
        return null;
    }

    private String affectedUserName(AuditLog auditLog) {
        if (auditLog.getUserAffected() != null && !isBlank(auditLog.getUserAffected().getName())) {
            return auditLog.getUserAffected().getName();
        }
        return snapshotValue(auditLog.getOldValue(), auditLog.getNewValue(), "Nome do usuário");
    }

    private String entityTypeFallback(String entityType) {
        return switch (entityType == null ? "" : entityType) {
            case "PROJECT" -> "Projeto (contexto indisponível)";
            case "SQUAD" -> "Squad (contexto indisponível)";
            case "JOB_POSTING" -> "Vaga (contexto indisponível)";
            case "PROFILE" -> "Perfil (contexto indisponível)";
            case "PROFILE_SKILL" -> "Skill (contexto indisponível)";
            case "RESOURCE_EQUIPMENT" -> "Equipamento (contexto indisponível)";
            default -> "Entidade (contexto indisponível)";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
