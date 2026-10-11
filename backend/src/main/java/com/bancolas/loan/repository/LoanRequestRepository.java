package com.bancolas.loan.repository;

import com.bancolas.loan.domain.LoanRequest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanRequestRepository extends ReactiveCrudRepository<LoanRequest, Long> {

    Mono<LoanRequest> findByRequestReference(String requestReference);

    Flux<LoanRequest> findTop20ByOrderByProcessedAtDesc();
}