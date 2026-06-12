package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.entity.JobPosting;
import com.vilt.talentos.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface JobPostingMapper {
    
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "squadName", source = "squad.name")
    @Mapping(target = "squadId", source = "squad.id")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userToName")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "userToNameOptional")
    JobPostingResponse toResponse(JobPosting jobPosting);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "squad", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    JobPosting toEntity(JobPostingRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "squad", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(JobPostingRequest request, @MappingTarget JobPosting jobPosting);

    @Named("userToName")
    default String userToName(User user) {
        return user != null ? user.getName() : "Sistema";
    }

    @Named("userToNameOptional")
    default String userToNameOptional(User user) {
        return user != null ? user.getName() : null;
    }
}
