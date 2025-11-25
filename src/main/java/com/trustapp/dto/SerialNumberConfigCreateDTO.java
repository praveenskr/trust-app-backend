package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerialNumberConfigCreateDTO {
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    private String entityType;
    
    @NotBlank(message = "Prefix is required")
    @Size(max = 50, message = "Prefix must not exceed 50 characters")
    private String prefix;
    
    @Size(max = 255, message = "Format pattern must not exceed 255 characters")
    private String formatPattern;
    
    @NotNull(message = "Current year is required")
    private Integer currentYear;
    
    @NotNull(message = "Last sequence is required")
    private Integer lastSequence;
    
    @NotNull(message = "Sequence length is required")
    private Integer sequenceLength;
}

