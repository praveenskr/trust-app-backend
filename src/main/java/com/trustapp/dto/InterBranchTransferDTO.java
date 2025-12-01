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
public class InterBranchTransferDTO {
    private Long id;
    private String transferNumber;
    private BranchInfo fromBranch;
    private BranchInfo toBranch;
    private BigDecimal amount;
    private LocalDate transferDate;
    private PaymentModeInfo paymentMode;
    private String referenceNumber;
    private String description;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfo createdBy;
    private UserInfo updatedBy;
    
    // Inner class for branch information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchInfo {
        private Long id;
        private String code;
        private String name;
    }
    
    // Inner class for payment mode information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentModeInfo {
        private Long id;
        private String code;
        private String name;
    }
    
    // Inner class for user information
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}

