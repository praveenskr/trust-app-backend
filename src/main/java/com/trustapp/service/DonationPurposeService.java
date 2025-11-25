package com.trustapp.service;

import com.trustapp.dto.DonationPurposeCreateDTO;
import com.trustapp.dto.DonationPurposeDTO;
import com.trustapp.dto.DonationPurposeUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.DonationPurposeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonationPurposeService {
    
    private final DonationPurposeRepository donationPurposeRepository;
    
    public DonationPurposeService(DonationPurposeRepository donationPurposeRepository) {
        this.donationPurposeRepository = donationPurposeRepository;
    }
    
    public List<DonationPurposeDTO> getAllDonationPurposes(boolean includeInactive) {
        return donationPurposeRepository.findAll(includeInactive);
    }
    
    public DonationPurposeDTO getDonationPurposeById(Long id) {
        return donationPurposeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation purpose not found with id: " + id));
    }
    
    @Transactional
    public DonationPurposeDTO createDonationPurpose(DonationPurposeCreateDTO createDTO, Long userId) {
        // Check for duplicate code
        if (donationPurposeRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Donation purpose code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        DonationPurposeDTO purposeDTO = new DonationPurposeDTO();
        purposeDTO.setCode(createDTO.getCode());
        purposeDTO.setName(createDTO.getName());
        purposeDTO.setDescription(createDTO.getDescription());
        purposeDTO.setDisplayOrder(createDTO.getDisplayOrder());
        purposeDTO.setIsActive(createDTO.getIsActive());
        
        Long id = donationPurposeRepository.save(purposeDTO, userId);
        return getDonationPurposeById(id);
    }
    
    @Transactional
    public DonationPurposeDTO updateDonationPurpose(Long id, DonationPurposeUpdateDTO updateDTO, Long userId) {
        // Check if purpose exists
        DonationPurposeDTO existingPurpose = getDonationPurposeById(id);
        
        // Convert UpdateDTO to DTO for updating
        DonationPurposeDTO purposeDTO = new DonationPurposeDTO();
        purposeDTO.setId(id);
        purposeDTO.setName(updateDTO.getName());
        purposeDTO.setDescription(updateDTO.getDescription());
        purposeDTO.setDisplayOrder(updateDTO.getDisplayOrder() != null ? updateDTO.getDisplayOrder() : existingPurpose.getDisplayOrder());
        purposeDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingPurpose.getIsActive());
        
        donationPurposeRepository.update(purposeDTO, userId);
        return getDonationPurposeById(id);
    }
    
    @Transactional
    public void deleteDonationPurpose(Long id, Long userId) {
        // Check if purpose exists
        getDonationPurposeById(id);
        
        // Delete will check for sub-categories and throw exception if they exist
        try {
            donationPurposeRepository.delete(id, userId);
        } catch (IllegalStateException e) {
            // Convert IllegalStateException to ValidationException for proper error handling
            throw new ValidationException(e.getMessage());
        }
    }
}

