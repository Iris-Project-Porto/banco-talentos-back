package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.SquadRequest;
import com.vilt.talentos.dto.SquadResponse;
import com.vilt.talentos.entity.Skill;
import com.vilt.talentos.entity.Squad;
import com.vilt.talentos.entity.User;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SquadMapper {
    
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "skills", source = "skills", qualifiedByName = "mapSkills")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToName")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "userToNameOptional")
    SquadResponse toResponse(Squad squad);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Squad toEntity(SquadRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(SquadRequest request, @MappingTarget Squad squad);

    @Named("mapSkills")
    default List<String> mapSkills(List<Skill> skills) {
        if (skills == null) return null;
        return skills.stream().map(Skill::getName).collect(Collectors.toList());
    }

    @Named("userToName")
    default String userToName(User user) {
        return user != null ? user.getName() : "Sistema";
    }

    @Named("userToNameOptional")
    default String userToNameOptional(User user) {
        return user != null ? user.getName() : null;
    }
}
