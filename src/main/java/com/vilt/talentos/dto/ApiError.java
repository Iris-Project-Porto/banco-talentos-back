package com.vilt.talentos.dto;

import java.time.Instant;

public record ApiError(
    int status,
    String message,
    Instant timestamp
) {
}
