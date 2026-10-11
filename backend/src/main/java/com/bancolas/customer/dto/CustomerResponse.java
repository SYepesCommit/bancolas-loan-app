package com.bancolas.customer.dto;

import java.math.BigDecimal;

public record CustomerResponse(
    String id,
    String status,
    BigDecimal availableCapacity
) {}