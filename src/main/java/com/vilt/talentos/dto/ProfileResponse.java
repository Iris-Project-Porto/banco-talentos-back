package com.vilt.talentos.dto;

import com.vilt.talentos.entity.DomainStatus;
import com.vilt.talentos.entity.RegistrationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
    UUID id,
    String name,
    String email,
    String groupName,
    String photoUrl,
    String jobTitle,
    String area,
    String about,
    String allocationStatus,
    String level,
    String levelOverride,
    Integer levelScore,
    String levelJustification,
    Integer experienceYears,
    String registrationNumber,
    RegistrationStatus registrationStatus,
    DomainStatus status,
    List<ProfileSkillResponse> skills,
    Instant createdAt,
    Instant updatedAt
) {}
