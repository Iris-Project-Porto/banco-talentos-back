package com.vilt.talentos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationStatus {
    NOT_REQUESTED("NÃO SOLICITADO"),
    REQUESTED("SOLICITADO"),
    AWAITING_APPROVAL("AGUARDANDO APROVAÇÃO"),
    APPROVED("APROVADO"),
    REJECTED("RECUSADO");

    private final String description;
}
