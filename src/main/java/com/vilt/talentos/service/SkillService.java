package com.vilt.talentos.service;

import com.vilt.talentos.dto.AdminSkillListResponse;
import com.vilt.talentos.dto.AvatarResponse;
import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.entity.AvatarType;
import com.vilt.talentos.entity.Profile;
import com.vilt.talentos.entity.Skill;
import com.vilt.talentos.entity.SkillCategory;
import com.vilt.talentos.exception.ConflictException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.SkillMapper;
import com.vilt.talentos.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepo;
    private final SkillMapper mapper;
    private final ProfileService profileService;

    private final AuditLogService auditLogService;

    public Page<SkillResponse> findAllActive(Pageable pageable) {
        Page<SkillResponse> page = skillRepo.findByActive(true, pageable)
                .map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por skills ativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma skill ativa encontrada.");
        }

        return page;
    }

    public Page<SkillResponse> findAllInactive(Pageable pageable) {
        Page<SkillResponse> page = skillRepo.findByActive(false, pageable)
                .map(mapper::toResponse);

        if (page.isEmpty()) {
            log.warn("Busca por skills inativas retornou vazia.");
            throw new ResourceNotFoundException("Nenhuma skill inativa encontrada.");
        }

        return page;
    }

    public SkillResponse findById(UUID id) {
        return skillRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Busca falhou: Skill ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Skill não encontrada");
                });
    }

    @Transactional(readOnly = true)
    public Page<AdminSkillListResponse> getAdminSkills(String name, SkillCategory category, Pageable pageable) {

        Skill filterSample = Skill.builder()
                .active(true)
                .name(name != null && !name.isBlank() ? name.trim() : null)
                .category(category)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnorePaths("id", "type", "profileSkills");

        Example<Skill> example = Example.of(filterSample, matcher);
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Skill> page = skillRepo.findAll(example, sortedPageable);

        return page.map(skill -> {
            long resourcesCount = skill.getProfileSkills().size();

            double avgProficiency = skill.getProfileSkills().stream()
                    .mapToDouble(ps -> ps.getProficiencyLevel())
                    .average()
                    .orElse(0.0);

            List<AvatarResponse> avatars = skill.getProfileSkills().stream()
                    .filter(ps -> ps.getProfile() != null)
                    .sorted((ps1, ps2) -> Double.compare(ps2.getProficiencyLevel(), ps1.getProficiencyLevel()))
                    .limit(5)
                    .map(ps -> {
                        Profile profile = ps.getProfile();
                        String photoUrl = profile.getPhotoUrl();
                        boolean hasValidPhoto = isValidPhotoUrl(photoUrl);

                        AvatarType avatarType = hasValidPhoto ? AvatarType.PHOTO : AvatarType.INITIALS;
                        String userName = (profile.getUser() != null && profile.getUser().getName() != null)
                                ? profile.getUser().getName()
                                : "";
                        String avatar = hasValidPhoto
                                ? photoUrl.trim()
                                : profileService.getInitials(userName);

                        return new AvatarResponse(
                                avatarType,
                                avatar);
                    })
                    .toList();

            return new AdminSkillListResponse(
                    skill.getId(),
                    skill.getName(),
                    skill.getType(),
                    skill.isActive(),
                    skill.getDescription(),
                    skill.getCategory(),
                    resourcesCount,
                    avgProficiency,
                    avatars,
                    skill.getCreatedAt());
        });
    }

    @Transactional
    public SkillResponse create(SkillRequest request) {
        String nameUpper = request.name().trim().toUpperCase();
        log.info("Iniciando processo de criação/validação para a skill: '{}'", nameUpper);

        return skillRepo.findByName(nameUpper)
                .map(existing -> {
                    if (!existing.isActive()) {
                        log.info(
                                "A skill '{}' já existia, mas estava inativa. Reativando e atualizando dados (ID: {}).",
                                nameUpper, existing.getId());

                        Map<String, Object> oldValue = skillAuditSnapshot(existing);

                        existing.setActive(true);
                        existing.setDescription(request.description());
                        existing.setCategory(request.category());

                        Skill savedExisting = skillRepo.save(existing);

                        Map<String, Object> newValue = skillAuditSnapshot(savedExisting);
                        auditLogService.record(com.vilt.talentos.entity.AuditOperation.UPDATE, "SKILL",
                                savedExisting.getId(), null, oldValue, newValue);

                        return mapper.toResponse(savedExisting);
                    }
                    log.info(
                            "A skill '{}' já existe e encontra-se ativa. Retornando registro existente sem alterações.",
                            nameUpper);
                    return mapper.toResponse(existing);
                })
                .orElseGet(() -> {
                    Skill skill = mapper.toEntity(request);
                    skill.setName(nameUpper);
                    Skill savedSkill = skillRepo.save(skill);

                    log.info("Nova skill '{}' criada com sucesso. ID gerado: {}", nameUpper, savedSkill.getId());

                    auditLogService.record(com.vilt.talentos.entity.AuditOperation.CREATE, "SKILL", savedSkill.getId(),
                            null, null, skillAuditSnapshot(savedSkill));

                    return mapper.toResponse(savedSkill);
                });
    }

    @Transactional
    public SkillResponse update(UUID id, SkillRequest request) {
        log.info("Iniciando atualização da skill ID: {}", id);

        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao atualizar: Skill ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Skill não encontrada");
                });

        String nameUpper = request.name().trim().toUpperCase();

        skillRepo.findByName(nameUpper)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("Falha ao atualizar: Skill com nome '{}' já existe.", nameUpper);
                        throw new ConflictException("Já existe outra skill com este nome");
                    }
                });

        Map<String, Object> oldValue = skillAuditSnapshot(skill);

        mapper.updateEntity(request, skill);
        skill.setName(nameUpper);

        Skill updatedSkill = skillRepo.save(skill);
        log.info("Skill ID: {} atualizada com sucesso.", id);

        Map<String, Object> newValue = skillAuditSnapshot(updatedSkill);
        if (!Objects.equals(oldValue, newValue)) {
            auditLogService.record(com.vilt.talentos.entity.AuditOperation.UPDATE, "SKILL", updatedSkill.getId(), null,
                    oldValue, newValue);
        }

        return mapper.toResponse(updatedSkill);
    }

    @Transactional
    public void setActiveStatus(UUID id, boolean active) {
        log.info("Alterando status da skill ID: {} para ativo={}", id, active);

        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Falha ao alterar status: Skill ID '{}' não encontrada.", id);
                    return new ResourceNotFoundException("Skill não encontrada");
                });

        if (skill.isActive() == active) {
            return;
        }

        Map<String, Object> oldValue = skillAuditSnapshot(skill);

        skill.setActive(active);
        skillRepo.save(skill);

        log.info("Status da skill ID: {} atualizado com sucesso.", id);

        auditLogService.record(
                active ? com.vilt.talentos.entity.AuditOperation.UPDATE
                        : com.vilt.talentos.entity.AuditOperation.DELETE,
                "SKILL",
                skill.getId(),
                null,
                oldValue,
                active ? skillAuditSnapshot(skill) : null);
    }

    boolean isValidPhotoUrl(String photoUrl) {
        if (photoUrl == null) {
            return false;
        }
        String trimmed = photoUrl.trim();
        return !trimmed.isEmpty()
                && !trimmed.equalsIgnoreCase("null")
                && !trimmed.equalsIgnoreCase("undefined");
    }

    private Map<String, Object> skillAuditSnapshot(Skill skill) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("ID", skill.getId());
        snapshot.put("Nome", skill.getName());
        snapshot.put("Tipo", skill.getType());
        snapshot.put("Categoria", skill.getCategory());
        snapshot.put("Descrição", skill.getDescription());
        snapshot.put("Ativa", skill.isActive());
        return snapshot;
    }
}