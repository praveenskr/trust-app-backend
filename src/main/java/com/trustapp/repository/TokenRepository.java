package com.trustapp.repository;

import com.trustapp.model.Token;
import com.trustapp.model.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TokenRepository {
    
    private final JdbcClient jdbcClient;
    
    public TokenRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public Token saveUserToken(Token token) {
        String sql = """
            INSERT INTO tokens (token, token_type, revoked, expired, expires_at, user_id)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(token.getToken())
            .param(token.getType().name())
            .param(token.getRevoked() != null ? token.getRevoked() : false)
            .param(token.getExpired() != null ? token.getExpired() : false)
            .param(token.getExpiresAt())
            .param(token.getUser().getId())
            .update(keyHolder);
        
        token.setId(keyHolder.getKey().longValue());
        return token;
    }
    
    public void updateStatus(Token token) {
        String sql = """
            UPDATE tokens
            SET expired = ?, revoked = ?
            WHERE token = ?
            """;
        
        jdbcClient.sql(sql)
            .param(token.getExpired())
            .param(token.getRevoked())
            .param(token.getToken())
            .update();
    }
    
    public void markTokenAsExpired(String jwt) {
        String sql = """
            UPDATE tokens
            SET expired = TRUE
            WHERE token = ?
            """;
        
        jdbcClient.sql(sql)
            .param(jwt)
            .update();
    }
    
    public List<Token> findAllValidJwtTokensByUser(Long userId) {
        String sql = """
            SELECT t.id, t.token, t.token_type, t.revoked, t.expired, t.expires_at, t.user_id,
                   u.id, u.username, u.email, u.password_hash, 
                   u.full_name, u.phone, u.is_active, u.is_locked
            FROM tokens t
            INNER JOIN users u ON t.user_id = u.id
            WHERE u.id = ? AND t.token_type = 'BEARER' 
            AND t.expired = FALSE AND t.revoked = FALSE
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .query((rs, rowNum) -> {
                Token token = new Token();
                token.setId(rs.getLong("t.id"));
                token.setToken(rs.getString("t.token"));
                token.setType(Token.TokenType.valueOf(rs.getString("t.token_type")));
                token.setRevoked(rs.getBoolean("t.revoked"));
                token.setExpired(rs.getBoolean("t.expired"));
                token.setExpiresAt(rs.getTimestamp("t.expires_at").toLocalDateTime());
                
                User user = User.builder()
                    .id(rs.getLong("u.id"))
                    .username(rs.getString("u.username"))
                    .email(rs.getString("u.email"))
                    .password(rs.getString("u.password_hash"))
                    .fullName(rs.getString("u.full_name"))
                    .phone(rs.getString("u.phone"))
                    .isActive(rs.getBoolean("u.is_active"))
                    .isLocked(rs.getBoolean("u.is_locked"))
                    .build();
                token.setUser(user);
                return token;
            })
            .list();
    }
    
    public void revokeAllUserTokens(List<Token> tokens) {
        for (Token token : tokens) {
            String sql = """
                UPDATE tokens
                SET revoked = ?, expired = ?
                WHERE id = ?
                """;
            jdbcClient.sql(sql)
                .param(true)
                .param(true)
                .param(token.getId())
                .update();
        }
    }
    
    public Optional<Token> findJwtToken(String jwt) {
        String sql = """
            SELECT t.id, t.token, t.token_type, t.revoked, t.expired, t.expires_at, t.user_id,
                   u.id, u.username, u.email, u.password_hash, 
                   u.full_name, u.phone, u.is_active, u.is_locked
            FROM tokens t
            JOIN users u ON t.user_id = u.id
            WHERE t.token = ? AND t.token_type = 'BEARER'
            """;
        
        return jdbcClient.sql(sql)
            .param(jwt)
            .query((rs, rowNum) -> {
                Token token = new Token();
                token.setId(rs.getLong("t.id"));
                token.setToken(rs.getString("t.token"));
                token.setType(Token.TokenType.valueOf(rs.getString("t.token_type")));
                token.setRevoked(rs.getBoolean("t.revoked"));
                token.setExpired(rs.getBoolean("t.expired"));
                token.setExpiresAt(rs.getTimestamp("t.expires_at").toLocalDateTime());
                
                User user = User.builder()
                    .id(rs.getLong("u.id"))
                    .username(rs.getString("u.username"))
                    .email(rs.getString("u.email"))
                    .password(rs.getString("u.password_hash"))
                    .fullName(rs.getString("u.full_name"))
                    .phone(rs.getString("u.phone"))
                    .isActive(rs.getBoolean("u.is_active"))
                    .isLocked(rs.getBoolean("u.is_locked"))
                    .build();
                token.setUser(user);
                return token;
            })
            .optional();
    }
    
    public Optional<Token> findRefreshToken(String refreshToken) {
        String sql = """
            SELECT t.id, t.token, t.token_type, t.revoked, t.expired, t.expires_at, t.user_id,
                   u.id, u.username, u.email, u.password_hash, 
                   u.full_name, u.phone, u.is_active, u.is_locked
            FROM tokens t
            JOIN users u ON t.user_id = u.id
            WHERE t.token = ? AND t.token_type = 'REFRESH'
            """;
        
        return jdbcClient.sql(sql)
            .param(refreshToken)
            .query((rs, rowNum) -> {
                Token token = new Token();
                token.setId(rs.getLong("t.id"));
                token.setToken(rs.getString("t.token"));
                token.setType(Token.TokenType.valueOf(rs.getString("t.token_type")));
                token.setRevoked(rs.getBoolean("t.revoked"));
                token.setExpired(rs.getBoolean("t.expired"));
                token.setExpiresAt(rs.getTimestamp("t.expires_at").toLocalDateTime());
                
                User user = User.builder()
                    .id(rs.getLong("u.id"))
                    .username(rs.getString("u.username"))
                    .email(rs.getString("u.email"))
                    .password(rs.getString("u.password_hash"))
                    .fullName(rs.getString("u.full_name"))
                    .phone(rs.getString("u.phone"))
                    .isActive(rs.getBoolean("u.is_active"))
                    .isLocked(rs.getBoolean("u.is_locked"))
                    .build();
                token.setUser(user);
                return token;
            })
            .optional();
    }
    
    public List<Token> findAllValidRefreshTokensByUser(Long userId) {
        String sql = """
            SELECT t.id, t.token, t.token_type, t.revoked, t.expired, t.expires_at, t.user_id,
                   u.id, u.username, u.email, u.password_hash, 
                   u.full_name, u.phone, u.is_active, u.is_locked
            FROM tokens t
            INNER JOIN users u ON t.user_id = u.id
            WHERE u.id = ? AND t.token_type = 'REFRESH' 
            AND (t.expired = FALSE OR t.revoked = FALSE)
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .query((rs, rowNum) -> {
                Token token = new Token();
                token.setId(rs.getLong("t.id"));
                token.setToken(rs.getString("t.token"));
                token.setType(Token.TokenType.valueOf(rs.getString("t.token_type")));
                token.setRevoked(rs.getBoolean("t.revoked"));
                token.setExpired(rs.getBoolean("t.expired"));
                token.setExpiresAt(rs.getTimestamp("t.expires_at").toLocalDateTime());
                
                User user = User.builder()
                    .id(rs.getLong("u.id"))
                    .username(rs.getString("u.username"))
                    .email(rs.getString("u.email"))
                    .password(rs.getString("u.password_hash"))
                    .fullName(rs.getString("u.full_name"))
                    .phone(rs.getString("u.phone"))
                    .isActive(rs.getBoolean("u.is_active"))
                    .isLocked(rs.getBoolean("u.is_locked"))
                    .build();
                token.setUser(user);
                return token;
            })
            .list();
    }
}

