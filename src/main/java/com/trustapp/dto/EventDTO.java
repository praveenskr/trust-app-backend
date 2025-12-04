package com.trustapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PLANNED, ACTIVE, COMPLETED, CANCELLED
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String previousStatus; // Used for status update responses

    // Nested branch object for API responses (matches documentation)
    private BranchInfo branch;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested user info for createdBy / updatedBy
    private UserInfo createdBy;
    private UserInfo updatedBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchInfo {
        private Long id;
        private String code;
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}

