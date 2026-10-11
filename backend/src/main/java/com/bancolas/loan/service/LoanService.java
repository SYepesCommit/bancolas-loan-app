package com.bancolas.loan.service;

import com.bancolas.config.RabbitMQConfig;
import com.bancolas.customer.domain.CustomerStatus;
import com.bancolas.customer.repository.CustomerRepository;
import com.bancolas.loan.constant.LogMessages;
import com.bancolas.loan.domain.LoanRejectionReason;
import com.bancolas.loan.domain.LoanRequest;
import com.bancolas.loan.domain.LoanStatus;
import com.bancolas.loan.dto.LoanApplicationRequest;
import com.bancolas.loan.dto.LoanApplicationResponse;
import com.bancolas.loan.event.LoanEvaluatedEvent;
import com.bancolas.loan.repository.LoanRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final CustomerRepository customerRepository;
    private final LoanRequestRepository loanRequestRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Mono<LoanApplicationResponse> processLoanApplication(LoanApplicationRequest request) {
        log.info(LogMessages.LOAN_PROCESSING, request.getRequestReference(), request.getCustomerId(),
                request.getAmount());

        return loanRequestRepository.findByRequestReference(request.getRequestReference())
                .map(this::mapToResponse)
                .doOnNext(existing -> log.info(LogMessages.LOAN_IDEMPOTENT_RESPONSE, existing.getRequestReference()))
                .switchIfEmpty(Mono.defer(() -> executeLoanEvaluation(request)
                        .doOnSuccess(this::publishLoanEvaluatedEvent)));
    }

    private Mono<LoanApplicationResponse> executeLoanEvaluation(LoanApplicationRequest request) {
        return customerRepository.findById(request.getCustomerId())
                .flatMap(customer -> {
                    if (customer.getStatus() == CustomerStatus.BLOCKED) {
                        log.warn(LogMessages.LOAN_REJECTED_BLOCKED, customer.getId());
                        return saveRejectedLoan(request, LoanRejectionReason.BLOCKED_CUSTOMER);
                    }

                    return customerRepository.deductCapacity(customer.getId(), request.getAmount())
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated > 0) {
                                    log.info(LogMessages.LOAN_APPROVED, request.getRequestReference());
                                    return saveApprovedLoan(request);
                                } else {
                                    log.warn(LogMessages.LOAN_REJECTED_CAPACITY, customer.getId());
                                    return saveRejectedLoan(request, LoanRejectionReason.INSUFFICIENT_CAPACITY);
                                }
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn(LogMessages.LOAN_REJECTED_NOT_FOUND, request.getCustomerId());
                    return saveRejectedLoan(request, LoanRejectionReason.CUSTOMER_NOT_FOUND);
                }));
    }

    private Mono<LoanApplicationResponse> saveApprovedLoan(LoanApplicationRequest request) {
        LoanRequest loanRequest = LoanRequest.builder()
                .requestReference(request.getRequestReference())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .termMonths(request.getTermMonths())
                .status(LoanStatus.APPROVED)
                .processedAt(LocalDateTime.now())
                .build();

        return loanRequestRepository.save(loanRequest).map(this::mapToResponse);
    }

    private Mono<LoanApplicationResponse> saveRejectedLoan(LoanApplicationRequest request, LoanRejectionReason reason) {
        LoanRequest loanRequest = LoanRequest.builder()
                .requestReference(request.getRequestReference())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .termMonths(request.getTermMonths())
                .status(LoanStatus.REJECTED)
                .rejectionReason(reason.getDescription())
                .processedAt(LocalDateTime.now())
                .build();

        return loanRequestRepository.save(loanRequest).map(this::mapToResponse);
    }

    public Flux<LoanApplicationResponse> getRecentLoans() {
        return loanRequestRepository.findTop20ByOrderByProcessedAtDesc()
                .map(this::mapToResponse);
    }

    private void publishLoanEvaluatedEvent(LoanApplicationResponse response) {
        if (response == null)
            return;

        Mono.fromRunnable(() -> {
            try {
                LoanEvaluatedEvent event = new LoanEvaluatedEvent(
                        response.getId(),
                        response.getRequestReference(),
                        response.getCustomerId(),
                        response.getAmount(),
                        response.getTermMonths(),
                        response.getStatus() != null ? response.getStatus().name() : null,
                        response.getRejectionReason(),
                        response.getProcessedAt() != null ? response.getProcessedAt().toString() : null);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY_EVALUATED,
                        event);

                log.info(LogMessages.RABBIT_EVENT_PUBLISHED, response.getRequestReference());
            } catch (Exception e) {
                log.error(LogMessages.RABBIT_EVENT_ERROR, response.getRequestReference(), e.getMessage());
            }
        })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private LoanApplicationResponse mapToResponse(LoanRequest entity) {
        return LoanApplicationResponse.builder()
                .id(entity.getId())
                .requestReference(entity.getRequestReference())
                .customerId(entity.getCustomerId())
                .amount(entity.getAmount())
                .termMonths(entity.getTermMonths())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .processedAt(entity.getProcessedAt())
                .build();
    }
}