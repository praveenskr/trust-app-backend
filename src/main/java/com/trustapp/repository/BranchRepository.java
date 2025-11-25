package com.trustapp.repository;

import com.trustapp.dto.BranchDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BranchRepository {
    
    private final JdbcClient jdbcClient;
    
    public BranchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<BranchDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, address, city, state, pincode, 
                   phone, email, contact_person AS contactPerson, is_active AS isActive, 
                   created_at AS createdAt, updated_at AS updatedAt
            FROM branches
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(BranchDTO.class)
            .list();
    }
    
    public Optional<BranchDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, address, city, state, pincode, 
                   phone, email, contact_person AS contactPerson, is_active AS isActive, 
                   created_at AS createdAt, updated_at AS updatedAt
            FROM branches
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(BranchDTO.class)
            .optional();
    }
    
    public Long save(BranchDTO branch, Long userId) {
        String sql = """
            INSERT INTO branches 
            (code, name, address, city, state, pincode, phone, email, 
             contact_person, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(branch.getCode())
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive() != null ? branch.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM branches
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(BranchDTO branch, Long userId) {
        String sql = """
            UPDATE branches
            SET name = ?, address = ?, city = ?, state = ?, pincode = ?,
                phone = ?, email = ?, contact_person = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive())
            .param(userId)
            .param(branch.getId())
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Check if active events exist (only check active ones since we use soft delete)
        String checkSql = "SELECT COUNT(*) FROM events WHERE branch_id = ? AND is_active = TRUE";
        Long count = jdbcClient.sql(checkSql)
            .param(id)
            .query(Long.class)
            .single();
        
        if (count > 0) {
            throw new IllegalStateException("Cannot delete branch with existing active events");
        }
        
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE branches
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

