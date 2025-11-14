package com.trustapp.service;

import com.trustapp.dto.BranchAccessDTO;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.UserBranchAccessRepository;
import com.trustapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserBranchAccessService {
    
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final UserRepository userRepository;
    
    public UserBranchAccessService(
            UserBranchAccessRepository userBranchAccessRepository,
            UserRepository userRepository) {
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.userRepository = userRepository;
    }
    
    public List<BranchAccessDTO> getUserBranchAccess(Long userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Long> branchIds = userBranchAccessRepository.findBranchIdsByUserId(userId);
        
        // Convert to BranchAccessDTO
        // Note: This would typically join with branches table to get branch details
        return branchIds.stream()
            .map(branchId -> {
                BranchAccessDTO dto = new BranchAccessDTO();
                dto.setUserId(userId);
                dto.setBranchId(branchId);
                // Branch name and code would be loaded from branch repository
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<BranchAccessDTO> assignBranches(Long userId, List<Long> branchIds, Long grantedBy) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate branches exist
        // Note: This would typically validate against branch repository
        
        // Assign branches (replaces existing)
        userBranchAccessRepository.assignBranches(userId, branchIds, grantedBy);
        
        return getUserBranchAccess(userId);
    }
    
    @Transactional
    public void removeAllBranches(Long userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        userBranchAccessRepository.removeAllBranches(userId);
    }
    
    public boolean hasAccessToBranch(Long userId, Long branchId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user is super user (has access to all branches)
        // Note: This would typically check user roles
        
        // Check branch access
        return userBranchAccessRepository.hasAccessToBranch(userId, branchId);
    }
}

