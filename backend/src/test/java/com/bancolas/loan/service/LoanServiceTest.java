package com.bancolas.loan.service;

import com.bancolas.customer.domain.Customer;
import com.bancolas.customer.domain.CustomerStatus;
import com.bancolas.customer.repository.CustomerRepository;
import com.bancolas.loan.domain.LoanRequest;
import com.bancolas.loan.domain.LoanStatus;
import com.bancolas.loan.dto.LoanApplicationRequest;
import com.bancolas.loan.repository.LoanRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRequestRepository loanRequestRepository;

    @InjectMocks
    private LoanService loanService;

    private LoanApplicationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = LoanApplicationRequest.builder()
                .requestReference("SOL-TEST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .termMonths(12)
                .build();
    }

    @Test
    @DisplayName("Debe aprobar la solicitud cuando el cliente es activo y tiene capacidad suficiente")
    void processLoanApplication_Approved() {
        Customer activeCustomer = Customer.builder()
                .id("USR-10")
                .status(CustomerStatus.ACTIVE)
                .availableCapacity(new BigDecimal("1000000.00"))
                .build();

        LoanRequest savedLoan = LoanRequest.builder()
                .id(1L)
                .requestReference("SOL-TEST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .termMonths(12)
                .status(LoanStatus.APPROVED)
                .processedAt(LocalDateTime.now())
                .build();

        when(loanRequestRepository.findByRequestReference("SOL-TEST-01")).thenReturn(Mono.empty());
        when(customerRepository.findById("USR-10")).thenReturn(Mono.just(activeCustomer));
        when(customerRepository.deductCapacity(eq("USR-10"), any())).thenReturn(Mono.just(1));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenReturn(Mono.just(savedLoan));

        StepVerifier.create(loanService.processLoanApplication(sampleRequest))
                .expectNextMatches(response -> 
                        response.getStatus() == LoanStatus.APPROVED &&
                        response.getRejectionReason() == null &&
                        response.getRequestReference().equals("SOL-TEST-01")
                )
                .verifyComplete();

        verify(customerRepository, times(1)).deductCapacity("USR-10", new BigDecimal("500000.00"));
    }

    @Test
    @DisplayName("Idempotencia (RF05): Debe retornar la solicitud existente si la referencia ya fue procesada")
    void processLoanApplication_Idempotency() {
        LoanRequest existingLoan = LoanRequest.builder()
                .id(99L)
                .requestReference("SOL-TEST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .status(LoanStatus.APPROVED)
                .processedAt(LocalDateTime.now())
                .build();

        when(loanRequestRepository.findByRequestReference("SOL-TEST-01")).thenReturn(Mono.just(existingLoan));

        StepVerifier.create(loanService.processLoanApplication(sampleRequest))
                .expectNextMatches(response -> 
                        response.getId().equals(99L) &&
                        response.getStatus() == LoanStatus.APPROVED
                )
                .verifyComplete();

        verify(customerRepository, never()).findById(anyString());
        verify(customerRepository, never()).deductCapacity(anyString(), any());
    }

    @Test
    @DisplayName("Rechazo por Concurrencia/Capacidad (RF04): Debe rechazar la solicitud si no hay capacidad suficiente")
    void processLoanApplication_Rejected_InsufficientCapacity() {
        Customer activeCustomer = Customer.builder()
                .id("USR-10")
                .status(CustomerStatus.ACTIVE)
                .availableCapacity(new BigDecimal("100000.00"))
                .build();

        LoanRequest rejectedLoan = LoanRequest.builder()
                .id(2L)
                .requestReference("SOL-TEST-01")
                .customerId("USR-10")
                .amount(new BigDecimal("500000.00"))
                .status(LoanStatus.REJECTED)
                .rejectionReason("Capacidad disponible insuficiente")
                .processedAt(LocalDateTime.now())
                .build();

        when(loanRequestRepository.findByRequestReference("SOL-TEST-01")).thenReturn(Mono.empty());
        when(customerRepository.findById("USR-10")).thenReturn(Mono.just(activeCustomer));
        when(customerRepository.deductCapacity(eq("USR-10"), any())).thenReturn(Mono.just(0));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenReturn(Mono.just(rejectedLoan));

        StepVerifier.create(loanService.processLoanApplication(sampleRequest))
                .expectNextMatches(response -> 
                        response.getStatus() == LoanStatus.REJECTED &&
                        response.getRejectionReason().equals("Capacidad disponible insuficiente")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Rechazo por Cliente Bloqueado (RF03): Debe rechazar sin intentar descontar capacidad")
    void processLoanApplication_Rejected_BlockedCustomer() {
        Customer blockedCustomer = Customer.builder()
                .id("USR-30")
                .status(CustomerStatus.BLOCKED)
                .availableCapacity(new BigDecimal("800000.00"))
                .build();

        sampleRequest.setCustomerId("USR-30");

        LoanRequest rejectedLoan = LoanRequest.builder()
                .id(3L)
                .requestReference("SOL-TEST-01")
                .customerId("USR-30")
                .status(LoanStatus.REJECTED)
                .rejectionReason("El cliente se encuentra bloqueado")
                .processedAt(LocalDateTime.now())
                .build();

        when(loanRequestRepository.findByRequestReference("SOL-TEST-01")).thenReturn(Mono.empty());
        when(customerRepository.findById("USR-30")).thenReturn(Mono.just(blockedCustomer));
        when(loanRequestRepository.save(any(LoanRequest.class))).thenReturn(Mono.just(rejectedLoan));

        StepVerifier.create(loanService.processLoanApplication(sampleRequest))
                .expectNextMatches(response -> 
                        response.getStatus() == LoanStatus.REJECTED &&
                        response.getRejectionReason().equals("El cliente se encuentra bloqueado")
                )
                .verifyComplete();

        verify(customerRepository, never()).deductCapacity(anyString(), any());
    }
}