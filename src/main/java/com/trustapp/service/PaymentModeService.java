package com.trustapp.service;

import com.trustapp.dto.PaymentModeCreateDTO;
import com.trustapp.dto.PaymentModeDTO;
import com.trustapp.dto.PaymentModeDropdownDTO;
import com.trustapp.dto.PaymentModeUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.PaymentModeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentModeService {
    
    private final PaymentModeRepository paymentModeRepository;
    
    public PaymentModeService(PaymentModeRepository paymentModeRepository) {
        this.paymentModeRepository = paymentModeRepository;
    }
    
    public List<PaymentModeDTO> getAllPaymentModes(boolean includeInactive) {
        return paymentModeRepository.findAll(includeInactive);
    }
    
    public PaymentModeDTO getPaymentModeById(Long id) {
        return paymentModeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment mode not found with id: " + id));
    }
    
    @Transactional
    public PaymentModeDTO createPaymentMode(PaymentModeCreateDTO createDTO) {
        // Check for duplicate code
        if (paymentModeRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Payment mode code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        PaymentModeDTO paymentModeDTO = new PaymentModeDTO();
        paymentModeDTO.setCode(createDTO.getCode());
        paymentModeDTO.setName(createDTO.getName());
        paymentModeDTO.setDescription(createDTO.getDescription());
        paymentModeDTO.setRequiresReceipt(createDTO.getRequiresReceipt());
        paymentModeDTO.setDisplayOrder(createDTO.getDisplayOrder());
        paymentModeDTO.setIsActive(createDTO.getIsActive());
        
        Long id = paymentModeRepository.save(paymentModeDTO);
        return getPaymentModeById(id);
    }
    
    @Transactional
    public PaymentModeDTO updatePaymentMode(Long id, PaymentModeUpdateDTO updateDTO) {
        // Check if payment mode exists
        PaymentModeDTO existingPaymentMode = getPaymentModeById(id);
        
        // Check for duplicate code (excluding current payment mode)
        if (paymentModeRepository.existsByCode(updateDTO.getCode(), id)) {
            throw new DuplicateResourceException("Payment mode code already exists: " + updateDTO.getCode());
        }
        
        // Convert UpdateDTO to DTO for updating
        PaymentModeDTO paymentModeDTO = new PaymentModeDTO();
        paymentModeDTO.setCode(updateDTO.getCode());
        paymentModeDTO.setName(updateDTO.getName());
        paymentModeDTO.setDescription(updateDTO.getDescription());
        paymentModeDTO.setRequiresReceipt(updateDTO.getRequiresReceipt());
        paymentModeDTO.setDisplayOrder(updateDTO.getDisplayOrder());
        paymentModeDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingPaymentMode.getIsActive());
        
        paymentModeRepository.update(id, paymentModeDTO);
        return getPaymentModeById(id);
    }
    
    @Transactional
    public void deletePaymentMode(Long id) {
        // Check if payment mode exists
        getPaymentModeById(id);
        
        // Perform soft delete
        paymentModeRepository.delete(id);
    }
    
    public List<PaymentModeDropdownDTO> getAllPaymentModesForDropdown() {
        return paymentModeRepository.findAllForDropdown();
    }
}

