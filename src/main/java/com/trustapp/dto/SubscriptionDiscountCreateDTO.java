package com.trustapp.dto;

import com.trustapp.dto.enums.DiscountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDiscountCreateDTO {
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    private Integer minQuantity;
    
    private Integer maxQuantity;
    
    @NotNull(message = "Valid from date is required")
    private LocalDate validFrom;
    
    private LocalDate validTo;
    
    private Boolean isActive;
}

