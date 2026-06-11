package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.*;
import com.vilt.talentos.entity.FormDefinition;
import com.vilt.talentos.entity.FormSubmission;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface FormMapper {
    @Mapping(target = "groupId", source = "group.id")
    FormDefinitionResponse toResponse(FormDefinition form);
    
    @Mapping(target = "groupId", source = "group.id")
    FormListResponse toListResponse(FormDefinition form);
    
    @Mapping(target = "formDefinitionId", source = "formDefinition.id")
    @Mapping(target = "userId", source = "user.id")
    FormSubmissionResponse toSubmissionResponse(FormSubmission submission);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group.id", source = "groupId")
    FormDefinition toEntity(FormCreateRequest request);
    
    @Mapping(target = "group.id", source = "groupId")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FormUpdateRequest request, @MappingTarget FormDefinition entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "formDefinition", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    FormSubmission toEntity(FormSubmissionRequest request);
}
