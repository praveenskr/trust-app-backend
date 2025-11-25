package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDTO {
    private Long id;
    private String code;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstNumber;
    private String panNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

