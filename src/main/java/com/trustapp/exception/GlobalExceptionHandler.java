package com.trustapp.exception;

import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.FieldErrorDetail;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        String message = ex.getMessage();
        
        // Check if it's an email not found error
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            // Check for patterns like "user not found with email" or "email" + "not found"
            if ((lowerMessage.contains("email") && lowerMessage.contains("not found")) ||
                lowerMessage.contains("user not found with email")) {
                ApiResponse<String> apiResponse = ApiResponse.error("Email address not found in system", "EMAIL_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }
        }
        
        // Default handling for other resource not found errors
        ApiResponse<String> apiResponse = ApiResponse.error(message, "RESOURCE_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicateResource(DuplicateResourceException ex) {
        String message = ex.getMessage();
        
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            // Check if it's an email duplicate
            if (lowerMessage.contains("email")) {
                ApiResponse<String> apiResponse = ApiResponse.error("Email address is already registered", "EMAIL_ALREADY_EXISTS");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
            }
            
            // Check if it's a username duplicate
            if (lowerMessage.contains("username")) {
                ApiResponse<String> apiResponse = ApiResponse.error("Username is already taken", "USERNAME_ALREADY_EXISTS");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
            }
        }
        
        // Default handling for other duplicate resources
        ApiResponse<String> apiResponse = ApiResponse.error(message, "DUPLICATE_RESOURCE");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        String message = ex.getMessage();
        
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            
            // Check if it's a password reset token error
            if (lowerMessage.contains("reset token") || 
                (lowerMessage.contains("password reset") && (lowerMessage.contains("invalid") || lowerMessage.contains("expired")))) {
                ApiResponse<String> apiResponse = ApiResponse.error("Password reset token is invalid or expired", "INVALID_RESET_TOKEN");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }
            
            // Check if it's a password mismatch error
            if (lowerMessage.contains("password") && lowerMessage.contains("match") ||
                lowerMessage.contains("password and confirm password") ||
                lowerMessage.contains("new password and confirm password")) {
                ApiResponse<String> apiResponse = ApiResponse.error("New password and confirm password don't match", "PASSWORD_MISMATCH");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }
        }
        
        // Default handling for other validation errors
        ApiResponse<String> apiResponse = ApiResponse.error(message, "VALIDATION_ERROR");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        ApiResponse<String> apiResponse = ApiResponse.error("Email or password is incorrect", "INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        boolean hasPasswordError = false;
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.add(new FieldErrorDetail(fieldName, errorMessage));
        });
        
        // Check if there's a password-related validation error
        for (FieldErrorDetail fieldError : fieldErrors) {
            String lowerFieldName = fieldError.getField().toLowerCase();
            String errorMessage = fieldError.getMessage();
            String lowerErrorMessage = errorMessage != null ? errorMessage.toLowerCase() : "";
            
            // Check if it's a password field and the error is about requirements/strength
            if ((lowerFieldName.contains("password") && 
                 (lowerErrorMessage.contains("at least") || 
                  lowerErrorMessage.contains("characters") ||
                  lowerErrorMessage.contains("requirement") ||
                  lowerErrorMessage.contains("strength") ||
                  lowerErrorMessage.contains("must"))) ||
                lowerErrorMessage.contains("password doesn't meet")) {
                hasPasswordError = true;
                break;
            }
        }
        
        // If password validation failed, return WEAK_PASSWORD error
        if (hasPasswordError) {
            ApiResponse<String> apiResponse = ApiResponse.error("Password doesn't meet security requirements", "WEAK_PASSWORD");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }
        
        // Default handling for other validation errors
        ApiResponse<?> apiResponse = ApiResponse.validationError("Validation Failed", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<String>> handleSignatureException(SignatureException ex) {
        ApiResponse<String> apiResponse = ApiResponse.error("Invalid or expired access token", "UNAUTHORIZED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<String>> handleExpiredJwtException(ExpiredJwtException ex) {
        ApiResponse<String> apiResponse = ApiResponse.error("The JWT token has expired", "UNAUTHORIZED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
    }
}

