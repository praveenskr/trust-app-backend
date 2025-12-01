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
public class InterBranchTransferStatusUpdateDTO {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|COMPLETED|CANCELLED", message = "Status must be PENDING, COMPLETED, or CANCELLED")
    private String status;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;
}


