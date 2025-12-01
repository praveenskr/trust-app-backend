package com.trustapp.service;

import com.trustapp.dto.BranchAccessCheckDTO;
import com.trustapp.dto.BranchAccessDTO;
import com.trustapp.exception.ForbiddenException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.BranchRepository;
import com.trustapp.repository.RoleRepository;
import com.trustapp.repository.UserBranchAccessRepository;
import com.trustapp.repository.UserRepository;
import com.trustapp.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserBranchAccessService {
    
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    public UserBranchAccessService(
            UserBranchAccessRepository userBranchAccessRepository,
            UserRepository userRepository,
            BranchRepository branchRepository,
            AuthenticationService authenticationService,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.authenticationService = authenticationService;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
    
    private boolean isSuperUser(Long userId) {
        // Get SUPER_USER role
        var superUserRole = roleRepository.findByCode("SUPER_USER");
        if (superUserRole.isEmpty()) {
            return false;
        }
        
        Long superUserRoleId = superUserRole.get().getId();
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        return roleIds.contains(superUserRoleId);
    }
    
    public List<BranchAccessDTO> getUserBranchAccess(Long userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Get branch access with branch details
        return userBranchAccessRepository.findByUserId(userId);
    }
    
    @Transactional
    public List<BranchAccessDTO> assignBranches(Long userId, List<Long> branchIds, Long grantedBy) {
        // Get authenticated user
        var currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Check if current user is super user
        if (!isSuperUser(currentUserId)) {
            throw new ForbiddenException("Access denied. Only super users can assign branch access.");
        }
        
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate branches exist and are active (if branchIds is provided and not empty)
        if (branchIds != null && !branchIds.isEmpty()) {
            validateBranches(branchIds);
        }
        
        // Use provided grantedBy if specified, otherwise use authenticated user
        Long grantorId = grantedBy != null ? grantedBy : currentUserId;
        
        // Assign branches (replaces existing)
        // If branchIds is null or empty, this will remove all branch access
        userBranchAccessRepository.assignBranches(userId, branchIds, grantorId);
        
        return getUserBranchAccess(userId);
    }
    
    private void validateBranches(List<Long> branchIds) {
        // Remove duplicates
        List<Long> uniqueBranchIds = branchIds.stream().distinct().collect(Collectors.toList());
        
        // Find which branch IDs exist and are active
        List<Long> existingActiveBranchIds = branchRepository.findActiveBranchIdsByIds(uniqueBranchIds);
        
        // Check if all provided branch IDs exist and are active
        Set<Long> existingSet = Set.copyOf(existingActiveBranchIds);
        List<Long> invalidBranchIds = uniqueBranchIds.stream()
            .filter(id -> !existingSet.contains(id))
            .collect(Collectors.toList());
        
        if (!invalidBranchIds.isEmpty()) {
            throw new ValidationException(
                "Invalid branch IDs. The following branches do not exist or are inactive: " + invalidBranchIds
            );
        }
    }
    
    @Transactional
    public void removeAllBranches(Long userId) {
        // Get authenticated user
        var currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Check if current user is super user
        if (!isSuperUser(currentUserId)) {
            throw new ForbiddenException("Access denied. Only super users can remove branch access.");
        }
        
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        userBranchAccessRepository.removeAllBranches(userId);
    }
    
    public BranchAccessCheckDTO checkBranchAccess(Long userId, Long branchId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Verify branch exists and get branch details
        var branch = branchRepository.findById(branchId)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));
        
        // Check if user is super user (has access to all branches)
        boolean isSuper = isSuperUser(userId);
        
        BranchAccessCheckDTO checkDTO = new BranchAccessCheckDTO();
        checkDTO.setUserId(userId);
        checkDTO.setBranchId(branchId);
        
        if (isSuper) {
            // Super users have access to all branches
            checkDTO.setHasAccess(true);
            checkDTO.setBranchName(branch.getName());
            checkDTO.setBranchCode(branch.getCode());
        } else {
            // Check branch access from user_branch_access table
            BranchAccessDTO access = userBranchAccessRepository.findAccessByUserIdAndBranchId(userId, branchId);
            if (access != null) {
                checkDTO.setHasAccess(true);
                checkDTO.setBranchName(access.getBranchName());
                checkDTO.setBranchCode(access.getBranchCode());
                checkDTO.setGrantedAt(access.getGrantedAt());
            } else {
                checkDTO.setHasAccess(false);
            }
        }
        
        return checkDTO;
    }
    
    public boolean hasAccessToBranch(Long userId, Long branchId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user is super user (has access to all branches)
        boolean isSuper = isSuperUser(userId);
        if (isSuper) {
            return true;
        }
        
        // Check branch access
        return userBranchAccessRepository.hasAccessToBranch(userId, branchId);
    }
}

