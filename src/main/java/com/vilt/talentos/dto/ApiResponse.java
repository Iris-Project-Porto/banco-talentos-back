package com.vilt.talentos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta padrão da API")
public record ApiResponse<T>(
    @Schema(description = "Mensagem descritiva", example = "Operação realizada com sucesso")
    String message,
    
    @Schema(description = "Dados de retorno da operação")
    T data
) {
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(message, null);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }
}
