package com.trustapp.service;

import com.trustapp.dto.*;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            UserBranchAccessRepository userBranchAccessRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<UserDTO> getAllUsers(boolean includeInactive) {
        List<UserDTO> users = userRepository.findAll(includeInactive);
        // Enrich with roles and branch access
        return users.stream()
            .map(this::enrichUser)
            .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long id) {
        UserDTO user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return enrichUser(user);
    }
    
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO, Long createdBy) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(userCreateDTO.getUsername(), null)) {
            throw new DuplicateResourceException("Username already exists: " + userCreateDTO.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(userCreateDTO.getEmail(), null)) {
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }
        
        // Validate roles exist
        validateRoles(userCreateDTO.getRoleIds());
        
        // Create user
        UserDTO user = new UserDTO();
        user.setUsername(userCreateDTO.getUsername());
        user.setEmail(userCreateDTO.getEmail());
        user.setFullName(userCreateDTO.getFullName());
        user.setPhone(userCreateDTO.getPhone());
        user.setIsActive(true);
        
        String passwordHash = passwordEncoder.encode(userCreateDTO.getPassword());
        Long userId = userRepository.save(user, passwordHash, createdBy);
        
        // Assign roles
        userRoleRepository.assignRoles(userId, userCreateDTO.getRoleIds(), createdBy);
        
        // Assign branch access (if not super user)
        if (userCreateDTO.getBranchIds() != null && !userCreateDTO.getBranchIds().isEmpty()) {
            userBranchAccessRepository.assignBranches(userId, userCreateDTO.getBranchIds(), createdBy);
        }
        
        return getUserById(userId);
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO, Long updatedBy) {
        // Check if user exists
        UserDTO existingUser = getUserById(id);
        
        // Validate email uniqueness
        if (userUpdateDTO.getEmail() != null && 
            userRepository.existsByEmail(userUpdateDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + userUpdateDTO.getEmail());
        }
        
        // Update user
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setEmail(userUpdateDTO.getEmail() != null ? userUpdateDTO.getEmail() : existingUser.getEmail());
        user.setFullName(userUpdateDTO.getFullName() != null ? userUpdateDTO.getFullName() : existingUser.getFullName());
        user.setPhone(userUpdateDTO.getPhone());
        user.setIsActive(userUpdateDTO.getIsActive() != null ? userUpdateDTO.getIsActive() : existingUser.getIsActive());
        
        userRepository.update(user, updatedBy);
        
        // Update roles if provided
        if (userUpdateDTO.getRoleIds() != null) {
            validateRoles(userUpdateDTO.getRoleIds());
            userRoleRepository.assignRoles(id, userUpdateDTO.getRoleIds(), updatedBy);
        }
        
        // Update branch access if provided
        if (userUpdateDTO.getBranchIds() != null) {
            userBranchAccessRepository.assignBranches(id, userUpdateDTO.getBranchIds(), updatedBy);
        }
        
        return getUserById(id);
    }
    
    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        UserDTO user = getUserById(userId);
        
        // Get password hash for verification
        String currentPasswordHash = userRepository.findPasswordHashByUsername(user.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User password not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), currentPasswordHash)) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Validate new password matches confirm password
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        
        String newPasswordHash = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        userRepository.updatePassword(userId, newPasswordHash);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        getUserById(id);
        
        // Check if user is a system user (cannot be deleted)
        // Implementation depends on your business rules
        
        userRepository.delete(id);
    }
    
    @Transactional
    public void unlockUser(Long id, Long unlockedBy) {
        // Check if user exists
        getUserById(id);
        // Reset failed login attempts
        userRepository.resetFailedLoginAttempts(id);
    }
    
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, passwordHash);
    }
    
    private UserDTO enrichUser(UserDTO user) {
        // Load roles
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        // Load branch access
        List<Long> branchIds = userBranchAccessRepository.findBranchIdsByUserId(user.getId());
        
        user.setRoleIds(roleIds);
        user.setBranchIds(branchIds);
        
        return user;
    }
    
    public List<UserDropdownDTO> getAllActiveUsersForDropdown() {
        return userRepository.findAllActiveForDropdown();
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

