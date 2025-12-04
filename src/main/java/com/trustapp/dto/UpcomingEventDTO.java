package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingEventDTO {
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private long daysUntilStart;

    // Minimal branch info (id, name) – using existing EventDTO.BranchInfo for consistency
    private EventDTO.BranchInfo branch;
}


