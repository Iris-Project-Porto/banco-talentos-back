package com.vilt.talentos.dto;

import java.util.UUID;

public record SquadResourceResponse(
    UUID id,
    String name,
    String email,
    String jobTitle,
    String photoUrl
) {
    public SquadResourceResponse(UUID id, String name, String email, String jobTitle) {
        this(id, name, email, jobTitle, null);
    }
}

