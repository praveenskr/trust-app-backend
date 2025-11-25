package com.trustapp.service;

import com.trustapp.dto.ExpenseSubCategoryCreateDTO;
import com.trustapp.dto.ExpenseSubCategoryDTO;
import com.trustapp.dto.ExpenseSubCategoryUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.repository.ExpenseCategoryRepository;
import com.trustapp.repository.ExpenseSubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseSubCategoryService {
    
    private final ExpenseSubCategoryRepository expenseSubCategoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    
    public ExpenseSubCategoryService(
            ExpenseSubCategoryRepository expenseSubCategoryRepository,
            ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseSubCategoryRepository = expenseSubCategoryRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }
    
    public List<ExpenseSubCategoryDTO> getAllExpenseSubCategories(Long categoryId, boolean includeInactive) {
        return expenseSubCategoryRepository.findAll(categoryId, includeInactive);
    }
    
    public ExpenseSubCategoryDTO getExpenseSubCategoryById(Long id) {
        return expenseSubCategoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense sub-category not found with id: " + id));
    }
    
    @Transactional
    public ExpenseSubCategoryDTO createExpenseSubCategory(ExpenseSubCategoryCreateDTO createDTO, Long userId) {
        // Validate that category exists
        expenseCategoryRepository.findById(createDTO.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with id: " + createDTO.getCategoryId()));
        
        // Check for duplicate code within the same category
        if (expenseSubCategoryRepository.existsByCodeAndCategoryId(createDTO.getCode(), createDTO.getCategoryId(), null)) {
            throw new DuplicateResourceException("Sub-category code already exists for this category: " + createDTO.getCode());
        }
        
        // Convert CreateDTO to DTO for saving
        ExpenseSubCategoryDTO subCategoryDTO = new ExpenseSubCategoryDTO();
        subCategoryDTO.setCategoryId(createDTO.getCategoryId());
        subCategoryDTO.setCode(createDTO.getCode());
        subCategoryDTO.setName(createDTO.getName());
        subCategoryDTO.setDescription(createDTO.getDescription());
        subCategoryDTO.setDisplayOrder(createDTO.getDisplayOrder());
        subCategoryDTO.setIsActive(createDTO.getIsActive());
        
        Long id = expenseSubCategoryRepository.save(subCategoryDTO, userId);
        return getExpenseSubCategoryById(id);
    }
    
    @Transactional
    public ExpenseSubCategoryDTO updateExpenseSubCategory(Long id, ExpenseSubCategoryUpdateDTO updateDTO, Long userId) {
        // Check if sub-category exists
        ExpenseSubCategoryDTO existingSubCategory = getExpenseSubCategoryById(id);
        
        // Convert UpdateDTO to DTO for updating
        ExpenseSubCategoryDTO subCategoryDTO = new ExpenseSubCategoryDTO();
        subCategoryDTO.setId(id);
        subCategoryDTO.setCategoryId(existingSubCategory.getCategoryId()); // Keep existing categoryId
        subCategoryDTO.setCode(existingSubCategory.getCode()); // Keep existing code (not updatable)
        subCategoryDTO.setName(updateDTO.getName());
        subCategoryDTO.setDescription(updateDTO.getDescription());
        subCategoryDTO.setDisplayOrder(updateDTO.getDisplayOrder() != null ? updateDTO.getDisplayOrder() : existingSubCategory.getDisplayOrder());
        subCategoryDTO.setIsActive(updateDTO.getIsActive() != null ? updateDTO.getIsActive() : existingSubCategory.getIsActive());
        
        expenseSubCategoryRepository.update(subCategoryDTO, userId);
        return getExpenseSubCategoryById(id);
    }
    
    @Transactional
    public void deleteExpenseSubCategory(Long id, Long userId) {
        // Check if sub-category exists
        getExpenseSubCategoryById(id);
        
        // Perform soft delete
        expenseSubCategoryRepository.delete(id, userId);
    }
}

