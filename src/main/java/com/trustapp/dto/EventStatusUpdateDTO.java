package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusUpdateDTO {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PLANNED|ACTIVE|COMPLETED|CANCELLED", 
             message = "Status must be one of: PLANNED, ACTIVE, COMPLETED, CANCELLED")
    private String status;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}

