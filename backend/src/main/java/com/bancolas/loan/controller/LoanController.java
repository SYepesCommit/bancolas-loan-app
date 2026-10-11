package com.bancolas.loan.controller;

import com.bancolas.loan.dto.LoanApplicationRequest;
import com.bancolas.loan.dto.LoanApplicationResponse;
import com.bancolas.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LoanApplicationResponse> applyForLoan(@RequestBody LoanApplicationRequest request) {
        return loanService.processLoanApplication(request);
    }

    @GetMapping
    public Flux<LoanApplicationResponse> getRecentLoans() {
        return loanService.getRecentLoans();
    }
}