package com.trustapp.controller;

import com.trustapp.dto.DonationPurposeCreateDTO;
import com.trustapp.dto.DonationPurposeDTO;
import com.trustapp.dto.DonationPurposeUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.DonationPurposeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/donation-purposes")
public class DonationPurposeController {
    
    private final DonationPurposeService donationPurposeService;
    
    public DonationPurposeController(DonationPurposeService donationPurposeService) {
        this.donationPurposeService = donationPurposeService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<DonationPurposeDTO>>> getAllDonationPurposes(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<DonationPurposeDTO> purposes = donationPurposeService.getAllDonationPurposes(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(purposes));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationPurposeDTO>> getDonationPurposeById(@PathVariable Long id) {
        DonationPurposeDTO purpose = donationPurposeService.getDonationPurposeById(id);
        return ResponseEntity.ok(ApiResponse.success(purpose));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<DonationPurposeDTO>> createDonationPurpose(
            @Valid @RequestBody DonationPurposeCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        DonationPurposeDTO created = donationPurposeService.createDonationPurpose(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Donation purpose created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationPurposeDTO>> updateDonationPurpose(
            @PathVariable Long id,
            @Valid @RequestBody DonationPurposeUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        DonationPurposeDTO updated = donationPurposeService.updateDonationPurpose(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Donation purpose updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDonationPurpose(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        donationPurposeService.deleteDonationPurpose(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Donation purpose deleted successfully"));
    }
}

