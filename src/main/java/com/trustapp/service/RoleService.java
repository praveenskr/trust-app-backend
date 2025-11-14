package com.trustapp.service;

import com.trustapp.dto.RoleCreateDTO;
import com.trustapp.dto.RoleDTO;
import com.trustapp.dto.RoleUpdateDTO;
import com.trustapp.exception.DuplicateResourceException;
import com.trustapp.exception.ResourceNotFoundException;
import com.trustapp.exception.ValidationException;
import com.trustapp.repository.PermissionRepository;
import com.trustapp.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }
    
    public List<RoleDTO> getAllRoles() {
        List<RoleDTO> roles = roleRepository.findAll();
        // Enrich with permissions
        return roles.stream()
            .map(role -> {
                List<com.trustapp.dto.PermissionDTO> permissions = 
                    permissionRepository.findByRoleId(role.getId());
                role.setPermissions(permissions);
                return role;
            })
            .toList();
    }
    
    public RoleDTO getRoleById(Long id) {
        RoleDTO role = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        // Enrich with permissions
        List<com.trustapp.dto.PermissionDTO> permissions = 
            permissionRepository.findByRoleId(role.getId());
        role.setPermissions(permissions);
        
        return role;
    }
    
    @Transactional
    public RoleDTO createRole(RoleCreateDTO roleCreateDTO) {
        // Validate code uniqueness
        if (roleRepository.existsByCode(roleCreateDTO.getCode(), null)) {
            throw new DuplicateResourceException("Role code already exists: " + roleCreateDTO.getCode());
        }
        
        // Create role
        RoleDTO role = new RoleDTO();
        role.setCode(roleCreateDTO.getCode());
        role.setName(roleCreateDTO.getName());
        role.setDescription(roleCreateDTO.getDescription());
        role.setIsSystemRole(roleCreateDTO.getIsSystemRole() != null ? roleCreateDTO.getIsSystemRole() : false);
        
        Long roleId = roleRepository.save(role);
        
        return getRoleById(roleId);
    }
    
    @Transactional
    public RoleDTO updateRole(Long id, RoleUpdateDTO roleUpdateDTO) {
        // Check if role exists
        RoleDTO existingRole = getRoleById(id);
        
        // Check if it's a system role (system roles should not be modified)
        if (existingRole.getIsSystemRole() != null && existingRole.getIsSystemRole()) {
            throw new ValidationException("System roles cannot be modified");
        }
        
        // Validate code uniqueness
        if (roleRepository.existsByCode(roleUpdateDTO.getCode(), id)) {
            throw new DuplicateResourceException("Role code already exists: " + roleUpdateDTO.getCode());
        }
        
        // Update role
        RoleDTO role = new RoleDTO();
        role.setId(id);
        role.setCode(roleUpdateDTO.getCode());
        role.setName(roleUpdateDTO.getName());
        role.setDescription(roleUpdateDTO.getDescription());
        
        roleRepository.update(role);
        
        return getRoleById(id);
    }
}

