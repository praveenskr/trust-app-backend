package com.trustapp.repository;

import com.trustapp.dto.SerialNumberConfigDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class SerialNumberConfigRepository {
    
    private final JdbcClient jdbcClient;
    
    public SerialNumberConfigRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<SerialNumberConfigDTO> findAll() {
        String sql = """
            SELECT id, entity_type AS entityType, prefix, format_pattern AS formatPattern,
                   current_year AS currentYear, last_sequence AS lastSequence,
                   sequence_length AS sequenceLength, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM serial_number_config
            ORDER BY entity_type ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(SerialNumberConfigDTO.class)
            .list();
    }
    
    public Optional<SerialNumberConfigDTO> findById(Long id) {
        String sql = """
            SELECT id, entity_type AS entityType, prefix, format_pattern AS formatPattern,
                   current_year AS currentYear, last_sequence AS lastSequence,
                   sequence_length AS sequenceLength, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM serial_number_config
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(SerialNumberConfigDTO.class)
            .optional();
    }
    
    public Optional<SerialNumberConfigDTO> findByEntityType(String entityType) {
        String sql = """
            SELECT id, entity_type AS entityType, prefix, format_pattern AS formatPattern,
                   current_year AS currentYear, last_sequence AS lastSequence,
                   sequence_length AS sequenceLength, created_at AS createdAt,
                   updated_at AS updatedAt
            FROM serial_number_config
            WHERE entity_type = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(entityType)
            .query(SerialNumberConfigDTO.class)
            .optional();
    }
    
    public boolean existsByEntityType(String entityType) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM serial_number_config
            WHERE entity_type = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(entityType)
            .query(Boolean.class)
            .single();
    }
    
    public Long save(SerialNumberConfigDTO config) {
        String sql = """
            INSERT INTO serial_number_config 
            (entity_type, prefix, format_pattern, current_year, last_sequence, sequence_length)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(config.getEntityType())
            .param(config.getPrefix())
            .param(config.getFormatPattern() != null ? config.getFormatPattern() : "{PREFIX}-{YEAR}-{SEQUENCE}")
            .param(config.getCurrentYear())
            .param(config.getLastSequence() != null ? config.getLastSequence() : 0)
            .param(config.getSequenceLength() != null ? config.getSequenceLength() : 4)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(Long id, SerialNumberConfigDTO config) {
        String sql = """
            UPDATE serial_number_config
            SET prefix = ?, format_pattern = ?, sequence_length = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(config.getPrefix())
            .param(config.getFormatPattern())
            .param(config.getSequenceLength())
            .param(id)
            .update();
    }
    
    @Transactional
    public String getNextSerialNumber(String entityType) {
        int currentYear = java.time.LocalDate.now().getYear();
        
        // Lock row for update
        String selectSql = """
            SELECT id, entity_type AS entityType, prefix, format_pattern AS formatPattern,
                   current_year AS currentYear, last_sequence AS lastSequence,
                   sequence_length AS sequenceLength
            FROM serial_number_config
            WHERE entity_type = ?
            FOR UPDATE
            """;
        
        Optional<SerialNumberConfigDTO> configOpt = jdbcClient.sql(selectSql)
            .param(entityType)
            .query(SerialNumberConfigDTO.class)
            .optional();
        
        SerialNumberConfigDTO config;
        if (configOpt.isEmpty()) {
            throw new IllegalStateException("Serial number config not found for entity: " + entityType);
        }
        
        config = configOpt.get();
        
        // Reset sequence if year changed
        if (config.getCurrentYear() != currentYear) {
            String updateYearSql = """
                UPDATE serial_number_config
                SET current_year = ?, last_sequence = 0,
                    updated_at = CURRENT_TIMESTAMP
                WHERE entity_type = ?
                """;
            jdbcClient.sql(updateYearSql)
                .param(currentYear)
                .param(entityType)
                .update();
            config.setCurrentYear(currentYear);
            config.setLastSequence(0);
        }
        
        // Increment sequence
        int newSequence = config.getLastSequence() + 1;
        String updateSeqSql = """
            UPDATE serial_number_config
            SET last_sequence = ?, updated_at = CURRENT_TIMESTAMP
            WHERE entity_type = ?
            """;
        jdbcClient.sql(updateSeqSql)
            .param(newSequence)
            .param(entityType)
            .update();
        
        // Generate serial number
        String sequence = String.format("%0" + config.getSequenceLength() + "d", newSequence);
        return config.getFormatPattern()
            .replace("{PREFIX}", config.getPrefix())
            .replace("{YEAR}", String.valueOf(currentYear))
            .replace("{SEQUENCE}", sequence);
    }
}

