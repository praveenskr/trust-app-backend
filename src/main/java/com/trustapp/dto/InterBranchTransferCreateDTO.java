package com.trustapp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterBranchTransferCreateDTO {
    
    @NotNull(message = "From branch ID is required")
    private Long fromBranchId;
    
    @NotNull(message = "To branch ID is required")
    private Long toBranchId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;
    
    @NotNull(message = "Payment mode ID is required")
    private Long paymentModeId;
    
    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
    
    private String status; // PENDING, COMPLETED, CANCELLED
}

