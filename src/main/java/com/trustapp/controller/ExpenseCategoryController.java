package com.trustapp.controller;

import com.trustapp.dto.ExpenseCategoryCreateDTO;
import com.trustapp.dto.ExpenseCategoryDTO;
import com.trustapp.dto.ExpenseCategoryDropdownDTO;
import com.trustapp.dto.ExpenseCategoryUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.ExpenseCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/expense-categories")
public class ExpenseCategoryController {
    
    private final ExpenseCategoryService expenseCategoryService;
    
    public ExpenseCategoryController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategoryDTO>>> getAllExpenseCategories(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<ExpenseCategoryDTO> categories = expenseCategoryService.getAllExpenseCategories(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategoryDTO>> getExpenseCategoryById(@PathVariable Long id) {
        ExpenseCategoryDTO category = expenseCategoryService.getExpenseCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseCategoryDTO>> createExpenseCategory(
            @Valid @RequestBody ExpenseCategoryCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        ExpenseCategoryDTO created = expenseCategoryService.createExpenseCategory(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Expense category created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategoryDTO>> updateExpenseCategory(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        ExpenseCategoryDTO updated = expenseCategoryService.updateExpenseCategory(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Expense category updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteExpenseCategory(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        expenseCategoryService.deleteExpenseCategory(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Expense category deleted successfully"));
    }
    
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<ExpenseCategoryDropdownDTO>>> getAllExpenseCategoriesForDropdown() {
        List<ExpenseCategoryDropdownDTO> categories = expenseCategoryService.getAllExpenseCategoriesForDropdown();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}

