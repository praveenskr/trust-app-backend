package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerialNumberConfigUpdateDTO {
    
    @NotBlank(message = "Prefix is required")
    @Size(max = 50, message = "Prefix must not exceed 50 characters")
    private String prefix;
    
    @Size(max = 255, message = "Format pattern must not exceed 255 characters")
    private String formatPattern;
    
    private Integer sequenceLength;
}

