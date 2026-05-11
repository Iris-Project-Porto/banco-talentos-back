package com.vilt.talentos.dto;

import java.util.Map;

public record ApiResponse(
    String message,
    Map<String, Object> data
) {
    public static ApiResponse success(String message) {
        return new ApiResponse(message, null);
    }
    
    public static ApiResponse success(String message, Map<String, Object> data) {
        return new ApiResponse(message, data);
    }
}
