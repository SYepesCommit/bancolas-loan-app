package com.bancolas.loan.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loan_request")
public class LoanRequest {

    @Id
    private Long id;

    @Column("request_reference")
    private String requestReference;

    @Column("customer_id")
    private String customerId;

    private BigDecimal amount;

    @Column("term_months")
    private Integer termMonths;

    private LoanStatus status;

    @Column("rejection_reason")
    private String rejectionReason;

    @Column("processed_at")
    private LocalDateTime processedAt;
}