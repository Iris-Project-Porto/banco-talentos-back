package com.vilt.talentos.service;

import com.vilt.talentos.dto.FormCreateRequest;
import com.vilt.talentos.dto.FormDefinitionResponse;
import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormUpdateRequest;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.mapper.FormMapper;
import com.vilt.talentos.repository.FormDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormDefinitionRepository repository;
    private final FormMapper mapper;

    @Transactional
    public FormDefinitionResponse create(FormCreateRequest request) {
        var formDefinition = mapper.toEntity(request);
        repository.save(formDefinition);
        return mapper.toResponse(formDefinition);
    }

    public Page<FormListResponse> findAll(Pageable pagination) {
        return repository.findAll(pagination).map(mapper::toListResponse);
    }

    @Transactional
    public void update(FormUpdateRequest request) {
        var formDefinition = repository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));
        mapper.updateEntity(request, formDefinition);
    }

    public FormListResponse findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toListResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Formulário não encontrado.");
        }
        repository.deleteById(id);
    }

    public java.util.List<FormDefinition> findAllByGroupId(UUID groupId) {
        return repository.findAllByGroup_Id(groupId);
    }

    public FormDefinition getReferenceById(UUID id) {
        return repository.getReferenceById(id);
    }
}
