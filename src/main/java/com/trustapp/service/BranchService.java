package com.trustapp.service;

import com.trustapp.dto.BranchCreateDTO;
import com.trustapp.dto.BranchDTO;
import com.trustapp.dto.BranchUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.BranchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BranchService {
    
    private final BranchRepository branchRepository;
    
    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }
    
    public List<BranchDTO> getAllBranches(boolean includeInactive) {
        return branchRepository.findAll(includeInactive);
    }
    
    public BranchDTO getBranchById(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }
    
    @Transactional
    public BranchDTO createBranch(BranchCreateDTO createDTO, Long userId) {
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
        
        Long id = branchRepository.save(branchDTO, userId);
        return getBranchById(id);
    }
    
    @Transactional
    public BranchDTO updateBranch(Long id, BranchUpdateDTO updateDTO, Long userId) {
        // Check if branch exists
        BranchDTO existingBranch = getBranchById(id);
        
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
        
        branchRepository.update(branchDTO, userId);
        return getBranchById(id);
    }
    
    @Transactional
    public void deleteBranch(Long id, Long userId) {
        // Check if branch exists
        getBranchById(id);
        
        // Delete will check for active events and throw exception if they exist
        try {
            branchRepository.delete(id, userId);
        } catch (IllegalStateException e) {
            // Convert IllegalStateException to ValidationException for proper error handling
            throw new ValidationException(e.getMessage());
        }
    }
}

