package com.bancolas.loan.controller;

import com.bancolas.loan.domain.LoanStatus;
import com.bancolas.loan.dto.LoanApplicationRequest;
import com.bancolas.loan.dto.LoanApplicationResponse;
import com.bancolas.loan.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = LoanController.class)
class LoanControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("POST /api/v1/loans - Debe responder HTTP 201 CREATED con el DTO de respuesta")
    void applyForLoan_Success() {
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .requestReference("SOL-REST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .termMonths(12)
                .build();

        LoanApplicationResponse mockResponse = LoanApplicationResponse.builder()
                .id(1L)
                .requestReference("SOL-REST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .termMonths(12)
                .status(LoanStatus.APPROVED)
                .processedAt(LocalDateTime.now())
                .build();

        when(loanService.processLoanApplication(any())).thenReturn(Mono.just(mockResponse));

        webTestClient.post()
                .uri("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.requestReference").isEqualTo("SOL-REST-01")
                .jsonPath("$.status").isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("GET /api/v1/loans - Debe responder HTTP 200 OK con el flujo de préstamos")
    void getRecentLoans_Success() {
        LoanApplicationResponse mockResponse = LoanApplicationResponse.builder()
                .id(1L)
                .requestReference("SOL-REST-01")
                .status(LoanStatus.APPROVED)
                .build();

        when(loanService.getRecentLoans()).thenReturn(Flux.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/loans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanApplicationResponse.class)
                .hasSize(1);
    }
}