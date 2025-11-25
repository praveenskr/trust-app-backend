package com.trustapp.repository;

import com.trustapp.dto.SubscriptionDiscountDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionDiscountRepository {
    
    private final JdbcClient jdbcClient;
    
    public SubscriptionDiscountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<SubscriptionDiscountDTO> findAll(Long planId, Boolean isActive, LocalDate validFrom, LocalDate validTo) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, plan_id AS planId, discount_type AS discountType, discount_value AS discountValue,
                   min_quantity AS minQuantity, max_quantity AS maxQuantity, valid_from AS validFrom,
                   valid_to AS validTo, is_active AS isActive, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM subscription_discounts
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        if (planId != null) {
            conditions.add("plan_id = ?");
            params.add(planId);
        }
        
        if (isActive != null) {
            conditions.add("is_active = ?");
            params.add(isActive);
        }
        
        if (validFrom != null) {
            conditions.add("valid_from = ?");
            params.add(validFrom);
        }
        
        if (validTo != null) {
            conditions.add("valid_to = ?");
            params.add(validTo);
        }
        
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }
        
        sql.append(" ORDER BY plan_id ASC, valid_from DESC, created_at DESC");
        
        JdbcClient.StatementSpec query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query(SubscriptionDiscountDTO.class).list();
    }
    
    public Optional<SubscriptionDiscountDTO> findById(Long id) {
        String sql = """
            SELECT id, plan_id AS planId, discount_type AS discountType, discount_value AS discountValue,
                   min_quantity AS minQuantity, max_quantity AS maxQuantity, valid_from AS validFrom,
                   valid_to AS validTo, is_active AS isActive, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM subscription_discounts
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(SubscriptionDiscountDTO.class)
            .optional();
    }
    
    public Long save(SubscriptionDiscountDTO discount, Long userId) {
        String sql = """
            INSERT INTO subscription_discounts
            (plan_id, discount_type, discount_value, min_quantity, max_quantity,
             valid_from, valid_to, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(discount.getPlanId())
            .param(discount.getDiscountType())
            .param(discount.getDiscountValue())
            .param(discount.getMinQuantity())
            .param(discount.getMaxQuantity())
            .param(discount.getValidFrom())
            .param(discount.getValidTo())
            .param(discount.getIsActive() != null ? discount.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(Long id, SubscriptionDiscountDTO discount, Long userId) {
        String sql = """
            UPDATE subscription_discounts
            SET plan_id = ?, discount_type = ?, discount_value = ?,
                min_quantity = ?, max_quantity = ?, valid_from = ?, valid_to = ?,
                is_active = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(discount.getPlanId())
            .param(discount.getDiscountType())
            .param(discount.getDiscountValue())
            .param(discount.getMinQuantity())
            .param(discount.getMaxQuantity())
            .param(discount.getValidFrom())
            .param(discount.getValidTo())
            .param(discount.getIsActive())
            .param(userId)
            .param(id)
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE subscription_discounts
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

