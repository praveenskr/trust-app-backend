package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDiscountDTO {
    private Long id;
    private Long planId;
    private String discountType;
    private BigDecimal discountValue;
    private Integer minQuantity;
    private Integer maxQuantity;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

