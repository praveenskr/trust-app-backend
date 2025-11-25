package com.trustapp.repository;

import com.trustapp.dto.PaymentModeDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentModeRepository {
    
    private final JdbcClient jdbcClient;
    
    public PaymentModeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<PaymentModeDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, description, requires_receipt AS requiresReceipt,
                   display_order AS displayOrder, is_active AS isActive,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM payment_modes
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(PaymentModeDTO.class)
            .list();
    }
    
    public Optional<PaymentModeDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, requires_receipt AS requiresReceipt,
                   display_order AS displayOrder, is_active AS isActive,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM payment_modes
            WHERE id = ? AND is_active = true
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(PaymentModeDTO.class)
            .optional();
    }
    
    public Long save(PaymentModeDTO paymentMode) {
        String sql = """
            INSERT INTO payment_modes 
            (code, name, description, requires_receipt, display_order, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(paymentMode.getCode())
            .param(paymentMode.getName())
            .param(paymentMode.getDescription())
            .param(paymentMode.getRequiresReceipt() != null ? paymentMode.getRequiresReceipt() : true)
            .param(paymentMode.getDisplayOrder() != null ? paymentMode.getDisplayOrder() : 0)
            .param(paymentMode.getIsActive() != null ? paymentMode.getIsActive() : true)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM payment_modes
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(Long id, PaymentModeDTO paymentMode) {
        String sql = """
            UPDATE payment_modes
            SET code = ?, name = ?, description = ?, requires_receipt = ?,
                display_order = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(paymentMode.getCode())
            .param(paymentMode.getName())
            .param(paymentMode.getDescription())
            .param(paymentMode.getRequiresReceipt())
            .param(paymentMode.getDisplayOrder())
            .param(paymentMode.getIsActive())
            .param(id)
            .update();
    }
    
    public int delete(Long id) {
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE payment_modes
            SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .update();
    }
}

