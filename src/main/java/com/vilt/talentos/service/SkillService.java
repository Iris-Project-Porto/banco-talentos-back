package com.vilt.talentos.service;

import com.vilt.talentos.dto.SkillRequest;
import com.vilt.talentos.dto.SkillResponse;
import com.vilt.talentos.entity.Skill;
import com.vilt.talentos.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepo;

    public List<SkillResponse> findAllActive() {
        List<SkillResponse> list = skillRepo.findByActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma skill ativa encontrada");
        }
        return list;
    }

    public List<SkillResponse> findAllInactive() {
        List<SkillResponse> list = skillRepo.findByActive(false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma skill inativa encontrada");
        }
        return list;
    }

    public SkillResponse findById(UUID id) {
        return skillRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill não encontrada"));
    }

    public SkillResponse create(SkillRequest request) {
        String nameUpper = request.name().trim().toUpperCase();
        
        return skillRepo.findByName(nameUpper)
                .map(existing -> {
                    if (!existing.isActive()) {
                        existing.setActive(true);
                        existing.setImportanceWeight(request.importanceWeight() != null ? request.importanceWeight() : 1);
                        return mapToResponse(skillRepo.save(existing));
                    }
                    return mapToResponse(existing);
                })
                .orElseGet(() -> {
                    Skill skill = Skill.builder()
                            .name(nameUpper)
                            .importanceWeight(request.importanceWeight() != null ? request.importanceWeight() : 1)
                            .active(true)
                            .build();
                    return mapToResponse(skillRepo.save(skill));
                });
    }

    public SkillResponse update(UUID id, SkillRequest request) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill não encontrada"));
        
        String nameUpper = request.name().trim().toUpperCase();
        
        skillRepo.findByName(nameUpper)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outra skill com este nome");
                    }
                });

        skill.setName(nameUpper);
        if (request.importanceWeight() != null) {
            skill.setImportanceWeight(request.importanceWeight());
        }
        
        return mapToResponse(skillRepo.save(skill));
    }

    public void setActiveStatus(UUID id, boolean active) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Skill não encontrada"));
        skill.setActive(active);
        skillRepo.save(skill);
    }

    private SkillResponse mapToResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.isActive(), s.getImportanceWeight());
    }
}
