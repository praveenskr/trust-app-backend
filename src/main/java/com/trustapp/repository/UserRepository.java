package com.trustapp.repository;

import com.trustapp.dto.UserDTO;
import com.trustapp.dto.UserDropdownDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    
    private final JdbcClient jdbcClient;
    
    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<UserDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, username, email, full_name AS fullName, phone, 
                   is_active AS isActive, is_locked AS isLocked,
                   last_login_at AS lastLoginAt, created_at AS createdAt, 
                   updated_at AS updatedAt
            FROM users
            """ + (includeInactive ? "" : "WHERE is_active = TRUE ") + """
            ORDER BY full_name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(UserDTO.class)
            .list();
    }
    
    public Optional<UserDTO> findById(Long id) {
        String sql = """
            SELECT id, username, email, full_name AS fullName, phone, 
                   is_active AS isActive, is_locked AS isLocked,
                   last_login_at AS lastLoginAt, created_at AS createdAt, 
                   updated_at AS updatedAt
            FROM users
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(UserDTO.class)
            .optional();
    }
    
    public Optional<UserDTO> findByUsername(String username) {
        String sql = """
            SELECT id, username, email, full_name AS fullName, phone, 
                   is_active AS isActive, is_locked AS isLocked, 
                   last_login_at AS lastLoginAt, created_at AS createdAt, 
                   updated_at AS updatedAt
            FROM users
            WHERE username = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(username)
            .query(UserDTO.class)
            .optional();
    }
    
    public Optional<String> findPasswordHashByUsername(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        return jdbcClient.sql(sql)
            .param(username)
            .query(String.class)
            .optional();
    }
    
    public Optional<UserDTO> findByEmail(String email) {
        String sql = """
            SELECT id, username, email, full_name AS fullName, phone, 
                   is_active AS isActive, is_locked AS isLocked,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM users
            WHERE email = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(email)
            .query(UserDTO.class)
            .optional();
    }
    
    public Optional<com.trustapp.model.User> findByEmailForAuthentication(String email) {
        String sql = """
            SELECT id, username, email, password_hash AS password, full_name AS fullName, 
                   phone, is_active AS isActive, is_locked AS isLocked
            FROM users
            WHERE email = ? AND is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(email)
            .query((rs, rowNum) -> {
                return com.trustapp.model.User.builder()
                    .id(rs.getLong("id"))
                    .username(rs.getString("username"))
                    .email(rs.getString("email"))
                    .password(rs.getString("password"))
                    .fullName(rs.getString("fullName"))
                    .phone(rs.getString("phone"))
                    .isActive(rs.getBoolean("isActive"))
                    .isLocked(rs.getBoolean("isLocked"))
                    .build();
            })
            .optional();
    }
    
    public Long save(UserDTO user, String passwordHash, Long createdBy) {
        String sql = """
            INSERT INTO users 
            (username, email, password_hash, full_name, phone, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(user.getUsername())
            .param(user.getEmail())
            .param(passwordHash)
            .param(user.getFullName())
            .param(user.getPhone())
            .param(user.getIsActive() != null ? user.getIsActive() : true)
            .param(createdBy)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(UserDTO user, Long updatedBy) {
        String sql = """
            UPDATE users
            SET email = ?, full_name = ?, phone = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(user.getEmail())
            .param(user.getFullName())
            .param(user.getPhone())
            .param(user.getIsActive())
            .param(updatedBy)
            .param(user.getId())
            .update();
    }
    
    public int updatePassword(Long userId, String passwordHash) {
        String sql = """
            UPDATE users
            SET password_hash = ?, password_changed_at = CURRENT_TIMESTAMP,
                failed_login_attempts = 0, is_locked = FALSE
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(passwordHash)
            .param(userId)
            .update();
    }
    
    public int incrementFailedLoginAttempts(Long userId) {
        String sql = """
            UPDATE users
            SET failed_login_attempts = failed_login_attempts + 1,
                is_locked = CASE 
                    WHEN failed_login_attempts + 1 >= 5 THEN TRUE 
                    ELSE is_locked 
                END
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .update();
    }
    
    public int resetFailedLoginAttempts(Long userId) {
        String sql = """
            UPDATE users
            SET failed_login_attempts = 0, last_login_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .update();
    }
    
    public boolean existsByUsername(String username, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM users
            WHERE username = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(username)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public boolean existsByEmail(String email, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM users
            WHERE email = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(email)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcClient.sql(sql)
            .param(id)
            .update();
    }
    
    public List<UserDropdownDTO> findAllActiveForDropdown() {
        String sql = """
            SELECT id, username, email, full_name AS fullName
            FROM users
            WHERE is_active = TRUE
            ORDER BY full_name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(UserDropdownDTO.class)
            .list();
    }
}

