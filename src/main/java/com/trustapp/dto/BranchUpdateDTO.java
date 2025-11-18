package com.trustapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchUpdateDTO {
    
    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String name;
    
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String contactPerson;
    private Boolean isActive;
}

