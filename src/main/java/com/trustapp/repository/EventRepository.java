package com.trustapp.repository;

import com.trustapp.dto.DonationTransactionDTO;
import com.trustapp.dto.EventDashboardDTO;
import com.trustapp.dto.EventDTO;
import com.trustapp.dto.EventDropdownDTO;
import com.trustapp.dto.EventStatisticsDTO;
import com.trustapp.dto.ExpenseTransactionDTO;
import com.trustapp.dto.UpcomingEventDTO;
import com.trustapp.dto.VoucherTransactionDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class EventRepository {
    
    private final JdbcClient jdbcClient;
    
    public EventRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<EventDTO> findAll(Long branchId, String status, boolean includeInactive,
                                   LocalDate fromDate, LocalDate toDate, String search,
                                   int page, int size, String sortBy, String sortDir) {
        StringBuilder sql = new StringBuilder("""
            SELECT e.id,
                   e.code,
                   e.name,
                   e.description,
                   e.start_date    AS startDate,
                   e.end_date      AS endDate,
                   e.status,
                   e.branch_id     AS branchId,
                   e.is_active     AS isActive,
                   e.created_at    AS createdAt,
                   e.updated_at    AS updatedAt,
                   b.id            AS branch_id_ref,
                   b.code          AS branch_code,
                   b.name          AS branch_name,
                   b.address       AS branch_address,
                   b.city          AS branch_city,
                   b.state         AS branch_state,
                   b.pincode       AS branch_pincode,
                   b.phone         AS branch_phone,
                   b.email         AS branch_email,
                   cb.id           AS createdBy_id,
                   cb.username     AS createdBy_username,
                   cb.email        AS createdBy_email,
                   ub.id           AS updatedBy_id,
                   ub.username     AS updatedBy_username,
                   ub.email        AS updatedBy_email
            FROM events e
            LEFT JOIN branches b ON e.branch_id = b.id
            LEFT JOIN users cb ON e.created_by = cb.id
            LEFT JOIN users ub ON e.updated_by = ub.id
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions
        if (branchId != null) {
            conditions.add("e.branch_id = ?");
            params.add(branchId);
        }
        
        if (status != null && !status.isEmpty()) {
            conditions.add("e.status = ?");
            params.add(status);
        }
        
        if (fromDate != null) {
            conditions.add("e.start_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("e.end_date <= ?");
            params.add(toDate);
        }
        
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("(e.name LIKE ? OR e.code LIKE ?)");
            String searchPattern = "%" + search.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (!includeInactive) {
            conditions.add("e.is_active = TRUE");
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
        
        return query.query(this::mapEventDTO).list();
    }
    
    public long count(Long branchId, String status, boolean includeInactive,
                      LocalDate fromDate, LocalDate toDate, String search) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM events e
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions (same as findAll)
        if (branchId != null) {
            conditions.add("e.branch_id = ?");
            params.add(branchId);
        }
        
        if (status != null && !status.isEmpty()) {
            conditions.add("e.status = ?");
            params.add(status);
        }
        
        if (fromDate != null) {
            conditions.add("e.start_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("e.end_date <= ?");
            params.add(toDate);
        }
        
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("(e.name LIKE ? OR e.code LIKE ?)");
            String searchPattern = "%" + search.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (!includeInactive) {
            conditions.add("e.is_active = TRUE");
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
            return "e.start_date";
        }
        return switch (sortBy.toLowerCase()) {
            case "enddate" -> "e.end_date";
            case "name" -> "e.name";
            case "createdat" -> "e.created_at";
            default -> "e.start_date";
        };
    }
    
    public Optional<EventDTO> findById(Long id) {
        String sql = """
            SELECT e.id,
                   e.code,
                   e.name,
                   e.description,
                   e.start_date    AS startDate,
                   e.end_date      AS endDate,
                   e.status,
                   e.branch_id     AS branchId,
                   e.is_active     AS isActive,
                   e.created_at    AS createdAt,
                   e.updated_at    AS updatedAt,
                   b.id            AS branch_id_ref,
                   b.code          AS branch_code,
                   b.name          AS branch_name,
                   b.address       AS branch_address,
                   b.city          AS branch_city,
                   b.state         AS branch_state,
                   b.pincode       AS branch_pincode,
                   b.phone         AS branch_phone,
                   b.email         AS branch_email,
                   cb.id           AS createdBy_id,
                   cb.username     AS createdBy_username,
                   cb.email        AS createdBy_email,
                   ub.id           AS updatedBy_id,
                   ub.username     AS updatedBy_username,
                   ub.email        AS updatedBy_email
            FROM events e
            LEFT JOIN branches b ON e.branch_id = b.id
            LEFT JOIN users cb ON e.created_by = cb.id
            LEFT JOIN users ub ON e.updated_by = ub.id
            WHERE e.id = ? AND e.is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(this::mapEventDTO)
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
            .param(event.getBranch() != null ? event.getBranch().getId() : null)
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
            .param(event.getBranch() != null ? event.getBranch().getId() : null)
            .param(event.getIsActive())
            .param(userId)
            .param(event.getId())
            .update();
    }
    
    public int updateStatus(Long id, String status, Long userId) {
        String sql = """
            UPDATE events
            SET status = ?,
                updated_by = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(status)
            .param(userId)
            .param(id)
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false, deleted_at, and deleted_by
        String sql = """
            UPDATE events
            SET is_active = FALSE, 
                deleted_at = CURRENT_TIMESTAMP,
                deleted_by = ?,
                updated_by = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(userId)
            .param(id)
            .update();
    }
    
    public List<EventDropdownDTO> findAllForDropdown(Long branchId) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, code, name
            FROM events
            WHERE is_active = TRUE
            """);
        
        List<Object> params = new ArrayList<>();
        
        if (branchId != null) {
            sql.append(" AND branch_id = ?");
            params.add(branchId);
        }
        
        sql.append(" ORDER BY name ASC");
        
        var query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query((rs, rowNum) -> {
            EventDropdownDTO dto = new EventDropdownDTO();
            dto.setId(rs.getLong("id"));
            dto.setCode(rs.getString("code"));
            dto.setName(rs.getString("name"));
            return dto;
        }).list();
    }
    
    /**
     * Check if event has associated donations
     */
    public boolean hasAssociatedDonations(Long eventId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donations
            WHERE event_id = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(eventId)
            .query(Boolean.class)
            .single();
    }
    
    /**
     * Check if event has associated expenses
     * Note: This assumes expenses table exists with event_id column
     */
    public boolean hasAssociatedExpenses(Long eventId) {
        try {
            String sql = """
                SELECT COUNT(*) > 0
                FROM expenses
                WHERE event_id = ? AND is_active = TRUE
                """;
            
            return jdbcClient.sql(sql)
                .param(eventId)
                .query(Boolean.class)
                .single();
        } catch (Exception e) {
            // If expenses table doesn't exist, return false
            return false;
        }
    }
    
    /**
     * Check if event has associated vouchers
     * Note: This assumes vouchers table exists with event_id column
     */
    public boolean hasAssociatedVouchers(Long eventId) {
        try {
            String sql = """
                SELECT COUNT(*) > 0
                FROM vouchers
                WHERE event_id = ? AND is_active = TRUE
                """;
            
            return jdbcClient.sql(sql)
                .param(eventId)
                .query(Boolean.class)
                .single();
        } catch (Exception e) {
            // If vouchers table doesn't exist, return false
            return false;
        }
    }
    
    /**
     * Check if event has any associated transactions (donations, expenses, or vouchers)
     */
    public boolean hasAssociatedTransactions(Long eventId) {
        return hasAssociatedDonations(eventId);
            // || hasAssociatedExpenses(eventId) 
            // || hasAssociatedVouchers(eventId);
    }

    private EventDTO mapEventDTO(ResultSet rs, int rowNum) throws SQLException {
        EventDTO dto = new EventDTO();
        dto.setId(rs.getLong("id"));
        dto.setCode(rs.getString("code"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setStartDate(rs.getDate("startDate") != null ? rs.getDate("startDate").toLocalDate() : null);
        dto.setEndDate(rs.getDate("endDate") != null ? rs.getDate("endDate").toLocalDate() : null);
        dto.setStatus(rs.getString("status"));
        dto.setIsActive(rs.getBoolean("isActive"));
        dto.setCreatedAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null);
        dto.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null);

        // Branch info
        Long branchRefId = rs.getObject("branch_id_ref", Long.class);
        if (branchRefId != null) {
            EventDTO.BranchInfo branch = new EventDTO.BranchInfo();
            branch.setId(branchRefId);
            branch.setCode(rs.getString("branch_code"));
            branch.setName(rs.getString("branch_name"));
            branch.setAddress(rs.getString("branch_address"));
            branch.setCity(rs.getString("branch_city"));
            branch.setState(rs.getString("branch_state"));
            branch.setPincode(rs.getString("branch_pincode"));
            branch.setPhone(rs.getString("branch_phone"));
            branch.setEmail(rs.getString("branch_email"));
            dto.setBranch(branch);
        }

        // createdBy
        Long createdById = rs.getObject("createdBy_id", Long.class);
        if (createdById != null) {
            EventDTO.UserInfo user = new EventDTO.UserInfo();
            user.setId(createdById);
            user.setUsername(rs.getString("createdBy_username"));
            user.setEmail(rs.getString("createdBy_email"));
            dto.setCreatedBy(user);
        }

        // updatedBy
        Long updatedById = rs.getObject("updatedBy_id", Long.class);
        if (updatedById != null) {
            EventDTO.UserInfo user = new EventDTO.UserInfo();
            user.setId(updatedById);
            user.setUsername(rs.getString("updatedBy_username"));
            user.setEmail(rs.getString("updatedBy_email"));
            dto.setUpdatedBy(user);
        }

        return dto;
    }
    
    /**
     * Get donation statistics for an event
     */
    public EventStatisticsDTO.DonationStatistics getDonationStatistics(Long eventId) {
        // Get aggregate statistics
        String aggregateSql = """
            SELECT 
                COUNT(*) as totalCount,
                COALESCE(SUM(amount), 0) as totalAmount,
                COALESCE(AVG(amount), 0) as averageAmount,
                COALESCE(MIN(amount), 0) as minAmount,
                COALESCE(MAX(amount), 0) as maxAmount
            FROM donations
            WHERE event_id = ? AND is_active = TRUE
            """;
        
        var aggregateResult = jdbcClient.sql(aggregateSql)
            .param(eventId)
            .query((rs, rowNum) -> {
                EventStatisticsDTO.DonationStatistics stats = new EventStatisticsDTO.DonationStatistics();
                stats.setTotalCount(rs.getLong("totalCount"));
                stats.setTotalAmount(rs.getBigDecimal("totalAmount"));
                stats.setAverageAmount(rs.getBigDecimal("averageAmount"));
                stats.setMinAmount(rs.getBigDecimal("minAmount"));
                stats.setMaxAmount(rs.getBigDecimal("maxAmount"));
                return stats;
            })
            .optional()
            .orElse(new EventStatisticsDTO.DonationStatistics());
        
        // Get statistics by payment mode
        String byPaymentModeSql = """
            SELECT 
                pm.name as paymentMode,
                COUNT(*) as count,
                COALESCE(SUM(d.amount), 0) as totalAmount
            FROM donations d
            INNER JOIN payment_modes pm ON d.payment_mode_id = pm.id
            WHERE d.event_id = ? AND d.is_active = TRUE
            GROUP BY pm.id, pm.name
            ORDER BY totalAmount DESC
            """;
        
        List<EventStatisticsDTO.PaymentModeStat> byPaymentMode = jdbcClient.sql(byPaymentModeSql)
            .param(eventId)
            .query((rs, rowNum) -> {
                EventStatisticsDTO.PaymentModeStat stat = new EventStatisticsDTO.PaymentModeStat();
                stat.setPaymentMode(rs.getString("paymentMode"));
                stat.setCount(rs.getLong("count"));
                stat.setTotalAmount(rs.getBigDecimal("totalAmount"));
                return stat;
            })
            .list();
        
        aggregateResult.setByPaymentMode(byPaymentMode);
        return aggregateResult;
    }
    
    /**
     * Get expense statistics for an event
     * Returns empty/default statistics if expenses table doesn't exist
     */
    public EventStatisticsDTO.ExpenseStatistics getExpenseStatistics(Long eventId) {
        try {
            // Get aggregate statistics
            String aggregateSql = """
                SELECT 
                    COUNT(*) as totalCount,
                    COALESCE(SUM(amount), 0) as totalAmount,
                    COALESCE(AVG(amount), 0) as averageAmount
                FROM expenses
                WHERE event_id = ? AND is_active = TRUE
                """;
            
            var aggregateResult = jdbcClient.sql(aggregateSql)
                .param(eventId)
                .query((rs, rowNum) -> {
                    EventStatisticsDTO.ExpenseStatistics stats = new EventStatisticsDTO.ExpenseStatistics();
                    stats.setTotalCount(rs.getLong("totalCount"));
                    stats.setTotalAmount(rs.getBigDecimal("totalAmount"));
                    stats.setAverageAmount(rs.getBigDecimal("averageAmount"));
                    return stats;
                })
                .optional()
                .orElse(new EventStatisticsDTO.ExpenseStatistics());
            
            // Get statistics by category
            String byCategorySql = """
                SELECT 
                    ec.name as category,
                    COUNT(*) as count,
                    COALESCE(SUM(e.amount), 0) as totalAmount
                FROM expenses e
                INNER JOIN expense_categories ec ON e.category_id = ec.id
                WHERE e.event_id = ? AND e.is_active = TRUE
                GROUP BY ec.id, ec.name
                ORDER BY totalAmount DESC
                """;
            
            List<EventStatisticsDTO.CategoryStat> byCategory = jdbcClient.sql(byCategorySql)
                .param(eventId)
                .query((rs, rowNum) -> {
                    EventStatisticsDTO.CategoryStat stat = new EventStatisticsDTO.CategoryStat();
                    stat.setCategory(rs.getString("category"));
                    stat.setCount(rs.getLong("count"));
                    stat.setTotalAmount(rs.getBigDecimal("totalAmount"));
                    return stat;
                })
                .list();
            
            aggregateResult.setByCategory(byCategory);
            return aggregateResult;
        } catch (Exception e) {
            // If expenses table doesn't exist, return empty statistics
            EventStatisticsDTO.ExpenseStatistics stats = new EventStatisticsDTO.ExpenseStatistics();
            stats.setTotalCount(0L);
            stats.setTotalAmount(BigDecimal.ZERO);
            stats.setAverageAmount(BigDecimal.ZERO);
            stats.setByCategory(new ArrayList<>());
            return stats;
        }
    }
    
    /**
     * Get voucher statistics for an event
     * Returns empty/default statistics if vouchers table doesn't exist
     */
    public EventStatisticsDTO.VoucherStatistics getVoucherStatistics(Long eventId) {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as totalCount,
                    COALESCE(SUM(amount), 0) as totalAmount,
                    COALESCE(AVG(amount), 0) as averageAmount
                FROM vouchers
                WHERE event_id = ? AND is_active = TRUE
                """;
            
            return jdbcClient.sql(sql)
                .param(eventId)
                .query((rs, rowNum) -> {
                    EventStatisticsDTO.VoucherStatistics stats = new EventStatisticsDTO.VoucherStatistics();
                    stats.setTotalCount(rs.getLong("totalCount"));
                    stats.setTotalAmount(rs.getBigDecimal("totalAmount"));
                    stats.setAverageAmount(rs.getBigDecimal("averageAmount"));
                    return stats;
                })
                .optional()
                .orElse(new EventStatisticsDTO.VoucherStatistics());
        } catch (Exception e) {
            // If vouchers table doesn't exist, return empty statistics
            EventStatisticsDTO.VoucherStatistics stats = new EventStatisticsDTO.VoucherStatistics();
            stats.setTotalCount(0L);
            stats.setTotalAmount(BigDecimal.ZERO);
            stats.setAverageAmount(BigDecimal.ZERO);
            return stats;
        }
    }
    
    /**
     * Get donations for an event with pagination and filters
     */
    public List<DonationTransactionDTO> getDonationsByEventId(Long eventId, LocalDate fromDate, LocalDate toDate,
                                                               int page, int size) {
        StringBuilder sql = new StringBuilder("""
            SELECT d.id, d.receipt_number AS receiptNumber, d.donor_name AS donorName,
                   d.amount, pm.name AS paymentMode, d.donation_date AS donationDate
            FROM donations d
            INNER JOIN payment_modes pm ON d.payment_mode_id = pm.id
            WHERE d.event_id = ? AND d.is_active = TRUE
            """);
        
        List<Object> params = new ArrayList<>();
        params.add(eventId);
        
        // Add date filters
        if (fromDate != null) {
            sql.append(" AND d.donation_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND d.donation_date <= ?");
            params.add(toDate);
        }
        
        sql.append(" ORDER BY d.donation_date DESC, d.id DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);
        
        return jdbcClient.sql(sql.toString())
            .params(params)
            .query((rs, rowNum) -> {
                DonationTransactionDTO dto = new DonationTransactionDTO();
                dto.setId(rs.getLong("id"));
                dto.setReceiptNumber(rs.getString("receiptNumber"));
                dto.setDonorName(rs.getString("donorName"));
                dto.setAmount(rs.getBigDecimal("amount"));
                dto.setPaymentMode(rs.getString("paymentMode"));
                dto.setDonationDate(rs.getObject("donationDate", LocalDate.class));
                return dto;
            })
            .list();
    }
    
    /**
     * Count donations for an event with filters
     */
    public long countDonationsByEventId(Long eventId, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM donations
            WHERE event_id = ? AND is_active = TRUE
            """);
        
        List<Object> params = new ArrayList<>();
        params.add(eventId);
        
        // Add date filters
        if (fromDate != null) {
            sql.append(" AND donation_date >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND donation_date <= ?");
            params.add(toDate);
        }
        
        return jdbcClient.sql(sql.toString())
            .params(params)
            .query(Long.class)
            .single();
    }
    
    /**
     * Get expenses for an event with pagination and filters
     * Returns empty list if expenses table doesn't exist
     */
    public List<ExpenseTransactionDTO> getExpensesByEventId(Long eventId, LocalDate fromDate, LocalDate toDate,
                                                             int page, int size) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT e.id, e.expense_number AS expenseNumber, v.name AS vendorName,
                       e.amount, ec.name AS category, e.expense_date AS expenseDate
                FROM expenses e
                LEFT JOIN vendors v ON e.vendor_id = v.id
                LEFT JOIN expense_categories ec ON e.category_id = ec.id
                WHERE e.event_id = ? AND e.is_active = TRUE
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(eventId);
            
            // Add date filters
            if (fromDate != null) {
                sql.append(" AND e.expense_date >= ?");
                params.add(fromDate);
            }
            if (toDate != null) {
                sql.append(" AND e.expense_date <= ?");
                params.add(toDate);
            }
            
            sql.append(" ORDER BY e.expense_date DESC, e.id DESC");
            sql.append(" LIMIT ? OFFSET ?");
            params.add(size);
            params.add(page * size);
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    ExpenseTransactionDTO dto = new ExpenseTransactionDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setExpenseNumber(rs.getString("expenseNumber"));
                    dto.setVendorName(rs.getString("vendorName"));
                    dto.setAmount(rs.getBigDecimal("amount"));
                    dto.setCategory(rs.getString("category"));
                    dto.setExpenseDate(rs.getObject("expenseDate", LocalDate.class));
                    return dto;
                })
                .list();
        } catch (Exception e) {
            // If expenses table doesn't exist, return empty list
            return Collections.emptyList();
        }
    }
    
    /**
     * Count expenses for an event with filters
     * Returns 0 if expenses table doesn't exist
     */
    public long countExpensesByEventId(Long eventId, LocalDate fromDate, LocalDate toDate) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM expenses
                WHERE event_id = ? AND is_active = TRUE
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(eventId);
            
            // Add date filters
            if (fromDate != null) {
                sql.append(" AND expense_date >= ?");
                params.add(fromDate);
            }
            if (toDate != null) {
                sql.append(" AND expense_date <= ?");
                params.add(toDate);
            }
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query(Long.class)
                .single();
        } catch (Exception e) {
            // If expenses table doesn't exist, return 0
            return 0L;
        }
    }
    
    /**
     * Get vouchers for an event with pagination and filters
     * Returns empty list if vouchers table doesn't exist
     */
    public List<VoucherTransactionDTO> getVouchersByEventId(Long eventId, LocalDate fromDate, LocalDate toDate,
                                                             int page, int size) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT v.id, v.voucher_number AS voucherNumber, ven.name AS vendorName,
                       v.amount, v.voucher_date AS voucherDate
                FROM vouchers v
                LEFT JOIN vendors ven ON v.vendor_id = ven.id
                WHERE v.event_id = ? AND v.is_active = TRUE
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(eventId);
            
            // Add date filters
            if (fromDate != null) {
                sql.append(" AND v.voucher_date >= ?");
                params.add(fromDate);
            }
            if (toDate != null) {
                sql.append(" AND v.voucher_date <= ?");
                params.add(toDate);
            }
            
            sql.append(" ORDER BY v.voucher_date DESC, v.id DESC");
            sql.append(" LIMIT ? OFFSET ?");
            params.add(size);
            params.add(page * size);
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    VoucherTransactionDTO dto = new VoucherTransactionDTO();
                    dto.setId(rs.getLong("id"));
                    dto.setVoucherNumber(rs.getString("voucherNumber"));
                    dto.setVendorName(rs.getString("vendorName"));
                    dto.setAmount(rs.getBigDecimal("amount"));
                    dto.setVoucherDate(rs.getObject("voucherDate", LocalDate.class));
                    return dto;
                })
                .list();
        } catch (Exception e) {
            // If vouchers table doesn't exist, return empty list
            return Collections.emptyList();
        }
    }
    
    /**
     * Count vouchers for an event with filters
     * Returns 0 if vouchers table doesn't exist
     */
    public long countVouchersByEventId(Long eventId, LocalDate fromDate, LocalDate toDate) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM vouchers
                WHERE event_id = ? AND is_active = TRUE
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(eventId);
            
            // Add date filters
            if (fromDate != null) {
                sql.append(" AND voucher_date >= ?");
                params.add(fromDate);
            }
            if (toDate != null) {
                sql.append(" AND voucher_date <= ?");
                params.add(toDate);
            }
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query(Long.class)
                .single();
        } catch (Exception e) {
            // If vouchers table doesn't exist, return 0
            return 0L;
        }
    }

    /**
     * Find upcoming events within a specified number of days ahead.
     * Includes PLANNED events and optionally ACTIVE events.
     */
    public List<UpcomingEventDTO> findUpcomingEvents(Long branchId, int daysAhead, boolean includeActive) {
        LocalDate today = LocalDate.now();
        LocalDate untilDate = today.plusDays(daysAhead);

        StringBuilder sql = new StringBuilder("""
            SELECT e.id,
                   e.code,
                   e.name,
                   e.start_date AS startDate,
                   e.end_date   AS endDate,
                   e.status,
                   b.id   AS branch_id_ref,
                   b.code AS branch_code,
                   b.name AS branch_name
            FROM events e
            LEFT JOIN branches b ON e.branch_id = b.id
            WHERE e.is_active = TRUE
              AND e.start_date >= ?
              AND e.start_date <= ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(today);
        params.add(untilDate);

        if (branchId != null) {
            sql.append(" AND e.branch_id = ?");
            params.add(branchId);
        }

        if (includeActive) {
            sql.append(" AND e.status IN ('PLANNED', 'ACTIVE')");
        } else {
            sql.append(" AND e.status = 'PLANNED'");
        }

        sql.append(" ORDER BY e.start_date ASC, e.name ASC");

        return jdbcClient.sql(sql.toString())
            .params(params)
            .query((rs, rowNum) -> mapUpcomingEventDTO(rs, today))
            .list();
    }

    private UpcomingEventDTO mapUpcomingEventDTO(ResultSet rs, LocalDate today) throws SQLException {
        UpcomingEventDTO dto = new UpcomingEventDTO();
        dto.setId(rs.getLong("id"));
        dto.setCode(rs.getString("code"));
        dto.setName(rs.getString("name"));

        LocalDate startDate = rs.getObject("startDate", LocalDate.class);
        LocalDate endDate = rs.getObject("endDate", LocalDate.class);

        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setStatus(rs.getString("status"));

        long daysUntilStart = 0;
        if (startDate != null) {
            daysUntilStart = ChronoUnit.DAYS.between(today, startDate);
            if (daysUntilStart < 0) {
                daysUntilStart = 0;
            }
        }
        dto.setDaysUntilStart(daysUntilStart);

        Long branchIdRef = rs.getObject("branch_id_ref", Long.class);
        if (branchIdRef != null) {
            EventDTO.BranchInfo branch = new EventDTO.BranchInfo();
            branch.setId(branchIdRef);
            branch.setCode(rs.getString("branch_code"));
            branch.setName(rs.getString("branch_name"));
            dto.setBranch(branch);
        } else {
            dto.setBranch(null);
        }

        return dto;
    }
    
    /**
     * Get event summary counts by status for dashboard
     */
    public EventDashboardDTO.Summary getEventSummary(Long branchId, int year) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                COUNT(*) as totalEvents,
                SUM(CASE WHEN status = 'PLANNED' THEN 1 ELSE 0 END) as plannedEvents,
                SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as activeEvents,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedEvents,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledEvents
            FROM events
            WHERE is_active = TRUE
              AND YEAR(start_date) = ?
            """);
        
        List<Object> params = new ArrayList<>();
        params.add(year);
        
        if (branchId != null) {
            sql.append(" AND branch_id = ?");
            params.add(branchId);
        }
        
        return jdbcClient.sql(sql.toString())
            .params(params)
            .query((rs, rowNum) -> {
                EventDashboardDTO.Summary summary = new EventDashboardDTO.Summary();
                summary.setTotalEvents(rs.getLong("totalEvents"));
                summary.setPlannedEvents(rs.getLong("plannedEvents"));
                summary.setActiveEvents(rs.getLong("activeEvents"));
                summary.setCompletedEvents(rs.getLong("completedEvents"));
                summary.setCancelledEvents(rs.getLong("cancelledEvents"));
                return summary;
            })
            .optional()
            .orElse(new EventDashboardDTO.Summary());
    }
    
    /**
     * Get upcoming events summary for dashboard (next 30 days)
     */
    public List<EventDashboardDTO.UpcomingEventSummary> getUpcomingEventsSummary(Long branchId) {
        LocalDate today = LocalDate.now();
        LocalDate untilDate = today.plusDays(30);
        
        StringBuilder sql = new StringBuilder("""
            SELECT e.id, e.code, e.name, e.start_date AS startDate, e.status
            FROM events e
            WHERE e.is_active = TRUE
              AND e.status IN ('PLANNED', 'ACTIVE')
              AND e.start_date >= ?
              AND e.start_date <= ?
            """);
        
        List<Object> params = new ArrayList<>();
        params.add(today);
        params.add(untilDate);
        
        if (branchId != null) {
            sql.append(" AND e.branch_id = ?");
            params.add(branchId);
        }
        
        sql.append(" ORDER BY e.start_date ASC LIMIT 10");
        
        return jdbcClient.sql(sql.toString())
            .params(params)
            .query((rs, rowNum) -> {
                EventDashboardDTO.UpcomingEventSummary summary = new EventDashboardDTO.UpcomingEventSummary();
                summary.setId(rs.getLong("id"));
                summary.setCode(rs.getString("code"));
                summary.setName(rs.getString("name"));
                summary.setStartDate(rs.getObject("startDate", LocalDate.class));
                summary.setStatus(rs.getString("status"));
                
                LocalDate startDate = summary.getStartDate();
                if (startDate != null) {
                    long daysUntilStart = ChronoUnit.DAYS.between(today, startDate);
                    summary.setDaysUntilStart(daysUntilStart >= 0 ? daysUntilStart : 0);
                } else {
                    summary.setDaysUntilStart(0L);
                }
                
                return summary;
            })
            .list();
    }
    
    /**
     * Get active events with financial summary for dashboard
     */
    public List<EventDashboardDTO.ActiveEventSummary> getActiveEventsSummary(Long branchId) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    e.id,
                    e.code,
                    e.name,
                    e.start_date AS startDate,
                    e.end_date AS endDate,
                    e.status,
                    COALESCE(SUM(d.amount), 0) AS totalDonations,
                    COALESCE(SUM(exp.amount), 0) AS totalExpenses
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                LEFT JOIN (
                    SELECT event_id, SUM(amount) as amount
                    FROM expenses
                    WHERE is_active = TRUE
                    GROUP BY event_id
                ) exp ON e.id = exp.event_id
                WHERE e.is_active = TRUE
                  AND e.status = 'ACTIVE'
                """);
            
            List<Object> params = new ArrayList<>();
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            sql.append(" GROUP BY e.id, e.code, e.name, e.start_date, e.end_date, e.status");
            sql.append(" ORDER BY e.start_date DESC");
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.ActiveEventSummary summary = new EventDashboardDTO.ActiveEventSummary();
                    summary.setId(rs.getLong("id"));
                    summary.setCode(rs.getString("code"));
                    summary.setName(rs.getString("name"));
                    summary.setStartDate(rs.getObject("startDate", LocalDate.class));
                    summary.setEndDate(rs.getObject("endDate", LocalDate.class));
                    summary.setStatus(rs.getString("status"));
                    
                    BigDecimal totalDonations = rs.getBigDecimal("totalDonations");
                    BigDecimal totalExpenses = rs.getBigDecimal("totalExpenses");
                    
                    summary.setTotalDonations(totalDonations != null ? totalDonations : BigDecimal.ZERO);
                    summary.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
                    summary.setNetAmount(summary.getTotalDonations().subtract(summary.getTotalExpenses()));
                    
                    return summary;
                })
                .list();
        } catch (Exception e) {
            // If expenses table doesn't exist, return active events without expenses
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    e.id,
                    e.code,
                    e.name,
                    e.start_date AS startDate,
                    e.end_date AS endDate,
                    e.status,
                    COALESCE(SUM(d.amount), 0) AS totalDonations
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                WHERE e.is_active = TRUE
                  AND e.status = 'ACTIVE'
                """);
            
            List<Object> params = new ArrayList<>();
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            sql.append(" GROUP BY e.id, e.code, e.name, e.start_date, e.end_date, e.status");
            sql.append(" ORDER BY e.start_date DESC");
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.ActiveEventSummary summary = new EventDashboardDTO.ActiveEventSummary();
                    summary.setId(rs.getLong("id"));
                    summary.setCode(rs.getString("code"));
                    summary.setName(rs.getString("name"));
                    summary.setStartDate(rs.getObject("startDate", LocalDate.class));
                    summary.setEndDate(rs.getObject("endDate", LocalDate.class));
                    summary.setStatus(rs.getString("status"));
                    
                    BigDecimal totalDonations = rs.getBigDecimal("totalDonations");
                    summary.setTotalDonations(totalDonations != null ? totalDonations : BigDecimal.ZERO);
                    summary.setTotalExpenses(BigDecimal.ZERO);
                    summary.setNetAmount(summary.getTotalDonations());
                    
                    return summary;
                })
                .list();
        }
    }
    
    /**
     * Get financial overview for dashboard
     */
    public EventDashboardDTO.FinancialOverview getFinancialOverview(Long branchId, int year) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    COALESCE(SUM(d.amount), 0) AS totalEventIncome,
                    COALESCE(SUM(exp.amount), 0) AS totalEventExpenses,
                    COUNT(DISTINCT e.id) AS eventCount
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                LEFT JOIN (
                    SELECT event_id, SUM(amount) as amount
                    FROM expenses
                    WHERE is_active = TRUE
                    GROUP BY event_id
                ) exp ON e.id = exp.event_id
                WHERE e.is_active = TRUE
                  AND YEAR(e.start_date) = ?
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(year);
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.FinancialOverview overview = new EventDashboardDTO.FinancialOverview();
                    
                    BigDecimal totalIncome = rs.getBigDecimal("totalEventIncome");
                    BigDecimal totalExpenses = rs.getBigDecimal("totalEventExpenses");
                    long eventCount = rs.getLong("eventCount");
                    
                    overview.setTotalEventIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
                    overview.setTotalEventExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
                    
                    BigDecimal netProfit = overview.getTotalEventIncome().subtract(overview.getTotalEventExpenses());
                    overview.setNetEventProfit(netProfit);
                    
                    BigDecimal averageProfit = BigDecimal.ZERO;
                    if (eventCount > 0) {
                        averageProfit = netProfit.divide(BigDecimal.valueOf(eventCount), 2, java.math.RoundingMode.HALF_UP);
                    }
                    overview.setAverageEventProfit(averageProfit);
                    
                    return overview;
                })
                .optional()
                .orElse(new EventDashboardDTO.FinancialOverview());
        } catch (Exception e) {
            // If expenses table doesn't exist, return financial overview without expenses
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    COALESCE(SUM(d.amount), 0) AS totalEventIncome,
                    COUNT(DISTINCT e.id) AS eventCount
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                WHERE e.is_active = TRUE
                  AND YEAR(e.start_date) = ?
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(year);
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.FinancialOverview overview = new EventDashboardDTO.FinancialOverview();
                    
                    BigDecimal totalIncome = rs.getBigDecimal("totalEventIncome");
                    long eventCount = rs.getLong("eventCount");
                    
                    overview.setTotalEventIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
                    overview.setTotalEventExpenses(BigDecimal.ZERO);
                    
                    BigDecimal netProfit = overview.getTotalEventIncome();
                    overview.setNetEventProfit(netProfit);
                    
                    BigDecimal averageProfit = BigDecimal.ZERO;
                    if (eventCount > 0) {
                        averageProfit = netProfit.divide(BigDecimal.valueOf(eventCount), 2, java.math.RoundingMode.HALF_UP);
                    }
                    overview.setAverageEventProfit(averageProfit);
                    
                    return overview;
                })
                .optional()
                .orElse(new EventDashboardDTO.FinancialOverview());
        }
    }
    
    /**
     * Get monthly breakdown for dashboard
     */
    public List<EventDashboardDTO.MonthlyBreakdown> getMonthlyBreakdown(Long branchId, int year) {
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    MONTHNAME(e.start_date) AS month,
                    MONTH(e.start_date) AS monthNum,
                    COUNT(DISTINCT e.id) AS eventCount,
                    COALESCE(SUM(d.amount), 0) AS totalIncome,
                    COALESCE(SUM(exp.amount), 0) AS totalExpenses
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                LEFT JOIN (
                    SELECT event_id, SUM(amount) as amount
                    FROM expenses
                    WHERE is_active = TRUE
                    GROUP BY event_id
                ) exp ON e.id = exp.event_id
                WHERE e.is_active = TRUE
                  AND YEAR(e.start_date) = ?
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(year);
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            sql.append(" GROUP BY MONTHNAME(e.start_date), MONTH(e.start_date)");
            sql.append(" ORDER BY monthNum ASC");
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.MonthlyBreakdown breakdown = new EventDashboardDTO.MonthlyBreakdown();
                    breakdown.setMonth(rs.getString("month"));
                    breakdown.setEventCount(rs.getLong("eventCount"));
                    
                    BigDecimal totalIncome = rs.getBigDecimal("totalIncome");
                    BigDecimal totalExpenses = rs.getBigDecimal("totalExpenses");
                    
                    breakdown.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
                    breakdown.setTotalExpenses(totalExpenses != null ? totalExpenses : BigDecimal.ZERO);
                    
                    return breakdown;
                })
                .list();
        } catch (Exception e) {
            // If expenses table doesn't exist, return monthly breakdown without expenses
            StringBuilder sql = new StringBuilder("""
                SELECT 
                    MONTHNAME(e.start_date) AS month,
                    MONTH(e.start_date) AS monthNum,
                    COUNT(DISTINCT e.id) AS eventCount,
                    COALESCE(SUM(d.amount), 0) AS totalIncome
                FROM events e
                LEFT JOIN donations d ON e.id = d.event_id AND d.is_active = TRUE
                WHERE e.is_active = TRUE
                  AND YEAR(e.start_date) = ?
                """);
            
            List<Object> params = new ArrayList<>();
            params.add(year);
            
            if (branchId != null) {
                sql.append(" AND e.branch_id = ?");
                params.add(branchId);
            }
            
            sql.append(" GROUP BY MONTHNAME(e.start_date), MONTH(e.start_date)");
            sql.append(" ORDER BY monthNum ASC");
            
            return jdbcClient.sql(sql.toString())
                .params(params)
                .query((rs, rowNum) -> {
                    EventDashboardDTO.MonthlyBreakdown breakdown = new EventDashboardDTO.MonthlyBreakdown();
                    breakdown.setMonth(rs.getString("month"));
                    breakdown.setEventCount(rs.getLong("eventCount"));
                    
                    BigDecimal totalIncome = rs.getBigDecimal("totalIncome");
                    breakdown.setTotalIncome(totalIncome != null ? totalIncome : BigDecimal.ZERO);
                    breakdown.setTotalExpenses(BigDecimal.ZERO);
                    
                    return breakdown;
                })
                .list();
        }
    }
}

