package com.trustapp.repository;

import com.trustapp.dto.VendorDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VendorRepository {
    
    private final JdbcClient jdbcClient;
    
    public VendorRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<VendorDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, contact_person AS contactPerson, phone, email, address,
                   city, state, pincode, gst_number AS gstNumber, pan_number AS panNumber,
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM vendors
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(VendorDTO.class)
            .list();
    }
    
    public Optional<VendorDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, contact_person AS contactPerson, phone, email, address,
                   city, state, pincode, gst_number AS gstNumber, pan_number AS panNumber,
                   is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt
            FROM vendors
            WHERE id = ? AND is_active = true
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(VendorDTO.class)
            .optional();
    }
    
    public Long save(VendorDTO vendor, Long userId) {
        String sql = """
            INSERT INTO vendors 
            (code, name, contact_person, phone, email, address, city, state, pincode,
             gst_number, pan_number, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(vendor.getCode())
            .param(vendor.getName())
            .param(vendor.getContactPerson())
            .param(vendor.getPhone())
            .param(vendor.getEmail())
            .param(vendor.getAddress())
            .param(vendor.getCity())
            .param(vendor.getState())
            .param(vendor.getPincode())
            .param(vendor.getGstNumber())
            .param(vendor.getPanNumber())
            .param(vendor.getIsActive() != null ? vendor.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM vendors
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int update(Long id, VendorDTO vendor, Long userId) {
        String sql = """
            UPDATE vendors
            SET code = ?, name = ?, contact_person = ?, phone = ?, email = ?,
                address = ?, city = ?, state = ?, pincode = ?,
                gst_number = ?, pan_number = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(vendor.getCode())
            .param(vendor.getName())
            .param(vendor.getContactPerson())
            .param(vendor.getPhone())
            .param(vendor.getEmail())
            .param(vendor.getAddress())
            .param(vendor.getCity())
            .param(vendor.getState())
            .param(vendor.getPincode())
            .param(vendor.getGstNumber())
            .param(vendor.getPanNumber())
            .param(vendor.getIsActive())
            .param(userId)
            .param(id)
            .update();
    }
    
    public int delete(Long id, Long userId) {
        // Soft delete: Set is_active = false
        String sql = """
            UPDATE vendors
            SET is_active = FALSE, updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(id)
            .update();
    }
}

