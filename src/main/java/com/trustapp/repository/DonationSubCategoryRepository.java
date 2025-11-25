package com.trustapp.repository;

import com.trustapp.dto.DonationSubCategoryDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DonationSubCategoryRepository {
    
    private final JdbcClient jdbcClient;
    
    public DonationSubCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<DonationSubCategoryDTO> findAll(Long purposeId, boolean includeInactive) {
        String sql;
        JdbcClient.StatementSpec query;
        
        if (purposeId != null) {
            sql = """
                SELECT id, purpose_id AS purposeId, code, name, description, 
                       display_order AS displayOrder, is_active AS isActive, 
                       created_at AS createdAt, updated_at AS updatedAt
                FROM donation_sub_categories
                WHERE purpose_id = ?
                """ + (includeInactive ? "" : "AND is_active = TRUE ") + """
                ORDER BY display_order ASC, name ASC
                """;
            query = jdbcClient.sql(sql).param(purposeId);
        } else {
            sql = """
                SELECT id, purpose_id AS purposeId, code, name, description, 
                       display_order AS displayOrder, is_active AS isActive, 
                       created_at AS createdAt, updated_at AS updatedAt
                FROM donation_sub_categories
                """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
                ORDER BY purpose_id ASC, display_order ASC, name ASC
                """;
            query = jdbcClient.sql(sql);
        }
        
        return query.query(DonationSubCategoryDTO.class).list();
    }
    
    public Optional<DonationSubCategoryDTO> findById(Long id) {
        String sql = """
            SELECT id, purpose_id AS purposeId, code, name, description, 
                   display_order AS displayOrder, is_active AS isActive, 
                   created_at AS createdAt, updated_at AS updatedAt
            FROM donation_sub_categories
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(DonationSubCategoryDTO.class)
            .optional();
    }
    
    public Long save(DonationSubCategoryDTO subCategory, Long userId) {
        String sql = """
            INSERT INTO donation_sub_categories 
            (purpose_id, code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(subCategory.getPurposeId())
            .param(subCategory.getCode())
            .param(subCategory.getName())
            .param(subCategory.getDescription())
            .param(subCategory.getDisplayOrder() != null ? subCategory.getDisplayOrder() : 0)
            .param(subCategory.getIsActive() != null ? subCategory.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCodeAndPurposeId(String code, Long purposeId, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donation_sub_categories
            WHERE code = ? AND purpose_id = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(purposeId)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(DonationSubCategoryDTO subCategory, Long userId) {
        String sql = """
            UPDATE donation_sub_categories
            SET name = ?, description = ?, display_order = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(subCategory.getName())
            .param(subCategory.getDescription())
            .param(subCategory.getDisplayOrder())
            .param(subCategory.getIsActive())
            .param(userId)
            .param(subCategory.getId())
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE donation_sub_categories
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

