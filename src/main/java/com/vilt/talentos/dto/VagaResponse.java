package com.vilt.talentos.dto;

import com.vilt.talentos.entity.Vaga;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record VagaResponse(
        UUID id,
        String titulo,
        String senioridade,
        String time,
        String solicitante,
        String tempoContratacao,
        int numeroVagas,
        String area,
        List<String> skills,
        String descricao,
        String status,
        String prioridade,
        String dataAbertura
) {
    public static VagaResponse from(Vaga v) {
        return new VagaResponse(
                v.getId(),
                v.getTitulo(),
                v.getSenioridade().label(),
                v.getTime(),
                v.getSolicitante(),
                v.getTempoContratacao(),
                v.getNumeroVagas(),
                v.getArea(),
                v.getSkills() != null ? Arrays.asList(v.getSkills()) : List.of(),
                v.getDescricao(),
                v.getStatus().getLabel(),
                v.getPrioridade().getLabel(),
                v.getDataAbertura() != null ? v.getDataAbertura().toString() : null
        );
    }
}
