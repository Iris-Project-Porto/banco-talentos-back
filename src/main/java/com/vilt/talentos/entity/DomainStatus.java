package com.vilt.talentos.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainStatus {
    PENDING("Pendente"),
    ACTIVE("Ativo"),
    INACTIVE("Inativo");

    private final String description;
}
