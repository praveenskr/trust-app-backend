package com.trustapp.service;

import com.trustapp.dto.SubscriptionPlanCreateDTO;
import com.trustapp.dto.SubscriptionPlanDTO;
import com.trustapp.dto.SubscriptionPlanUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionPlanService {
    
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    
    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }
    
    public List<SubscriptionPlanDTO> getAllSubscriptionPlans(String planType, boolean includeInactive) {
        return subscriptionPlanRepository.findAll(planType, includeInactive);
    }
    
    public SubscriptionPlanDTO getSubscriptionPlanById(Long id) {
        return subscriptionPlanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + id));
    }
    
    @Transactional
    public SubscriptionPlanDTO createSubscriptionPlan(SubscriptionPlanCreateDTO createDTO, Long userId) {
        // Check for duplicate code
        if (subscriptionPlanRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Subscription plan code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        SubscriptionPlanDTO planDTO = new SubscriptionPlanDTO();
        planDTO.setCode(createDTO.getCode());
        planDTO.setName(createDTO.getName());
        planDTO.setPlanType(createDTO.getPlanType() != null ? createDTO.getPlanType().name() : null);
        planDTO.setDurationMonths(createDTO.getDurationMonths());
        planDTO.setAmount(createDTO.getAmount());
        planDTO.setDescription(createDTO.getDescription());
        planDTO.setIsActive(createDTO.getIsActive());
        
        Long id = subscriptionPlanRepository.save(planDTO, userId);
        return getSubscriptionPlanById(id);
    }
    
    @Transactional
    public SubscriptionPlanDTO updateSubscriptionPlan(Long id, SubscriptionPlanUpdateDTO updateDTO, Long userId) {
        // Check if plan exists
        getSubscriptionPlanById(id);
        
        // Check for duplicate code (excluding current plan)
        if (subscriptionPlanRepository.existsByCode(updateDTO.getCode(), id)) {
            throw new DuplicateResourceException("Subscription plan code already exists: " + updateDTO.getCode());
        }
        
        // Convert UpdateDTO to DTO for updating
        SubscriptionPlanDTO planDTO = new SubscriptionPlanDTO();
        planDTO.setCode(updateDTO.getCode());
        planDTO.setName(updateDTO.getName());
        planDTO.setPlanType(updateDTO.getPlanType() != null ? updateDTO.getPlanType().name() : null);
        planDTO.setDurationMonths(updateDTO.getDurationMonths());
        planDTO.setAmount(updateDTO.getAmount());
        planDTO.setDescription(updateDTO.getDescription());
        planDTO.setIsActive(updateDTO.getIsActive());
        
        subscriptionPlanRepository.update(id, planDTO, userId);
        return getSubscriptionPlanById(id);
    }
    
    @Transactional
    public void deleteSubscriptionPlan(Long id, Long userId) {
        // Check if plan exists
        getSubscriptionPlanById(id);
        
        // Delete will check for active discounts and throw exception if they exist
        try {
            subscriptionPlanRepository.delete(id, userId);
        } catch (IllegalStateException e) {
            // Convert IllegalStateException to ValidationException for proper error handling
            throw new ValidationException(e.getMessage());
        }
    }
}

