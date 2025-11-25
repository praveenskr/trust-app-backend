package com.trustapp.service;

import com.trustapp.dto.PasswordResetDTO;
import com.trustapp.dto.PasswordResetRequestDTO;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.PasswordResetTokenRepository;
import com.trustapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {
    
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final int TOKEN_LENGTH = 32;
    
    public PasswordResetService(
            JavaMailSender mailSender,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.mailSender = mailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        // Find user by email
        var user = userRepository.findByEmail(requestDTO.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requestDTO.getEmail()));
        
        // Check if user is active
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new ValidationException("Cannot reset password for inactive user");
        }
        
        // Invalidate existing tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user.getId());
        
        // Generate secure token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
        
        // Save token
        passwordResetTokenRepository.save(user.getId(), token, expiresAt);
        
        // Send email with reset link
        sendPasswordResetEmail(user.getEmail(), token);
    }
    
    @Transactional
    public void resetPassword(PasswordResetDTO resetDTO) {
        // Validate passwords match
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        
        // Find user by token
        Long userId = passwordResetTokenRepository.findUserIdByToken(resetDTO.getToken())
            .orElseThrow(() -> new ValidationException("Invalid or expired password reset token"));
        
        // Verify user exists and is active
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new ValidationException("Cannot reset password for inactive user");
        }
        
        // Update password
        String passwordHash = passwordEncoder.encode(resetDTO.getNewPassword());
        userRepository.updatePassword(userId, passwordHash);
        
        // Mark token as used
        passwordResetTokenRepository.markTokenAsUsed(resetDTO.getToken());
        
        // Invalidate all other tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(userId);
    }
    
    public boolean validateToken(String token) {
        return passwordResetTokenRepository.findUserIdByToken(token).isPresent();
    }
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    public void sendPasswordResetEmail(String email, String token) {
        String resetUrl = frontendUrl + "/reset-password/" + token;
        String body = "Click the link below to reset your password.\n\n" + resetUrl;
    
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset");
        message.setText(body);
    
        mailSender.send(message);
    }
}

