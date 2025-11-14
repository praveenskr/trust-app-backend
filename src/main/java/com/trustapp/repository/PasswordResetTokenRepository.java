package com.trustapp.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepository {
    
    private final JdbcClient jdbcClient;
    
    public PasswordResetTokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public void save(Long userId, String token, LocalDateTime expiresAt) {
        String sql = """
            INSERT INTO password_reset_tokens (user_id, token, expires_at)
            VALUES (?, ?, ?)
            """;
        
        jdbcClient.sql(sql)
            .param(userId)
            .param(token)
            .param(expiresAt)
            .update();
    }
    
    public Optional<Long> findUserIdByToken(String token) {
        String sql = """
            SELECT user_id
            FROM password_reset_tokens
            WHERE token = ? 
            AND expires_at > CURRENT_TIMESTAMP
            AND used_at IS NULL
            """;
        
        return jdbcClient.sql(sql)
            .param(token)
            .query(Long.class)
            .optional();
    }
    
    public void markTokenAsUsed(String token) {
        String sql = """
            UPDATE password_reset_tokens
            SET used_at = CURRENT_TIMESTAMP
            WHERE token = ?
            """;
        
        jdbcClient.sql(sql)
            .param(token)
            .update();
    }
    
    public void invalidateUserTokens(Long userId) {
        String sql = """
            UPDATE password_reset_tokens
            SET used_at = CURRENT_TIMESTAMP
            WHERE user_id = ? AND used_at IS NULL
            """;
        
        jdbcClient.sql(sql)
            .param(userId)
            .update();
    }
    
    public void deleteExpiredTokens() {
        String sql = """
            DELETE FROM password_reset_tokens
            WHERE expires_at < CURRENT_TIMESTAMP
            AND used_at IS NOT NULL
            """;
        
        jdbcClient.sql(sql).update();
    }
}

