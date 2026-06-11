package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.ProjectRequest;
import com.vilt.talentos.dto.ProjectResponse;
import com.vilt.talentos.entity.Project;
import com.vilt.talentos.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProjectMapper {
    
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToName")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "userToNameOptional")
    ProjectResponse toResponse(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Project toEntity(ProjectRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(ProjectRequest request, @MappingTarget Project project);

    @Named("userToName")
    default String userToName(User user) {
        return user != null ? user.getName() : "Sistema";
    }

    @Named("userToNameOptional")
    default String userToNameOptional(User user) {
        return user != null ? user.getName() : null;
    }
}
