package com.trustapp.repository;

import com.trustapp.dto.PermissionDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PermissionRepository {
    
    private final JdbcClient jdbcClient;
    
    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<PermissionDTO> findAll() {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM permissions
            ORDER BY module, resource, action
            """;
        
        return jdbcClient.sql(sql)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByModule(String module) {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM permissions
            WHERE module = ?
            ORDER BY resource, action
            """;
        
        return jdbcClient.sql(sql)
            .param(module)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByRoleId(Long roleId) {
        String sql = """
            SELECT p.id, p.code, p.name, p.description, p.module, p.resource, p.action,
                   p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            WHERE rp.role_id = ?
            ORDER BY p.module, p.resource, p.action
            """;
        
        return jdbcClient.sql(sql)
            .param(roleId)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT p.id, p.code, p.name, p.description, p.module, p.resource, p.action,
                   p.created_at AS createdAt, p.updated_at AS updatedAt
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN user_roles ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = ?
            ORDER BY p.module, p.resource, p.action
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .query(PermissionDTO.class)
            .list();
    }
    
    public Optional<PermissionDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM permissions
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(PermissionDTO.class)
            .optional();
    }
}

