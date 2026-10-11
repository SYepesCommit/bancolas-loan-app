package com.bancolas.customer.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.bancolas.customer.domain.Customer;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CustomerRepository extends ReactiveCrudRepository<Customer, String> {

    @Modifying
    @Query("UPDATE customer SET available_capacity = available_capacity - :amount, version = version + 1 " +
           "WHERE id = :customerId AND available_capacity >= :amount AND status = 'ACTIVE'")
    Mono<Integer> deductCapacity(String customerId, BigDecimal amount);
}