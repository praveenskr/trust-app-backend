package com.trustapp.controller;

import com.trustapp.dto.BranchAccessCheckDTO;
import com.trustapp.dto.BranchAccessDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.UserBranchAccessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/branches")
public class UserBranchAccessController {
    
    private final UserBranchAccessService userBranchAccessService;
    
    public UserBranchAccessController(UserBranchAccessService userBranchAccessService) {
        this.userBranchAccessService = userBranchAccessService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchAccessDTO>>> getUserBranchAccess(@PathVariable Long userId) {
        List<BranchAccessDTO> branchAccess = userBranchAccessService.getUserBranchAccess(userId);
        return ResponseEntity.ok(ApiResponse.success(branchAccess));
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<List<BranchAccessDTO>>> assignBranches(
            @PathVariable Long userId,
            @Valid @RequestBody List<Long> branchIds,
            @RequestParam(required = false, defaultValue = "1") Long grantedBy) {
        List<BranchAccessDTO> branchAccess = userBranchAccessService.assignBranches(userId, branchIds, grantedBy);
        return ResponseEntity.ok(ApiResponse.success("Branches assigned successfully", branchAccess));
    }
    
    @DeleteMapping
    public ResponseEntity<ApiResponse<?>> removeAllBranches(@PathVariable Long userId) {
        userBranchAccessService.removeAllBranches(userId);
        return ResponseEntity.ok(ApiResponse.success("All branch access removed successfully"));
    }
    
    @GetMapping("/{branchId}/check")
    public ResponseEntity<ApiResponse<BranchAccessCheckDTO>> checkBranchAccess(
            @PathVariable Long userId,
            @PathVariable Long branchId) {
        BranchAccessCheckDTO checkResult = userBranchAccessService.checkBranchAccess(userId, branchId);
        return ResponseEntity.ok(ApiResponse.success(checkResult));
    }
}

