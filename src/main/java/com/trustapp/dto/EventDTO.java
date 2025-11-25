package com.trustapp.dto;

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
    private Long branchId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

