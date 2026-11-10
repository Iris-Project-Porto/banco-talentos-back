package com.vilt.talentos.dto;

import java.util.UUID;

public record SquadResourceInput(
    UUID id,
    String jobTitle
) {}

