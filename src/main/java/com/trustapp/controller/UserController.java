package com.trustapp.controller;

import com.trustapp.dto.*;
import com.trustapp.dto.response.ApiResponse;
import com.trustapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    // @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<UserDTO> users = userService.getAllUsers(includeInactive);
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping
    // @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody UserCreateDTO userCreateDTO,
            @RequestParam(required = false, defaultValue = "1") Long createdBy) {
        UserDTO created = userService.createUser(userCreateDTO, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User created successfully", created));
    }
    
    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO,
            @RequestParam(required = false, defaultValue = "1") Long updatedBy) {
        UserDTO updated = userService.updateUser(id, userUpdateDTO, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
    
    @PostMapping("/{id}/change-password")
    // @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(id, passwordChangeDTO);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
    
    @PostMapping("/{id}/unlock")
    // @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<ApiResponse<?>> unlockUser(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "1") Long unlockedBy) {
        userService.unlockUser(id, unlockedBy);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully"));
    }
}

