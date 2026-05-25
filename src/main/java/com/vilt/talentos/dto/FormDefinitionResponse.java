package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormDefinition;

import java.util.Map;
import java.util.UUID;

public record FormDefinitionResponse(UUID id, Map<String, Object> elements, boolean active, String title, UUID groupId) {

    public FormDefinitionResponse(FormDefinition form){
        this(form.getId(),form.getElements(),form.isActive(),form.getTitle(),form.getGroupId());
    }
}
