package com.trustapp.controller;

import com.trustapp.dto.InterBranchTransferCreateDTO;
import com.trustapp.dto.InterBranchTransferDTO;
import com.trustapp.dto.InterBranchTransferStatusUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.dto.response.PageResponseDTO;
import com.trustapp.service.InterBranchTransferService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/branches/transfers")
public class InterBranchTransferController {
    
    private final InterBranchTransferService interBranchTransferService;
    
    public InterBranchTransferController(InterBranchTransferService interBranchTransferService) {
        this.interBranchTransferService = interBranchTransferService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<InterBranchTransferDTO>>> getAllTransfers(
            @RequestParam(required = false) Long fromBranchId,
            @RequestParam(required = false) Long toBranchId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "transferDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        
        PageResponseDTO<InterBranchTransferDTO> pageResponse = interBranchTransferService.getAllTransfers(
            fromBranchId, toBranchId, status, fromDate, toDate, page, size, sortBy, sortDir
        );
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<InterBranchTransferDTO>> createTransfer(
            @Valid @RequestBody InterBranchTransferCreateDTO createDTO,
            @RequestParam(required = false) Long createdBy) {
        InterBranchTransferDTO transfer = interBranchTransferService.createTransfer(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Inter-branch transfer created successfully", transfer));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InterBranchTransferDTO>> updateTransferStatus(
            @PathVariable Long id,
            @Valid @RequestBody InterBranchTransferStatusUpdateDTO updateDTO) {
        InterBranchTransferDTO updated = interBranchTransferService.updateTransferStatus(id, updateDTO);
        return ResponseEntity.ok(
            ApiResponse.success("Transfer status updated successfully", updated)
        );
    }
}

