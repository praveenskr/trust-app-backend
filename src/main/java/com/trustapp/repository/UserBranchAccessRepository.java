package com.trustapp.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserBranchAccessRepository {
    
    private final JdbcClient jdbcClient;
    
    public UserBranchAccessRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<Long> findBranchIdsByUserId(Long userId) {
        String sql = "SELECT branch_id FROM user_branch_access WHERE user_id = ?";
        return jdbcClient.sql(sql)
            .param(userId)
            .query(Long.class)
            .list();
    }
    
    public void assignBranches(Long userId, List<Long> branchIds, Long grantedBy) {
        // Delete existing branch access
        String deleteSql = "DELETE FROM user_branch_access WHERE user_id = ?";
        jdbcClient.sql(deleteSql).param(userId).update();
        
        // Insert new branch access
        if (branchIds != null && !branchIds.isEmpty()) {
            String insertSql = """
                INSERT INTO user_branch_access (user_id, branch_id, granted_by)
                VALUES (?, ?, ?)
                """;
            for (Long branchId : branchIds) {
                jdbcClient.sql(insertSql)
                    .param(userId)
                    .param(branchId)
                    .param(grantedBy)
                    .update();
            }
        }
    }
    
    public void removeAllBranches(Long userId) {
        String sql = "DELETE FROM user_branch_access WHERE user_id = ?";
        jdbcClient.sql(sql).param(userId).update();
    }
    
    public boolean hasAccessToBranch(Long userId, Long branchId) {
        String sql = """
            SELECT COUNT(*) AS count
            FROM user_branch_access
            WHERE user_id = ? AND branch_id = ?
            """;
        
        Long count = jdbcClient.sql(sql)
            .param(userId)
            .param(branchId)
            .query(Long.class)
            .single();
        
        return count != null && count > 0;
    }
}

