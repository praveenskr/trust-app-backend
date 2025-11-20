package com.trustapp.service;

import com.trustapp.dto.VendorCreateDTO;
import com.trustapp.dto.VendorDTO;
import com.trustapp.dto.VendorUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorService {
    
    private final VendorRepository vendorRepository;
    
    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }
    
    public List<VendorDTO> getAllVendors(boolean includeInactive) {
        return vendorRepository.findAll(includeInactive);
    }
    
    public VendorDTO getVendorById(Long id) {
        return vendorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + id));
    }
    
    @Transactional
    public VendorDTO createVendor(VendorCreateDTO createDTO, Long userId) {
        // Check for duplicate code
        if (vendorRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Vendor code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setCode(createDTO.getCode());
        vendorDTO.setName(createDTO.getName());
        vendorDTO.setContactPerson(createDTO.getContactPerson());
        vendorDTO.setPhone(createDTO.getPhone());
        vendorDTO.setEmail(createDTO.getEmail());
        vendorDTO.setAddress(createDTO.getAddress());
        vendorDTO.setCity(createDTO.getCity());
        vendorDTO.setState(createDTO.getState());
        vendorDTO.setPincode(createDTO.getPincode());
        vendorDTO.setGstNumber(createDTO.getGstNumber());
        vendorDTO.setPanNumber(createDTO.getPanNumber());
        vendorDTO.setIsActive(createDTO.getIsActive());
        
        Long id = vendorRepository.save(vendorDTO, userId);
        return getVendorById(id);
    }
    
    @Transactional
    public VendorDTO updateVendor(Long id, VendorUpdateDTO updateDTO, Long userId) {
        // Check if vendor exists
        VendorDTO existingVendor = getVendorById(id);
        
        // Check for duplicate code (excluding current vendor)
        if (vendorRepository.existsByCode(updateDTO.getCode(), id)) {
            throw new DuplicateResourceException("Vendor code already exists: " + updateDTO.getCode());
        }
        
        // Convert UpdateDTO to DTO for updating
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setCode(updateDTO.getCode());
        vendorDTO.setName(updateDTO.getName());
        vendorDTO.setContactPerson(updateDTO.getContactPerson());
        vendorDTO.setPhone(updateDTO.getPhone());
        vendorDTO.setEmail(updateDTO.getEmail());
        vendorDTO.setAddress(updateDTO.getAddress());
        vendorDTO.setCity(updateDTO.getCity());
        vendorDTO.setState(updateDTO.getState());
        vendorDTO.setPincode(updateDTO.getPincode());
        vendorDTO.setGstNumber(updateDTO.getGstNumber());
        vendorDTO.setPanNumber(updateDTO.getPanNumber());
        vendorDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingVendor.getIsActive());
        
        vendorRepository.update(id, vendorDTO, userId);
        return getVendorById(id);
    }
    
    @Transactional
    public void deleteVendor(Long id, Long userId) {
        // Check if vendor exists
        getVendorById(id);
        
        // Perform soft delete
        vendorRepository.delete(id, userId);
    }
}

