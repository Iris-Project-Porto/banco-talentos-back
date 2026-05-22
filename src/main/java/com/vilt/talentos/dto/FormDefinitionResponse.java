package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormDefinition;

import java.util.UUID;

public record FormDefinitionResponse(UUID id, String elements, boolean active, String title, UUID groupId) {

    public FormDefinitionResponse(FormDefinition form){
        this(form.getId,form.getElements(),form.getActive(),form.getTitle(),form.getGroupId());
    }
}
