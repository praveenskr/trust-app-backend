package com.trustapp.controller;

import com.trustapp.dto.DonationCreateDTO;
import com.trustapp.dto.DonationDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
}

