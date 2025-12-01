package com.trustapp.repository;

import com.trustapp.dto.DonationDTO;
import com.trustapp.dto.DonorDropdownDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DonationRepository {
    
    private final JdbcClient jdbcClient;
    
    public DonationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public Long save(String receiptNumber, String donorName, String donorAddress, String panNumber,
                     String donorPhone, String donorEmail, java.math.BigDecimal amount,
                     Long paymentModeId, Long purposeId, Long subCategoryId, Long eventId,
                     Long branchId, java.time.LocalDate donationDate, String notes, Long userId) {
        String sql = """
            INSERT INTO donations 
            (receipt_number, donor_name, donor_address, pan_number, donor_phone, donor_email,
             amount, payment_mode_id, purpose_id, sub_category_id, event_id, branch_id,
             donation_date, notes, receipt_generated, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(receiptNumber)
            .param(donorName)
            .param(donorAddress)
            .param(panNumber)
            .param(donorPhone)
            .param(donorEmail)
            .param(amount)
            .param(paymentModeId)
            .param(purposeId)
            .param(subCategoryId)
            .param(eventId)
            .param(branchId)
            .param(donationDate)
            .param(notes)
            .param(false)
            .param(true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public Optional<DonationDTO> findById(Long id) {
        String sql = """
            SELECT d.id, d.receipt_number AS receiptNumber, d.donor_name AS donorName,
                   d.donor_address AS donorAddress, d.pan_number AS panNumber,
                   d.donor_phone AS donorPhone, d.donor_email AS donorEmail,
                   d.amount, d.donation_date AS donationDate, d.notes,
                   d.receipt_generated AS receiptGenerated, d.receipt_generated_at AS receiptGeneratedAt,
                   d.receipt_file_path AS receiptFilePath, d.is_active AS isActive,
                   d.created_at AS createdAt, d.updated_at AS updatedAt,
                   pm.id AS paymentModeId, pm.code AS paymentModeCode, pm.name AS paymentModeName, pm.description AS paymentModeDescription,
                   dp.id AS purposeId, dp.code AS purposeCode, dp.name AS purposeName, dp.description AS purposeDescription,
                   dsc.id AS subCategoryId, dsc.code AS subCategoryCode, dsc.name AS subCategoryName, dsc.description AS subCategoryDescription,
                   e.id AS eventId, e.code AS eventCode, e.name AS eventName, e.description AS eventDescription,
                   e.start_date AS eventStartDate, e.end_date AS eventEndDate, e.status AS eventStatus,
                   b.id AS branchId, b.code AS branchCode, b.name AS branchName,
                   b.address AS branchAddress, b.city AS branchCity, b.state AS branchState,
                   u1.id AS createdById, u1.username AS createdByUsername, u1.email AS createdByEmail,
                   u2.id AS updatedById, u2.username AS updatedByUsername, u2.email AS updatedByEmail
            FROM donations d
            INNER JOIN payment_modes pm ON d.payment_mode_id = pm.id
            INNER JOIN donation_purposes dp ON d.purpose_id = dp.id
            LEFT JOIN donation_sub_categories dsc ON d.sub_category_id = dsc.id
            LEFT JOIN events e ON d.event_id = e.id
            INNER JOIN branches b ON d.branch_id = b.id
            LEFT JOIN users u1 ON d.created_by = u1.id
            LEFT JOIN users u2 ON d.updated_by = u2.id
            WHERE d.id = ? AND d.is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query((rs, rowNum) -> {
                DonationDTO donation = mapRowToDonationDTO(rs);
                
                // Created By
                Long createdById = rs.getObject("createdById", Long.class);
                if (createdById != null) {
                    com.trustapp.dto.UserDTO createdBy = new com.trustapp.dto.UserDTO();
                    createdBy.setId(createdById);
                    createdBy.setUsername(rs.getString("createdByUsername"));
                    createdBy.setEmail(rs.getString("createdByEmail"));
                    donation.setCreatedBy(createdBy);
                }
                
                // Updated By
                Long updatedById = rs.getObject("updatedById", Long.class);
                if (updatedById != null) {
                    com.trustapp.dto.UserDTO updatedBy = new com.trustapp.dto.UserDTO();
                    updatedBy.setId(updatedById);
                    updatedBy.setUsername(rs.getString("updatedByUsername"));
                    updatedBy.setEmail(rs.getString("updatedByEmail"));
                    donation.setUpdatedBy(updatedBy);
                }
                
                return donation;
            })
            .optional();
    }
    
    public boolean existsByReceiptNumber(String receiptNumber) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donations
            WHERE receipt_number = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(receiptNumber)
            .query(Boolean.class)
            .single();
    }
    
    public int update(Long id, String donorName, String donorAddress, String panNumber,
                      String donorPhone, String donorEmail, java.math.BigDecimal amount,
                      Long paymentModeId, Long purposeId, Long subCategoryId, Long eventId,
                      Long branchId, java.time.LocalDate donationDate, String notes, Long userId) {
        String sql = """
            UPDATE donations
            SET donor_name = ?, donor_address = ?, pan_number = ?, donor_phone = ?,
                donor_email = ?, amount = ?, payment_mode_id = ?, purpose_id = ?,
                sub_category_id = ?, event_id = ?, branch_id = ?, donation_date = ?,
                notes = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(donorName)
            .param(donorAddress)
            .param(panNumber)
            .param(donorPhone)
            .param(donorEmail)
            .param(amount)
            .param(paymentModeId)
            .param(purposeId)
            .param(subCategoryId)
            .param(eventId)
            .param(branchId)
            .param(donationDate)
            .param(notes)
            .param(userId)
            .param(id)
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false, deleted_at = CURRENT_TIMESTAMP, deleted_by = userId
        String sql = """
            UPDATE donations
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
    
    public List<DonationDTO> findAll(Long branchId, Long purposeId, Long eventId, Long paymentModeId,
                                     LocalDate fromDate, LocalDate toDate, String donorName,
                                     String panNumber, String receiptNumber, boolean includeInactive,
                                     int page, int size, String sortBy, String sortDir) {
        StringBuilder sql = new StringBuilder("""
            SELECT d.id, d.receipt_number AS receiptNumber, d.donor_name AS donorName,
                   d.donor_address AS donorAddress, d.pan_number AS panNumber,
                   d.donor_phone AS donorPhone, d.donor_email AS donorEmail,
                   d.amount, d.donation_date AS donationDate, d.notes,
                   d.receipt_generated AS receiptGenerated, d.receipt_generated_at AS receiptGeneratedAt,
                   d.receipt_file_path AS receiptFilePath, d.is_active AS isActive,
                   d.created_at AS createdAt, d.updated_at AS updatedAt,
                   pm.id AS paymentModeId, pm.code AS paymentModeCode, pm.name AS paymentModeName, pm.description AS paymentModeDescription,
                   dp.id AS purposeId, dp.code AS purposeCode, dp.name AS purposeName, dp.description AS purposeDescription,
                   dsc.id AS subCategoryId, dsc.code AS subCategoryCode, dsc.name AS subCategoryName, dsc.description AS subCategoryDescription,
                   e.id AS eventId, e.code AS eventCode, e.name AS eventName, e.description AS eventDescription,
                   e.start_date AS eventStartDate, e.end_date AS eventEndDate, e.status AS eventStatus,
                   b.id AS branchId, b.code AS branchCode, b.name AS branchName,
                   b.address AS branchAddress, b.city AS branchCity, b.state AS branchState
            FROM donations d
            INNER JOIN payment_modes pm ON d.payment_mode_id = pm.id
            INNER JOIN donation_purposes dp ON d.purpose_id = dp.id
            LEFT JOIN donation_sub_categories dsc ON d.sub_category_id = dsc.id
            LEFT JOIN events e ON d.event_id = e.id
            INNER JOIN branches b ON d.branch_id = b.id
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions
        if (branchId != null) {
            conditions.add("d.branch_id = ?");
            params.add(branchId);
        }
        
        if (purposeId != null) {
            conditions.add("d.purpose_id = ?");
            params.add(purposeId);
        }
        
        if (eventId != null) {
            conditions.add("d.event_id = ?");
            params.add(eventId);
        }
        
        if (paymentModeId != null) {
            conditions.add("d.payment_mode_id = ?");
            params.add(paymentModeId);
        }
        
        if (fromDate != null) {
            conditions.add("d.donation_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("d.donation_date <= ?");
            params.add(toDate);
        }
        
        if (donorName != null && !donorName.trim().isEmpty()) {
            conditions.add("d.donor_name LIKE ?");
            params.add("%" + donorName.trim() + "%");
        }
        
        if (panNumber != null && !panNumber.trim().isEmpty()) {
            conditions.add("d.pan_number = ?");
            params.add(panNumber.trim().toUpperCase());
        }
        
        if (receiptNumber != null && !receiptNumber.trim().isEmpty()) {
            conditions.add("d.receipt_number = ?");
            params.add(receiptNumber.trim());
        }
        
        if (!includeInactive) {
            conditions.add("d.is_active = TRUE");
        }
        
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }
        
        // Add ORDER BY
        String sortField = switch (sortBy != null ? sortBy.toLowerCase() : "donationdate") {
            case "amount" -> "d.amount";
            case "createdat" -> "d.created_at";
            default -> "d.donation_date";
        };
        
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
        
        return query.query((rs, rowNum) -> mapRowToDonationDTO(rs)).list();
    }
    
    public long count(Long branchId, Long purposeId, Long eventId, Long paymentModeId,
                      LocalDate fromDate, LocalDate toDate, String donorName,
                      String panNumber, String receiptNumber, boolean includeInactive) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM donations d
            """);
        
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        
        // Build WHERE conditions (same as findAll)
        if (branchId != null) {
            conditions.add("d.branch_id = ?");
            params.add(branchId);
        }
        
        if (purposeId != null) {
            conditions.add("d.purpose_id = ?");
            params.add(purposeId);
        }
        
        if (eventId != null) {
            conditions.add("d.event_id = ?");
            params.add(eventId);
        }
        
        if (paymentModeId != null) {
            conditions.add("d.payment_mode_id = ?");
            params.add(paymentModeId);
        }
        
        if (fromDate != null) {
            conditions.add("d.donation_date >= ?");
            params.add(fromDate);
        }
        
        if (toDate != null) {
            conditions.add("d.donation_date <= ?");
            params.add(toDate);
        }
        
        if (donorName != null && !donorName.trim().isEmpty()) {
            conditions.add("d.donor_name LIKE ?");
            params.add("%" + donorName.trim() + "%");
        }
        
        if (panNumber != null && !panNumber.trim().isEmpty()) {
            conditions.add("d.pan_number = ?");
            params.add(panNumber.trim().toUpperCase());
        }
        
        if (receiptNumber != null && !receiptNumber.trim().isEmpty()) {
            conditions.add("d.receipt_number = ?");
            params.add(receiptNumber.trim());
        }
        
        if (!includeInactive) {
            conditions.add("d.is_active = TRUE");
        }
        
        if (!conditions.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", conditions));
        }
        
        JdbcClient.StatementSpec query = jdbcClient.sql(sql.toString());
        for (Object param : params) {
            query = query.param(param);
        }
        
        return query.query(Long.class).single();
    }

    public List<DonorDropdownDTO> findAllActiveDonorNames() {
        String sql = """
            SELECT MIN(d.id) AS id, d.donor_name AS name
            FROM donations d
            WHERE d.is_active = TRUE
            GROUP BY d.donor_name
            ORDER BY d.donor_name ASC
            """;

        return jdbcClient.sql(sql)
                .query((rs, rowNum) -> new DonorDropdownDTO(
                        rs.getLong("id"),
                        rs.getString("name")
                ))
                .list();
    }
    
    private DonationDTO mapRowToDonationDTO(ResultSet rs) throws SQLException {
        DonationDTO donation = new DonationDTO();
        donation.setId(rs.getLong("id"));
        donation.setReceiptNumber(rs.getString("receiptNumber"));
        donation.setDonorName(rs.getString("donorName"));
        donation.setDonorAddress(rs.getString("donorAddress"));
        donation.setPanNumber(rs.getString("panNumber"));
        donation.setDonorPhone(rs.getString("donorPhone"));
        donation.setDonorEmail(rs.getString("donorEmail"));
        donation.setAmount(rs.getBigDecimal("amount"));
        donation.setDonationDate(rs.getDate("donationDate").toLocalDate());
        donation.setNotes(rs.getString("notes"));
        donation.setReceiptGenerated(rs.getBoolean("receiptGenerated"));
        donation.setReceiptGeneratedAt(rs.getTimestamp("receiptGeneratedAt") != null 
            ? rs.getTimestamp("receiptGeneratedAt").toLocalDateTime() : null);
        donation.setReceiptFilePath(rs.getString("receiptFilePath"));
        donation.setIsActive(rs.getBoolean("isActive"));
        donation.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        donation.setUpdatedAt(rs.getTimestamp("updatedAt") != null 
            ? rs.getTimestamp("updatedAt").toLocalDateTime() : null);
        
        // Payment Mode
        com.trustapp.dto.PaymentModeDTO paymentMode = new com.trustapp.dto.PaymentModeDTO();
        paymentMode.setId(rs.getLong("paymentModeId"));
        paymentMode.setCode(rs.getString("paymentModeCode"));
        paymentMode.setName(rs.getString("paymentModeName"));
        paymentMode.setDescription(rs.getString("paymentModeDescription"));
        donation.setPaymentMode(paymentMode);
        
        // Purpose
        com.trustapp.dto.DonationPurposeDTO purpose = new com.trustapp.dto.DonationPurposeDTO();
        purpose.setId(rs.getLong("purposeId"));
        purpose.setCode(rs.getString("purposeCode"));
        purpose.setName(rs.getString("purposeName"));
        purpose.setDescription(rs.getString("purposeDescription"));
        donation.setPurpose(purpose);
        
        // Sub Category
        Long subCategoryId = rs.getObject("subCategoryId", Long.class);
        if (subCategoryId != null) {
            com.trustapp.dto.DonationSubCategoryDTO subCategory = new com.trustapp.dto.DonationSubCategoryDTO();
            subCategory.setId(subCategoryId);
            subCategory.setCode(rs.getString("subCategoryCode"));
            subCategory.setName(rs.getString("subCategoryName"));
            subCategory.setDescription(rs.getString("subCategoryDescription"));
            donation.setSubCategory(subCategory);
        }
        
        // Event
        Long eventId = rs.getObject("eventId", Long.class);
        if (eventId != null) {
            com.trustapp.dto.EventDTO event = new com.trustapp.dto.EventDTO();
            event.setId(eventId);
            event.setCode(rs.getString("eventCode"));
            event.setName(rs.getString("eventName"));
            event.setDescription(rs.getString("eventDescription"));
            if (rs.getDate("eventStartDate") != null) {
                event.setStartDate(rs.getDate("eventStartDate").toLocalDate());
            }
            if (rs.getDate("eventEndDate") != null) {
                event.setEndDate(rs.getDate("eventEndDate").toLocalDate());
            }
            event.setStatus(rs.getString("eventStatus"));
            donation.setEvent(event);
        }
        
        // Branch
        com.trustapp.dto.BranchDTO branch = new com.trustapp.dto.BranchDTO();
        branch.setId(rs.getLong("branchId"));
        branch.setCode(rs.getString("branchCode"));
        branch.setName(rs.getString("branchName"));
        branch.setAddress(rs.getString("branchAddress"));
        branch.setCity(rs.getString("branchCity"));
        branch.setState(rs.getString("branchState"));
        donation.setBranch(branch);
        
        return donation;
    }
}

