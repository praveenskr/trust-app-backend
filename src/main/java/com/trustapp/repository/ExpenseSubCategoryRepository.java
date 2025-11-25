package com.trustapp.repository;

import com.trustapp.dto.ExpenseSubCategoryDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ExpenseSubCategoryRepository {
    
    private final JdbcClient jdbcClient;
    
    public ExpenseSubCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<ExpenseSubCategoryDTO> findAll(Long categoryId, boolean includeInactive) {
        String sql;
        JdbcClient.StatementSpec query;
        
        if (categoryId != null) {
            sql = """
                SELECT id, category_id AS categoryId, code, name, description, 
                       display_order AS displayOrder, is_active AS isActive, 
                       created_at AS createdAt, updated_at AS updatedAt
                FROM expense_sub_categories
                WHERE category_id = ?
                """ + (includeInactive ? "" : "AND is_active = TRUE ") + """
                ORDER BY display_order ASC, name ASC
                """;
            query = jdbcClient.sql(sql).param(categoryId);
        } else {
            sql = """
                SELECT id, category_id AS categoryId, code, name, description, 
                       display_order AS displayOrder, is_active AS isActive, 
                       created_at AS createdAt, updated_at AS updatedAt
                FROM expense_sub_categories
                """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
                ORDER BY category_id ASC, display_order ASC, name ASC
                """;
            query = jdbcClient.sql(sql);
        }
        
        return query.query(ExpenseSubCategoryDTO.class).list();
    }
    
    public Optional<ExpenseSubCategoryDTO> findById(Long id) {
        String sql = """
            SELECT id, category_id AS categoryId, code, name, description, 
                   display_order AS displayOrder, is_active AS isActive, 
                   created_at AS createdAt, updated_at AS updatedAt
            FROM expense_sub_categories
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(ExpenseSubCategoryDTO.class)
            .optional();
    }
    
    public Long save(ExpenseSubCategoryDTO subCategory, Long userId) {
        String sql = """
            INSERT INTO expense_sub_categories 
            (category_id, code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(subCategory.getCategoryId())
            .param(subCategory.getCode())
            .param(subCategory.getName())
            .param(subCategory.getDescription())
            .param(subCategory.getDisplayOrder() != null ? subCategory.getDisplayOrder() : 0)
            .param(subCategory.getIsActive() != null ? subCategory.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCodeAndCategoryId(String code, Long categoryId, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM expense_sub_categories
            WHERE code = ? AND category_id = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(categoryId)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(ExpenseSubCategoryDTO subCategory, Long userId) {
        String sql = """
            UPDATE expense_sub_categories
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
            UPDATE expense_sub_categories
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

