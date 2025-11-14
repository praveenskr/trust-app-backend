package com.trustapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    private Long id;
    private String token;
    
    @Builder.Default
    private TokenType type = TokenType.BEARER;
    
    private Boolean revoked;
    private Boolean expired;
    private User user;
    private LocalDateTime expiresAt;
    
    public enum TokenType {
        BEARER, REFRESH
    }
}

