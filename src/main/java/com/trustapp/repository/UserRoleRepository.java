package com.trustapp.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleRepository {
    
    private final JdbcClient jdbcClient;
    
    public UserRoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<Long> findRoleIdsByUserId(Long userId) {
        String sql = "SELECT role_id FROM user_roles WHERE user_id = ?";
        return jdbcClient.sql(sql)
            .param(userId)
            .query(Long.class)
            .list();
    }
    
    public void assignRoles(Long userId, List<Long> roleIds, Long assignedBy) {
        // Delete existing roles
        String deleteSql = "DELETE FROM user_roles WHERE user_id = ?";
        jdbcClient.sql(deleteSql).param(userId).update();
        
        // Insert new roles
        if (roleIds != null && !roleIds.isEmpty()) {
            String insertSql = """
                INSERT INTO user_roles (user_id, role_id, assigned_by)
                VALUES (?, ?, ?)
                """;
            for (Long roleId : roleIds) {
                jdbcClient.sql(insertSql)
                    .param(userId)
                    .param(roleId)
                    .param(assignedBy)
                    .update();
            }
        }
    }
    
    public void removeAllRoles(Long userId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ?";
        jdbcClient.sql(sql).param(userId).update();
    }
}

