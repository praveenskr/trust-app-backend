# User Management Module - Technical Implementation Plan

## Overview

The User Management module handles user accounts, roles, permissions, and branch access control for the Trust Management System. This module implements Role-Based Access Control (RBAC) with three primary roles: Super User, Manager, and Accountant. Super users have access to all branches, while Managers and Accountants have branch-specific access.

---

## Table of Contents

1. [Database Schema](#database-schema)
2. [Entity Classes (DTOs)](#entity-classes-dtos)
3. [Repository Layer (JDBC)](#repository-layer-jdbc)
4. [Service Layer](#service-layer)
5. [Controller Layer (REST APIs)](#controller-layer-rest-apis)
6. [API Contract Details](#api-contract-details)
7. [Authentication & Authorization](#authentication--authorization)
8. [Validation & Error Handling](#validation--error-handling)
9. [Testing Strategy](#testing-strategy)
10. [Implementation Steps](#implementation-steps)

---

## Database Schema

### 1. Users Table

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_active (is_active),
    INDEX idx_locked (is_locked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Roles Table

```sql
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. Permissions Table

```sql
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    module VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_module (module),
    INDEX idx_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. User Roles Table

```sql
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. Role Permissions Table

```sql
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. User Branch Access Table

```sql
CREATE TABLE user_branch_access (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_branch (user_id, branch_id),
    INDEX idx_user_id (user_id),
    INDEX idx_branch_id (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 7. Password Reset Tokens Table

```sql
CREATE TABLE password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Entity Classes (DTOs)

### UserDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class UserDTO {
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    private Boolean isActive;
    private Boolean isLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relationships
    private List<RoleDTO> roles;
    private List<Long> roleIds;
    private List<BranchAccessDTO> branchAccess;
    private List<Long> branchIds;
    
    // Getters and Setters
}
```

### UserCreateDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserCreateDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String phone;
    
    @NotNull(message = "At least one role must be assigned")
    private List<Long> roleIds;
    
    private List<Long> branchIds; // For Manager and Accountant roles
    
    // Getters and Setters
}
```

### UserUpdateDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class UserUpdateDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private Boolean isActive;
    
    private List<Long> roleIds;
    
    private List<Long> branchIds;
    
    // Getters and Setters
}
```

### RoleDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class RoleDTO {
    private Long id;
    
    @NotBlank(message = "Role code is required")
    @Size(max = 50, message = "Role code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name must not exceed 100 characters")
    private String name;
    
    private String description;
    private Boolean isSystemRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<PermissionDTO> permissions;
    
    // Getters and Setters
}
```

### PermissionDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class PermissionDTO {
    private Long id;
    
    @NotBlank(message = "Permission code is required")
    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    private String code;
    
    @NotBlank(message = "Permission name is required")
    @Size(max = 255, message = "Permission name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Module is required")
    private String module;
    
    @NotBlank(message = "Resource is required")
    private String resource;
    
    @NotBlank(message = "Action is required")
    private String action;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}
```

### BranchAccessDTO

```java
package com.trustapp.usermanagement.dto;

import java.time.LocalDateTime;

public class BranchAccessDTO {
    private Long id;
    private Long userId;
    private Long branchId;
    private String branchName;
    private String branchCode;
    private LocalDateTime grantedAt;
    
    // Getters and Setters
}
```

### PasswordChangeDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeDTO {
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    // Getters and Setters
}
```

### PasswordResetRequestDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    // Getters and Setters
}
```

### PasswordResetDTO

```java
package com.trustapp.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDTO {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
    
    // Getters and Setters
}
```

---

## Repository Layer (JDBC)

### UserRepository

```java
package com.trustapp.usermanagement.repository;

import com.trustapp.usermanagement.dto.UserDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
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
            SELECT id, username, email, full_name, phone, is_active, is_locked,
                   last_login_at, created_at, updated_at
            FROM users
            """ + (includeInactive ? "" : "WHERE is_active = TRUE") + """
            ORDER BY full_name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(UserDTO.class)
            .list();
    }
    
    public Optional<UserDTO> findById(Long id) {
        String sql = """
            SELECT id, username, email, full_name, phone, is_active, is_locked,
                   last_login_at, created_at, updated_at
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
            SELECT id, username, email, password_hash, full_name, phone, 
                   is_active, is_locked, failed_login_attempts, last_login_at,
                   created_at, updated_at
            FROM users
            WHERE username = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(username)
            .query(UserDTO.class)
            .optional();
    }
    
    public Optional<UserDTO> findByEmail(String email) {
        String sql = """
            SELECT id, username, email, full_name, phone, is_active, is_locked,
                   created_at, updated_at
            FROM users
            WHERE email = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(email)
            .query(UserDTO.class)
            .optional();
    }
    
    public Long save(UserDTO user, String passwordHash, Long createdBy) {
        String sql = """
            INSERT INTO users 
            (username, email, password_hash, full_name, phone, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
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
}
```

### UserRoleRepository

```java
package com.trustapp.usermanagement.repository;

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
```

### UserBranchAccessRepository

```java
package com.trustapp.usermanagement.repository;

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
            SELECT COUNT(*) > 0
            FROM user_branch_access
            WHERE user_id = ? AND branch_id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .param(branchId)
            .query(Boolean.class)
            .single();
    }
}
```

### RoleRepository

```java
package com.trustapp.usermanagement.repository;

import com.trustapp.usermanagement.dto.RoleDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepository {
    
    private final JdbcClient jdbcClient;
    
    public RoleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<RoleDTO> findAll() {
        String sql = """
            SELECT id, code, name, description, is_system_role,
                   created_at, updated_at
            FROM roles
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(RoleDTO.class)
            .list();
    }
    
    public Optional<RoleDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, is_system_role,
                   created_at, updated_at
            FROM roles
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(RoleDTO.class)
            .optional();
    }
    
    public Optional<RoleDTO> findByCode(String code) {
        String sql = """
            SELECT id, code, name, description, is_system_role,
                   created_at, updated_at
            FROM roles
            WHERE code = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .query(RoleDTO.class)
            .optional();
    }
}
```

### PermissionRepository

```java
package com.trustapp.usermanagement.repository;

import com.trustapp.usermanagement.dto.PermissionDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PermissionRepository {
    
    private final JdbcClient jdbcClient;
    
    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<PermissionDTO> findAll() {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at, updated_at
            FROM permissions
            ORDER BY module, resource, action
            """;
        
        return jdbcClient.sql(sql)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByModule(String module) {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at, updated_at
            FROM permissions
            WHERE module = ?
            ORDER BY resource, action
            """;
        
        return jdbcClient.sql(sql)
            .param(module)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByRoleId(Long roleId) {
        String sql = """
            SELECT p.id, p.code, p.name, p.description, p.module, p.resource, p.action,
                   p.created_at, p.updated_at
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            WHERE rp.role_id = ?
            ORDER BY p.module, p.resource, p.action
            """;
        
        return jdbcClient.sql(sql)
            .param(roleId)
            .query(PermissionDTO.class)
            .list();
    }
    
    public List<PermissionDTO> findByUserId(Long userId) {
        String sql = """
            SELECT DISTINCT p.id, p.code, p.name, p.description, p.module, p.resource, p.action,
                   p.created_at, p.updated_at
            FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN user_roles ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = ?
            ORDER BY p.module, p.resource, p.action
            """;
        
        return jdbcClient.sql(sql)
            .param(userId)
            .query(PermissionDTO.class)
            .list();
    }
    
    public Optional<PermissionDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, module, resource, action,
                   created_at, updated_at
            FROM permissions
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(PermissionDTO.class)
            .optional();
    }
}
```

### PasswordResetTokenRepository

```java
package com.trustapp.usermanagement.repository;

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
```

---

## Service Layer

### UserService

```java
package com.trustapp.usermanagement.service;

import com.trustapp.usermanagement.dto.*;
import com.trustapp.usermanagement.repository.*;
import com.trustapp.usermanagement.exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            UserBranchAccessRepository userBranchAccessRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<UserDTO> getAllUsers(boolean includeInactive) {
        List<UserDTO> users = userRepository.findAll(includeInactive);
        // Enrich with roles and branch access
        return users.stream()
            .map(this::enrichUser)
            .collect(Collectors.toList());
    }
    
    public UserDTO getUserById(Long id) {
        UserDTO user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return enrichUser(user);
    }
    
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO, Long createdBy) {
        // Validate username uniqueness
        if (userRepository.existsByUsername(userCreateDTO.getUsername(), null)) {
            throw new DuplicateResourceException("Username already exists: " + userCreateDTO.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(userCreateDTO.getEmail(), null)) {
            throw new DuplicateResourceException("Email already exists: " + userCreateDTO.getEmail());
        }
        
        // Validate roles exist
        validateRoles(userCreateDTO.getRoleIds());
        
        // Create user
        UserDTO user = new UserDTO();
        user.setUsername(userCreateDTO.getUsername());
        user.setEmail(userCreateDTO.getEmail());
        user.setFullName(userCreateDTO.getFullName());
        user.setPhone(userCreateDTO.getPhone());
        user.setIsActive(true);
        
        String passwordHash = passwordEncoder.encode(userCreateDTO.getPassword());
        Long userId = userRepository.save(user, passwordHash, createdBy);
        
        // Assign roles
        userRoleRepository.assignRoles(userId, userCreateDTO.getRoleIds(), createdBy);
        
        // Assign branch access (if not super user)
        if (userCreateDTO.getBranchIds() != null && !userCreateDTO.getBranchIds().isEmpty()) {
            userBranchAccessRepository.assignBranches(userId, userCreateDTO.getBranchIds(), createdBy);
        }
        
        return getUserById(userId);
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO, Long updatedBy) {
        // Check if user exists
        UserDTO existingUser = getUserById(id);
        
        // Validate email uniqueness
        if (userUpdateDTO.getEmail() != null && 
            userRepository.existsByEmail(userUpdateDTO.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists: " + userUpdateDTO.getEmail());
        }
        
        // Update user
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setEmail(userUpdateDTO.getEmail() != null ? userUpdateDTO.getEmail() : existingUser.getEmail());
        user.setFullName(userUpdateDTO.getFullName() != null ? userUpdateDTO.getFullName() : existingUser.getFullName());
        user.setPhone(userUpdateDTO.getPhone());
        user.setIsActive(userUpdateDTO.getIsActive() != null ? userUpdateDTO.getIsActive() : existingUser.getIsActive());
        
        userRepository.update(user, updatedBy);
        
        // Update roles if provided
        if (userUpdateDTO.getRoleIds() != null) {
            validateRoles(userUpdateDTO.getRoleIds());
            userRoleRepository.assignRoles(id, userUpdateDTO.getRoleIds(), updatedBy);
        }
        
        // Update branch access if provided
        if (userUpdateDTO.getBranchIds() != null) {
            userBranchAccessRepository.assignBranches(id, userUpdateDTO.getBranchIds(), updatedBy);
        }
        
        return getUserById(id);
    }
    
    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO passwordChangeDTO) {
        UserDTO user = getUserById(userId);
        
        // Verify current password
        // Note: This requires fetching the password hash from database
        // Implementation depends on your authentication setup
        
        // Validate new password matches confirm password
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        
        String newPasswordHash = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        userRepository.updatePassword(userId, newPasswordHash);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        // Check if user exists
        getUserById(id);
        
        // Check if user is a system user (cannot be deleted)
        // Implementation depends on your business rules
        
        userRepository.delete(id);
    }
    
    @Transactional
    public void unlockUser(Long id, Long unlockedBy) {
        UserDTO user = getUserById(id);
        // Reset failed login attempts
        userRepository.resetFailedLoginAttempts(id);
    }
    
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(userId, passwordHash);
    }
    
    private UserDTO enrichUser(UserDTO user) {
        // Load roles
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(user.getId());
        // Load branch access
        List<Long> branchIds = userBranchAccessRepository.findBranchIdsByUserId(user.getId());
        
        user.setRoleIds(roleIds);
        user.setBranchIds(branchIds);
        
        return user;
    }
    
    private void validateRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ValidationException("At least one role must be assigned");
        }
        
        for (Long roleId : roleIds) {
            roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        }
    }
}
```

### UserBranchAccessService

```java
package com.trustapp.usermanagement.service;

import com.trustapp.usermanagement.dto.BranchAccessDTO;
import com.trustapp.usermanagement.repository.UserBranchAccessRepository;
import com.trustapp.usermanagement.repository.UserRepository;
import com.trustapp.usermanagement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserBranchAccessService {
    
    private final UserBranchAccessRepository userBranchAccessRepository;
    private final UserRepository userRepository;
    
    public UserBranchAccessService(
            UserBranchAccessRepository userBranchAccessRepository,
            UserRepository userRepository) {
        this.userBranchAccessRepository = userBranchAccessRepository;
        this.userRepository = userRepository;
    }
    
    public List<BranchAccessDTO> getUserBranchAccess(Long userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Long> branchIds = userBranchAccessRepository.findBranchIdsByUserId(userId);
        
        // Convert to BranchAccessDTO
        // Note: This would typically join with branches table to get branch details
        return branchIds.stream()
            .map(branchId -> {
                BranchAccessDTO dto = new BranchAccessDTO();
                dto.setUserId(userId);
                dto.setBranchId(branchId);
                // Branch name and code would be loaded from branch repository
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    @Transactional
    public List<BranchAccessDTO> assignBranches(Long userId, List<Long> branchIds, Long grantedBy) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate branches exist
        // Note: This would typically validate against branch repository
        
        // Assign branches (replaces existing)
        userBranchAccessRepository.assignBranches(userId, branchIds, grantedBy);
        
        return getUserBranchAccess(userId);
    }
    
    @Transactional
    public void removeAllBranches(Long userId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        userBranchAccessRepository.removeAllBranches(userId);
    }
    
    public boolean hasAccessToBranch(Long userId, Long branchId) {
        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if user is super user (has access to all branches)
        // Note: This would typically check user roles
        
        // Check branch access
        return userBranchAccessRepository.hasAccessToBranch(userId, branchId);
    }
}
```

### PasswordResetService

```java
package com.trustapp.usermanagement.service;

import com.trustapp.usermanagement.dto.PasswordResetDTO;
import com.trustapp.usermanagement.dto.PasswordResetRequestDTO;
import com.trustapp.usermanagement.repository.PasswordResetTokenRepository;
import com.trustapp.usermanagement.repository.UserRepository;
import com.trustapp.usermanagement.exception.ResourceNotFoundException;
import com.trustapp.usermanagement.exception.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final int TOKEN_LENGTH = 32;
    
    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        // Find user by email
        var user = userRepository.findByEmail(requestDTO.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + requestDTO.getEmail()));
        
        // Check if user is active
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new ValidationException("Cannot reset password for inactive user");
        }
        
        // Invalidate existing tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(user.getId());
        
        // Generate secure token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);
        
        // Save token
        passwordResetTokenRepository.save(user.getId(), token, expiresAt);
        
        // Send email with reset link
        // Note: This would typically use an email service
        // sendPasswordResetEmail(user.getEmail(), token);
    }
    
    @Transactional
    public void resetPassword(PasswordResetDTO resetDTO) {
        // Validate passwords match
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            throw new ValidationException("New password and confirm password do not match");
        }
        
        // Find user by token
        Long userId = passwordResetTokenRepository.findUserIdByToken(resetDTO.getToken())
            .orElseThrow(() -> new ValidationException("Invalid or expired password reset token"));
        
        // Verify user exists and is active
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new ValidationException("Cannot reset password for inactive user");
        }
        
        // Update password
        String passwordHash = passwordEncoder.encode(resetDTO.getNewPassword());
        userRepository.updatePassword(userId, passwordHash);
        
        // Mark token as used
        passwordResetTokenRepository.markTokenAsUsed(resetDTO.getToken());
        
        // Invalidate all other tokens for this user
        passwordResetTokenRepository.invalidateUserTokens(userId);
    }
    
    public boolean validateToken(String token) {
        return passwordResetTokenRepository.findUserIdByToken(token).isPresent();
    }
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
```

---

## Controller Layer (REST APIs)

### UserController

```java
package com.trustapp.usermanagement.controller;

import com.trustapp.usermanagement.dto.*;
import com.trustapp.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<UserDTO> users = userService.getAllUsers(includeInactive);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody UserCreateDTO userCreateDTO,
            @AuthenticationPrincipal Long userId) {
        UserDTO created = userService.createUser(userCreateDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO,
            @AuthenticationPrincipal Long userId) {
        UserDTO updated = userService.updateUser(id, userUpdateDTO, userId);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(id, passwordChangeDTO);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> unlockUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        userService.unlockUser(id, userId);
        return ResponseEntity.noContent().build();
    }
}
```

### RoleController

```java
package com.trustapp.usermanagement.controller;

import com.trustapp.usermanagement.dto.RoleDTO;
import com.trustapp.usermanagement.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }
}
```

### PermissionController

```java
package com.trustapp.usermanagement.controller;

import com.trustapp.usermanagement.dto.PermissionDTO;
import com.trustapp.usermanagement.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/permissions")
public class PermissionController {
    
    private final PermissionService permissionService;
    
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions(
            @RequestParam(required = false) String module) {
        List<PermissionDTO> permissions = module != null 
            ? permissionService.getPermissionsByModule(module)
            : permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<List<PermissionDTO>> getUserPermissions(@PathVariable Long userId) {
        List<PermissionDTO> permissions = permissionService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }
}
```

### UserBranchAccessController

```java
package com.trustapp.usermanagement.controller;

import com.trustapp.usermanagement.dto.BranchAccessDTO;
import com.trustapp.usermanagement.service.UserBranchAccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/branches")
public class UserBranchAccessController {
    
    private final UserBranchAccessService userBranchAccessService;
    
    public UserBranchAccessController(UserBranchAccessService userBranchAccessService) {
        this.userBranchAccessService = userBranchAccessService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<List<BranchAccessDTO>> getUserBranchAccess(@PathVariable Long userId) {
        List<BranchAccessDTO> branchAccess = userBranchAccessService.getUserBranchAccess(userId);
        return ResponseEntity.ok(branchAccess);
    }
    
    @PutMapping
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<List<BranchAccessDTO>> assignBranches(
            @PathVariable Long userId,
            @Valid @RequestBody List<Long> branchIds,
            @AuthenticationPrincipal Long grantedBy) {
        List<BranchAccessDTO> branchAccess = userBranchAccessService.assignBranches(userId, branchIds, grantedBy);
        return ResponseEntity.ok(branchAccess);
    }
    
    @DeleteMapping
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<Void> removeAllBranches(@PathVariable Long userId) {
        userBranchAccessService.removeAllBranches(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{branchId}/check")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<Boolean> checkBranchAccess(
            @PathVariable Long userId,
            @PathVariable Long branchId) {
        boolean hasAccess = userBranchAccessService.hasAccessToBranch(userId, branchId);
        return ResponseEntity.ok(hasAccess);
    }
}
```

### PasswordResetController

```java
package com.trustapp.usermanagement.controller;

import com.trustapp.usermanagement.dto.PasswordResetDTO;
import com.trustapp.usermanagement.dto.PasswordResetRequestDTO;
import com.trustapp.usermanagement.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO requestDTO) {
        passwordResetService.requestPasswordReset(requestDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    
    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        passwordResetService.resetPassword(resetDTO);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/validate/{token}")
    public ResponseEntity<Boolean> validateToken(@PathVariable String token) {
        boolean isValid = passwordResetService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}
```

---

## API Contract Details

### Users API

#### 1. Get All Users

**Endpoint:** `GET /api/users`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`) - Include inactive users in response

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@trustapp.com",
    "fullName": "Administrator",
    "phone": "+91-22-12345678",
    "isActive": true,
    "isLocked": false,
    "lastLoginAt": "2024-01-15T10:30:00",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-15T10:30:00",
    "roleIds": [1],
    "branchIds": []
  },
  {
    "id": 2,
    "username": "manager1",
    "email": "manager1@trustapp.com",
    "fullName": "Branch Manager",
    "phone": "+91-22-87654321",
    "isActive": true,
    "isLocked": false,
    "lastLoginAt": "2024-01-16T09:15:00",
    "createdAt": "2024-01-10T08:00:00",
    "updatedAt": "2024-01-16T09:15:00",
    "roleIds": [2],
    "branchIds": [1, 2]
  }
]
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

#### 2. Get User by ID

**Endpoint:** `GET /api/users/{id}`

**Path Parameters:**
- `id` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@trustapp.com",
  "fullName": "Administrator",
  "phone": "+91-22-12345678",
  "isActive": true,
  "isLocked": false,
  "lastLoginAt": "2024-01-15T10:30:00",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-15T10:30:00",
  "roleIds": [1],
  "branchIds": []
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

#### 3. Create User

**Endpoint:** `POST /api/users`

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "username": "accountant1",
  "email": "accountant1@trustapp.com",
  "password": "SecurePass123!",
  "fullName": "Accountant User",
  "phone": "+91-22-11111111",
  "roleIds": [3],
  "branchIds": [1]
}
```

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 3,
  "username": "accountant1",
  "email": "accountant1@trustapp.com",
  "fullName": "Accountant User",
  "phone": "+91-22-11111111",
  "isActive": true,
  "isLocked": false,
  "lastLoginAt": null,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T14:20:00",
  "roleIds": [3],
  "branchIds": [1]
}
```

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "username": "Username is required",
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters",
    "roleIds": "At least one role must be assigned"
  }
}
```

`409 Conflict` - Duplicate Username
```json
{
  "error": "Duplicate Resource",
  "message": "Username already exists: accountant1"
}
```

`409 Conflict` - Duplicate Email
```json
{
  "error": "Duplicate Resource",
  "message": "Email already exists: accountant1@trustapp.com"
}
```

#### 4. Update User

**Endpoint:** `PUT /api/users/{id}`

**Path Parameters:**
- `id` (required, Long) - User ID

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "email": "updated@trustapp.com",
  "fullName": "Updated Accountant User",
  "phone": "+91-22-22222222",
  "isActive": true,
  "roleIds": [3],
  "branchIds": [1, 2]
}
```

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 3,
  "username": "accountant1",
  "email": "updated@trustapp.com",
  "fullName": "Updated Accountant User",
  "phone": "+91-22-22222222",
  "isActive": true,
  "isLocked": false,
  "lastLoginAt": null,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T15:30:00",
  "roleIds": [3],
  "branchIds": [1, 2]
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

#### 5. Delete User

**Endpoint:** `DELETE /api/users/{id}`

**Path Parameters:**
- `id` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `204 No Content`

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

#### 6. Change Password

**Endpoint:** `POST /api/users/{id}/change-password`

**Path Parameters:**
- `id` (required, Long) - User ID

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Response:** `204 No Content`

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "currentPassword": "Current password is required",
    "newPassword": "New password must be at least 8 characters",
    "confirmPassword": "Confirm password is required"
  }
}
```

`400 Bad Request` - Password Mismatch
```json
{
  "error": "Validation Failed",
  "message": "New password and confirm password do not match"
}
```

`400 Bad Request` - Invalid Current Password
```json
{
  "error": "Validation Failed",
  "message": "Current password is incorrect"
}
```

#### 7. Unlock User

**Endpoint:** `POST /api/users/{id}/unlock`

**Path Parameters:**
- `id` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `204 No Content`

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

---

### Roles API

#### 1. Get All Roles

**Endpoint:** `GET /api/users/roles`

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "SUPER_USER",
    "name": "Super User",
    "description": "Super user with access to all branches and all permissions",
    "isSystemRole": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 2,
    "code": "MANAGER",
    "name": "Manager",
    "description": "Branch manager with access to assigned branches",
    "isSystemRole": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 3,
    "code": "ACCOUNTANT",
    "name": "Accountant",
    "description": "Accountant with access to assigned branches",
    "isSystemRole": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

#### 2. Get Role by ID

**Endpoint:** `GET /api/users/roles/{id}`

**Path Parameters:**
- `id` (required, Long) - Role ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "SUPER_USER",
  "name": "Super User",
  "description": "Super user with access to all branches and all permissions",
  "isSystemRole": true,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "Role not found with id: 999"
}
```

---

### Permissions API

#### 1. Get All Permissions

**Endpoint:** `GET /api/users/permissions`

**Query Parameters:**
- `module` (optional, String) - Filter permissions by module name

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body (All Permissions):**
```json
[
  {
    "id": 1,
    "code": "USER_VIEW",
    "name": "View Users",
    "description": "Permission to view user information",
    "module": "User Management",
    "resource": "User",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 2,
    "code": "USER_CREATE",
    "name": "Create Users",
    "description": "Permission to create new users",
    "module": "User Management",
    "resource": "User",
    "action": "CREATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 3,
    "code": "USER_UPDATE",
    "name": "Update Users",
    "description": "Permission to update user information",
    "module": "User Management",
    "resource": "User",
    "action": "UPDATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 4,
    "code": "USER_DELETE",
    "name": "Delete Users",
    "description": "Permission to delete users",
    "module": "User Management",
    "resource": "User",
    "action": "DELETE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 5,
    "code": "ROLE_VIEW",
    "name": "View Roles",
    "description": "Permission to view role information",
    "module": "User Management",
    "resource": "Role",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 6,
    "code": "PERMISSION_VIEW",
    "name": "View Permissions",
    "description": "Permission to view permission information",
    "module": "User Management",
    "resource": "Permission",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

**Response Body (Filtered by Module):**
```json
[
  {
    "id": 1,
    "code": "USER_VIEW",
    "name": "View Users",
    "description": "Permission to view user information",
    "module": "User Management",
    "resource": "User",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 2,
    "code": "USER_CREATE",
    "name": "Create Users",
    "description": "Permission to create new users",
    "module": "User Management",
    "resource": "User",
    "action": "CREATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 3,
    "code": "USER_UPDATE",
    "name": "Update Users",
    "description": "Permission to update user information",
    "module": "User Management",
    "resource": "User",
    "action": "UPDATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 4,
    "code": "USER_DELETE",
    "name": "Delete Users",
    "description": "Permission to delete users",
    "module": "User Management",
    "resource": "User",
    "action": "DELETE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

#### 2. Get User Permissions

**Endpoint:** `GET /api/users/permissions/user/{userId}`

**Path Parameters:**
- `userId` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "USER_VIEW",
    "name": "View Users",
    "description": "Permission to view user information",
    "module": "User Management",
    "resource": "User",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 2,
    "code": "USER_CREATE",
    "name": "Create Users",
    "description": "Permission to create new users",
    "module": "User Management",
    "resource": "User",
    "action": "CREATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 3,
    "code": "USER_UPDATE",
    "name": "Update Users",
    "description": "Permission to update user information",
    "module": "User Management",
    "resource": "User",
    "action": "UPDATE",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  {
    "id": 5,
    "code": "ROLE_VIEW",
    "name": "View Roles",
    "description": "Permission to view role information",
    "module": "User Management",
    "resource": "Role",
    "action": "VIEW",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

---

### User Branch Access API

#### 1. Get User Branch Access

**Endpoint:** `GET /api/users/{userId}/branches`

**Path Parameters:**
- `userId` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "branchId": 1,
    "branchName": "Main Branch",
    "branchCode": "BR001",
    "grantedAt": "2024-01-10T08:00:00"
  },
  {
    "id": 2,
    "userId": 2,
    "branchId": 2,
    "branchName": "Secondary Branch",
    "branchCode": "BR002",
    "grantedAt": "2024-01-10T08:00:00"
  }
]
```

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

#### 2. Assign Branches to User

**Endpoint:** `PUT /api/users/{userId}/branches`

**Path Parameters:**
- `userId` (required, Long) - User ID

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
[1, 2, 3]
```

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "userId": 2,
    "branchId": 1,
    "branchName": "Main Branch",
    "branchCode": "BR001",
    "grantedAt": "2024-01-16T15:30:00"
  },
  {
    "id": 2,
    "userId": 2,
    "branchId": 2,
    "branchName": "Secondary Branch",
    "branchCode": "BR002",
    "grantedAt": "2024-01-16T15:30:00"
  },
  {
    "id": 3,
    "userId": 2,
    "branchId": 3,
    "branchName": "Tertiary Branch",
    "branchCode": "BR003",
    "grantedAt": "2024-01-16T15:30:00"
  }
]
```

**Note:** This endpoint replaces all existing branch access with the provided list. To remove all branch access, send an empty array `[]`.

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "message": "Invalid branch IDs provided"
}
```

`401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

`403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

`404 Not Found` - User Not Found
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

`404 Not Found` - Branch Not Found
```json
{
  "error": "Resource Not Found",
  "message": "One or more branches not found"
}
```

#### 3. Remove All Branch Access

**Endpoint:** `DELETE /api/users/{userId}/branches`

**Path Parameters:**
- `userId` (required, Long) - User ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `204 No Content`

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

#### 4. Check Branch Access

**Endpoint:** `GET /api/users/{userId}/branches/{branchId}/check`

**Path Parameters:**
- `userId` (required, Long) - User ID
- `branchId` (required, Long) - Branch ID

**Request Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`

**Response Body:**
```json
true
```

**Note:** Returns `true` if the user has access to the specified branch, `false` otherwise. Super users always have access to all branches.

**Error Response:** `401 Unauthorized`
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

**Error Response:** `403 Forbidden`
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "User not found with id: 999"
}
```

---

### Password Reset API

#### 1. Request Password Reset

**Endpoint:** `POST /api/auth/password-reset/request`

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "email": "user@trustapp.com"
}
```

**Response:** `202 Accepted`

**Response Body:** No content

**Note:** This endpoint always returns 202 Accepted to prevent email enumeration attacks. If the email exists, a password reset token will be generated and sent via email (if email service is configured). The token expires after 24 hours.

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "email": "Email is required"
  }
}
```

`400 Bad Request` - Invalid Email Format
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "email": "Invalid email format"
  }
}
```

#### 2. Reset Password

**Endpoint:** `POST /api/auth/password-reset/reset`

**Request Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "token": "abc123xyz456...",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Response:** `204 No Content`

**Response Body:** No content

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "token": "Token is required",
    "newPassword": "New password must be at least 8 characters",
    "confirmPassword": "Confirm password is required"
  }
}
```

`400 Bad Request` - Password Mismatch
```json
{
  "error": "Validation Failed",
  "message": "New password and confirm password do not match"
}
```

`400 Bad Request` - Invalid or Expired Token
```json
{
  "error": "Validation Failed",
  "message": "Invalid or expired password reset token"
}
```

`400 Bad Request` - Inactive User
```json
{
  "error": "Validation Failed",
  "message": "Cannot reset password for inactive user"
}
```

#### 3. Validate Password Reset Token

**Endpoint:** `GET /api/auth/password-reset/validate/{token}`

**Path Parameters:**
- `token` (required, String) - Password reset token

**Request Headers:**
- None (public endpoint)

**Response:** `200 OK`

**Response Body:**
```json
true
```

**Note:** Returns `true` if the token is valid and not expired, `false` otherwise. This endpoint can be used by the frontend to check if a token is valid before showing the password reset form.

**Response Examples:**

Valid Token:
```json
true
```

Invalid/Expired Token:
```json
false
```
