package com.trustapp.repository;

import com.trustapp.dto.ExpenseCategoryDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ExpenseCategoryRepository {
    
    private final JdbcClient jdbcClient;
    
    public ExpenseCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<ExpenseCategoryDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, description, display_order AS displayOrder, 
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM expense_categories
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(ExpenseCategoryDTO.class)
            .list();
    }
    
    public Optional<ExpenseCategoryDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, display_order AS displayOrder, 
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM expense_categories
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(ExpenseCategoryDTO.class)
            .optional();
    }
    
    public Long save(ExpenseCategoryDTO category, Long userId) {
        String sql = """
            INSERT INTO expense_categories 
            (code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(category.getCode())
            .param(category.getName())
            .param(category.getDescription())
            .param(category.getDisplayOrder() != null ? category.getDisplayOrder() : 0)
            .param(category.getIsActive() != null ? category.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM expense_categories
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(ExpenseCategoryDTO category, Long userId) {
        String sql = """
            UPDATE expense_categories
            SET name = ?, description = ?, display_order = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(category.getName())
            .param(category.getDescription())
            .param(category.getDisplayOrder())
            .param(category.getIsActive())
            .param(userId)
            .param(category.getId())
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Check if active sub-categories exist (only check active ones since we use soft delete)
        String checkSql = "SELECT COUNT(*) FROM expense_sub_categories WHERE category_id = ? AND is_active = TRUE";
        Long count = jdbcClient.sql(checkSql)
            .param(id)
            .query(Long.class)
            .single();
        
        if (count > 0) {
            throw new IllegalStateException("Cannot delete category with existing active sub-categories");
        }
        
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE expense_categories
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

