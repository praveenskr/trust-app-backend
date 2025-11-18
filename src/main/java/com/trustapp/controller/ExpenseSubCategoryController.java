package com.trustapp.controller;

import com.trustapp.dto.ExpenseSubCategoryCreateDTO;
import com.trustapp.dto.ExpenseSubCategoryDTO;
import com.trustapp.dto.ExpenseSubCategoryUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.ExpenseSubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/expense-sub-categories")
public class ExpenseSubCategoryController {
    
    private final ExpenseSubCategoryService expenseSubCategoryService;
    
    public ExpenseSubCategoryController(ExpenseSubCategoryService expenseSubCategoryService) {
        this.expenseSubCategoryService = expenseSubCategoryService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseSubCategoryDTO>>> getAllExpenseSubCategories(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<ExpenseSubCategoryDTO> subCategories = expenseSubCategoryService.getAllExpenseSubCategories(categoryId, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(subCategories));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseSubCategoryDTO>> getExpenseSubCategoryById(@PathVariable Long id) {
        ExpenseSubCategoryDTO subCategory = expenseSubCategoryService.getExpenseSubCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(subCategory));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseSubCategoryDTO>> createExpenseSubCategory(
            @Valid @RequestBody ExpenseSubCategoryCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        ExpenseSubCategoryDTO created = expenseSubCategoryService.createExpenseSubCategory(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Expense sub-category created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseSubCategoryDTO>> updateExpenseSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseSubCategoryUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        ExpenseSubCategoryDTO updated = expenseSubCategoryService.updateExpenseSubCategory(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Expense sub-category updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteExpenseSubCategory(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        expenseSubCategoryService.deleteExpenseSubCategory(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Expense sub-category deleted successfully"));
    }
}

