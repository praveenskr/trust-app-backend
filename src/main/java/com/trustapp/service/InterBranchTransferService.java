package com.trustapp.service;

import com.trustapp.dto.InterBranchTransferCreateDTO;
import com.trustapp.dto.InterBranchTransferDTO;
import com.trustapp.dto.InterBranchTransferStatusUpdateDTO;
import com.trustapp.dto.UserDTO;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ForbiddenException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InterBranchTransferService {
    
    private final InterBranchTransferRepository interBranchTransferRepository;
    private final BranchRepository branchRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final SerialNumberConfigRepository serialNumberConfigRepository;
    private final UserRepository userRepository;
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    public InterBranchTransferService(
            InterBranchTransferRepository interBranchTransferRepository,
            BranchRepository branchRepository,
            PaymentModeRepository paymentModeRepository,
            SerialNumberConfigRepository serialNumberConfigRepository,
            UserRepository userRepository,
            UserBranchAccessRepository userBranchAccessRepository,
            AuthenticationService authenticationService,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository) {
        this.interBranchTransferRepository = interBranchTransferRepository;
        this.branchRepository = branchRepository;
        this.paymentModeRepository = paymentModeRepository;
        this.serialNumberConfigRepository = serialNumberConfigRepository;
        this.userRepository = userRepository;
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.authenticationService = authenticationService;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }
    
    private boolean isSuperUser(Long userId) {
        var superUserRole = roleRepository.findByCode("SUPER_USER");
        if (superUserRole.isEmpty()) {
            return false;
        }
        Long superUserRoleId = superUserRole.get().getId();
        var roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        return roleIds.contains(superUserRoleId);
    }
    
    @Transactional
    public InterBranchTransferDTO createTransfer(InterBranchTransferCreateDTO createDTO, Long createdBy) {
        // Get authenticated user
        var currentUser = authenticationService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        
        // Use provided createdBy if specified, otherwise use authenticated user
        Long creatorId = createdBy != null ? createdBy : currentUserId;
        
        // Validate user exists
        userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));
        
        // Validate fromBranchId exists and is active
        branchRepository.findById(createDTO.getFromBranchId())
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + createDTO.getFromBranchId()));
        
        // Validate toBranchId exists and is active
        branchRepository.findById(createDTO.getToBranchId())
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + createDTO.getToBranchId()));
        
        // Validate fromBranchId != toBranchId
        if (createDTO.getFromBranchId().equals(createDTO.getToBranchId())) {
            throw new ValidationException("Source branch and destination branch must be different");
        }
        
        // Check if user has access to source branch (fromBranchId)
        boolean isSuper = isSuperUser(currentUserId);
        if (!isSuper) {
            boolean hasAccess = userBranchAccessRepository.hasAccessToBranch(currentUserId, createDTO.getFromBranchId());
            if (!hasAccess) {
                throw new ForbiddenException("Access denied. You do not have permission to access the source branch.");
            }
        }
        
        // Validate payment mode exists and is active
        paymentModeRepository.findById(createDTO.getPaymentModeId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode not found with id: " + createDTO.getPaymentModeId()));
        
        // Validate status if provided
        String status = createDTO.getStatus();
        if (status != null && !status.isEmpty()) {
            if (!status.equals("PENDING") && !status.equals("COMPLETED") && !status.equals("CANCELLED")) {
                throw new ValidationException("Status must be one of: PENDING, COMPLETED, CANCELLED");
            }
        } else {
            status = "PENDING"; // Default status
        }
        
        // Generate transfer number
        String transferNumber;
        try {
            transferNumber = serialNumberConfigRepository.getNextSerialNumber("INTER_BRANCH_TRANSFER");
        } catch (IllegalStateException e) {
            throw new ResourceNotFoundException("Serial number config not found for entity: INTER_BRANCH_TRANSFER");
        }
        
        // Check for duplicate transfer number (shouldn't happen, but safety check)
        if (interBranchTransferRepository.existsByTransferNumber(transferNumber)) {
            throw new DuplicateResourceException("Transfer number already exists: " + transferNumber);
        }
        
        // Create transfer
        Long transferId = interBranchTransferRepository.save(
            transferNumber,
            createDTO.getFromBranchId(),
            createDTO.getToBranchId(),
            createDTO.getAmount(),
            createDTO.getTransferDate(),
            createDTO.getPaymentModeId(),
            createDTO.getReferenceNumber(),
            createDTO.getDescription(),
            status,
            creatorId
        );
        
        return getTransferById(transferId);
    }
    
    public InterBranchTransferDTO getTransferById(Long id) {
        return interBranchTransferRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inter-branch transfer not found with id: " + id));
    }
    
    public PageResponseDTO<InterBranchTransferDTO> getAllTransfers(Long fromBranchId, Long toBranchId, String status,
                                                                   LocalDate fromDate, LocalDate toDate,
                                                                   Integer page, Integer size, String sortBy, String sortDir) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();
        
        // Check if user is super user
        boolean isSuper = isSuperUser(userId);
        
        // Get accessible branch IDs (null for super users means all transfers)
        List<Long> accessibleBranchIds = isSuper ? null : userBranchAccessRepository.findBranchIdsByUserId(userId);
        
        // Set defaults
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        String sortField = sortBy != null ? sortBy : "transferDate";
        String sortDirection = sortDir != null ? sortDir : "DESC";
        
        // Get transfers with filters
        List<InterBranchTransferDTO> transfers = interBranchTransferRepository.findAll(
            fromBranchId, toBranchId, status, fromDate, toDate,
            accessibleBranchIds, pageNum, pageSize, sortField, sortDirection
        );
        
        // Get total count
        long totalElements = interBranchTransferRepository.count(
            fromBranchId, toBranchId, status, fromDate, toDate, accessibleBranchIds
        );
        
        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageNum == 0;
        boolean isLast = pageNum >= totalPages - 1;
        int numberOfElements = transfers.size();
        
        PageResponseDTO<InterBranchTransferDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(transfers);
        pageResponse.setTotalElements(totalElements);
        pageResponse.setTotalPages(totalPages);
        pageResponse.setSize(pageSize);
        pageResponse.setNumber(pageNum);
        pageResponse.setFirst(isFirst);
        pageResponse.setLast(isLast);
        pageResponse.setNumberOfElements(numberOfElements);
        
        return pageResponse;
    }

    @Transactional
    public InterBranchTransferDTO updateTransferStatus(Long id, InterBranchTransferStatusUpdateDTO updateDTO) {
        // Get authenticated user
        UserDTO currentUser = authenticationService.getCurrentUser();
        Long userId = currentUser.getId();

        // Get existing transfer
        InterBranchTransferDTO existing = getTransferById(id);

        // Check branch access: user must have access to either from or to branch, unless super user
        boolean isSuper = isSuperUser(userId);
        if (!isSuper) {
            Long fromBranchId = existing.getFromBranch() != null ? existing.getFromBranch().getId() : null;
            Long toBranchId = existing.getToBranch() != null ? existing.getToBranch().getId() : null;

            boolean hasFromAccess = fromBranchId != null && userBranchAccessRepository.hasAccessToBranch(userId, fromBranchId);
            boolean hasToAccess = toBranchId != null && userBranchAccessRepository.hasAccessToBranch(userId, toBranchId);

            if (!hasFromAccess && !hasToAccess) {
                throw new ForbiddenException("Access denied. You do not have permission to update this transfer.");
            }
        }

        // DTO already validates allowed status values
        String newStatus = updateDTO.getStatus();

        // Determine reference number: use provided if not null, otherwise keep existing
        String newReferenceNumber = updateDTO.getReferenceNumber() != null
            ? updateDTO.getReferenceNumber()
            : existing.getReferenceNumber();

        // Perform update
        int updated = interBranchTransferRepository.updateStatus(id, newStatus, newReferenceNumber, userId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Inter-branch transfer not found or inactive with id: " + id);
        }

        // Return updated transfer
        return getTransferById(id);
    }
}

