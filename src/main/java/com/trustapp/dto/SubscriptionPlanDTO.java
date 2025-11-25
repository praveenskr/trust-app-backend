package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private Long id;
    private String code;
    private String name;
    private String planType;
    private Integer durationMonths;
    private BigDecimal amount;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

