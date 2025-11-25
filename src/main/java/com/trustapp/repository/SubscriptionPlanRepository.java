package com.trustapp.repository;

import com.trustapp.dto.SubscriptionPlanDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionPlanRepository {
    
    private final JdbcClient jdbcClient;
    
    public SubscriptionPlanRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<SubscriptionPlanDTO> findAll(String planType, boolean includeInactive) {
        String whereClause = "";
        if (planType != null && !planType.isEmpty()) {
            whereClause += " AND plan_type = ? ";
        }
        if (!includeInactive) {
            whereClause += " AND is_active = TRUE ";
        }
        
        String sql = """
            SELECT id, code, name, plan_type AS planType, duration_months AS durationMonths,
                   amount, description, is_active AS isActive, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM subscription_plans
            WHERE 1=1
            """ + whereClause + """
            ORDER BY name ASC
            """;
        
        var query = jdbcClient.sql(sql);
        if (planType != null && !planType.isEmpty()) {
            query = query.param(planType);
        }
        
        return query.query(SubscriptionPlanDTO.class).list();
    }
    
    public Optional<SubscriptionPlanDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, plan_type AS planType, duration_months AS durationMonths,
                   amount, description, is_active AS isActive, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM subscription_plans
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(SubscriptionPlanDTO.class)
            .optional();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM subscription_plans
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public Long save(SubscriptionPlanDTO plan, Long userId) {
        String sql = """
            INSERT INTO subscription_plans 
            (code, name, plan_type, duration_months, amount, description, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(plan.getCode())
            .param(plan.getName())
            .param(plan.getPlanType())
            .param(plan.getDurationMonths())
            .param(plan.getAmount())
            .param(plan.getDescription())
            .param(plan.getIsActive() != null ? plan.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(Long id, SubscriptionPlanDTO plan, Long userId) {
        String sql = """
            UPDATE subscription_plans
            SET code = ?, name = ?, plan_type = ?, duration_months = ?,
                amount = ?, description = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(plan.getCode())
            .param(plan.getName())
            .param(plan.getPlanType())
            .param(plan.getDurationMonths())
            .param(plan.getAmount())
            .param(plan.getDescription())
            .param(plan.getIsActive())
            .param(userId)
            .param(id)
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Check if active subscription discounts exist
        String checkSql = "SELECT COUNT(*) FROM subscription_discounts WHERE plan_id = ? AND is_active = TRUE";
        Long count = jdbcClient.sql(checkSql)
            .param(id)
            .query(Long.class)
            .single();
        
        if (count > 0) {
            throw new IllegalStateException("Cannot delete subscription plan with existing active discounts");
        }
        
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE subscription_plans
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

