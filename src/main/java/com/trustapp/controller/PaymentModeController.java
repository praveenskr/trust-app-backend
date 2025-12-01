package com.trustapp.controller;

import com.trustapp.dto.PaymentModeCreateDTO;
import com.trustapp.dto.PaymentModeDTO;
import com.trustapp.dto.PaymentModeDropdownDTO;
import com.trustapp.dto.PaymentModeUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.PaymentModeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/master/payment-modes")
public class PaymentModeController {
    
    private final PaymentModeService paymentModeService;
    
    public PaymentModeController(PaymentModeService paymentModeService) {
        this.paymentModeService = paymentModeService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentModeDTO>>> getAllPaymentModes(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<PaymentModeDTO> paymentModes = paymentModeService.getAllPaymentModes(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(paymentModes));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentModeDTO>> getPaymentModeById(@PathVariable Long id) {
        PaymentModeDTO paymentMode = paymentModeService.getPaymentModeById(id);
        return ResponseEntity.ok(ApiResponse.success(paymentMode));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentModeDTO>> createPaymentMode(
            @Valid @RequestBody PaymentModeCreateDTO createDTO) {
        PaymentModeDTO created = paymentModeService.createPaymentMode(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Payment mode created successfully", created));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentModeDTO>> updatePaymentMode(
            @PathVariable Long id,
            @Valid @RequestBody PaymentModeUpdateDTO updateDTO) {
        PaymentModeDTO updated = paymentModeService.updatePaymentMode(id, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Payment mode updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePaymentMode(@PathVariable Long id) {
        paymentModeService.deletePaymentMode(id);
        return ResponseEntity.ok(ApiResponse.success("Payment mode deleted successfully"));
    }
    
    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<PaymentModeDropdownDTO>>> getAllPaymentModesForDropdown() {
        List<PaymentModeDropdownDTO> paymentModes = paymentModeService.getAllPaymentModesForDropdown();
        return ResponseEntity.ok(ApiResponse.success(paymentModes));
    }
}

