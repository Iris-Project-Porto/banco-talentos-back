package com.vilt.talentos.service;

import com.vilt.talentos.dto.FormListResponse;
import com.vilt.talentos.dto.FormSubmissionRequest;
import com.vilt.talentos.dto.FormSubmissionResponse;
import com.vilt.talentos.exception.ResourceNotFoundException;
import com.vilt.talentos.repository.FormDefinitionRepository;
import com.vilt.talentos.repository.FormSubmissionRepository;
import com.vilt.talentos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;
    private final FormDefinitionRepository formDefinitionRepository;
    private final UserRepository userRepository;
    private final com.vilt.talentos.mapper.FormMapper mapper;

    public List<FormListResponse> getFormsByUserGroup(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (user.getGroup() == null) {
            return List.of();
        }

        UUID groupId = user.getGroup().getId();
        return formDefinitionRepository.findAllByGroup_Id(groupId).stream()
                .map(mapper::toListResponse)
                .toList();
    }

    @Transactional
    public FormSubmissionResponse createSubmission(UUID userId, FormSubmissionRequest request) {
        var formDefinition = formDefinitionRepository.findById(request.formDefinitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Formulário não encontrado."));

        var user = userRepository.getReferenceById(userId);

        var formSubmission = mapper.toEntity(request);
        formSubmission.setFormDefinition(formDefinition);
        formSubmission.setUser(user);

        formSubmissionRepository.save(formSubmission);
        return mapper.toSubmissionResponse(formSubmission);
    }

    public FormSubmissionResponse getSubmissionById(UUID id) {
        var submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submissão de respostas não encontrada."));
        return mapper.toSubmissionResponse(submission);
    }
}
