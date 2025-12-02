package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationTransactionDTO {
    private Long id;
    private String receiptNumber;
    private String donorName;
    private BigDecimal amount;
    private String paymentMode;
    private LocalDate donationDate;
}

