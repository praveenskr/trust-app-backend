package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherTransactionDTO {
    private Long id;
    private String voucherNumber;
    private String vendorName;
    private BigDecimal amount;
    private LocalDate voucherDate;
}

