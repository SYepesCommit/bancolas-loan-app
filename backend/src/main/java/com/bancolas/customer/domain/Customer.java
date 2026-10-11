package com.bancolas.customer.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customer")
public class Customer {

    @Id
    private String id;

    @Column("status")
    private CustomerStatus status;

    @Column("available_capacity")
    private BigDecimal availableCapacity;

    @Version
    private Long version;

    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(this.status);
    }

    public boolean hasSufficientCapacity(BigDecimal amount) {
        return this.availableCapacity != null && this.availableCapacity.compareTo(amount) >= 0;
    }
}