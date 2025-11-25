package com.trustapp.controller;

import com.trustapp.dto.SubscriptionPlanCreateDTO;
import com.trustapp.dto.SubscriptionPlanDTO;
import com.trustapp.dto.SubscriptionPlanUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/subscription-plans")
public class SubscriptionPlanController {
    
    private final SubscriptionPlanService subscriptionPlanService;
    
    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> getAllSubscriptionPlans(
            @RequestParam(required = false) String planType,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<SubscriptionPlanDTO> plans = subscriptionPlanService.getAllSubscriptionPlans(planType, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> getSubscriptionPlanById(@PathVariable Long id) {
        SubscriptionPlanDTO plan = subscriptionPlanService.getSubscriptionPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanCreateDTO createDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        SubscriptionPlanDTO created = subscriptionPlanService.createSubscriptionPlan(createDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Subscription plan created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> updateSubscriptionPlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanUpdateDTO updateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        SubscriptionPlanDTO updated = subscriptionPlanService.updateSubscriptionPlan(id, updateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteSubscriptionPlan(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long deletedBy) {
        subscriptionPlanService.deleteSubscriptionPlan(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully"));
    }
}

