package com.trustapp.controller;

import com.trustapp.dto.DonationSubCategoryCreateDTO;
import com.trustapp.dto.DonationSubCategoryDTO;
import com.trustapp.dto.DonationSubCategoryUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.DonationSubCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/donation-sub-categories")
public class DonationSubCategoryController {
    
    private final DonationSubCategoryService donationSubCategoryService;
    
    public DonationSubCategoryController(DonationSubCategoryService donationSubCategoryService) {
        this.donationSubCategoryService = donationSubCategoryService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonationSubCategoryDTO>>> getAllDonationSubCategories(
            @RequestParam(required = false) Long purposeId,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<DonationSubCategoryDTO> subCategories = donationSubCategoryService.getAllDonationSubCategories(purposeId, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(subCategories));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationSubCategoryDTO>> getDonationSubCategoryById(@PathVariable Long id) {
        DonationSubCategoryDTO subCategory = donationSubCategoryService.getDonationSubCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(subCategory));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<DonationSubCategoryDTO>> createDonationSubCategory(
            @Valid @RequestBody DonationSubCategoryCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        DonationSubCategoryDTO created = donationSubCategoryService.createDonationSubCategory(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Donation sub-category created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationSubCategoryDTO>> updateDonationSubCategory(
            @PathVariable Long id,
            @Valid @RequestBody DonationSubCategoryUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        DonationSubCategoryDTO updated = donationSubCategoryService.updateDonationSubCategory(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Donation sub-category updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDonationSubCategory(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        donationSubCategoryService.deleteDonationSubCategory(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Donation sub-category deleted successfully"));
    }
}

