package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerialNumberConfigDTO {
    private Long id;
    private String entityType;
    private String prefix;
    private String formatPattern;
    private Integer currentYear;
    private Integer lastSequence;
    private Integer sequenceLength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

