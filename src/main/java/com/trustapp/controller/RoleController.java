package com.trustapp.controller;

import com.trustapp.dto.RoleCreateDTO;
import com.trustapp.dto.RoleDTO;
import com.trustapp.dto.RoleUpdateDTO;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    // @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
    
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<RoleDTO>> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }
    
    @PostMapping
    // @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(@Valid @RequestBody RoleCreateDTO roleCreateDTO) {
        RoleDTO created = roleService.createRole(roleCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Role created successfully", created));
    }
    
    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleUpdateDTO roleUpdateDTO) {
        RoleDTO updated = roleService.updateRole(id, roleUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }
}

