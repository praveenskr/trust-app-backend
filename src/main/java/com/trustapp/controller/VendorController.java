package com.trustapp.controller;

import com.trustapp.dto.VendorCreateDTO;
import com.trustapp.dto.VendorDTO;
import com.trustapp.dto.VendorUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.VendorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/vendors")
public class VendorController {
    
    private final VendorService vendorService;
    
    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorDTO>>> getAllVendors(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<VendorDTO> vendors = vendorService.getAllVendors(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorDTO>> getVendorById(@PathVariable Long id) {
        VendorDTO vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<VendorDTO>> createVendor(
            @Valid @RequestBody VendorCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        VendorDTO created = vendorService.createVendor(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Vendor created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorDTO>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        VendorDTO updated = vendorService.updateVendor(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Vendor updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteVendor(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        vendorService.deleteVendor(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Vendor deleted successfully"));
    }
}

