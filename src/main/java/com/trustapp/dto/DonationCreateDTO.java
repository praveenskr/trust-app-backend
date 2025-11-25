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
public class DonationCreateDTO {
    
    @NotBlank(message = "Donor name is required")
    @Size(max = 255, message = "Donor name must not exceed 255 characters")
    private String donorName;
    
    @Size(max = 5000, message = "Donor address must not exceed 5000 characters")
    private String donorAddress;
    
    @Pattern(regexp = "^[A-Z0-9]{10}$", message = "PAN number must be exactly 10 alphanumeric characters (uppercase)")
    private String panNumber;
    
    @Size(max = 20, message = "Donor phone must not exceed 20 characters")
    private String donorPhone;
    
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Donor email must not exceed 255 characters")
    private String donorEmail;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 digits and 2 decimal places")
    private BigDecimal amount;
    
    @NotNull(message = "Payment mode is required")
    private Long paymentModeId;
    
    @NotNull(message = "Purpose is required")
    private Long purposeId;
    
    private Long subCategoryId;
    
    private Long eventId;
    
    @NotNull(message = "Branch is required")
    private Long branchId;
    
    @NotNull(message = "Donation date is required")
    @PastOrPresent(message = "Donation date cannot be in the future")
    private LocalDate donationDate;
    
    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}

