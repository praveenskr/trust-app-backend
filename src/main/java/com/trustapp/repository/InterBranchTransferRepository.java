package com.trustapp.repository;

import com.trustapp.dto.InterBranchTransferDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InterBranchTransferRepository {
    
    private final JdbcClient jdbcClient;
    
    public InterBranchTransferRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public Long save(String transferNumber, Long fromBranchId, Long toBranchId, BigDecimal amount,
                     LocalDate transferDate, Long paymentModeId, String referenceNumber,
                     String description, String status, Long createdBy) {
        String sql = """
            INSERT INTO inter_branch_transfers 
            (transfer_number, from_branch_id, to_branch_id, amount, transfer_date,
             payment_mode_id, reference_number, description, status, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(transferNumber)
            .param(fromBranchId)
            .param(toBranchId)
            .param(amount)
            .param(Date.valueOf(transferDate))
            .param(paymentModeId)
            .param(referenceNumber)
            .param(description)
            .param(status != null ? status : "PENDING")
            .param(true)
            .param(createdBy)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public Optional<InterBranchTransferDTO> findById(Long id) {
        String sql = """
            SELECT ibt.id, ibt.transfer_number AS transferNumber,
                   ibt.amount, ibt.transfer_date AS transferDate,
                   ibt.reference_number AS referenceNumber, ibt.description,
                   ibt.status, ibt.is_active AS isActive,
                   ibt.created_at AS createdAt, ibt.updated_at AS updatedAt,
                   fb.id AS fromBranchId, fb.code AS fromBranchCode, fb.name AS fromBranchName,
                   tb.id AS toBranchId, tb.code AS toBranchCode, tb.name AS toBranchName,
                   pm.id AS paymentModeId, pm.code AS paymentModeCode, pm.name AS paymentModeName,
                   u1.id AS createdById, u1.username AS createdByUsername, u1.email AS createdByEmail,
                   u2.id AS updatedById, u2.username AS updatedByUsername, u2.email AS updatedByEmail
            FROM inter_branch_transfers ibt
            INNER JOIN branches fb ON ibt.from_branch_id = fb.id
            INNER JOIN branches tb ON ibt.to_branch_id = tb.id
            INNER JOIN payment_modes pm ON ibt.payment_mode_id = pm.id
            LEFT JOIN users u1 ON ibt.created_by = u1.id
            LEFT JOIN users u2 ON ibt.updated_by = u2.id
            WHERE ibt.id = ? AND ibt.is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query((rs, rowNum) -> mapRowToInterBranchTransferDTO(rs))
            .optional();
    }
    
    public boolean existsByTransferNumber(String transferNumber) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM inter_branch_transfers
            WHERE transfer_number = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(transferNumber)
            .query(Boolean.class)
            .single();
    }

    public int updateStatus(Long id, String status, String referenceNumber, Long updatedBy) {
        String sql = """
            UPDATE inter_branch_transfers
            SET status = ?, reference_number = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ? AND is_active = TRUE
            """;

        return jdbcClient.sql(sql)
            .param(status)
            .param(referenceNumber)
            .param(updatedBy)
            .param(id)
            .update();
    }
    
    public List<InterBranchTransferDTO> findAll(Long fromBranchId, Long toBranchId, String status,
                                                 LocalDate fromDate, LocalDate toDate,
                                                 List<Long> accessibleBranchIds,
                                                 int page, int size, String sortBy, String sortDir) {
        StringBuilder sql = new StringBuilder("""
            SELECT ibt.id, ibt.transfer_number AS transferNumber,
                   ibt.amount, ibt.transfer_date AS transferDate,
                   ibt.reference_number AS referenceNumber, ibt.description,
                   ibt.status, ibt.is_active AS isActive,
                   ibt.created_at AS createdAt, ibt.updated_at AS updatedAt,
                   fb.id AS fromBranchId, fb.code AS fromBranchCode, fb.name AS fromBranchName,
                   tb.id AS toBranchId, tb.code AS toBranchCode, tb.name AS toBranchName,
                   pm.id AS paymentModeId, pm.code AS paymentModeCode, pm.name AS paymentModeName,
                   u1.id AS createdById, u1.username AS createdByUsername, u1.email AS createdByEmail,
                   u2.id AS updatedById, u2.username AS updatedByUsername, u2.email AS updatedByEmail
            FROM inter_branch_transfers ibt
            INNER JOIN branches fb ON ibt.from_branch_id = fb.id
            INNER JOIN branches tb ON ibt.to_branch_id = tb.id
            INNER JOIN payment_modes pm ON ibt.payment_mode_id = pm.id
            LEFT JOIN users u1 ON ibt.created_by = u1.id
            LEFT JOIN users u2 ON ibt.updated_by = u2.id
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions
        conditions.add("ibt.is_active = TRUE");
        
        if (fromBranchId != null) {
            conditions.add("ibt.from_branch_id = ?");
            params.add(fromBranchId);
        }
        
        if (toBranchId != null) {
            conditions.add("ibt.to_branch_id = ?");
            params.add(toBranchId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            conditions.add("ibt.status = ?");
            params.add(status);
        }
        
        if (fromDate != null) {
            conditions.add("ibt.transfer_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("ibt.transfer_date <= ?");
            params.add(toDate);
        }
        
        // Branch access filter - users see only transfers involving branches they have access to
        // If accessibleBranchIds is null, user is super user (see all transfers)
        // If accessibleBranchIds is empty, user has no branch access (see no transfers)
        // Otherwise, filter by: fromBranchId IN accessibleBranchIds OR toBranchId IN accessibleBranchIds
        if (accessibleBranchIds != null) {
            if (accessibleBranchIds.isEmpty()) {
                // User has no branch access, return empty result
                conditions.add("1 = 0");
            } else {
                // User has access to specific branches - show transfers where from or to branch is in accessible list
                StringBuilder branchFilter = new StringBuilder("(");
                branchFilter.append("ibt.from_branch_id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) branchFilter.append(", ");
                    branchFilter.append("?");
                }
                branchFilter.append(") OR ibt.to_branch_id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) branchFilter.append(", ");
                    branchFilter.append("?");
                }
                branchFilter.append(")");
                branchFilter.append(")");
                conditions.add(branchFilter.toString());
                // Add params twice - once for fromBranchId IN clause, once for toBranchId IN clause
                for (Long branchId : accessibleBranchIds) {
                    params.add(branchId);
                }
                for (Long branchId : accessibleBranchIds) {
                    params.add(branchId);
                }
            }
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        // Add ORDER BY
        String sortField = getSortField(sortBy);
        String direction = "DESC".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(sortField).append(" ").append(direction);
        
        // Add pagination
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);
        
        JdbcClient.StatementSpec query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query((rs, rowNum) -> mapRowToInterBranchTransferDTO(rs)).list();
    }
    
    public long count(Long fromBranchId, Long toBranchId, String status,
                      LocalDate fromDate, LocalDate toDate,
                      List<Long> accessibleBranchIds) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM inter_branch_transfers ibt
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions (same as findAll)
        conditions.add("ibt.is_active = TRUE");
        
        if (fromBranchId != null) {
            conditions.add("ibt.from_branch_id = ?");
            params.add(fromBranchId);
        }
        
        if (toBranchId != null) {
            conditions.add("ibt.to_branch_id = ?");
            params.add(toBranchId);
        }
        
        if (status != null && !status.trim().isEmpty()) {
            conditions.add("ibt.status = ?");
            params.add(status);
        }
        
        if (fromDate != null) {
            conditions.add("ibt.transfer_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("ibt.transfer_date <= ?");
            params.add(toDate);
        }
        
        // Branch access filter (same as findAll)
        if (accessibleBranchIds != null) {
            if (accessibleBranchIds.isEmpty()) {
                conditions.add("1 = 0");
            } else {
                StringBuilder branchFilter = new StringBuilder("(");
                branchFilter.append("ibt.from_branch_id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) branchFilter.append(", ");
                    branchFilter.append("?");
                }
                branchFilter.append(") OR ibt.to_branch_id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) branchFilter.append(", ");
                    branchFilter.append("?");
                }
                branchFilter.append(")");
                branchFilter.append(")");
                conditions.add(branchFilter.toString());
                // Add params twice - once for fromBranchId IN clause, once for toBranchId IN clause
                for (Long branchId : accessibleBranchIds) {
                    params.add(branchId);
                }
                for (Long branchId : accessibleBranchIds) {
                    params.add(branchId);
                }
            }
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        JdbcClient.StatementSpec query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query(Long.class).single();
    }
    
    private String getSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "ibt.transfer_date";
        }
        return switch (sortBy.toLowerCase()) {
            case "amount" -> "ibt.amount";
            case "createdat" -> "ibt.created_at";
            default -> "ibt.transfer_date";
        };
    }
    
    private InterBranchTransferDTO mapRowToInterBranchTransferDTO(ResultSet rs) throws SQLException {
        InterBranchTransferDTO dto = new InterBranchTransferDTO();
        dto.setId(rs.getLong("id"));
        dto.setTransferNumber(rs.getString("transferNumber"));
        dto.setAmount(rs.getBigDecimal("amount"));
        dto.setTransferDate(rs.getDate("transferDate") != null ? 
            rs.getDate("transferDate").toLocalDate() : null);
        dto.setReferenceNumber(rs.getString("referenceNumber"));
        dto.setDescription(rs.getString("description"));
        dto.setStatus(rs.getString("status"));
        dto.setIsActive(rs.getBoolean("isActive"));
        dto.setCreatedAt(rs.getTimestamp("createdAt") != null ? 
            rs.getTimestamp("createdAt").toLocalDateTime() : null);
        dto.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? 
            rs.getTimestamp("updatedAt").toLocalDateTime() : null);
        
        // From Branch
        InterBranchTransferDTO.BranchInfo fromBranch = new InterBranchTransferDTO.BranchInfo();
        fromBranch.setId(rs.getLong("fromBranchId"));
        fromBranch.setCode(rs.getString("fromBranchCode"));
        fromBranch.setName(rs.getString("fromBranchName"));
        dto.setFromBranch(fromBranch);
        
        // To Branch
        InterBranchTransferDTO.BranchInfo toBranch = new InterBranchTransferDTO.BranchInfo();
        toBranch.setId(rs.getLong("toBranchId"));
        toBranch.setCode(rs.getString("toBranchCode"));
        toBranch.setName(rs.getString("toBranchName"));
        dto.setToBranch(toBranch);
        
        // Payment Mode
        InterBranchTransferDTO.PaymentModeInfo paymentMode = new InterBranchTransferDTO.PaymentModeInfo();
        paymentMode.setId(rs.getLong("paymentModeId"));
        paymentMode.setCode(rs.getString("paymentModeCode"));
        paymentMode.setName(rs.getString("paymentModeName"));
        dto.setPaymentMode(paymentMode);
        
        // Created By
        Long createdById = rs.getObject("createdById", Long.class);
        if (createdById != null) {
            InterBranchTransferDTO.UserInfo createdBy = new InterBranchTransferDTO.UserInfo();
            createdBy.setId(createdById);
            createdBy.setUsername(rs.getString("createdByUsername"));
            createdBy.setEmail(rs.getString("createdByEmail"));
            dto.setCreatedBy(createdBy);
        }
        
        // Updated By
        Long updatedById = rs.getObject("updatedById", Long.class);
        if (updatedById != null) {
            InterBranchTransferDTO.UserInfo updatedBy = new InterBranchTransferDTO.UserInfo();
            updatedBy.setId(updatedById);
            updatedBy.setUsername(rs.getString("updatedByUsername"));
            updatedBy.setEmail(rs.getString("updatedByEmail"));
            dto.setUpdatedBy(updatedBy);
        }
        
        return dto;
    }
}

