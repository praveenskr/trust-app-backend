package com.trustapp.service;

import com.trustapp.dto.DonationSubCategoryCreateDTO;
import com.trustapp.dto.DonationSubCategoryDTO;
import com.trustapp.dto.DonationSubCategoryDropdownDTO;
import com.trustapp.dto.DonationSubCategoryUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.DonationPurposeRepository;
import com.trustapp.repository.DonationSubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonationSubCategoryService {
    
    private final DonationSubCategoryRepository donationSubCategoryRepository;
    private final DonationPurposeRepository donationPurposeRepository;
    
    public DonationSubCategoryService(
            DonationSubCategoryRepository donationSubCategoryRepository,
            DonationPurposeRepository donationPurposeRepository) {
        this.donationSubCategoryRepository = donationSubCategoryRepository;
        this.donationPurposeRepository = donationPurposeRepository;
    }
    
    public List<DonationSubCategoryDTO> getAllDonationSubCategories(Long purposeId, boolean includeInactive) {
        return donationSubCategoryRepository.findAll(purposeId, includeInactive);
    }
    
    public DonationSubCategoryDTO getDonationSubCategoryById(Long id) {
        return donationSubCategoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Donation sub-category not found with id: " + id));
    }
    
    @Transactional
    public DonationSubCategoryDTO createDonationSubCategory(DonationSubCategoryCreateDTO createDTO, Long userId) {
        // Validate that purpose exists
        donationPurposeRepository.findById(createDTO.getPurposeId())
            .orElseThrow(() -> new ResourceNotFoundException("Donation purpose not found with id: " + createDTO.getPurposeId()));
        
        // Check for duplicate code within the same purpose
        if (donationSubCategoryRepository.existsByCodeAndPurposeId(createDTO.getCode(), createDTO.getPurposeId(), null)) {
            throw new DuplicateResourceException("Sub-category code already exists for this purpose: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        DonationSubCategoryDTO subCategoryDTO = new DonationSubCategoryDTO();
        subCategoryDTO.setPurposeId(createDTO.getPurposeId());
        subCategoryDTO.setCode(createDTO.getCode());
        subCategoryDTO.setName(createDTO.getName());
        subCategoryDTO.setDescription(createDTO.getDescription());
        subCategoryDTO.setDisplayOrder(createDTO.getDisplayOrder());
        subCategoryDTO.setIsActive(createDTO.getIsActive());
        
        Long id = donationSubCategoryRepository.save(subCategoryDTO, userId);
        return getDonationSubCategoryById(id);
    }
    
    @Transactional
    public DonationSubCategoryDTO updateDonationSubCategory(Long id, DonationSubCategoryUpdateDTO updateDTO, Long userId) {
        // Check if sub-category exists
        DonationSubCategoryDTO existingSubCategory = getDonationSubCategoryById(id);
        
        // Convert UpdateDTO to DTO for updating
        DonationSubCategoryDTO subCategoryDTO = new DonationSubCategoryDTO();
        subCategoryDTO.setId(id);
        subCategoryDTO.setPurposeId(existingSubCategory.getPurposeId()); // Keep existing purposeId
        subCategoryDTO.setCode(existingSubCategory.getCode()); // Keep existing code (not updatable)
        subCategoryDTO.setName(updateDTO.getName());
        subCategoryDTO.setDescription(updateDTO.getDescription());
        subCategoryDTO.setDisplayOrder(updateDTO.getDisplayOrder() != null ? updateDTO.getDisplayOrder() : existingSubCategory.getDisplayOrder());
        subCategoryDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingSubCategory.getIsActive());
        
        donationSubCategoryRepository.update(subCategoryDTO, userId);
        return getDonationSubCategoryById(id);
    }
    
    @Transactional
    public void deleteDonationSubCategory(Long id, Long userId) {
        // Check if sub-category exists
        getDonationSubCategoryById(id);
        
        // Perform soft delete
        donationSubCategoryRepository.delete(id, userId);
    }
    
    public List<DonationSubCategoryDropdownDTO> getAllDonationSubCategoriesForDropdown(Long purposeId) {
        return donationSubCategoryRepository.findAllForDropdown(purposeId);
    }
}

