package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTransactionDTO {
    private Long id;
    private String expenseNumber;
    private String vendorName;
    private BigDecimal amount;
    private String category;
    private LocalDate expenseDate;
}

