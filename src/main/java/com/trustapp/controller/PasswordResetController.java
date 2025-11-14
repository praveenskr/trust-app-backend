package com.trustapp.controller;

import com.trustapp.dto.PasswordResetDTO;
import com.trustapp.dto.PasswordResetRequestDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password-reset")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        passwordResetService.requestPasswordReset(requestDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success("Password reset email sent successfully"));
    }
    
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        passwordResetService.resetPassword(resetDTO);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
    
    @GetMapping("/validate/{token}")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}

