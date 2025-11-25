package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerialNumberNextResponseDTO {
    private String serialNumber;
    private String entityType;
    private Integer year;
    private Integer sequence;
}

