package com.trustapp.repository;

import com.trustapp.dto.RoleDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {
    
    private final JdbcClient jdbcClient;
    
    public RoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<RoleDTO> findAll() {
        String sql = """
            SELECT id, code, name, description, is_system_role AS isSystemRole,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM roles
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(RoleDTO.class)
            .list();
    }
    
    public Optional<RoleDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, is_system_role AS isSystemRole,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM roles
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(RoleDTO.class)
            .optional();
    }
    
    public Optional<RoleDTO> findByCode(String code) {
        String sql = """
            SELECT id, code, name, description, is_system_role AS isSystemRole,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM roles
            WHERE code = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .query(RoleDTO.class)
            .optional();
    }
    
    public Long save(RoleDTO role) {
        String sql = """
            INSERT INTO roles (code, name, description, is_system_role)
            VALUES (?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(role.getCode())
            .param(role.getName())
            .param(role.getDescription())
            .param(role.getIsSystemRole() != null ? role.getIsSystemRole() : false)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(RoleDTO role) {
        String sql = """
            UPDATE roles
            SET code = ?, name = ?, description = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(role.getCode())
            .param(role.getName())
            .param(role.getDescription())
            .param(role.getId())
            .update();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM roles
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
}

