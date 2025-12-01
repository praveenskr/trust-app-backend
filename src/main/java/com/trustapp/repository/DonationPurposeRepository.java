package com.trustapp.repository;

import com.trustapp.dto.DonationPurposeDTO;
import com.trustapp.dto.DonationPurposeDropdownDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DonationPurposeRepository {
    
    private final JdbcClient jdbcClient;
    
    public DonationPurposeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<DonationPurposeDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, description, display_order AS displayOrder, 
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM donation_purposes
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(DonationPurposeDTO.class)
            .list();
    }
    
    public Optional<DonationPurposeDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, display_order AS displayOrder, 
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM donation_purposes
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(DonationPurposeDTO.class)
            .optional();
    }
    
    public Long save(DonationPurposeDTO purpose, Long userId) {
        String sql = """
            INSERT INTO donation_purposes 
            (code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(purpose.getCode())
            .param(purpose.getName())
            .param(purpose.getDescription())
            .param(purpose.getDisplayOrder() != null ? purpose.getDisplayOrder() : 0)
            .param(purpose.getIsActive() != null ? purpose.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(DonationPurposeDTO purpose, Long userId) {
        String sql = """
            UPDATE donation_purposes
            SET name = ?, description = ?, display_order = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(purpose.getName())
            .param(purpose.getDescription())
            .param(purpose.getDisplayOrder())
            .param(purpose.getIsActive())
            .param(userId)
            .param(purpose.getId())
            .update();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donation_purposes
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int delete(Long id, Long userId) {
        // Check if active sub-categories exist (only check active ones since we use soft delete)
        String checkSql = "SELECT COUNT(*) FROM donation_sub_categories WHERE purpose_id = ? AND is_active = TRUE";
        Long count = jdbcClient.sql(checkSql)
            .param(id)
            .query(Long.class)
            .single();
        
        if (count > 0) {
            throw new IllegalStateException("Cannot delete purpose with existing active sub-categories");
        }
        
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE donation_purposes
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
    
    public List<DonationPurposeDropdownDTO> findAllForDropdown() {
        String sql = """
            SELECT id, code, name
            FROM donation_purposes
            WHERE is_active = TRUE
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query((rs, rowNum) -> {
                DonationPurposeDropdownDTO dto = new DonationPurposeDropdownDTO();
                dto.setId(rs.getLong("id"));
                dto.setCode(rs.getString("code"));
                dto.setName(rs.getString("name"));
                return dto;
            })
            .list();
    }
}

