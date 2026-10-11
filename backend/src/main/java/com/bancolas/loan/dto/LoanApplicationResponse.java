package com.bancolas.loan.dto;

import com.bancolas.loan.domain.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {

    private Long id;
    private String requestReference;
    private String customerId;
    private BigDecimal amount;
    private Integer termMonths;
    private LoanStatus status;
    private String rejectionReason;
    private LocalDateTime processedAt;
}