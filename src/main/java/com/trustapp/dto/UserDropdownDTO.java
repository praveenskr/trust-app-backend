package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDropdownDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
}

