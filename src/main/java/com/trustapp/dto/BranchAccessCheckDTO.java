package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchAccessCheckDTO {
    private Boolean hasAccess;
    private Long userId;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private LocalDateTime grantedAt;
}

