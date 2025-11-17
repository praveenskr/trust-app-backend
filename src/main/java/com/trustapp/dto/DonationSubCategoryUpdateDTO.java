package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationSubCategoryUpdateDTO {
    
    @NotBlank(message = "Sub-category name is required")
    @Size(max = 255, message = "Sub-category name must not exceed 255 characters")
    private String name;
    
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
}

