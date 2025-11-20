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
public class VendorUpdateDTO {
    
    @NotBlank(message = "Vendor code is required")
    @Size(max = 50, message = "Vendor code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Vendor name is required")
    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    private String name;
    
    private String contactPerson;
    
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String pincode;
    
    private String gstNumber;
    
    private String panNumber;
    
    private Boolean isActive;
}

