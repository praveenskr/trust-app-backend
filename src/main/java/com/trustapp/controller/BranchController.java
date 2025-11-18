package com.trustapp.controller;

import com.trustapp.dto.BranchCreateDTO;
import com.trustapp.dto.BranchDTO;
import com.trustapp.dto.BranchUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
public class BranchController {
    
    private final BranchService branchService;
    
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<BranchDTO> branches = branchService.getAllBranches(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(branches));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> getBranchById(@PathVariable Long id) {
        BranchDTO branch = branchService.getBranchById(id);
        return ResponseEntity.ok(ApiResponse.success(branch));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<BranchDTO>> createBranch(
            @Valid @RequestBody BranchCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        BranchDTO created = branchService.createBranch(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Branch created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        BranchDTO updated = branchService.updateBranch(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBranch(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        branchService.deleteBranch(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }
}

