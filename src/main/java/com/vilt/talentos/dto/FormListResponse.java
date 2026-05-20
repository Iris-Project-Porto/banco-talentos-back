package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormDefinition;

import java.util.UUID;

public record FormListResponse(UUID id,
                               UUID groupId,
                               String title,
                               int version,
                               String elements,
                               boolean active){

    public FormListResponse(FormDefinition form){
        this(form.getId(),form.getGroupId(),form.getVersion(),form.getTitle(),form.getActive(),form.getElements());
    }
}
