package com.trustapp.repository;

import com.trustapp.dto.EventDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EventRepository {
    
    private final JdbcClient jdbcClient;
    
    public EventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<EventDTO> findAll(Long branchId, String status, boolean includeInactive) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, code, name, description, start_date AS startDate, end_date AS endDate,
                   status, branch_id AS branchId, is_active AS isActive,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM events
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        if (branchId != null) {
            conditions.add("branch_id = ?");
            params.add(branchId);
        }
        
        if (status != null && !status.isEmpty()) {
            conditions.add("status = ?");
            params.add(status);
        }
        
        if (!includeInactive) {
            conditions.add("is_active = TRUE");
        }
        
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }
        
        sql.append(" ORDER BY start_date DESC, name ASC");
        
        JdbcClient.StatementSpec query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query(EventDTO.class).list();
    }
    
    public Optional<EventDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, start_date AS startDate, end_date AS endDate,
                   status, branch_id AS branchId, is_active AS isActive,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM events
            WHERE id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(EventDTO.class)
            .optional();
    }
    
    public Long save(EventDTO event, Long userId) {
        String sql = """
            INSERT INTO events 
            (code, name, description, start_date, end_date, status, branch_id, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(event.getCode())
            .param(event.getName())
            .param(event.getDescription())
            .param(event.getStartDate())
            .param(event.getEndDate())
            .param(event.getStatus() != null ? event.getStatus() : "PLANNED")
            .param(event.getBranchId())
            .param(event.getIsActive() != null ? event.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM events
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(EventDTO event, Long userId) {
        String sql = """
            UPDATE events
            SET name = ?, description = ?, start_date = ?, end_date = ?,
                status = ?, branch_id = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(event.getName())
            .param(event.getDescription())
            .param(event.getStartDate())
            .param(event.getEndDate())
            .param(event.getStatus())
            .param(event.getBranchId())
            .param(event.getIsActive())
            .param(userId)
            .param(event.getId())
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE events
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

