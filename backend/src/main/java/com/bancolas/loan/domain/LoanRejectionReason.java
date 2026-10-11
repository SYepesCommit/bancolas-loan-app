package com.bancolas.loan.domain;

import lombok.Getter;

@Getter
public enum LoanRejectionReason {
    INSUFFICIENT_CAPACITY("Capacidad disponible insuficiente"),
    BLOCKED_CUSTOMER("El cliente se encuentra bloqueado"),
    CUSTOMER_NOT_FOUND("El cliente no fue encontrado");

    private final String description;

    LoanRejectionReason(String description) {
        this.description = description;
    }
}