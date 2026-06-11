package com.vilt.talentos.service;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.entity.Skill;
import com.vilt.talentos.exception.ConflictException;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.SkillMapper;
import com.vilt.talentos.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepo;
    private final SkillMapper mapper;

    public Page<SkillResponse> findAllActive(Pageable pageable) {
        Page<SkillResponse> page = skillRepo.findByActive(true, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma skill ativa encontrada.");
        }
        return page;
    }

    public Page<SkillResponse> findAllInactive(Pageable pageable) {
        Page<SkillResponse> page = skillRepo.findByActive(false, pageable)
                .map(mapper::toResponse);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhuma skill inativa encontrada.");
        }
        return page;
    }

    public SkillResponse findById(UUID id) {
        return skillRepo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Skill não encontrada"));
    }

    public SkillResponse create(SkillRequest request) {
        String nameUpper = request.name().trim().toUpperCase();
        
        return skillRepo.findByName(nameUpper)
                .map(existing -> {
                    if (!existing.isActive()) {
                        existing.setActive(true);
                        existing.setImportanceWeight(request.importanceWeight() != null ? request.importanceWeight() : 1);
                        return mapper.toResponse(skillRepo.save(existing));
                    }
                    return mapper.toResponse(existing);
                })
                .orElseGet(() -> {
                    Skill skill = mapper.toEntity(request);
                    skill.setName(nameUpper);
                    return mapper.toResponse(skillRepo.save(skill));
                });
    }

    public SkillResponse update(UUID id, SkillRequest request) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill não encontrada"));
        
        String nameUpper = request.name().trim().toUpperCase();
        
        skillRepo.findByName(nameUpper)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("Já existe outra skill com este nome");
                    }
                });

        mapper.updateEntity(request, skill);
        skill.setName(nameUpper);
        
        return mapper.toResponse(skillRepo.save(skill));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill não encontrada"));
        skill.setActive(active);
        skillRepo.save(skill);
    }
}
