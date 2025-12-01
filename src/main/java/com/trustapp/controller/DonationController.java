package com.trustapp.controller;

import com.trustapp.dto.DonationCreateDTO;
import com.trustapp.dto.DonationDTO;
import com.trustapp.dto.DonationUpdateDTO;
import com.trustapp.dto.DonorDropdownDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/donations")
public class DonationController {
    
    private final DonationService donationService;
    
    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<DonationDTO>>> getAllDonations(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) Long purposeId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long paymentModeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String receiptNumber,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "donationDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        
        PageResponseDTO<DonationDTO> pageResponse = donationService.getAllDonations(
            branchId, purposeId, eventId, paymentModeId,
            fromDate, toDate, donorName, panNumber, receiptNumber,
            includeInactive, page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationDTO>> getDonationById(@PathVariable Long id) {
        DonationDTO donation = donationService.getDonationById(id);
        return ResponseEntity.ok(ApiResponse.success(donation));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DonationDTO>> updateDonation(
            @PathVariable Long id,
            @Valid @RequestBody DonationUpdateDTO updateDTO,
            @RequestParam(required = false) Long updatedBy) {
        // Default to 1 if not provided (should be replaced with authenticated user in production)
        Long userId = updatedBy != null ? updatedBy : 1L;
        
        DonationDTO updated = donationService.updateDonation(id, updateDTO, userId);
        return ResponseEntity.ok(ApiResponse.success("Donation transaction updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDonation(
            @PathVariable Long id,
            @RequestParam(required = false) Long deletedBy) {
        // Default to 1 if not provided (should be replaced with authenticated user in production)
        Long userId = deletedBy != null ? deletedBy : 1L;
        
        donationService.deleteDonation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Donation transaction deleted successfully"));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<DonationDTO>> createDonation(
            @Valid @RequestBody DonationCreateDTO createDTO,
            @RequestParam(required = false) Long createdBy) {
        // Default to 1 if not provided (should be replaced with authenticated user in production)
        Long userId = createdBy != null ? createdBy : 1L;
        
        DonationDTO created = donationService.createDonation(createDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Donation transaction created successfully", created));
    }

    @GetMapping("/donor-names")
    public ResponseEntity<ApiResponse<List<DonorDropdownDTO>>> getAllActiveDonorNames() {
        List<DonorDropdownDTO> donors = donationService.getAllActiveDonorNames();
        return ResponseEntity.ok(ApiResponse.success(donors));
    }
}

