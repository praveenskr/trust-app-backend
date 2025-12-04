package com.trustapp.service;

import com.trustapp.dto.BranchCreateDTO;
import com.trustapp.dto.BranchDTO;
import com.trustapp.dto.BranchDropdownDTO;
import com.trustapp.dto.BranchStatisticsDTO;
import com.trustapp.dto.BranchUpdateDTO;
import com.trustapp.dto.UserDTO;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ForbiddenException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.BranchRepository;
import com.trustapp.repository.RoleRepository;
import com.trustapp.repository.UserBranchAccessRepository;
import com.trustapp.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BranchService {
    
    private final BranchRepository branchRepository;
    private final AuthenticationService authenticationService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserBranchAccessRepository userBranchAccessRepository;
    
    public BranchService(BranchRepository branchRepository, 
                        AuthenticationService authenticationService,
                        UserRoleRepository userRoleRepository,
                        RoleRepository roleRepository,
                        UserBranchAccessRepository userBranchAccessRepository) {
        this.branchRepository = branchRepository;
        this.authenticationService = authenticationService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userBranchAccessRepository = userBranchAccessRepository;
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
    
    public PageResponseDTO<BranchDTO> getAllBranches(Boolean includeInactive, String city, String state, 
                                                      String search, Integer page, Integer size, 
                                                      String sortBy, String sortDir) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if user is super user
        boolean isSuper = isSuperUser(userId);
        
        // Get accessible branch IDs (null for super users means all branches)
        List<Long> accessibleBranchIds = isSuper ? null : userBranchAccessRepository.findBranchIdsByUserId(userId);
        
        // Set defaults
        boolean includeInactiveFlag = includeInactive != null && includeInactive;
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        String sortField = sortBy != null ? sortBy : "name";
        String sortDirection = sortDir != null ? sortDir : "ASC";
        
        // Get branches with filters
        List<BranchDTO> branches = branchRepository.findAll(
            includeInactiveFlag, city, state, search, accessibleBranchIds, 
            pageNum, pageSize, sortField, sortDirection
        );
        
        // Get total count
        long totalElements = branchRepository.count(
            includeInactiveFlag, city, state, search, accessibleBranchIds
        );
        
        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageNum == 0;
        boolean isLast = pageNum >= totalPages - 1;
        int numberOfElements = branches.size();
        
        PageResponseDTO<BranchDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(branches);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages(totalPages);
        pageResponse.setSize(pageSize);
        pageResponse.setNumber(pageNum);
        pageResponse.setFirst(isFirst);
        pageResponse.setLast(isLast);
        pageResponse.setNumberOfElements(numberOfElements);
        
        return pageResponse;
    }
    
    public BranchDTO getBranchById(Long id) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if branch exists
        BranchDTO branch = branchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
        
        // Check if user is super user
        boolean isSuper = isSuperUser(userId);
        
        // If not super user, check branch access
        if (!isSuper) {
            boolean hasAccess = userBranchAccessRepository.hasAccessToBranch(userId, id);
            if (!hasAccess) {
                throw new ForbiddenException("Access denied. You do not have permission to access this branch.");
            }
        }
        
        return branch;
    }
    
    @Transactional
    public BranchDTO createBranch(BranchCreateDTO createDTO, Long createdBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if user is super user
        if (!isSuperUser(userId)) {
            throw new ForbiddenException("Access denied. Only super users can create branches.");
        }
        
        // Use provided createdBy if specified, otherwise use authenticated user
        Long creatorId = createdBy != null ? createdBy : userId;
        
        // Check for duplicate code
        if (branchRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Branch code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setCode(createDTO.getCode());
        branchDTO.setName(createDTO.getName());
        branchDTO.setAddress(createDTO.getAddress());
        branchDTO.setCity(createDTO.getCity());
        branchDTO.setState(createDTO.getState());
        branchDTO.setPincode(createDTO.getPincode());
        branchDTO.setPhone(createDTO.getPhone());
        branchDTO.setEmail(createDTO.getEmail());
        branchDTO.setContactPerson(createDTO.getContactPerson());
        branchDTO.setIsActive(createDTO.getIsActive());
        
        Long id = branchRepository.save(branchDTO, creatorId);
        return getBranchById(id);
    }
    
    @Transactional
    public BranchDTO updateBranch(Long id, BranchUpdateDTO updateDTO, Long updatedBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if user is super user
        if (!isSuperUser(userId)) {
            throw new ForbiddenException("Access denied. Only super users can update branches.");
        }
        
        // Check if branch exists
        BranchDTO existingBranch = getBranchById(id);
        
        // Check if branch has active transactions (donations, expenses, or events)
        if (branchRepository.hasActiveTransactions(id)) {
            throw new ValidationException("Cannot update branch. Branch has active donations, expenses or events. Please deactivate them first.");
        }
        
        // Use provided updatedBy if specified, otherwise use authenticated user
        Long updaterId = updatedBy != null ? updatedBy : userId;
        
        // Convert UpdateDTO to DTO for updating
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setId(id);
        branchDTO.setCode(existingBranch.getCode()); // Code is not updatable
        branchDTO.setName(updateDTO.getName());
        branchDTO.setAddress(updateDTO.getAddress());
        branchDTO.setCity(updateDTO.getCity());
        branchDTO.setState(updateDTO.getState());
        branchDTO.setPincode(updateDTO.getPincode());
        branchDTO.setPhone(updateDTO.getPhone());
        branchDTO.setEmail(updateDTO.getEmail());
        branchDTO.setContactPerson(updateDTO.getContactPerson());
        branchDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingBranch.getIsActive());
        
        branchRepository.update(branchDTO, updaterId);
        return getBranchById(id);
    }
    
    @Transactional
    public void deleteBranch(Long id, Long deletedBy) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if user is super user
        if (!isSuperUser(userId)) {
            throw new ForbiddenException("Access denied. Only super users can delete branches.");
        }
        
        // Check if branch exists
        getBranchById(id);
        
        // Use provided deletedBy if specified, otherwise use authenticated user
        Long deleterId = deletedBy != null ? deletedBy : userId;
        
        // Delete will check for active transactions (donations, expenses, events, or users) and throw exception if they exist
        try {
            branchRepository.delete(id, deleterId);
        } catch (IllegalStateException e) {
            // Convert IllegalStateException to ValidationException for proper error handling
            throw new ValidationException(e.getMessage());
        }
    }
    
    public BranchStatisticsDTO getBranchStatistics(Long id, LocalDate fromDate, LocalDate toDate) {
        // Check if branch exists and user has access (or is super user)
        // This will throw exception if branch doesn't exist or user doesn't have access
        getBranchById(id);
        
        // Get statistics
        return branchRepository.getBranchStatistics(id, fromDate, toDate);
    }
    
    /**
     * Returns all active branches for dropdowns, without applying any user access checks.
     * Intended for admin-style screens like User Branch Access where all branches
     * need to be visible for assignment.
     */
    public List<BranchDropdownDTO> getAllActiveBranchesForDropdown() {
        // Passing null for accessibleBranchIds means "no branch access filter" in repository
        return branchRepository.findAllForDropdown(null);
    }
    
    /**
     * Returns only the branches that the current user has access to.
     * This is used on transaction and reporting screens where users should
     * see only their permitted branches.
     */
    public List<BranchDropdownDTO> getUserAccessibleBranchesForDropdown() {
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Get accessible branch IDs (null for super users, list for regular users)
        List<Long> accessibleBranchIds = isSuperUser(userId) 
            ? null 
            : userBranchAccessRepository.findBranchIdsByUserId(userId);
        
        // If user has no accessible branches, return empty list
        if (accessibleBranchIds != null && accessibleBranchIds.isEmpty()) {
            return List.of();
        }
        
        // Return active branches that user has access to
        return branchRepository.findAllForDropdown(accessibleBranchIds);
    }
}

