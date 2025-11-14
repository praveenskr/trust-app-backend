package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    private List<RoleDTO> roles;
    private List<Long> roleIds;
    private List<BranchAccessDTO> branchAccess;
    private List<Long> branchIds;
}

