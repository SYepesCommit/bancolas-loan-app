package com.bancolas.customer.service;

import com.bancolas.customer.dto.CustomerResponse;
import com.bancolas.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Flux<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .map(c -> new CustomerResponse(
                        c.getId(),
                        c.getStatus() != null ? c.getStatus().name() : null,
                        c.getAvailableCapacity()));
    }
}