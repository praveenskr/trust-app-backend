package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentModeUpdateDTO {
    
    @NotBlank(message = "Payment mode code is required")
    @Size(max = 50, message = "Payment mode code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Payment mode name is required")
    @Size(max = 255, message = "Payment mode name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    private Boolean requiresReceipt;
    
    private Integer displayOrder;
    
    private Boolean isActive;
}

