package com.trustapp.controller;

import com.trustapp.dto.BranchCreateDTO;
import com.trustapp.dto.BranchDTO;
import com.trustapp.dto.BranchDropdownDTO;
import com.trustapp.dto.BranchStatisticsDTO;
import com.trustapp.dto.BranchUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/branches")
public class BranchController {
    
    private final BranchService branchService;
    
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<BranchDTO>>> getAllBranches(
            @RequestParam(required = false) Boolean includeInactive,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        PageResponseDTO<BranchDTO> branches = branchService.getAllBranches(
            includeInactive, city, state, search, page, size, sortBy, sortDir);
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
            @RequestParam(required = false) Long createdBy) {
        BranchDTO created = branchService.createBranch(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Branch created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateDTO updateDTO,
            @RequestParam(required = false) Long updatedBy) {
        BranchDTO updated = branchService.updateBranch(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteBranch(
            @PathVariable Long id,
            @RequestParam(required = false) Long deletedBy) {
        branchService.deleteBranch(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully"));
    }
    
    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<BranchStatisticsDTO>> getBranchStatistics(
            @PathVariable Long id,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
        BranchStatisticsDTO statistics = branchService.getBranchStatistics(id, from, to);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<BranchDropdownDTO>>> getAllBranchesForDropdown() {
        List<BranchDropdownDTO> branches = branchService.getAllBranchesForDropdown();
        return ResponseEntity.ok(ApiResponse.success(branches));
    }
}

