package com.bancolas.loan.event;

import java.io.Serializable;
import java.math.BigDecimal;

public record LoanEvaluatedEvent(
    Long loanId,
    String requestReference,
    String customerId,
    BigDecimal amount,
    Integer termMonths,
    String status,
    String rejectionReason,
    String timestamp
) implements Serializable {}