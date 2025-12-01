package com.trustapp.service;

import com.trustapp.dto.ExpenseCategoryCreateDTO;
import com.trustapp.dto.ExpenseCategoryDTO;
import com.trustapp.dto.ExpenseCategoryDropdownDTO;
import com.trustapp.dto.ExpenseCategoryUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseCategoryService {
    
    private final ExpenseCategoryRepository expenseCategoryRepository;
    
    public ExpenseCategoryService(ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseCategoryRepository = expenseCategoryRepository;
    }
    
    public List<ExpenseCategoryDTO> getAllExpenseCategories(boolean includeInactive) {
        return expenseCategoryRepository.findAll(includeInactive);
    }
    
    public ExpenseCategoryDTO getExpenseCategoryById(Long id) {
        return expenseCategoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + id));
    }
    
    @Transactional
    public ExpenseCategoryDTO createExpenseCategory(ExpenseCategoryCreateDTO createDTO, Long userId) {
        // Check for duplicate code
        if (expenseCategoryRepository.existsByCode(createDTO.getCode(), null)) {
            throw new DuplicateResourceException("Expense category code already exists: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        ExpenseCategoryDTO categoryDTO = new ExpenseCategoryDTO();
        categoryDTO.setCode(createDTO.getCode());
        categoryDTO.setName(createDTO.getName());
        categoryDTO.setDescription(createDTO.getDescription());
        categoryDTO.setDisplayOrder(createDTO.getDisplayOrder());
        categoryDTO.setIsActive(createDTO.getIsActive());
        
        Long id = expenseCategoryRepository.save(categoryDTO, userId);
        return getExpenseCategoryById(id);
    }
    
    @Transactional
    public ExpenseCategoryDTO updateExpenseCategory(Long id, ExpenseCategoryUpdateDTO updateDTO, Long userId) {
        // Check if category exists
        ExpenseCategoryDTO existingCategory = getExpenseCategoryById(id);
        
        // Convert UpdateDTO to DTO for updating
        ExpenseCategoryDTO categoryDTO = new ExpenseCategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName(updateDTO.getName());
        categoryDTO.setDescription(updateDTO.getDescription());
        categoryDTO.setDisplayOrder(updateDTO.getDisplayOrder() != null ? updateDTO.getDisplayOrder() : existingCategory.getDisplayOrder());
        categoryDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingCategory.getIsActive());
        
        expenseCategoryRepository.update(categoryDTO, userId);
        return getExpenseCategoryById(id);
    }
    
    @Transactional
    public void deleteExpenseCategory(Long id, Long userId) {
        // Check if category exists
        getExpenseCategoryById(id);
        
        // Delete will check for sub-categories and throw exception if they exist
        try {
            expenseCategoryRepository.delete(id, userId);
        } catch (IllegalStateException e) {
            // Convert IllegalStateException to ValidationException for proper error handling
            throw new ValidationException(e.getMessage());
        }
    }
    
    public List<ExpenseCategoryDropdownDTO> getAllExpenseCategoriesForDropdown() {
        return expenseCategoryRepository.findAllForDropdown();
    }
}

