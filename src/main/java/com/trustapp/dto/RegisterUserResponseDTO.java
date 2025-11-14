package com.trustapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserResponseDTO {
    
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
}

