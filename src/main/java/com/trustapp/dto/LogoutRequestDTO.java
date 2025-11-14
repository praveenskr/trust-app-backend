package com.trustapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
}

