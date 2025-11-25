package com.trustapp.service;

import com.trustapp.dto.*;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DonationService {
    
    private final DonationRepository donationRepository;
    private final PaymentModeRepository paymentModeRepository;
    private final DonationPurposeRepository donationPurposeRepository;
    private final DonationSubCategoryRepository donationSubCategoryRepository;
    private final EventRepository eventRepository;
    private final BranchRepository branchRepository;
    private final SerialNumberConfigRepository serialNumberConfigRepository;
    private final UserRepository userRepository;
    
    public DonationService(
            DonationRepository donationRepository,
            PaymentModeRepository paymentModeRepository,
            DonationPurposeRepository donationPurposeRepository,
            DonationSubCategoryRepository donationSubCategoryRepository,
            EventRepository eventRepository,
            BranchRepository branchRepository,
            SerialNumberConfigRepository serialNumberConfigRepository,
            UserRepository userRepository) {
        this.donationRepository = donationRepository;
        this.paymentModeRepository = paymentModeRepository;
        this.donationPurposeRepository = donationPurposeRepository;
        this.donationSubCategoryRepository = donationSubCategoryRepository;
        this.eventRepository = eventRepository;
        this.branchRepository = branchRepository;
        this.serialNumberConfigRepository = serialNumberConfigRepository;
        this.userRepository = userRepository;
    }
    
    public DonationDTO getDonationById(Long id) {
        return donationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation transaction not found with id: " + id));
    }
    
    public PageResponseDTO<DonationDTO> getAllDonations(Long branchId, Long purposeId, Long eventId,
                                                         Long paymentModeId, LocalDate fromDate, LocalDate toDate,
                                                         String donorName, String panNumber, String receiptNumber,
                                                         Boolean includeInactive, Integer page, Integer size,
                                                         String sortBy, String sortDir) {
        // Set defaults
        boolean includeInactiveFlag = includeInactive != null && includeInactive;
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        String sortField = sortBy != null ? sortBy : "donationDate";
        String sortDirection = sortDir != null ? sortDir : "DESC";
        
        // Get donations
        List<DonationDTO> donations = donationRepository.findAll(
            branchId, purposeId, eventId, paymentModeId,
            fromDate, toDate, donorName, panNumber, receiptNumber,
            includeInactiveFlag, pageNum, pageSize, sortField, sortDirection
        );
        
        // Get total count
        long totalElements = donationRepository.count(
            branchId, purposeId, eventId, paymentModeId,
            fromDate, toDate, donorName, panNumber, receiptNumber,
            includeInactiveFlag
        );
        
        // Calculate pagination metadata
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = pageNum == 0;
        boolean isLast = pageNum >= totalPages - 1;
        int numberOfElements = donations.size();
        
        PageResponseDTO<DonationDTO> pageResponse = new PageResponseDTO<>();
        pageResponse.setContent(donations);
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
    public DonationDTO createDonation(DonationCreateDTO createDTO, Long createdBy) {
        // Validate user exists (createdBy)
        userRepository.findById(createdBy)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createdBy));
        
        // Validate payment mode exists and is active
        PaymentModeDTO paymentMode = paymentModeRepository.findById(createDTO.getPaymentModeId())
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode not found with id: " + createDTO.getPaymentModeId()));
        
        // Validate purpose exists and is active
        DonationPurposeDTO purpose = donationPurposeRepository.findById(createDTO.getPurposeId())
            .orElseThrow(() -> new ResourceNotFoundException("Donation purpose not found with id: " + createDTO.getPurposeId()));
        
        // Validate sub-category if provided
        DonationSubCategoryDTO subCategory = null;
        if (createDTO.getSubCategoryId() != null) {
            subCategory = donationSubCategoryRepository.findById(createDTO.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Donation sub-category not found with id: " + createDTO.getSubCategoryId()));
            
            // Validate sub-category belongs to the specified purpose
            if (!subCategory.getPurposeId().equals(createDTO.getPurposeId())) {
                throw new ValidationException("Sub-category does not belong to the specified purpose");
            }
        }
        
        // Validate event if provided
        EventDTO event = null;
        if (createDTO.getEventId() != null) {
            event = eventRepository.findById(createDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + createDTO.getEventId()));
        }
        
        // Validate branch exists and is active
        BranchDTO branch = branchRepository.findById(createDTO.getBranchId())
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + createDTO.getBranchId()));
        
        // Validate donation date is not in the future
        if (createDTO.getDonationDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Donation date cannot be in the future");
        }
        
        // Generate receipt number
        String receiptNumber;
        try {
            receiptNumber = serialNumberConfigRepository.getNextSerialNumber("DONATION");
        } catch (IllegalStateException e) {
            throw new ResourceNotFoundException("Serial number config not found for entity: DONATION");
        }
        
        // Check for duplicate receipt number (shouldn't happen, but safety check)
        if (donationRepository.existsByReceiptNumber(receiptNumber)) {
            throw new DuplicateResourceException("Receipt number already exists: " + receiptNumber);
        }
        
        // Create donation
        Long donationId = donationRepository.save(
            receiptNumber,
            createDTO.getDonorName(),
            createDTO.getDonorAddress(),
            createDTO.getPanNumber(),
            createDTO.getDonorPhone(),
            createDTO.getDonorEmail(),
            createDTO.getAmount(),
            createDTO.getPaymentModeId(),
            createDTO.getPurposeId(),
            createDTO.getSubCategoryId(),
            createDTO.getEventId(),
            createDTO.getBranchId(),
            createDTO.getDonationDate(),
            createDTO.getNotes(),
            createdBy
        );
        
        return getDonationById(donationId);
    }
}

