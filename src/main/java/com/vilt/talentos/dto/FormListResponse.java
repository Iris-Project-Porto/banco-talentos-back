package com.vilt.talentos.dto;

import java.util.Map;
import java.util.UUID;

public record FormListResponse(UUID id,
                               UUID groupId,
                               String title,
                               int version,
                               Map<String, Object> elements,
                               boolean active){
}
