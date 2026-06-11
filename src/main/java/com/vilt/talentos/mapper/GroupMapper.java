package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.GroupRequest;
import com.vilt.talentos.dto.GroupResponse;
import com.vilt.talentos.entity.Group;
import com.vilt.talentos.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface GroupMapper {
    
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToName")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "userToNameOptional")
    GroupResponse toResponse(Group group);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Group toEntity(GroupRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(GroupRequest request, @MappingTarget Group group);

    @Named("userToName")
    default String userToName(User user) {
        return user != null ? user.getName() : "Sistema";
    }

    @Named("userToNameOptional")
    default String userToNameOptional(User user) {
        return user != null ? user.getName() : null;
    }
}
