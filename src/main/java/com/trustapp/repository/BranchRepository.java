package com.trustapp.repository;

import com.trustapp.dto.BranchDTO;
import com.trustapp.dto.BranchDropdownDTO;
import com.trustapp.dto.BranchStatisticsDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class BranchRepository {
    
    private final JdbcClient jdbcClient;
    
    public BranchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<BranchDTO> findAll(boolean includeInactive, String city, String state, String search,
                                   List<Long> accessibleBranchIds, int page, int size, String sortBy, String sortDir) {
        StringBuilder sql = new StringBuilder("""
            SELECT b.id, b.code, b.name, b.address, b.city, b.state, b.pincode, 
                   b.phone, b.email, b.contact_person AS contactPerson, b.is_active AS isActive, 
                   b.created_at AS createdAt, b.updated_at AS updatedAt,
                   cb.id AS createdBy_id, cb.username AS createdBy_username, cb.email AS createdBy_email,
                   ub.id AS updatedBy_id, ub.username AS updatedBy_username, ub.email AS updatedBy_email
            FROM branches b
            LEFT JOIN users cb ON b.created_by = cb.id
            LEFT JOIN users ub ON b.updated_by = ub.id
            """);
        
        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder();
        boolean hasWhere = false;
        
        // Active filter
        if (!includeInactive) {
            whereClause.append("b.is_active = TRUE");
            hasWhere = true;
        }
        
        // City filter
        if (city != null && !city.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("b.city = ?");
            hasWhere = true;
        }
        
        // State filter
        if (state != null && !state.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("b.state = ?");
            hasWhere = true;
        }
        
        // Search filter (name or code)
        if (search != null && !search.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("(b.name LIKE ? OR b.code LIKE ?)");
            hasWhere = true;
        }
        
        // Branch access filter (if not null, filter by accessible branches)
        if (accessibleBranchIds != null) {
            if (hasWhere) whereClause.append(" AND ");
            if (accessibleBranchIds.isEmpty()) {
                // User has no branch access, return empty result
                whereClause.append("1 = 0");
            } else {
                whereClause.append("b.id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) whereClause.append(", ");
                    whereClause.append("?");
                }
                whereClause.append(")");
            }
            hasWhere = true;
        }
        
        if (hasWhere) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        // Build ORDER BY clause
        String sortField = getSortField(sortBy);
        String sortDirection = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);
        
        // Add pagination
        sql.append(" LIMIT ? OFFSET ?");
        
        // Build query with parameters
        var query = jdbcClient.sql(sql.toString());
        
        // Set parameters in order
        if (city != null && !city.trim().isEmpty()) {
            query = query.param(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            query = query.param(state);
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search + "%";
            query = query.param(searchPattern).param(searchPattern);
        }
        if (accessibleBranchIds != null && !accessibleBranchIds.isEmpty()) {
            for (Long branchId : accessibleBranchIds) {
                query = query.param(branchId);
            }
        }
        query = query.param(size).param(page * size);
        
        return query.query((rs, rowNum) -> mapBranchDTO(rs)).list();
    }
    
    public long count(boolean includeInactive, String city, String state, String search, List<Long> accessibleBranchIds) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM branches b");
        
        // Build WHERE clause (same as findAll)
        StringBuilder whereClause = new StringBuilder();
        boolean hasWhere = false;
        
        if (!includeInactive) {
            whereClause.append("b.is_active = TRUE");
            hasWhere = true;
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("b.city = ?");
            hasWhere = true;
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("b.state = ?");
            hasWhere = true;
        }
        
        if (search != null && !search.trim().isEmpty()) {
            if (hasWhere) whereClause.append(" AND ");
            whereClause.append("(b.name LIKE ? OR b.code LIKE ?)");
            hasWhere = true;
        }
        
        if (accessibleBranchIds != null) {
            if (hasWhere) whereClause.append(" AND ");
            if (accessibleBranchIds.isEmpty()) {
                whereClause.append("1 = 0");
            } else {
                whereClause.append("b.id IN (");
                for (int i = 0; i < accessibleBranchIds.size(); i++) {
                    if (i > 0) whereClause.append(", ");
                    whereClause.append("?");
                }
                whereClause.append(")");
            }
            hasWhere = true;
        }
        
        if (hasWhere) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        var query = jdbcClient.sql(sql.toString());
        
        // Set parameters in same order
        if (city != null && !city.trim().isEmpty()) {
            query = query.param(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            query = query.param(state);
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search + "%";
            query = query.param(searchPattern).param(searchPattern);
        }
        if (accessibleBranchIds != null && !accessibleBranchIds.isEmpty()) {
            for (Long branchId : accessibleBranchIds) {
                query = query.param(branchId);
            }
        }
        
        return query.query(Long.class).single();
    }
    
    private String getSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "b.name";
        }
        return switch (sortBy.toLowerCase()) {
            case "code" -> "b.code";
            case "city" -> "b.city";
            case "createdat" -> "b.created_at";
            default -> "b.name";
        };
    }
    
    private BranchDTO mapBranchDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        BranchDTO dto = new BranchDTO();
        dto.setId(rs.getLong("id"));
        dto.setCode(rs.getString("code"));
        dto.setName(rs.getString("name"));
        dto.setAddress(rs.getString("address"));
        dto.setCity(rs.getString("city"));
        dto.setState(rs.getString("state"));
        dto.setPincode(rs.getString("pincode"));
        dto.setPhone(rs.getString("phone"));
        dto.setEmail(rs.getString("email"));
        dto.setContactPerson(rs.getString("contactPerson"));
        dto.setIsActive(rs.getBoolean("isActive"));
        dto.setCreatedAt(rs.getTimestamp("createdAt") != null ? 
            rs.getTimestamp("createdAt").toLocalDateTime() : null);
        dto.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? 
            rs.getTimestamp("updatedAt").toLocalDateTime() : null);
        
        // Set createdBy user info
        Long createdById = rs.getObject("createdBy_id", Long.class);
        if (createdById != null) {
            BranchDTO.UserInfo createdBy = new BranchDTO.UserInfo();
            createdBy.setId(createdById);
            createdBy.setUsername(rs.getString("createdBy_username"));
            createdBy.setEmail(rs.getString("createdBy_email"));
            dto.setCreatedBy(createdBy);
        }
        
        // Set updatedBy user info
        Long updatedById = rs.getObject("updatedBy_id", Long.class);
        if (updatedById != null) {
            BranchDTO.UserInfo updatedBy = new BranchDTO.UserInfo();
            updatedBy.setId(updatedById);
            updatedBy.setUsername(rs.getString("updatedBy_username"));
            updatedBy.setEmail(rs.getString("updatedBy_email"));
            dto.setUpdatedBy(updatedBy);
        }
        
        return dto;
    }
    
    public Optional<BranchDTO> findById(Long id) {
        String sql = """
            SELECT b.id, b.code, b.name, b.address, b.city, b.state, b.pincode, 
                   b.phone, b.email, b.contact_person AS contactPerson, b.is_active AS isActive, 
                   b.created_at AS createdAt, b.updated_at AS updatedAt,
                   cb.id AS createdBy_id, cb.username AS createdBy_username, cb.email AS createdBy_email,
                   ub.id AS updatedBy_id, ub.username AS updatedBy_username, ub.email AS updatedBy_email
            FROM branches b
            LEFT JOIN users cb ON b.created_by = cb.id
            LEFT JOIN users ub ON b.updated_by = ub.id
            WHERE b.id = ? AND b.is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query((rs, rowNum) -> {
                BranchDTO dto = new BranchDTO();
                dto.setId(rs.getLong("id"));
                dto.setCode(rs.getString("code"));
                dto.setName(rs.getString("name"));
                dto.setAddress(rs.getString("address"));
                dto.setCity(rs.getString("city"));
                dto.setState(rs.getString("state"));
                dto.setPincode(rs.getString("pincode"));
                dto.setPhone(rs.getString("phone"));
                dto.setEmail(rs.getString("email"));
                dto.setContactPerson(rs.getString("contactPerson"));
                dto.setIsActive(rs.getBoolean("isActive"));
                dto.setCreatedAt(rs.getTimestamp("createdAt") != null ? 
                    rs.getTimestamp("createdAt").toLocalDateTime() : null);
                dto.setUpdatedAt(rs.getTimestamp("updatedAt") != null ? 
                    rs.getTimestamp("updatedAt").toLocalDateTime() : null);
                
                // Set createdBy user info
                Long createdById = rs.getObject("createdBy_id", Long.class);
                if (createdById != null) {
                    BranchDTO.UserInfo createdBy = new BranchDTO.UserInfo();
                    createdBy.setId(createdById);
                    createdBy.setUsername(rs.getString("createdBy_username"));
                    createdBy.setEmail(rs.getString("createdBy_email"));
                    dto.setCreatedBy(createdBy);
                }
                
                // Set updatedBy user info
                Long updatedById = rs.getObject("updatedBy_id", Long.class);
                if (updatedById != null) {
                    BranchDTO.UserInfo updatedBy = new BranchDTO.UserInfo();
                    updatedBy.setId(updatedById);
                    updatedBy.setUsername(rs.getString("updatedBy_username"));
                    updatedBy.setEmail(rs.getString("updatedBy_email"));
                    dto.setUpdatedBy(updatedBy);
                }
                
                return dto;
            })
            .optional();
    }
    
    public Long save(BranchDTO branch, Long userId) {
        String sql = """
            INSERT INTO branches 
            (code, name, address, city, state, pincode, phone, email, 
             contact_person, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(branch.getCode())
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive() != null ? branch.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM branches
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public List<Long> findActiveBranchIdsByIds(List<Long> branchIds) {
        if (branchIds == null || branchIds.isEmpty()) {
            return List.of();
        }
        
        StringBuilder sql = new StringBuilder("""
            SELECT id
            FROM branches
            WHERE is_active = TRUE AND id IN (
            """);
        
        for (int i = 0; i < branchIds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");
        
        var query = jdbcClient.sql(sql.toString());
        for (Long branchId : branchIds) {
            query = query.param(branchId);
        }
        
        return query.query(Long.class).list();
    }
    
    public int update(BranchDTO branch, Long userId) {
        String sql = """
            UPDATE branches
            SET name = ?, address = ?, city = ?, state = ?, pincode = ?,
                phone = ?, email = ?, contact_person = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive())
            .param(userId)
            .param(branch.getId())
            .update();
    }
    
    public boolean hasActiveTransactions(Long branchId) {
        // Check for active donations
        String donationsSql = "SELECT COUNT(*) FROM donations WHERE branch_id = ? AND is_active = TRUE";
        Long donationsCount = jdbcClient.sql(donationsSql)
            .param(branchId)
            .query(Long.class)
            .single();
        
        if (donationsCount != null && donationsCount > 0) {
            return true;
        }
        
        // Check for active expenses
        // String expensesSql = "SELECT COUNT(*) FROM expenses WHERE branch_id = ? AND is_active = TRUE";
        // Long expensesCount = jdbcClient.sql(expensesSql)
        //     .param(branchId)
        //     .query(Long.class)
        //     .single();
        
        // if (expensesCount != null && expensesCount > 0) {
        //     return true;
        // }
        
        // Check for active events
        String eventsSql = "SELECT COUNT(*) FROM events WHERE branch_id = ? AND is_active = TRUE";
        Long eventsCount = jdbcClient.sql(eventsSql)
            .param(branchId)
            .query(Long.class)
            .single();
        
        return eventsCount != null && eventsCount > 0;
    }
    
    public boolean hasActiveTransactionsForDelete(Long branchId) {
        // Check for active donations
        String donationsSql = "SELECT COUNT(*) FROM donations WHERE branch_id = ? AND is_active = TRUE";
        Long donationsCount = jdbcClient.sql(donationsSql)
            .param(branchId)
            .query(Long.class)
            .single();
        
        if (donationsCount != null && donationsCount > 0) {
            return true;
        }
        
        // Check for active expenses (commented out if table doesn't exist)
        // String expensesSql = "SELECT COUNT(*) FROM expenses WHERE branch_id = ? AND is_active = TRUE";
        // Long expensesCount = jdbcClient.sql(expensesSql)
        //     .param(branchId)
        //     .query(Long.class)
        //     .single();
        // 
        // if (expensesCount != null && expensesCount > 0) {
        //     return true;
        // }
        
        // Check for active events
        String eventsSql = "SELECT COUNT(*) FROM events WHERE branch_id = ? AND is_active = TRUE";
        Long eventsCount = jdbcClient.sql(eventsSql)
            .param(branchId)
            .query(Long.class)
            .single();
        
        if (eventsCount != null && eventsCount > 0) {
            return true;
        }
        
        // Check for users with access to this branch
        String usersSql = "SELECT COUNT(*) FROM user_branch_access WHERE branch_id = ?";
        Long usersCount = jdbcClient.sql(usersSql)
            .param(branchId)
            .query(Long.class)
            .single();
        
        return usersCount != null && usersCount > 0;
    }
    
    public int delete(Long id, Long userId) {
        // Check if branch has active transactions (donations, expenses, events, or users)
        if (hasActiveTransactionsForDelete(id)) {
            throw new IllegalStateException("Cannot delete branch. Branch has active donations, expenses, events or users. Please remove associations first.");
        }
        
        // Soft delete: Set is_active = false, deleted_at, and deleted_by
        String sql = """
            UPDATE branches
            SET is_active = FALSE, deleted_at = CURRENT_TIMESTAMP, deleted_by = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(userId)
            .param(id)
            .update();
    }
    
    public BranchStatisticsDTO getBranchStatistics(Long branchId, LocalDate fromDate, LocalDate toDate) {
        BranchStatisticsDTO stats = new BranchStatisticsDTO();
        
        // Get branch basic info
        BranchDTO branch = findById(branchId).orElse(null);
        if (branch != null) {
            BranchStatisticsDTO.BranchInfo branchInfo = new BranchStatisticsDTO.BranchInfo();
            branchInfo.setId(branch.getId());
            branchInfo.setCode(branch.getCode());
            branchInfo.setName(branch.getName());
            stats.setBranch(branchInfo);
        }
        
        // Build date filter clause
        String dateFilter = "";
        if (fromDate != null && toDate != null) {
            dateFilter = " AND donation_date BETWEEN ? AND ?";
        } else if (fromDate != null) {
            dateFilter = " AND donation_date >= ?";
        } else if (toDate != null) {
            dateFilter = " AND donation_date <= ?";
        }
        
        // Get donation statistics
        String donationsSql = """
            SELECT 
                COUNT(*) AS totalCount,
                COALESCE(SUM(amount), 0) AS totalAmount,
                COALESCE(AVG(amount), 0) AS averageAmount,
                COALESCE(MIN(amount), 0) AS minAmount,
                COALESCE(MAX(amount), 0) AS maxAmount
            FROM donations
            WHERE branch_id = ? AND is_active = TRUE
            """ + dateFilter;
        
        var donationQuery = jdbcClient.sql(donationsSql).param(branchId);
        if (fromDate != null && toDate != null) {
            donationQuery = donationQuery.param(Date.valueOf(fromDate)).param(Date.valueOf(toDate));
        } else if (fromDate != null) {
            donationQuery = donationQuery.param(Date.valueOf(fromDate));
        } else if (toDate != null) {
            donationQuery = donationQuery.param(Date.valueOf(toDate));
        }
        
        var donationStats = donationQuery.query((rs, rowNum) -> {
            BranchStatisticsDTO.DonationStatistics dto = new BranchStatisticsDTO.DonationStatistics();
            dto.setTotalCount(rs.getLong("totalCount"));
            dto.setTotalAmount(rs.getBigDecimal("totalAmount"));
            dto.setAverageAmount(rs.getBigDecimal("averageAmount"));
            dto.setMinAmount(rs.getBigDecimal("minAmount"));
            dto.setMaxAmount(rs.getBigDecimal("maxAmount"));
            return dto;
        }).single();
        
        // Get donations by payment mode
        String paymentModeSql = """
            SELECT 
                pm.name AS paymentMode,
                COUNT(*) AS count,
                COALESCE(SUM(d.amount), 0) AS totalAmount
            FROM donations d
            INNER JOIN payment_modes pm ON d.payment_mode_id = pm.id
            WHERE d.branch_id = ? AND d.is_active = TRUE
            """ + dateFilter + """
            GROUP BY pm.id, pm.name
            ORDER BY totalAmount DESC
            """;
        
        var paymentModeQuery = jdbcClient.sql(paymentModeSql).param(branchId);
        if (fromDate != null && toDate != null) {
            paymentModeQuery = paymentModeQuery.param(Date.valueOf(fromDate)).param(Date.valueOf(toDate));
        } else if (fromDate != null) {
            paymentModeQuery = paymentModeQuery.param(Date.valueOf(fromDate));
        } else if (toDate != null) {
            paymentModeQuery = paymentModeQuery.param(Date.valueOf(toDate));
        }
        
        List<BranchStatisticsDTO.PaymentModeStat> paymentModeStats = paymentModeQuery.query((rs, rowNum) -> {
            BranchStatisticsDTO.PaymentModeStat stat = new BranchStatisticsDTO.PaymentModeStat();
            stat.setPaymentMode(rs.getString("paymentMode"));
            stat.setCount(rs.getLong("count"));
            stat.setTotalAmount(rs.getBigDecimal("totalAmount"));
            return stat;
        }).list();
        
        donationStats.setByPaymentMode(paymentModeStats);
        stats.setDonations(donationStats);
        
        // Get expense statistics (if table exists - commented for now)
        BranchStatisticsDTO.ExpenseStatistics expenseStats = new BranchStatisticsDTO.ExpenseStatistics();
        expenseStats.setTotalCount(0L);
        expenseStats.setTotalAmount(BigDecimal.ZERO);
        expenseStats.setAverageAmount(BigDecimal.ZERO);
        stats.setExpenses(expenseStats);
        
        // Get voucher statistics (if table exists - commented for now)
        BranchStatisticsDTO.VoucherStatistics voucherStats = new BranchStatisticsDTO.VoucherStatistics();
        voucherStats.setTotalCount(0L);
        voucherStats.setTotalAmount(BigDecimal.ZERO);
        voucherStats.setAverageAmount(BigDecimal.ZERO);
        stats.setVouchers(voucherStats);
        
        // Get event statistics
        // For events, we filter by start_date if date range is provided
        String eventDateFilter = "";
        if (fromDate != null && toDate != null) {
            eventDateFilter = " AND start_date BETWEEN ? AND ?";
        } else if (fromDate != null) {
            eventDateFilter = " AND start_date >= ?";
        } else if (toDate != null) {
            eventDateFilter = " AND start_date <= ?";
        }
        
        String eventsSql = """
            SELECT 
                COUNT(*) AS totalCount,
                SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS activeCount,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount,
                SUM(CASE WHEN status = 'PLANNED' THEN 1 ELSE 0 END) AS plannedCount
            FROM events
            WHERE branch_id = ? AND is_active = TRUE
            """ + eventDateFilter;
        
        var eventQuery = jdbcClient.sql(eventsSql).param(branchId);
        if (fromDate != null && toDate != null) {
            eventQuery = eventQuery.param(Date.valueOf(fromDate)).param(Date.valueOf(toDate));
        } else if (fromDate != null) {
            eventQuery = eventQuery.param(Date.valueOf(fromDate));
        } else if (toDate != null) {
            eventQuery = eventQuery.param(Date.valueOf(toDate));
        }
        
        var eventStats = eventQuery.query((rs, rowNum) -> {
            BranchStatisticsDTO.EventStatistics dto = new BranchStatisticsDTO.EventStatistics();
            dto.setTotalCount(rs.getLong("totalCount"));
            dto.setActiveCount(rs.getLong("activeCount"));
            dto.setCompletedCount(rs.getLong("completedCount"));
            dto.setPlannedCount(rs.getLong("plannedCount"));
            return dto;
        }).single();
        stats.setEvents(eventStats);
        
        // Get user statistics (from user_branch_access)
        String usersSql = """
            SELECT 
                COUNT(DISTINCT uba.user_id) AS totalCount,
                SUM(CASE WHEN u.is_active = TRUE THEN 1 ELSE 0 END) AS activeCount,
                SUM(CASE WHEN u.is_active = FALSE THEN 1 ELSE 0 END) AS inactiveCount
            FROM user_branch_access uba
            INNER JOIN users u ON uba.user_id = u.id
            WHERE uba.branch_id = ?
            """;
        
        var userStats = jdbcClient.sql(usersSql)
            .param(branchId)
            .query((rs, rowNum) -> {
                BranchStatisticsDTO.UserStatistics dto = new BranchStatisticsDTO.UserStatistics();
                dto.setTotalCount(rs.getLong("totalCount"));
                dto.setActiveCount(rs.getLong("activeCount"));
                dto.setInactiveCount(rs.getLong("inactiveCount"));
                return dto;
            }).single();
        stats.setUsers(userStats);
        
        // Calculate financial summary
        BigDecimal totalIncome = donationStats.getTotalAmount();
        BigDecimal totalExpenses = expenseStats.getTotalAmount();
        BigDecimal netAmount = totalIncome.subtract(totalExpenses);
        BigDecimal profitMargin = totalIncome.compareTo(BigDecimal.ZERO) > 0 
            ? netAmount.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;
        
        BranchStatisticsDTO.FinancialSummary financialSummary = new BranchStatisticsDTO.FinancialSummary();
        financialSummary.setTotalIncome(totalIncome);
        financialSummary.setTotalExpenses(totalExpenses);
        financialSummary.setNetAmount(netAmount);
        financialSummary.setProfitMargin(profitMargin);
        stats.setFinancialSummary(financialSummary);
        
        // Get inter-branch transfer statistics
        String transferDateFilter = "";
        if (fromDate != null && toDate != null) {
            transferDateFilter = " AND transfer_date BETWEEN ? AND ?";
        } else if (fromDate != null) {
            transferDateFilter = " AND transfer_date >= ?";
        } else if (toDate != null) {
            transferDateFilter = " AND transfer_date <= ?";
        }
        
        // Get incoming transfers (to this branch)
        String incomingSql = """
            SELECT COALESCE(SUM(amount), 0) AS totalIncoming
            FROM inter_branch_transfers
            WHERE to_branch_id = ? AND is_active = TRUE AND status = 'COMPLETED'
            """ + transferDateFilter;
        
        var incomingQuery = jdbcClient.sql(incomingSql).param(branchId);
        if (fromDate != null && toDate != null) {
            incomingQuery = incomingQuery.param(Date.valueOf(fromDate)).param(Date.valueOf(toDate));
        } else if (fromDate != null) {
            incomingQuery = incomingQuery.param(Date.valueOf(fromDate));
        } else if (toDate != null) {
            incomingQuery = incomingQuery.param(Date.valueOf(toDate));
        }
        
        BigDecimal totalIncoming = incomingQuery.query(BigDecimal.class).single();
        
        // Get outgoing transfers (from this branch)
        String outgoingSql = """
            SELECT COALESCE(SUM(amount), 0) AS totalOutgoing
            FROM inter_branch_transfers
            WHERE from_branch_id = ? AND is_active = TRUE AND status = 'COMPLETED'
            """ + transferDateFilter;
        
        var outgoingQuery = jdbcClient.sql(outgoingSql).param(branchId);
        if (fromDate != null && toDate != null) {
            outgoingQuery = outgoingQuery.param(Date.valueOf(fromDate)).param(Date.valueOf(toDate));
        } else if (fromDate != null) {
            outgoingQuery = outgoingQuery.param(Date.valueOf(fromDate));
        } else if (toDate != null) {
            outgoingQuery = outgoingQuery.param(Date.valueOf(toDate));
        }
        
        BigDecimal totalOutgoing = outgoingQuery.query(BigDecimal.class).single();
        BigDecimal netTransfer = totalIncoming.subtract(totalOutgoing);
        
        BranchStatisticsDTO.InterBranchTransferStatistics transferStats = new BranchStatisticsDTO.InterBranchTransferStatistics();
        transferStats.setTotalIncoming(totalIncoming);
        transferStats.setTotalOutgoing(totalOutgoing);
        transferStats.setNetTransfer(netTransfer);
        stats.setInterBranchTransfers(transferStats);
        
        return stats;
    }
    
    public List<BranchDropdownDTO> findAllForDropdown(List<Long> accessibleBranchIds) {
        // Service ensures: null = super user (all branches), not null = has accessible branches (not empty)
        StringBuilder sql = new StringBuilder("""
            SELECT b.id, b.code, b.name
            FROM branches b
            WHERE b.is_active = TRUE
            """);
        
        // Add branch access filter if not super user (accessibleBranchIds is not null)
        if (accessibleBranchIds != null) {
            sql.append(" AND b.id IN (");
            for (int i = 0; i < accessibleBranchIds.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
        }
        
        sql.append(" ORDER BY b.name ASC");
        
        var query = jdbcClient.sql(sql.toString());
        
        // Set parameters only if filtering by accessible branches
        if (accessibleBranchIds != null) {
            for (Long branchId : accessibleBranchIds) {
                query = query.param(branchId);
            }
        }
        
        return query.query((rs, rowNum) -> {
            BranchDropdownDTO dto = new BranchDropdownDTO();
            dto.setId(rs.getLong("id"));
            dto.setCode(rs.getString("code"));
            dto.setName(rs.getString("name"));
            return dto;
        }).list();
    }
}

