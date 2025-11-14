package com.trustapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private Boolean isActive;
    
    private List<Long> roleIds;
    
    private List<Long> branchIds;
}

