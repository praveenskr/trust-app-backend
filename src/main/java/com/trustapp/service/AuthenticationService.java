package com.trustapp.service;

import com.trustapp.dto.LoginResponseDTO;
import com.trustapp.dto.LoginUserDTO;
import com.trustapp.dto.RegisterUserDTO;
import com.trustapp.dto.RegisterUserResponseDTO;
import com.trustapp.dto.UserDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.model.Token;
import com.trustapp.model.User;
import com.trustapp.repository.RoleRepository;
import com.trustapp.repository.TokenRepository;
import com.trustapp.repository.UserRepository;
import com.trustapp.repository.UserRoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    
    public AuthenticationService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }
    
    @Transactional
    public RegisterUserResponseDTO register(RegisterUserDTO registerUserDTO) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(registerUserDTO.getUsername(), null)) {
            throw new DuplicateResourceException("Username already exists: " + registerUserDTO.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(registerUserDTO.getEmail(), null)) {
            throw new DuplicateResourceException("Email already exists: " + registerUserDTO.getEmail());
        }
        
        // Validate roles exist
        validateRoles(registerUserDTO.getRoleIds());
        
        // Create user
        UserDTO user = new UserDTO();
        user.setUsername(registerUserDTO.getUsername());
        user.setEmail(registerUserDTO.getEmail());
        user.setFullName(registerUserDTO.getFullName());
        user.setIsActive(true);
        
        String passwordHash = passwordEncoder.encode(registerUserDTO.getPassword());
        Long userId = userRepository.save(user, passwordHash, null);
        
        // Assign roles
        userRoleRepository.assignRoles(userId, registerUserDTO.getRoleIds(), null);
        
        // Reload user to get all fields including timestamps
        UserDTO savedUser = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found after creation"));
        
        return RegisterUserResponseDTO.builder()
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .fullName(savedUser.getFullName())
            .isEmailVerified(false) // Email verification not implemented yet
            .createdAt(savedUser.getCreatedAt())
            .build();
    }
    
    public LoginResponseDTO authenticate(LoginUserDTO loginUserDTO) {
        String email = loginUserDTO.getEmail();
        
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                email,
                loginUserDTO.getPassword()
            )
        );
        
        User authenticatedUser = userRepository.findByEmailForAuthentication(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        String jwtToken = jwtService.generateToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);
        
        // Save tokens to database
        saveBearerToken(authenticatedUser, jwtToken);
        saveRefreshToken(authenticatedUser, refreshToken);
        
        // Update last login time
        userRepository.resetFailedLoginAttempts(authenticatedUser.getId());
        
        LoginResponseDTO.UserDTO userDTO = LoginResponseDTO.UserDTO.builder()
            .id(authenticatedUser.getId())
            .username(authenticatedUser.getUsername())
            .email(authenticatedUser.getEmail())
            .fullName(authenticatedUser.getFullName())
            .phone(authenticatedUser.getPhone())
            .isActive(authenticatedUser.getIsActive())
            .build();
        
        LoginResponseDTO.TokensDTO tokensDTO = LoginResponseDTO.TokensDTO.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtService.getExpirationTime())
            .tokenType("Bearer")
            .build();
        
        return LoginResponseDTO.builder()
            .user(userDTO)
            .tokens(tokensDTO)
            .build();
    }
    
    private void saveBearerToken(User user, String jwtToken) {
        var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .type(Token.TokenType.BEARER)
            .revoked(false)
            .expired(false)
            .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getExpirationTime())))
            .build();
        
        tokenRepository.saveUserToken(token);
    }
    
    private void saveRefreshToken(User user, String refreshToken) {
        var token = Token.builder()
            .user(user)
            .token(refreshToken)
            .type(Token.TokenType.REFRESH)
            .revoked(false)
            .expired(false)
            .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshExpirationTime())))
            .build();
        
        tokenRepository.saveUserToken(token);
    }
    
    private void validateRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ValidationException("At least one role must be assigned");
        }
        
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        }
    }
}

