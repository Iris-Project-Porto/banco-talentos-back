package com.vilt.talentos.dto;

import com.vilt.talentos.entity.Vaga;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record VagaRequest(
        @NotBlank String titulo,
        @NotNull Vaga.Senioridade senioridade,
        @NotBlank String time,
        @NotBlank String solicitante,
        String tempoContratacao,
        @Min(1) int numeroVagas,
        String area,
        List<String> skills,
        String descricao,
        @NotNull Vaga.StatusVaga status,
        @NotNull Vaga.Prioridade prioridade,
        LocalDate dataAbertura
) {}
