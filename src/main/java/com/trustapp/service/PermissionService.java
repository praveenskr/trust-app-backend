package com.trustapp.service;

import com.trustapp.dto.PermissionDTO;
import com.trustapp.repository.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    
    private final PermissionRepository permissionRepository;
    
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
    
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    public List<PermissionDTO> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }
    
    public List<PermissionDTO> getUserPermissions(Long userId) {
        return permissionRepository.findByUserId(userId);
    }
}

