package com.trustapp.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateDTO {
    
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String status; // PLANNED, ACTIVE, COMPLETED, CANCELLED
    
    private Long branchId;
    
    private Boolean isActive;
}

