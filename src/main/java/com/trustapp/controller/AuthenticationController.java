package com.trustapp.controller;

import com.trustapp.dto.LoginResponseDTO;
import com.trustapp.dto.LoginUserDTO;
import com.trustapp.dto.LogoutRequestDTO;
import com.trustapp.dto.RefreshTokenRequestDTO;
import com.trustapp.dto.RefreshTokenResponseDTO;
import com.trustapp.dto.RegisterUserDTO;
import com.trustapp.dto.RegisterUserResponseDTO;
import com.trustapp.dto.UserDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    private final LogoutHandler logoutHandler;
    
    public AuthenticationController(AuthenticationService authenticationService, LogoutHandler logoutHandler) {
        this.authenticationService = authenticationService;
        this.logoutHandler = logoutHandler;
    }
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponseDTO>> register(@Valid @RequestBody RegisterUserDTO registerUserDTO) {
        RegisterUserResponseDTO response = authenticationService.register(registerUserDTO);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully. Please check your email for verification.", response));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> authenticate(@Valid @RequestBody LoginUserDTO loginUserDTO) {
        LoginResponseDTO response = authenticationService.authenticate(loginUserDTO);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            RefreshTokenResponseDTO response = authenticationService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Refresh token is invalid or expired", "INVALID_REFRESH_TOKEN"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@Valid @RequestBody LogoutRequestDTO request, 
    HttpServletRequest httpRequest, HttpServletResponse httpResponse, Authentication authentication) {
        // Pass refresh token to LogoutService via request attribute
        httpRequest.setAttribute("refreshToken", request.getRefreshToken());        
        logoutHandler.logout(httpRequest, httpResponse, authentication);
        SecurityContextHolder.clearContext();
        ApiResponse<?> response = ApiResponse.success("Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
}

