package com.trustapp.service;

import com.trustapp.dto.SubscriptionDiscountCreateDTO;
import com.trustapp.dto.SubscriptionDiscountDTO;
import com.trustapp.dto.SubscriptionDiscountUpdateDTO;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.SubscriptionDiscountRepository;
import com.trustapp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionDiscountService {
    
    private final SubscriptionDiscountRepository subscriptionDiscountRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    
    public SubscriptionDiscountService(
            SubscriptionDiscountRepository subscriptionDiscountRepository,
            SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionDiscountRepository = subscriptionDiscountRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }
    
    public List<SubscriptionDiscountDTO> getAllSubscriptionDiscounts(Long planId, Boolean isActive, LocalDate validFrom, LocalDate validTo) {
        return subscriptionDiscountRepository.findAll(planId, isActive, validFrom, validTo);
    }
    
    public SubscriptionDiscountDTO getSubscriptionDiscountById(Long id) {
        return subscriptionDiscountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription discount not found with id: " + id));
    }
    
    @Transactional
    public SubscriptionDiscountDTO createSubscriptionDiscount(SubscriptionDiscountCreateDTO createDTO, Long userId) {
        // Validate plan exists
        subscriptionPlanRepository.findById(createDTO.getPlanId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + createDTO.getPlanId()));
        
        // Validate date range
        if (createDTO.getValidTo() != null && createDTO.getValidFrom() != null) {
            if (createDTO.getValidTo().isBefore(createDTO.getValidFrom())) {
                throw new ValidationException("Valid to date must be after valid from date");
            }
        }
        
        // Validate quantity range
        if (createDTO.getMinQuantity() != null && createDTO.getMaxQuantity() != null) {
            if (createDTO.getMaxQuantity() < createDTO.getMinQuantity()) {
                throw new ValidationException("Max quantity must be greater than or equal to min quantity");
            }
        }
        
        // Convert CreateDTO to DTO for saving
        SubscriptionDiscountDTO discountDTO = new SubscriptionDiscountDTO();
        discountDTO.setPlanId(createDTO.getPlanId());
        discountDTO.setDiscountType(createDTO.getDiscountType() != null ? createDTO.getDiscountType().name() : null);
        discountDTO.setDiscountValue(createDTO.getDiscountValue());
        discountDTO.setMinQuantity(createDTO.getMinQuantity() != null ? createDTO.getMinQuantity() : 1);
        discountDTO.setMaxQuantity(createDTO.getMaxQuantity());
        discountDTO.setValidFrom(createDTO.getValidFrom());
        discountDTO.setValidTo(createDTO.getValidTo());
        discountDTO.setIsActive(createDTO.getIsActive());
        
        Long id = subscriptionDiscountRepository.save(discountDTO, userId);
        return getSubscriptionDiscountById(id);
    }
    
    @Transactional
    public SubscriptionDiscountDTO updateSubscriptionDiscount(Long id, SubscriptionDiscountUpdateDTO updateDTO, Long userId) {
        // Check if discount exists
        SubscriptionDiscountDTO existingDiscount = getSubscriptionDiscountById(id);
        
        // Validate plan exists
        subscriptionPlanRepository.findById(updateDTO.getPlanId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + updateDTO.getPlanId()));
        
        // Validate date range
        if (updateDTO.getValidTo() != null && updateDTO.getValidFrom() != null) {
            if (updateDTO.getValidTo().isBefore(updateDTO.getValidFrom())) {
                throw new ValidationException("Valid to date must be after valid from date");
            }
        }
        
        // Validate quantity range
        if (updateDTO.getMinQuantity() != null && updateDTO.getMaxQuantity() != null) {
            if (updateDTO.getMaxQuantity() < updateDTO.getMinQuantity()) {
                throw new ValidationException("Max quantity must be greater than or equal to min quantity");
            }
        }
        
        // Convert UpdateDTO to DTO for updating
        SubscriptionDiscountDTO discountDTO = new SubscriptionDiscountDTO();
        discountDTO.setPlanId(updateDTO.getPlanId());
        discountDTO.setDiscountType(updateDTO.getDiscountType() != null ? updateDTO.getDiscountType().name() : null);
        discountDTO.setDiscountValue(updateDTO.getDiscountValue());
        discountDTO.setMinQuantity(updateDTO.getMinQuantity());
        discountDTO.setMaxQuantity(updateDTO.getMaxQuantity());
        discountDTO.setValidFrom(updateDTO.getValidFrom());
        discountDTO.setValidTo(updateDTO.getValidTo());
        discountDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingDiscount.getIsActive());
        
        subscriptionDiscountRepository.update(id, discountDTO, userId);
        return getSubscriptionDiscountById(id);
    }
    
    @Transactional
    public void deleteSubscriptionDiscount(Long id, Long userId) {
        // Check if discount exists
        getSubscriptionDiscountById(id);
        
        // Perform soft delete
        subscriptionDiscountRepository.delete(id, userId);
    }
}

