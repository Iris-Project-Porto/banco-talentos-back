package com.vilt.talentos.dto;

import com.vilt.talentos.entity.FormDefinition;

import java.util.Map;
import java.util.UUID;

public record FormListResponse(UUID id,
                               UUID groupId,
                               String title,
                               int version,
                               Map<String, Object> elements,
                               boolean active){

    public FormListResponse(FormDefinition form){
        this(
                form.getId(),
                form.getGroupId(),
                form.getTitle(),
                form.getVersion(),
                form.getElements(),
                form.getActive()
        );
    }
}
