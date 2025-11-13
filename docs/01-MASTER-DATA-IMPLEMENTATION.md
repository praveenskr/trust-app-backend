# Master Data Module - Technical Implementation Plan

## Overview

The Master Data module manages all configurable reference data used across the Trust Management System. This includes branches, donation purposes, expense categories, events, subscription plans, serial number configurations, vendors, and payment modes.

---

## Table of Contents

1. [Database Schema](#database-schema)
2. [Entity Classes (DTOs)](#entity-classes-dtos)
3. [Repository Layer (JDBC)](#repository-layer-jdbc)
4. [Service Layer](#service-layer)
5. [Controller Layer (REST APIs)](#controller-layer-rest-apis)
6. [API Contract Details](#api-contract-details)
7. [Validation & Error Handling](#validation--error-handling)
8. [Testing Strategy](#testing-strategy)
9. [Implementation Steps](#implementation-steps)

---

## Database Schema

### 1. Branches Table

```sql
CREATE TABLE branches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(255),
    contact_person VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_code (code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. Donation Purposes Table

```sql
CREATE TABLE donation_purposes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. Donation Sub-Categories Table

```sql
CREATE TABLE donation_sub_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    purpose_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (purpose_id) REFERENCES donation_purposes(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_purpose_code (purpose_id, code),
    INDEX idx_purpose_id (purpose_id),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. Expense Categories Table

```sql
CREATE TABLE expense_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. Expense Sub-Categories Table

```sql
CREATE TABLE expense_sub_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_category_code (category_id, code),
    INDEX idx_category_id (category_id),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. Events Table

```sql
CREATE TABLE events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    status ENUM('PLANNED', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNED',
    branch_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    INDEX idx_code (code),
    INDEX idx_branch_id (branch_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 7. Subscription Plans Table

```sql
CREATE TABLE subscription_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    plan_type ENUM('MONTHLY', 'QUARTERLY', 'YEARLY', 'LIFETIME') NOT NULL,
    duration_months INT,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_code (code),
    INDEX idx_plan_type (plan_type),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 8. Serial Number Configuration Table

```sql
CREATE TABLE serial_number_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(100) NOT NULL UNIQUE,
    prefix VARCHAR(50) NOT NULL,
    format_pattern VARCHAR(255) DEFAULT '{PREFIX}-{YEAR}-{SEQUENCE}',
    current_year INT NOT NULL,
    last_sequence INT DEFAULT 0,
    sequence_length INT DEFAULT 4,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_entity_type (entity_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 9. Vendors Table

```sql
CREATE TABLE vendors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    gst_number VARCHAR(50),
    pan_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 10. Payment Modes Table

```sql
CREATE TABLE payment_modes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    requires_receipt BOOLEAN DEFAULT TRUE,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 11. Subscription Discounts Table

```sql
CREATE TABLE subscription_discounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    min_quantity INT DEFAULT 1,
    max_quantity INT,
    valid_from DATE NOT NULL,
    valid_to DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE RESTRICT,
    INDEX idx_plan_id (plan_id),
    INDEX idx_valid_dates (valid_from, valid_to),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Entity Classes (DTOs)

### BranchDTO

```java
package com.trustapp.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class BranchDTO {
    private Long id;
    
    @NotBlank(message = "Branch code is required")
    @Size(max = 50, message = "Branch code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Branch name is required")
    @Size(max = 255, message = "Branch name must not exceed 255 characters")
    private String name;
    
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String contactPerson;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
    // Constructor
}
```

### DonationPurposeDTO

```java
package com.trustapp.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class DonationPurposeDTO {
    private Long id;
    
    @NotBlank(message = "Purpose code is required")
    @Size(max = 50, message = "Purpose code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Purpose name is required")
    @Size(max = 255, message = "Purpose name must not exceed 255 characters")
    private String name;
    
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}
```

### DonationSubCategoryDTO

```java
package com.trustapp.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class DonationSubCategoryDTO {
    private Long id;
    
    @NotNull(message = "Purpose ID is required")
    private Long purposeId;
    
    @NotBlank(message = "Sub-category code is required")
    @Size(max = 50, message = "Sub-category code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Sub-category name is required")
    @Size(max = 255, message = "Sub-category name must not exceed 255 characters")
    private String name;
    
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}
```

### EventDTO

```java
package com.trustapp.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventDTO {
    private Long id;
    
    @NotBlank(message = "Event code is required")
    @Size(max = 50, message = "Event code must not exceed 50 characters")
    private String code;
    
    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String name;
    
    private String description;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    private String status; // PLANNED, ACTIVE, COMPLETED, CANCELLED
    private Long branchId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}
```

### SerialNumberConfigDTO

```java
package com.trustapp.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class SerialNumberConfigDTO {
    private Long id;
    
    @NotBlank(message = "Entity type is required")
    @Size(max = 100, message = "Entity type must not exceed 100 characters")
    private String entityType;
    
    @NotBlank(message = "Prefix is required")
    @Size(max = 50, message = "Prefix must not exceed 50 characters")
    private String prefix;
    
    private String formatPattern;
    
    @NotNull(message = "Current year is required")
    private Integer currentYear;
    
    @NotNull(message = "Last sequence is required")
    private Integer lastSequence;
    
    @NotNull(message = "Sequence length is required")
    private Integer sequenceLength;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getters and Setters
}
```

---

## Repository Layer (JDBC)

### BranchRepository

```java
package com.trustapp.masterdata.repository;

import com.trustapp.masterdata.dto.BranchDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class BranchRepository {
    
    private final JdbcClient jdbcClient;
    
    public BranchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<BranchDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, address, city, state, pincode, 
                   phone, email, contact_person, is_active, 
                   created_at, updated_at, created_by, updated_by
            FROM branches
            """ + (includeInactive ? "" : "WHERE is_active = TRUE") + """
            ORDER BY name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(BranchDTO.class)
            .list();
    }
    
    public Optional<BranchDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, address, city, state, pincode, 
                   phone, email, contact_person, is_active, 
                   created_at, updated_at, created_by, updated_by
            FROM branches
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(BranchDTO.class)
            .optional();
    }
    
    public Optional<BranchDTO> findByCode(String code) {
        String sql = """
            SELECT id, code, name, address, city, state, pincode, 
                   phone, email, contact_person, is_active, 
                   created_at, updated_at, created_by, updated_by
            FROM branches
            WHERE code = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .query(BranchDTO.class)
            .optional();
    }
    
    public Long save(BranchDTO branch, Long userId) {
        String sql = """
            INSERT INTO branches 
            (code, name, address, city, state, pincode, phone, email, 
             contact_person, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(branch.getCode())
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive() != null ? branch.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(BranchDTO branch, Long userId) {
        String sql = """
            UPDATE branches
            SET name = ?, address = ?, city = ?, state = ?, pincode = ?,
                phone = ?, email = ?, contact_person = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(branch.getName())
            .param(branch.getAddress())
            .param(branch.getCity())
            .param(branch.getState())
            .param(branch.getPincode())
            .param(branch.getPhone())
            .param(branch.getEmail())
            .param(branch.getContactPerson())
            .param(branch.getIsActive())
            .param(userId)
            .param(branch.getId())
            .update();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM branches
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int delete(Long id) {
        String sql = "DELETE FROM branches WHERE id = ?";
        return jdbcClient.sql(sql)
            .param(id)
            .update();
    }
}
```

### DonationPurposeRepository

```java
package com.trustapp.masterdata.repository;

import com.trustapp.masterdata.dto.DonationPurposeDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class DonationPurposeRepository {
    
    private final JdbcClient jdbcClient;
    
    public DonationPurposeRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<DonationPurposeDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, code, name, description, display_order, is_active,
                   created_at, updated_at, created_by, updated_by
            FROM donation_purposes
            """ + (includeInactive ? "" : "WHERE is_active = TRUE") + """
            ORDER BY display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(DonationPurposeDTO.class)
            .list();
    }
    
    public Optional<DonationPurposeDTO> findById(Long id) {
        String sql = """
            SELECT id, code, name, description, display_order, is_active,
                   created_at, updated_at, created_by, updated_by
            FROM donation_purposes
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(DonationPurposeDTO.class)
            .optional();
    }
    
    public Long save(DonationPurposeDTO purpose, Long userId) {
        String sql = """
            INSERT INTO donation_purposes 
            (code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(purpose.getCode())
            .param(purpose.getName())
            .param(purpose.getDescription())
            .param(purpose.getDisplayOrder() != null ? purpose.getDisplayOrder() : 0)
            .param(purpose.getIsActive() != null ? purpose.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(DonationPurposeDTO purpose, Long userId) {
        String sql = """
            UPDATE donation_purposes
            SET name = ?, description = ?, display_order = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(purpose.getName())
            .param(purpose.getDescription())
            .param(purpose.getDisplayOrder())
            .param(purpose.getIsActive())
            .param(userId)
            .param(purpose.getId())
            .update();
    }
    
    public boolean existsByCode(String code, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donation_purposes
            WHERE code = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int delete(Long id) {
        // Check if sub-categories exist
        String checkSql = "SELECT COUNT(*) FROM donation_sub_categories WHERE purpose_id = ?";
        Long count = jdbcClient.sql(checkSql)
            .param(id)
            .query(Long.class)
            .single();
        
        if (count > 0) {
            throw new IllegalStateException("Cannot delete purpose with existing sub-categories");
        }
        
        String sql = "DELETE FROM donation_purposes WHERE id = ?";
        return jdbcClient.sql(sql)
            .param(id)
            .update();
    }
}
```

### DonationSubCategoryRepository

```java
package com.trustapp.masterdata.repository;

import com.trustapp.masterdata.dto.DonationSubCategoryDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class DonationSubCategoryRepository {
    
    private final JdbcClient jdbcClient;
    
    public DonationSubCategoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<DonationSubCategoryDTO> findByPurposeId(Long purposeId, boolean includeInactive) {
        String sql = """
            SELECT id, purpose_id, code, name, description, display_order, is_active,
                   created_at, updated_at, created_by, updated_by
            FROM donation_sub_categories
            WHERE purpose_id = ?
            """ + (includeInactive ? "" : "AND is_active = TRUE") + """
            ORDER BY display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .param(purposeId)
            .query(DonationSubCategoryDTO.class)
            .list();
    }
    
    public List<DonationSubCategoryDTO> findAll(boolean includeInactive) {
        String sql = """
            SELECT id, purpose_id, code, name, description, display_order, is_active,
                   created_at, updated_at, created_by, updated_by
            FROM donation_sub_categories
            """ + (includeInactive ? "" : "WHERE is_active = TRUE") + """
            ORDER BY purpose_id ASC, display_order ASC, name ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(DonationSubCategoryDTO.class)
            .list();
    }
    
    public Optional<DonationSubCategoryDTO> findById(Long id) {
        String sql = """
            SELECT id, purpose_id, code, name, description, display_order, is_active,
                   created_at, updated_at, created_by, updated_by
            FROM donation_sub_categories
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(id)
            .query(DonationSubCategoryDTO.class)
            .optional();
    }
    
    public Long save(DonationSubCategoryDTO subCategory, Long userId) {
        String sql = """
            INSERT INTO donation_sub_categories 
            (purpose_id, code, name, description, display_order, is_active, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(subCategory.getPurposeId())
            .param(subCategory.getCode())
            .param(subCategory.getName())
            .param(subCategory.getDescription())
            .param(subCategory.getDisplayOrder() != null ? subCategory.getDisplayOrder() : 0)
            .param(subCategory.getIsActive() != null ? subCategory.getIsActive() : true)
            .param(userId)
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(DonationSubCategoryDTO subCategory, Long userId) {
        String sql = """
            UPDATE donation_sub_categories
            SET name = ?, description = ?, display_order = ?, is_active = ?,
                updated_by = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(subCategory.getName())
            .param(subCategory.getDescription())
            .param(subCategory.getDisplayOrder())
            .param(subCategory.getIsActive())
            .param(userId)
            .param(subCategory.getId())
            .update();
    }
    
    public boolean existsByCodeAndPurposeId(String code, Long purposeId, Long excludeId) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM donation_sub_categories
            WHERE code = ? AND purpose_id = ? AND (? IS NULL OR id != ?)
            """;
        
        return jdbcClient.sql(sql)
            .param(code)
            .param(purposeId)
            .param(excludeId)
            .param(excludeId)
            .query(Boolean.class)
            .single();
    }
    
    public int delete(Long id) {
        String sql = "DELETE FROM donation_sub_categories WHERE id = ?";
        return jdbcClient.sql(sql)
            .param(id)
            .update();
    }
}
```

### SerialNumberConfigRepository

```java
package com.trustapp.masterdata.repository;

import com.trustapp.masterdata.dto.SerialNumberConfigDTO;
import org.springframework.jdbc.core.simple.JdbcClient;
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
            SELECT id, entity_type, prefix, format_pattern, current_year,
                   last_sequence, sequence_length, created_at, updated_at
            FROM serial_number_config
            ORDER BY entity_type ASC
            """;
        
        return jdbcClient.sql(sql)
            .query(SerialNumberConfigDTO.class)
            .list();
    }
    
    public Optional<SerialNumberConfigDTO> findByEntityType(String entityType) {
        String sql = """
            SELECT id, entity_type, prefix, format_pattern, current_year,
                   last_sequence, sequence_length, created_at, updated_at
            FROM serial_number_config
            WHERE entity_type = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(entityType)
            .query(SerialNumberConfigDTO.class)
            .optional();
    }
    
    @Transactional
    public String getNextSerialNumber(String entityType) {
        int currentYear = java.time.LocalDate.now().getYear();
        
        // Lock row for update
        String selectSql = """
            SELECT id, entity_type, prefix, format_pattern, current_year,
                   last_sequence, sequence_length
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
        return String.format(config.getFormatPattern()
            .replace("{PREFIX}", config.getPrefix())
            .replace("{YEAR}", String.valueOf(currentYear))
            .replace("{SEQUENCE}", sequence));
    }
    
    public Long save(SerialNumberConfigDTO config) {
        String sql = """
            INSERT INTO serial_number_config 
            (entity_type, prefix, format_pattern, current_year, last_sequence, sequence_length)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        var keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(config.getEntityType())
            .param(config.getPrefix())
            .param(config.getFormatPattern())
            .param(config.getCurrentYear())
            .param(config.getLastSequence())
            .param(config.getSequenceLength())
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    public int update(SerialNumberConfigDTO config) {
        String sql = """
            UPDATE serial_number_config
            SET prefix = ?, format_pattern = ?, sequence_length = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE entity_type = ?
            """;
        
        return jdbcClient.sql(sql)
            .param(config.getPrefix())
            .param(config.getFormatPattern())
            .param(config.getSequenceLength())
            .param(config.getEntityType())
            .update();
    }
}
```

---

## Service Layer

### BranchService

```java
package com.trustapp.masterdata.service;

import com.trustapp.masterdata.dto.BranchDTO;
import com.trustapp.masterdata.repository.BranchRepository;
import com.trustapp.masterdata.exception.ResourceNotFoundException;
import com.trustapp.masterdata.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BranchService {
    
    private final BranchRepository branchRepository;
    
    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }
    
    public List<BranchDTO> getAllBranches(boolean includeInactive) {
        return branchRepository.findAll(includeInactive);
    }
    
    public BranchDTO getBranchById(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + id));
    }
    
    public BranchDTO getBranchByCode(String code) {
        return branchRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with code: " + code));
    }
    
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO, Long userId) {
        // Check for duplicate code
        if (branchRepository.existsByCode(branchDTO.getCode(), null)) {
            throw new DuplicateResourceException("Branch code already exists: " + branchDTO.getCode());
        }
        
        Long id = branchRepository.save(branchDTO, userId);
        return getBranchById(id);
    }
    
    @Transactional
    public BranchDTO updateBranch(Long id, BranchDTO branchDTO, Long userId) {
        // Check if branch exists
        getBranchById(id);
        
        // Check for duplicate code
        if (branchRepository.existsByCode(branchDTO.getCode(), id)) {
            throw new DuplicateResourceException("Branch code already exists: " + branchDTO.getCode());
        }
        
        branchDTO.setId(id);
        branchRepository.update(branchDTO, userId);
        return getBranchById(id);
    }
    
    @Transactional
    public void deleteBranch(Long id) {
        // Check if branch exists
        getBranchById(id);
        
        // TODO: Check if branch has associated transactions
        // If yes, throw exception or soft delete
        
        branchRepository.delete(id);
    }
}
```

---

## Controller Layer (REST APIs)

### BranchController

```java
package com.trustapp.masterdata.controller;

import com.trustapp.masterdata.dto.BranchDTO;
import com.trustapp.masterdata.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/master/branches")
public class BranchController {
    
    private final BranchService branchService;
    
    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }
    
    @GetMapping
    public ResponseEntity<List<BranchDTO>> getAllBranches(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        List<BranchDTO> branches = branchService.getAllBranches(includeInactive);
        return ResponseEntity.ok(branches);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BranchDTO> getBranchById(@PathVariable Long id) {
        BranchDTO branch = branchService.getBranchById(id);
        return ResponseEntity.ok(branch);
    }
    
    @PostMapping
    public ResponseEntity<BranchDTO> createBranch(
            @Valid @RequestBody BranchDTO branchDTO,
            @AuthenticationPrincipal Long userId) {
        BranchDTO created = branchService.createBranch(branchDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BranchDTO> updateBranch(
            @PathVariable Long id,
            @Valid @RequestBody BranchDTO branchDTO,
            @AuthenticationPrincipal Long userId) {
        BranchDTO updated = branchService.updateBranch(id, branchDTO, userId);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## API Contract Details

### Branches API

#### 1. Get All Branches

**Endpoint:** `GET /api/master/branches`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`) - Include inactive branches in response

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "BR001",
    "name": "Main Branch",
    "address": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "phone": "+91-22-12345678",
    "email": "main@trustapp.com",
    "contactPerson": "John Doe",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Branch by ID

**Endpoint:** `GET /api/master/branches/{id}`

**Path Parameters:**
- `id` (required, Long) - Branch ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "BR001",
  "name": "Main Branch",
  "address": "123 Main Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "phone": "+91-22-12345678",
  "email": "main@trustapp.com",
  "contactPerson": "John Doe",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "Branch not found with id: 999"
}
```

#### 3. Create Branch

**Endpoint:** `POST /api/master/branches`

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "code": "BR002",
  "name": "Secondary Branch",
  "address": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "phone": "+91-11-87654321",
  "email": "secondary@trustapp.com",
  "contactPerson": "Jane Smith",
  "isActive": true
}
```

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 2,
  "code": "BR002",
  "name": "Secondary Branch",
  "address": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "phone": "+91-11-87654321",
  "email": "secondary@trustapp.com",
  "contactPerson": "Jane Smith",
  "isActive": true,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

**Error Responses:**

`400 Bad Request` - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "code": "Branch code is required",
    "name": "Branch name is required",
    "email": "Invalid email format"
  }
}
```

`409 Conflict` - Duplicate Code
```json
{
  "error": "Duplicate Resource",
  "message": "Branch code already exists: BR001"
}
```

#### 4. Update Branch

**Endpoint:** `PUT /api/master/branches/{id}`

**Path Parameters:**
- `id` (required, Long) - Branch ID

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "code": "BR002",
  "name": "Updated Branch Name",
  "address": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "phone": "+91-11-87654321",
  "email": "updated@trustapp.com",
  "contactPerson": "Jane Smith",
  "isActive": true
}
```

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 2,
  "code": "BR002",
  "name": "Updated Branch Name",
  "address": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "phone": "+91-11-87654321",
  "email": "updated@trustapp.com",
  "contactPerson": "Jane Smith",
  "isActive": true,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T15:30:00"
}
```

#### 5. Delete Branch

**Endpoint:** `DELETE /api/master/branches/{id}`

**Path Parameters:**
- `id` (required, Long) - Branch ID

**Response:** `204 No Content`

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "Branch not found with id: 999"
}
```

---

### Donation Purposes API

#### 1. Get All Donation Purposes

**Endpoint:** `GET /api/master/donation-purposes`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`) - Include inactive purposes

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "DP001",
    "name": "Education",
    "description": "Donations for educational purposes",
    "displayOrder": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "code": "DP002",
    "name": "Healthcare",
    "description": "Donations for healthcare purposes",
    "displayOrder": 2,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Donation Purpose by ID

**Endpoint:** `GET /api/master/donation-purposes/{id}`

**Path Parameters:**
- `id` (required, Long) - Purpose ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "DP001",
  "name": "Education",
  "description": "Donations for educational purposes",
  "displayOrder": 1,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Donation Purpose

**Endpoint:** `POST /api/master/donation-purposes`

**Request Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "code": "DP003",
  "name": "Relief Work",
  "description": "Donations for disaster relief and emergency assistance",
  "displayOrder": 3,
  "isActive": true
}
```

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 3,
  "code": "DP003",
  "name": "Relief Work",
  "description": "Donations for disaster relief and emergency assistance",
  "displayOrder": 3,
  "isActive": true,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

#### 4. Update Donation Purpose

**Endpoint:** `PUT /api/master/donation-purposes/{id}`

**Path Parameters:**
- `id` (required, Long) - Purpose ID

**Request Body:**
```json
{
  "code": "DP003",
  "name": "Updated Relief Work",
  "description": "Updated description",
  "displayOrder": 3,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Donation Purpose

**Endpoint:** `DELETE /api/master/donation-purposes/{id}`

**Path Parameters:**
- `id` (required, Long) - Purpose ID

**Response:** `204 No Content`

**Error Response:** `400 Bad Request` - If sub-categories exist
```json
{
  "error": "Validation Failed",
  "message": "Cannot delete purpose with existing sub-categories"
}
```

---

### Donation Sub-Categories API

#### 1. Get All Donation Sub-Categories

**Endpoint:** `GET /api/master/donation-sub-categories`

**Query Parameters:**
- `purposeId` (optional, Long) - Filter by purpose ID
- `includeInactive` (optional, boolean, default: `false`) - Include inactive sub-categories

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "purposeId": 1,
    "code": "DSC001",
    "name": "School Fees",
    "description": "Donations for school fees",
    "displayOrder": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "purposeId": 1,
    "code": "DSC002",
    "name": "Books & Supplies",
    "description": "Donations for books and educational supplies",
    "displayOrder": 2,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Donation Sub-Category by ID

**Endpoint:** `GET /api/master/donation-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "purposeId": 1,
  "code": "DSC001",
  "name": "School Fees",
  "description": "Donations for school fees",
  "displayOrder": 1,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Donation Sub-Category

**Endpoint:** `POST /api/master/donation-sub-categories`

**Request Body:**
```json
{
  "purposeId": 1,
  "code": "DSC003",
  "name": "Scholarship",
  "description": "Donations for scholarships",
  "displayOrder": 3,
  "isActive": true
}
```

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 3,
  "purposeId": 1,
  "code": "DSC003",
  "name": "Scholarship",
  "description": "Donations for scholarships",
  "displayOrder": 3,
  "isActive": true,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

**Error Response:** `400 Bad Request` - Invalid purpose ID
```json
{
  "error": "Validation Failed",
  "message": "Purpose ID is required"
}
```

#### 4. Update Donation Sub-Category

**Endpoint:** `PUT /api/master/donation-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Request Body:**
```json
{
  "purposeId": 1,
  "code": "DSC003",
  "name": "Updated Scholarship",
  "description": "Updated description",
  "displayOrder": 3,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Donation Sub-Category

**Endpoint:** `DELETE /api/master/donation-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Response:** `204 No Content`

---

### Expense Categories API

#### 1. Get All Expense Categories

**Endpoint:** `GET /api/master/expense-categories`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "EC001",
    "name": "Administrative",
    "description": "Administrative expenses",
    "displayOrder": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Expense Category by ID

**Endpoint:** `GET /api/master/expense-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Category ID

**Response:** `200 OK`

#### 3. Create Expense Category

**Endpoint:** `POST /api/master/expense-categories`

**Request Body:**
```json
{
  "code": "EC002",
  "name": "Operational",
  "description": "Operational expenses",
  "displayOrder": 2,
  "isActive": true
}
```

**Response:** `201 Created`

#### 4. Update Expense Category

**Endpoint:** `PUT /api/master/expense-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Category ID

**Request Body:**
```json
{
  "code": "EC002",
  "name": "Updated Operational",
  "description": "Updated description",
  "displayOrder": 2,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Expense Category

**Endpoint:** `DELETE /api/master/expense-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Category ID

**Response:** `204 No Content`

---

### Expense Sub-Categories API

#### 1. Get All Expense Sub-Categories

**Endpoint:** `GET /api/master/expense-sub-categories`

**Query Parameters:**
- `categoryId` (optional, Long) - Filter by category ID
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "categoryId": 1,
    "code": "ESC001",
    "name": "Office Supplies",
    "description": "Office supplies and stationery",
    "displayOrder": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Expense Sub-Category by ID

**Endpoint:** `GET /api/master/expense-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Response:** `200 OK`

#### 3. Create Expense Sub-Category

**Endpoint:** `POST /api/master/expense-sub-categories`

**Request Body:**
```json
{
  "categoryId": 1,
  "code": "ESC002",
  "name": "Utilities",
  "description": "Electricity, water, internet bills",
  "displayOrder": 2,
  "isActive": true
}
```

**Response:** `201 Created`

#### 4. Update Expense Sub-Category

**Endpoint:** `PUT /api/master/expense-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Request Body:**
```json
{
  "categoryId": 1,
  "code": "ESC002",
  "name": "Updated Utilities",
  "description": "Updated description",
  "displayOrder": 2,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Expense Sub-Category

**Endpoint:** `DELETE /api/master/expense-sub-categories/{id}`

**Path Parameters:**
- `id` (required, Long) - Sub-category ID

**Response:** `204 No Content`

---

### Events API

#### 1. Get All Events

**Endpoint:** `GET /api/master/events`

**Query Parameters:**
- `branchId` (optional, Long) - Filter by branch ID
- `status` (optional, String) - Filter by status (PLANNED, ACTIVE, COMPLETED, CANCELLED)
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "EVT001",
    "name": "Annual Fundraiser",
    "description": "Annual fundraising event",
    "startDate": "2024-03-01",
    "endDate": "2024-03-05",
    "status": "PLANNED",
    "branchId": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Event by ID

**Endpoint:** `GET /api/master/events/{id}`

**Path Parameters:**
- `id` (required, Long) - Event ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "EVT001",
  "name": "Annual Fundraiser",
  "description": "Annual fundraising event",
  "startDate": "2024-03-01",
  "endDate": "2024-03-05",
  "status": "PLANNED",
  "branchId": 1,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Event

**Endpoint:** `POST /api/master/events`

**Request Body:**
```json
{
  "code": "EVT002",
  "name": "Charity Run",
  "description": "5K charity run event",
  "startDate": "2024-04-15",
  "endDate": "2024-04-15",
  "status": "PLANNED",
  "branchId": 1,
  "isActive": true
}
```

**Response:** `201 Created`

**Error Response:** `400 Bad Request` - Invalid date range
```json
{
  "error": "Validation Failed",
  "message": "End date must be after start date"
}
```

#### 4. Update Event

**Endpoint:** `PUT /api/master/events/{id}`

**Path Parameters:**
- `id` (required, Long) - Event ID

**Request Body:**
```json
{
  "code": "EVT002",
  "name": "Updated Charity Run",
  "description": "Updated description",
  "startDate": "2024-04-15",
  "endDate": "2024-04-15",
  "status": "ACTIVE",
  "branchId": 1,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Event

**Endpoint:** `DELETE /api/master/events/{id}`

**Path Parameters:**
- `id` (required, Long) - Event ID

**Response:** `204 No Content`

---

### Subscription Plans API

#### 1. Get All Subscription Plans

**Endpoint:** `GET /api/master/subscription-plans`

**Query Parameters:**
- `planType` (optional, String) - Filter by plan type (MONTHLY, QUARTERLY, YEARLY, LIFETIME)
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "SP001",
    "name": "Monthly Plan",
    "planType": "MONTHLY",
    "durationMonths": 1,
    "amount": 1000.00,
    "description": "Monthly subscription plan",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Subscription Plan by ID

**Endpoint:** `GET /api/master/subscription-plans/{id}`

**Path Parameters:**
- `id` (required, Long) - Plan ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "SP001",
  "name": "Monthly Plan",
  "planType": "MONTHLY",
  "durationMonths": 1,
  "amount": 1000.00,
  "description": "Monthly subscription plan",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Subscription Plan

**Endpoint:** `POST /api/master/subscription-plans`

**Request Body:**
```json
{
  "code": "SP002",
  "name": "Yearly Plan",
  "planType": "YEARLY",
  "durationMonths": 12,
  "amount": 10000.00,
  "description": "Yearly subscription plan with discount",
  "isActive": true
}
```

**Response:** `201 Created`

**Error Response:** `400 Bad Request` - Invalid plan type
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "planType": "Plan type must be one of: MONTHLY, QUARTERLY, YEARLY, LIFETIME"
  }
}
```

#### 4. Update Subscription Plan

**Endpoint:** `PUT /api/master/subscription-plans/{id}`

**Path Parameters:**
- `id` (required, Long) - Plan ID

**Request Body:**
```json
{
  "code": "SP002",
  "name": "Updated Yearly Plan",
  "planType": "YEARLY",
  "durationMonths": 12,
  "amount": 9500.00,
  "description": "Updated description",
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Subscription Plan

**Endpoint:** `DELETE /api/master/subscription-plans/{id}`

**Path Parameters:**
- `id` (required, Long) - Plan ID

**Response:** `204 No Content`

---

### Serial Number Configuration API

#### 1. Get All Serial Number Configurations

**Endpoint:** `GET /api/master/serial-config`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "entityType": "DONATION",
    "prefix": "DON",
    "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
    "currentYear": 2024,
    "lastSequence": 125,
    "sequenceLength": 4,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-16T14:20:00"
  },
  {
    "id": 2,
    "entityType": "EXPENSE",
    "prefix": "EXP",
    "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
    "currentYear": 2024,
    "lastSequence": 89,
    "sequenceLength": 4,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-16T14:20:00"
  }
]
```

#### 2. Get Serial Number Config by Entity Type

**Endpoint:** `GET /api/master/serial-config/{entityType}`

**Path Parameters:**
- `entityType` (required, String) - Entity type (e.g., DONATION, EXPENSE, RECEIPT)

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "entityType": "DONATION",
  "prefix": "DON",
  "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
  "currentYear": 2024,
  "lastSequence": 125,
  "sequenceLength": 4,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "Serial number config not found for entity: INVALID"
}
```

#### 3. Get Next Serial Number

**Endpoint:** `GET /api/master/serial-config/next/{entityType}`

**Path Parameters:**
- `entityType` (required, String) - Entity type

**Response:** `200 OK`

**Response Body:**
```json
{
  "serialNumber": "DON-2024-0126",
  "entityType": "DONATION",
  "year": 2024,
  "sequence": 126
}
```

**Error Response:** `404 Not Found`
```json
{
  "error": "Resource Not Found",
  "message": "Serial number config not found for entity: INVALID"
}
```

#### 4. Create Serial Number Configuration

**Endpoint:** `POST /api/master/serial-config`

**Request Body:**
```json
{
  "entityType": "RECEIPT",
  "prefix": "RCP",
  "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
  "currentYear": 2024,
  "lastSequence": 0,
  "sequenceLength": 4
}
```

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 3,
  "entityType": "RECEIPT",
  "prefix": "RCP",
  "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
  "currentYear": 2024,
  "lastSequence": 0,
  "sequenceLength": 4,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T14:20:00"
}
```

**Error Response:** `409 Conflict` - Duplicate entity type
```json
{
  "error": "Duplicate Resource",
  "message": "Serial number config already exists for entity type: RECEIPT"
}
```

#### 5. Update Serial Number Configuration

**Endpoint:** `PUT /api/master/serial-config/{entityType}`

**Path Parameters:**
- `entityType` (required, String) - Entity type

**Request Body:**
```json
{
  "prefix": "RCPT",
  "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
  "sequenceLength": 5
}
```

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 3,
  "entityType": "RECEIPT",
  "prefix": "RCPT",
  "formatPattern": "{PREFIX}-{YEAR}-{SEQUENCE}",
  "currentYear": 2024,
  "lastSequence": 0,
  "sequenceLength": 5,
  "createdAt": "2024-01-16T14:20:00",
  "updatedAt": "2024-01-16T15:30:00"
}
```

---

### Vendors API

#### 1. Get All Vendors

**Endpoint:** `GET /api/master/vendors`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "VND001",
    "name": "ABC Supplies Pvt Ltd",
    "contactPerson": "Rajesh Kumar",
    "phone": "+91-22-12345678",
    "email": "contact@abcsupplies.com",
    "address": "123 Business Park",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "gstNumber": "27AABCU1234A1Z5",
    "panNumber": "AABCU1234A",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Vendor by ID

**Endpoint:** `GET /api/master/vendors/{id}`

**Path Parameters:**
- `id` (required, Long) - Vendor ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "VND001",
  "name": "ABC Supplies Pvt Ltd",
  "contactPerson": "Rajesh Kumar",
  "phone": "+91-22-12345678",
  "email": "contact@abcsupplies.com",
  "address": "123 Business Park",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "gstNumber": "27AABCU1234A1Z5",
  "panNumber": "AABCU1234A",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Vendor

**Endpoint:** `POST /api/master/vendors`

**Request Body:**
```json
{
  "code": "VND002",
  "name": "XYZ Services Ltd",
  "contactPerson": "Priya Sharma",
  "phone": "+91-11-87654321",
  "email": "info@xyzservices.com",
  "address": "456 Industrial Area",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "gstNumber": "07XYZSE5678B2Z6",
  "panNumber": "XYZSE5678B",
  "isActive": true
}
```

**Response:** `201 Created`

#### 4. Update Vendor

**Endpoint:** `PUT /api/master/vendors/{id}`

**Path Parameters:**
- `id` (required, Long) - Vendor ID

**Request Body:**
```json
{
  "code": "VND002",
  "name": "Updated XYZ Services Ltd",
  "contactPerson": "Priya Sharma",
  "phone": "+91-11-87654321",
  "email": "updated@xyzservices.com",
  "address": "456 Industrial Area",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "gstNumber": "07XYZSE5678B2Z6",
  "panNumber": "XYZSE5678B",
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Vendor

**Endpoint:** `DELETE /api/master/vendors/{id}`

**Path Parameters:**
- `id` (required, Long) - Vendor ID

**Response:** `204 No Content`

---

### Payment Modes API

#### 1. Get All Payment Modes

**Endpoint:** `GET /api/master/payment-modes`

**Query Parameters:**
- `includeInactive` (optional, boolean, default: `false`)

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "code": "PM001",
    "name": "Cash",
    "description": "Cash payment",
    "requiresReceipt": true,
    "displayOrder": 1,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "code": "PM002",
    "name": "UPI",
    "description": "Unified Payments Interface",
    "requiresReceipt": false,
    "displayOrder": 2,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 3,
    "code": "PM003",
    "name": "Bank Transfer",
    "description": "Bank transfer or NEFT/RTGS",
    "requiresReceipt": true,
    "displayOrder": 3,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Payment Mode by ID

**Endpoint:** `GET /api/master/payment-modes/{id}`

**Path Parameters:**
- `id` (required, Long) - Payment mode ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "code": "PM001",
  "name": "Cash",
  "description": "Cash payment",
  "requiresReceipt": true,
  "displayOrder": 1,
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Payment Mode

**Endpoint:** `POST /api/master/payment-modes`

**Request Body:**
```json
{
  "code": "PM004",
  "name": "Credit Card",
  "description": "Credit card payment",
  "requiresReceipt": false,
  "displayOrder": 4,
  "isActive": true
}
```

**Response:** `201 Created`

#### 4. Update Payment Mode

**Endpoint:** `PUT /api/master/payment-modes/{id}`

**Path Parameters:**
- `id` (required, Long) - Payment mode ID

**Request Body:**
```json
{
  "code": "PM004",
  "name": "Updated Credit Card",
  "description": "Updated description",
  "requiresReceipt": false,
  "displayOrder": 4,
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Payment Mode

**Endpoint:** `DELETE /api/master/payment-modes/{id}`

**Path Parameters:**
- `id` (required, Long) - Payment mode ID

**Response:** `204 No Content`

---

### Subscription Discounts API

#### 1. Get All Subscription Discounts

**Endpoint:** `GET /api/master/subscription-discounts`

**Query Parameters:**
- `planId` (optional, Long) - Filter by plan ID
- `isActive` (optional, boolean) - Filter by active status
- `validFrom` (optional, Date) - Filter by valid from date
- `validTo` (optional, Date) - Filter by valid to date

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "planId": 2,
    "discountType": "PERCENTAGE",
    "discountValue": 10.00,
    "minQuantity": 1,
    "maxQuantity": 5,
    "validFrom": "2024-01-01",
    "validTo": "2024-12-31",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "planId": 2,
    "discountType": "FIXED",
    "discountValue": 500.00,
    "minQuantity": 6,
    "maxQuantity": null,
    "validFrom": "2024-01-01",
    "validTo": "2024-12-31",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### 2. Get Subscription Discount by ID

**Endpoint:** `GET /api/master/subscription-discounts/{id}`

**Path Parameters:**
- `id` (required, Long) - Discount ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "planId": 2,
  "discountType": "PERCENTAGE",
  "discountValue": 10.00,
  "minQuantity": 1,
  "maxQuantity": 5,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 3. Create Subscription Discount

**Endpoint:** `POST /api/master/subscription-discounts`

**Request Body:**
```json
{
  "planId": 2,
  "discountType": "PERCENTAGE",
  "discountValue": 15.00,
  "minQuantity": 10,
  "maxQuantity": null,
  "validFrom": "2024-02-01",
  "validTo": "2024-12-31",
  "isActive": true
}
```

**Response:** `201 Created`

**Error Response:** `400 Bad Request` - Invalid discount type or date range
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "discountType": "Discount type must be PERCENTAGE or FIXED",
    "validTo": "Valid to date must be after valid from date"
  }
}
```

#### 4. Update Subscription Discount

**Endpoint:** `PUT /api/master/subscription-discounts/{id}`

**Path Parameters:**
- `id` (required, Long) - Discount ID

**Request Body:**
```json
{
  "planId": 2,
  "discountType": "PERCENTAGE",
  "discountValue": 20.00,
  "minQuantity": 10,
  "maxQuantity": null,
  "validFrom": "2024-02-01",
  "validTo": "2024-12-31",
  "isActive": true
}
```

**Response:** `200 OK`

#### 5. Delete Subscription Discount

**Endpoint:** `DELETE /api/master/subscription-discounts/{id}`

**Path Parameters:**
- `id` (required, Long) - Discount ID

**Response:** `204 No Content`

---

### Common Error Responses

#### 400 Bad Request - Validation Error
```json
{
  "error": "Validation Failed",
  "fieldErrors": {
    "fieldName": "Error message for field"
  }
}
```

#### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

#### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

#### 404 Not Found
```json
{
  "error": "Resource Not Found",
  "message": "Resource not found with id: {id}"
}
```

#### 409 Conflict - Duplicate Resource
```json
{
  "error": "Duplicate Resource",
  "message": "Resource with code '{code}' already exists"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Validation & Error Handling

### Custom Exceptions

```java
package com.trustapp.masterdata.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

### Global Exception Handler

```java
package com.trustapp.masterdata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Resource Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResource(DuplicateResourceException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Duplicate Resource");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        errors.put("error", "Validation Failed");
        errors.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
```

---

## Testing Strategy

### Unit Tests

```java
package com.trustapp.masterdata.service;

import com.trustapp.masterdata.dto.BranchDTO;
import com.trustapp.masterdata.repository.BranchRepository;
import com.trustapp.masterdata.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {
    
    @Mock
    private BranchRepository branchRepository;
    
    @InjectMocks
    private BranchService branchService;
    
    @Test
    void testGetBranchById_Success() {
        // Given
        Long id = 1L;
        BranchDTO branch = new BranchDTO();
        branch.setId(id);
        branch.setCode("BR001");
        branch.setName("Main Branch");
        
        when(branchRepository.findById(id)).thenReturn(Optional.of(branch));
        
        // When
        BranchDTO result = branchService.getBranchById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("BR001", result.getCode());
        verify(branchRepository, times(1)).findById(id);
    }
    
    @Test
    void testGetBranchById_NotFound() {
        // Given
        Long id = 999L;
        when(branchRepository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            branchService.getBranchById(id);
        });
    }
    
    @Test
    void testCreateBranch_Success() {
        // Given
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setCode("BR002");
        branchDTO.setName("New Branch");
        Long userId = 1L;
        Long savedId = 2L;
        
        when(branchRepository.existsByCode("BR002", null)).thenReturn(false);
        when(branchRepository.save(any(BranchDTO.class), eq(userId))).thenReturn(savedId);
        
        BranchDTO savedBranch = new BranchDTO();
        savedBranch.setId(savedId);
        savedBranch.setCode("BR002");
        savedBranch.setName("New Branch");
        when(branchRepository.findById(savedId)).thenReturn(Optional.of(savedBranch));
        
        // When
        BranchDTO result = branchService.createBranch(branchDTO, userId);
        
        // Then
        assertNotNull(result);
        assertEquals(savedId, result.getId());
        verify(branchRepository, times(1)).existsByCode("BR002", null);
        verify(branchRepository, times(1)).save(any(BranchDTO.class), eq(userId));
    }
}
```

### Integration Tests

```java
package com.trustapp.masterdata.integration;

import com.trustapp.masterdata.dto.BranchDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class BranchControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateBranch() throws Exception {
        String branchJson = """
            {
                "code": "BR001",
                "name": "Test Branch",
                "address": "123 Test St",
                "city": "Test City",
                "isActive": true
            }
            """;
        
        mockMvc.perform(post("/api/master/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(branchJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BR001"))
                .andExpect(jsonPath("$.name").value("Test Branch"));
    }
}
```

---

## Implementation Steps

### Phase 1: Database Setup (Day 1)

1. Create MySQL database
2. Execute all DDL scripts for master data tables
3. Create indexes as per schema
4. Insert initial/default data:
   - Payment modes (Cash, UPI, Bank Transfer)
   - Default serial number configurations
   - Default subscription plans

### Phase 2: Repository Layer (Days 2-3)

1. Create DTO classes for all entities
2. Implement BranchRepository with JDBC Client
3. Implement DonationPurposeRepository
4. Implement DonationSubCategoryRepository
5. Implement ExpenseCategoryRepository
6. Implement ExpenseSubCategoryRepository
7. Implement EventRepository
8. Implement SubscriptionPlanRepository
9. Implement SerialNumberConfigRepository
10. Implement VendorRepository
11. Implement PaymentModeRepository
12. Implement SubscriptionDiscountRepository

### Phase 3: Service Layer (Days 4-5)

1. Implement BranchService
2. Implement DonationPurposeService
3. Implement DonationSubCategoryService
4. Implement ExpenseCategoryService
5. Implement ExpenseSubCategoryService
6. Implement EventService
7. Implement SubscriptionPlanService
8. Implement SerialNumberConfigService
9. Implement VendorService
10. Implement PaymentModeService
11. Implement SubscriptionDiscountService

### Phase 4: Controller Layer (Day 6)

1. Implement BranchController
2. Implement DonationPurposeController
3. Implement DonationSubCategoryController
4. Implement ExpenseCategoryController
5. Implement ExpenseSubCategoryController
6. Implement EventController
7. Implement SubscriptionPlanController
8. Implement SerialNumberConfigController
9. Implement VendorController
10. Implement PaymentModeController
11. Implement SubscriptionDiscountController

### Phase 5: Validation & Error Handling (Day 7)

1. Create custom exception classes
2. Implement GlobalExceptionHandler
3. Add validation annotations to DTOs
4. Test validation scenarios

### Phase 6: Testing (Days 8-9)

1. Write unit tests for all repositories
2. Write unit tests for all services
3. Write integration tests for all controllers
4. Achieve 80%+ code coverage

### Phase 7: Documentation & Review (Day 10)

1. Update API documentation (Swagger)
2. Code review
3. Performance testing
4. Security review

---

## API Endpoints Summary

### Branches
- `GET /api/master/branches` - List all branches
- `GET /api/master/branches/{id}` - Get branch by ID
- `POST /api/master/branches` - Create branch
- `PUT /api/master/branches/{id}` - Update branch
- `DELETE /api/master/branches/{id}` - Delete branch

### Donation Purposes
- `GET /api/master/donation-purposes` - List all purposes
- `GET /api/master/donation-purposes/{id}` - Get purpose by ID
- `POST /api/master/donation-purposes` - Create purpose
- `PUT /api/master/donation-purposes/{id}` - Update purpose
- `DELETE /api/master/donation-purposes/{id}` - Delete purpose

### Donation Sub-Categories
- `GET /api/master/donation-sub-categories` - List all sub-categories
- `GET /api/master/donation-sub-categories?purposeId={id}` - List by purpose
- `GET /api/master/donation-sub-categories/{id}` - Get sub-category by ID
- `POST /api/master/donation-sub-categories` - Create sub-category
- `PUT /api/master/donation-sub-categories/{id}` - Update sub-category
- `DELETE /api/master/donation-sub-categories/{id}` - Delete sub-category

### Serial Number Config
- `GET /api/master/serial-config` - List all configurations
- `GET /api/master/serial-config/{entityType}` - Get config by entity type
- `GET /api/master/serial-config/next/{entityType}` - Get next serial number
- `POST /api/master/serial-config` - Create configuration
- `PUT /api/master/serial-config/{entityType}` - Update configuration

---

## Notes

1. **Transaction Management**: Use `@Transactional` for operations that modify multiple tables
2. **Row Locking**: Use `FOR UPDATE` in serial number generation to prevent race conditions
3. **Soft Delete**: Consider implementing soft delete for branches and other critical entities
4. **Audit Trail**: All create/update operations should log user and timestamp
5. **Caching**: Consider caching frequently accessed master data (e.g., payment modes, active purposes)
6. **Validation**: Always validate foreign key references before operations
7. **Error Messages**: Provide clear, user-friendly error messages

---

**Document Version:** 1.0  
**Last Updated:** [Current Date]  
**Module:** Master Data  
**Status:** Ready for Implementation


