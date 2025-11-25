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
public class DonationDTO {
    private Long id;
    private String receiptNumber;
    private String donorName;
    private String donorAddress;
    private String panNumber;
    private String donorPhone;
    private String donorEmail;
    private BigDecimal amount;
    
    // Nested DTOs
    private PaymentModeDTO paymentMode;
    private DonationPurposeDTO purpose;
    private DonationSubCategoryDTO subCategory;
    private EventDTO event;
    private BranchDTO branch;
    
    private LocalDate donationDate;
    private String notes;
    private Boolean receiptGenerated;
    private LocalDateTime receiptGeneratedAt;
    private String receiptFilePath;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User references
    private UserDTO createdBy;
    private UserDTO updatedBy;
}

