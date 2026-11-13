package com.vilt.talentos.dto;

import com.vilt.talentos.entity.JobPostingStatus;
import org.springdoc.core.annotations.ParameterObject;
import java.util.List;
import java.util.UUID;

@ParameterObject 
public record JobPostingFilterRequest(
        String vacancyCode,
        String title,
        UUID projectId,
        UUID squadId,
        String experienceLevel, 
        String modality,      
        JobPostingStatus status,
        String recruiter,
        List<String> skills    
) {}