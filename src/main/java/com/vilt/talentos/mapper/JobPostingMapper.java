package com.vilt.talentos.mapper;

import com.vilt.talentos.dto.JobPostingRequest;
import com.vilt.talentos.dto.JobPostingResponse;
import com.vilt.talentos.dto.JobPostingSkillResponse;
import com.vilt.talentos.entity.JobPosting;
import com.vilt.talentos.entity.JobPostingSkill;
import com.vilt.talentos.entity.JobPostingStatus;
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
    @Mapping(target = "skills", source = "skills")
    JobPostingResponse toResponse(JobPosting jobPosting);

    @Mapping(target = "name", source = "skill.name")
    JobPostingSkillResponse toSkillResponse(JobPostingSkill skill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "squad", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
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
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(JobPostingRequest request, @MappingTarget JobPosting jobPosting);

    @Named("stringToStatus")
    default JobPostingStatus stringToStatus(String status) {
        if (status == null) return JobPostingStatus.OPEN;
        try {
            return JobPostingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JobPostingStatus.OPEN;
        }
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
