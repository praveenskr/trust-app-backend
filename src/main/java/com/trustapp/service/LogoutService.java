package com.trustapp.service;

import com.trustapp.model.User;
import com.trustapp.repository.TokenRepository;
import com.trustapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void logout(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);
        
        // Read refresh token from request attribute (set by controller)
        String refreshToken = (String) request.getAttribute("refreshToken");

        // Revoke the access token (Bearer token)
        var storedAccessToken = tokenRepository.findJwtToken(jwt).orElse(null);
        if (storedAccessToken != null && !storedAccessToken.getExpired() && !storedAccessToken.getRevoked()) {
            storedAccessToken.setExpired(true);
            storedAccessToken.setRevoked(true);
            tokenRepository.updateStatus(storedAccessToken);
        }
        
        // Validate and revoke the refresh token
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                // Extract username from refresh token
                String userEmail = jwtService.extractUsername(refreshToken);
                
                if (userEmail != null) {
                    // Find user
                    User user = userRepository.findByEmailForAuthentication(userEmail).orElse(null);
                    
                    if (user != null) {
                        // Validate refresh token (check signature, expiration, etc.)
                        if (jwtService.isTokenValid(refreshToken, user)) {
                            // Find token in database
                            var storedRefreshToken = tokenRepository.findRefreshToken(refreshToken).orElse(null);
                            if (storedRefreshToken != null && !storedRefreshToken.getExpired() && !storedRefreshToken.getRevoked()) {
                                // Revoke the refresh token
                                storedRefreshToken.setExpired(true);
                                storedRefreshToken.setRevoked(true);
                                tokenRepository.updateStatus(storedRefreshToken);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // If refresh token validation fails, just log and continue
                // Logout should always succeed
                log.error("Failed to validate refresh token during logout: {}", e.getMessage());
            }
        }
    }

}

