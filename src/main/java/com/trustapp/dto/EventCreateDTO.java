package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreateDTO {
    
    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private String status; // PLANNED, ACTIVE, COMPLETED, CANCELLED
    
    private Long branchId;
    
    private Boolean isActive;
}

