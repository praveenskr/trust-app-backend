package com.trustapp.controller;

import com.trustapp.dto.PermissionDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/permissions")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @GetMapping
    // @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getAllPermissions(
            @RequestParam(required = false) String module) {
        List<PermissionDTO> permissions = module != null 
            ? permissionService.getPermissionsByModule(module)
            : permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
    
    @GetMapping("/user/{userId}")
    // @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionDTO>>> getUserPermissions(@PathVariable Long userId) {
        List<PermissionDTO> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}

