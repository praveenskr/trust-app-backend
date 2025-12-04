package com.trustapp.repository;

import com.trustapp.dto.BranchAccessDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserBranchAccessRepository {
    
    private final JdbcClient jdbcClient;
    
    public UserBranchAccessRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<BranchAccessDTO> findByUserId(Long userId) {
        String sql = """
            SELECT uba.id, 
                   u.id AS userInfoId, u.username, u.email, u.full_name AS fullName,
                   uba.branch_id AS branchId,
                   b.name AS branchName, b.code AS branchCode,
                   uba.granted_at AS grantedAt
            FROM user_branch_access uba
            INNER JOIN branches b ON uba.branch_id = b.id
            INNER JOIN users u ON uba.user_id = u.id
            WHERE uba.user_id = ? AND b.is_active = TRUE
            ORDER BY b.name ASC
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .query((rs, rowNum) -> {
                BranchAccessDTO dto = new BranchAccessDTO();
                dto.setId(rs.getLong("id"));
                
                // Create UserInfo object
                BranchAccessDTO.UserInfo userInfo = new BranchAccessDTO.UserInfo();
                userInfo.setId(rs.getLong("userInfoId"));
                userInfo.setUsername(rs.getString("username"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setFullName(rs.getString("fullName"));
                dto.setUser(userInfo);
                
                dto.setBranchId(rs.getLong("branchId"));
                dto.setBranchName(rs.getString("branchName"));
                dto.setBranchCode(rs.getString("branchCode"));
                dto.setGrantedAt(rs.getTimestamp("grantedAt") != null ? 
                    rs.getTimestamp("grantedAt").toLocalDateTime() : null);
                return dto;
            })
            .list();
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
    
    public BranchAccessDTO findAccessByUserIdAndBranchId(Long userId, Long branchId) {
        String sql = """
            SELECT uba.id,
                   u.id AS userInfoId, u.username, u.email, u.full_name AS fullName,
                   uba.branch_id AS branchId,
                   b.name AS branchName, b.code AS branchCode,
                   uba.granted_at AS grantedAt
            FROM user_branch_access uba
            INNER JOIN branches b ON uba.branch_id = b.id
            INNER JOIN users u ON uba.user_id = u.id
            WHERE uba.user_id = ? AND uba.branch_id = ? AND b.is_active = TRUE
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(branchId)
            .query((rs, rowNum) -> {
                BranchAccessDTO dto = new BranchAccessDTO();
                dto.setId(rs.getLong("id"));
                
                // Create UserInfo object
                BranchAccessDTO.UserInfo userInfo = new BranchAccessDTO.UserInfo();
                userInfo.setId(rs.getLong("userInfoId"));
                userInfo.setUsername(rs.getString("username"));
                userInfo.setEmail(rs.getString("email"));
                userInfo.setFullName(rs.getString("fullName"));
                dto.setUser(userInfo);
                
                dto.setBranchId(rs.getLong("branchId"));
                dto.setBranchName(rs.getString("branchName"));
                dto.setBranchCode(rs.getString("branchCode"));
                dto.setGrantedAt(rs.getTimestamp("grantedAt") != null ? 
                    rs.getTimestamp("grantedAt").toLocalDateTime() : null);
                return dto;
            })
            .optional()
            .orElse(null);
    }
}

