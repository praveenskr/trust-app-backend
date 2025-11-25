package com.trustapp.controller;

import com.trustapp.dto.SubscriptionDiscountCreateDTO;
import com.trustapp.dto.SubscriptionDiscountDTO;
import com.trustapp.dto.SubscriptionDiscountUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.SubscriptionDiscountService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/master/subscription-discounts")
public class SubscriptionDiscountController {
    
    private final SubscriptionDiscountService subscriptionDiscountService;
    
    public SubscriptionDiscountController(SubscriptionDiscountService subscriptionDiscountService) {
        this.subscriptionDiscountService = subscriptionDiscountService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionDiscountDTO>>> getAllSubscriptionDiscounts(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validTo) {
        List<SubscriptionDiscountDTO> discounts = subscriptionDiscountService.getAllSubscriptionDiscounts(planId, isActive, validFrom, validTo);
        return ResponseEntity.ok(ApiResponse.success(discounts));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionDiscountDTO>> getSubscriptionDiscountById(@PathVariable Long id) {
        SubscriptionDiscountDTO discount = subscriptionDiscountService.getSubscriptionDiscountById(id);
        return ResponseEntity.ok(ApiResponse.success(discount));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionDiscountDTO>> createSubscriptionDiscount(
            @Valid @RequestBody SubscriptionDiscountCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        SubscriptionDiscountDTO created = subscriptionDiscountService.createSubscriptionDiscount(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Subscription discount created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionDiscountDTO>> updateSubscriptionDiscount(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionDiscountUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        SubscriptionDiscountDTO updated = subscriptionDiscountService.updateSubscriptionDiscount(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Subscription discount updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteSubscriptionDiscount(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        subscriptionDiscountService.deleteSubscriptionDiscount(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Subscription discount deleted successfully"));
    }
}

