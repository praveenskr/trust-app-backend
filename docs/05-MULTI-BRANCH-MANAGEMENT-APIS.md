# Multi-Branch Management APIs - Technical Documentation

## Overview

This document provides comprehensive API documentation for Multi-Branch Management in the Trust Management System. The Multi-Branch Management module allows organizations to manage multiple branches, control user access to branches, track branch-specific transactions, and manage inter-branch transfers. The system enforces data isolation where users can only access data for branches they have been granted access to, except for super users who have access to all branches.

---

## Table of Contents

1. [Database Schema](#database-schema)
2. [Branch Management APIs](#branch-management-apis)
3. [User Branch Access APIs](#user-branch-access-apis)
4. [Branch Statistics & Reporting](#branch-statistics--reporting)
5. [Inter-Branch Transfers](#inter-branch-transfers)
6. [Request/Response Formats](#requestresponse-formats)
7. [Validation Rules](#validation-rules)
8. [Access Control & Security](#access-control--security)
9. [Error Handling](#error-handling)
10. [Examples](#examples)

---

## Database Schema

### Branches Table

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
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_city (city),
    INDEX idx_state (state),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### User Branch Access Table

```sql
CREATE TABLE user_branch_access (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_branch (user_id, branch_id),
    INDEX idx_user_id (user_id),
    INDEX idx_branch_id (branch_id),
    INDEX idx_granted_at (granted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Inter-Branch Transfers Table

```sql
CREATE TABLE inter_branch_transfers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transfer_number VARCHAR(100) UNIQUE,
    from_branch_id BIGINT NOT NULL,
    to_branch_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transfer_date DATE NOT NULL,
    payment_mode_id BIGINT NOT NULL,
    reference_number VARCHAR(100),
    description TEXT,
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (from_branch_id) REFERENCES branches(id) ON DELETE RESTRICT,
    FOREIGN KEY (to_branch_id) REFERENCES branches(id) ON DELETE RESTRICT,
    FOREIGN KEY (payment_mode_id) REFERENCES payment_modes(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_transfer_number (transfer_number),
    INDEX idx_from_branch (from_branch_id),
    INDEX idx_to_branch (to_branch_id),
    INDEX idx_transfer_date (transfer_date),
    INDEX idx_status (status),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Table Fields Description

#### Branches Table

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for branch |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE | Unique branch code (e.g., "BR001") |
| `name` | VARCHAR(255) | NOT NULL | Branch name |
| `address` | TEXT | NULLABLE | Complete branch address |
| `city` | VARCHAR(100) | NULLABLE | City name |
| `state` | VARCHAR(100) | NULLABLE | State name |
| `pincode` | VARCHAR(10) | NULLABLE | Postal/ZIP code |
| `phone` | VARCHAR(20) | NULLABLE | Contact phone number |
| `email` | VARCHAR(255) | NULLABLE | Contact email address |
| `contact_person` | VARCHAR(255) | NULLABLE | Primary contact person name |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Record last update timestamp |
| `created_by` | BIGINT | NULLABLE, FK | User ID who created the record |
| `updated_by` | BIGINT | NULLABLE, FK | User ID who last updated the record |
| `deleted_at` | TIMESTAMP | NULLABLE | Soft delete timestamp |
| `deleted_by` | BIGINT | NULLABLE, FK | User ID who deleted the record |

#### User Branch Access Table

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `user_id` | BIGINT | NOT NULL, FK | User ID |
| `branch_id` | BIGINT | NOT NULL, FK | Branch ID |
| `granted_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | When access was granted |
| `granted_by` | BIGINT | NULLABLE, FK | User ID who granted access |

#### Inter-Branch Transfers Table

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `transfer_number` | VARCHAR(100) | UNIQUE | Auto-generated transfer number |
| `from_branch_id` | BIGINT | NOT NULL, FK | Source branch ID |
| `to_branch_id` | BIGINT | NOT NULL, FK | Destination branch ID |
| `amount` | DECIMAL(15, 2) | NOT NULL | Transfer amount |
| `transfer_date` | DATE | NOT NULL | Date of transfer |
| `payment_mode_id` | BIGINT | NOT NULL, FK | Payment mode (Cash, Bank Transfer, etc.) |
| `reference_number` | VARCHAR(100) | NULLABLE | External reference number |
| `description` | TEXT | NULLABLE | Transfer description |
| `status` | ENUM | DEFAULT 'PENDING' | Transfer status: PENDING, COMPLETED, CANCELLED |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Record last update timestamp |
| `created_by` | BIGINT | NULLABLE, FK | User ID who created the record |
| `updated_by` | BIGINT | NULLABLE, FK | User ID who last updated the record |

---

## Branch Management APIs

### Base URL
```
/api/branches
```

### 1. Create Branch

**Endpoint**: `POST /api/branches`

**Description**: Creates a new branch record. Only super users can create branches.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "code": "BR001",
  "name": "Main Branch",
  "address": "123 Temple Street, Downtown",
  "city": "Bangalore",
  "state": "Karnataka",
  "pincode": "560001",
  "phone": "+91-80-12345678",
  "email": "main@trustapp.com",
  "contactPerson": "John Doe",
  "isActive": true
}
```

**Request Parameters** (Query):
- `createdBy` (optional, Long): User ID creating the record. Defaults to authenticated user.

**Response**: `201 Created`

```json
{
  "status": "success",
  "message": "Branch created successfully",
  "data": {
    "id": 1,
    "code": "BR001",
    "name": "Main Branch",
    "address": "123 Temple Street, Downtown",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "phone": "+91-80-12345678",
    "email": "main@trustapp.com",
    "contactPerson": "John Doe",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "createdBy": {
      "id": 1,
      "username": "admin",
      "email": "admin@trustapp.com"
    }
  }
}
```

**Error Responses**:

- `400 Bad Request` - Validation errors
- `403 Forbidden` - Insufficient permissions (not a super user)
- `409 Conflict` - Duplicate branch code

```json
{
  "status": "error",
  "message": "Branch code already exists: BR001",
  "errorCode": "DUPLICATE_RESOURCE"
}
```

---

### 2. Get All Branches

**Endpoint**: `GET /api/branches`

**Description**: Retrieves a list of branches. Regular users see only branches they have access to. Super users see all branches.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `includeInactive` | Boolean | No | false | Include soft-deleted branches |
| `city` | String | No | null | Filter by city |
| `state` | String | No | null | Filter by state |
| `search` | String | No | null | Search by name or code (partial match) |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Page size |
| `sortBy` | String | No | "name" | Sort field (name, code, city, createdAt) |
| `sortDir` | String | No | "ASC" | Sort direction (ASC, DESC) |

**Example Request**:
```
GET /api/branches?city=Bangalore&includeInactive=false&page=0&size=20
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "code": "BR001",
        "name": "Main Branch",
        "address": "123 Temple Street, Downtown",
        "city": "Bangalore",
        "state": "Karnataka",
        "pincode": "560001",
        "phone": "+91-80-12345678",
        "email": "main@trustapp.com",
        "contactPerson": "John Doe",
        "isActive": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      },
      {
        "id": 2,
        "code": "BR002",
        "name": "North Branch",
        "address": "456 North Avenue",
        "city": "Bangalore",
        "state": "Karnataka",
        "pincode": "560002",
        "phone": "+91-80-87654321",
        "email": "north@trustapp.com",
        "contactPerson": "Jane Smith",
        "isActive": true,
        "createdAt": "2024-01-20T14:20:00",
        "updatedAt": "2024-01-20T14:20:00"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0,
    "first": true,
    "last": true,
    "numberOfElements": 5
  }
}
```

---

### 3. Get Branch by ID

**Endpoint**: `GET /api/branches/{id}`

**Description**: Retrieves a specific branch by its ID. User must have access to this branch (or be a super user).

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Branch ID

**Example Request**:
```
GET /api/branches/1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "code": "BR001",
    "name": "Main Branch",
    "address": "123 Temple Street, Downtown",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "phone": "+91-80-12345678",
    "email": "main@trustapp.com",
    "contactPerson": "John Doe",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "createdBy": {
      "id": 1,
      "username": "admin",
      "email": "admin@trustapp.com"
    },
    "updatedBy": {
      "id": 1,
      "username": "admin",
      "email": "admin@trustapp.com"
    }
  }
}
```

**Error Responses**:

- `403 Forbidden` - User does not have access to this branch
```json
{
  "status": "error",
  "message": "Access denied. You do not have permission to access this branch.",
  "errorCode": "FORBIDDEN"
}
```

- `404 Not Found` - Branch not found

---

### 4. Update Branch

**Endpoint**: `PUT /api/branches/{id}`

**Description**: Updates an existing branch. Only super users can update branches. Note: Branch code cannot be updated after creation.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Branch ID

**Request Body**:
```json
{
  "name": "Main Branch - Updated",
  "address": "123 Temple Street, Downtown, Updated Address",
  "city": "Bangalore",
  "state": "Karnataka",
  "pincode": "560001",
  "phone": "+91-80-12345679",
  "email": "main.updated@trustapp.com",
  "contactPerson": "John Doe Updated",
  "isActive": true
}
```

**Request Parameters** (Query):
- `updatedBy` (optional, Long): User ID updating the record. Defaults to authenticated user.

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Branch updated successfully",
  "data": {
    "id": 1,
    "code": "BR001",
    "name": "Main Branch - Updated",
    "address": "123 Temple Street, Downtown, Updated Address",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "phone": "+91-80-12345679",
    "email": "main.updated@trustapp.com",
    "contactPerson": "John Doe Updated",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-16T11:45:00",
    "updatedBy": {
      "id": 1,
      "username": "admin"
    }
  }
}
```

**Error Responses**:

- `400 Bad Request` - Validation errors
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Branch not found
- `409 Conflict` - Cannot update branch with active transactions

```json
{
  "status": "error",
  "message": "Cannot update branch. Branch has active donations, expenses, or events. Please deactivate them first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 5. Delete Branch (Soft Delete)

**Endpoint**: `DELETE /api/branches/{id}`

**Description**: Soft deletes a branch by setting `is_active = false` and `deleted_at` timestamp. Only super users can delete branches. Branch must not have active transactions.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Branch ID

**Request Parameters** (Query):
- `deletedBy` (optional, Long): User ID deleting the record. Defaults to authenticated user.

**Example Request**:
```
DELETE /api/branches/1?deletedBy=1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Branch deleted successfully"
}
```

**Error Responses**:

- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Branch not found
- `409 Conflict` - Cannot delete branch with active transactions

```json
{
  "status": "error",
  "message": "Cannot delete branch. Branch has active donations, expenses, events, or users. Please remove associations first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 6. Get Branch Statistics

**Endpoint**: `GET /api/branches/{id}/statistics`

**Description**: Retrieves comprehensive statistics for a specific branch including financial summaries, transaction counts, and user counts.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Branch ID

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter statistics from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter statistics to this date |

**Example Request**:
```
GET /api/branches/1/statistics?fromDate=2024-01-01&toDate=2024-01-31
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch"
    },
    "donations": {
      "totalCount": 500,
      "totalAmount": 2000000.00,
      "averageAmount": 4000.00,
      "minAmount": 100.00,
      "maxAmount": 100000.00,
      "byPaymentMode": [
        {
          "paymentMode": "Cash",
          "count": 200,
          "totalAmount": 800000.00
        },
        {
          "paymentMode": "UPI",
          "count": 200,
          "totalAmount": 800000.00
        },
        {
          "paymentMode": "Bank Transfer",
          "count": 100,
          "totalAmount": 400000.00
        }
      ]
    },
    "expenses": {
      "totalCount": 150,
      "totalAmount": 800000.00,
      "averageAmount": 5333.33
    },
    "vouchers": {
      "totalCount": 50,
      "totalAmount": 250000.00,
      "averageAmount": 5000.00
    },
    "events": {
      "totalCount": 10,
      "activeCount": 2,
      "completedCount": 7,
      "plannedCount": 1
    },
    "users": {
      "totalCount": 15,
      "activeCount": 12,
      "inactiveCount": 3
    },
    "financialSummary": {
      "totalIncome": 2000000.00,
      "totalExpenses": 1050000.00,
      "netAmount": 950000.00,
      "profitMargin": 47.50
    },
    "interBranchTransfers": {
      "totalIncoming": 200000.00,
      "totalOutgoing": 150000.00,
      "netTransfer": 50000.00
    }
  }
}
```

---

## User Branch Access APIs

### Base URL
```
/api/users/{userId}/branches
```

### 1. Get User Branch Access

**Endpoint**: `GET /api/users/{userId}/branches`

**Description**: Retrieves all branches a user has access to.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `userId` (Long, required): User ID

**Example Request**:
```
GET /api/users/2/branches
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "branchId": 1,
      "branchName": "Main Branch",
      "branchCode": "BR001",
      "grantedAt": "2024-01-10T10:00:00"
    },
    {
      "id": 2,
      "userId": 2,
      "branchId": 2,
      "branchName": "North Branch",
      "branchCode": "BR002",
      "grantedAt": "2024-01-10T10:00:00"
    }
  ]
}
```

**Error Responses**:

- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - User not found

---

### 2. Assign Branches to User

**Endpoint**: `PUT /api/users/{userId}/branches`

**Description**: Assigns branches to a user. This replaces all existing branch access. Only super users can assign branch access.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `userId` (Long, required): User ID

**Request Body**:
```json
[1, 2, 3]
```

**Request Parameters** (Query):
- `grantedBy` (optional, Long): User ID granting access. Defaults to authenticated user.

**Example Request**:
```
PUT /api/users/2/branches?grantedBy=1
Content-Type: application/json

[1, 2, 3]
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Branches assigned successfully",
  "data": [
    {
      "id": 1,
      "userId": 2,
      "branchId": 1,
      "branchName": "Main Branch",
      "branchCode": "BR001",
      "grantedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 2,
      "userId": 2,
      "branchId": 2,
      "branchName": "North Branch",
      "branchCode": "BR002",
      "grantedAt": "2024-01-15T10:30:00"
    },
    {
      "id": 3,
      "userId": 2,
      "branchId": 3,
      "branchName": "South Branch",
      "branchCode": "BR003",
      "grantedAt": "2024-01-15T10:30:00"
    }
  ]
}
```

**Error Responses**:

- `400 Bad Request` - Invalid branch IDs
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - User or branch not found

---

### 3. Remove All Branch Access from User

**Endpoint**: `DELETE /api/users/{userId}/branches`

**Description**: Removes all branch access from a user. Only super users can remove branch access.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `userId` (Long, required): User ID

**Example Request**:
```
DELETE /api/users/2/branches
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "All branch access removed successfully"
}
```

**Error Responses**:

- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - User not found

---

### 4. Check User Branch Access

**Endpoint**: `GET /api/users/{userId}/branches/{branchId}/check`

**Description**: Checks if a user has access to a specific branch.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `userId` (Long, required): User ID
- `branchId` (Long, required): Branch ID

**Example Request**:
```
GET /api/users/2/branches/1/check
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "hasAccess": true,
    "userId": 2,
    "branchId": 1,
    "branchName": "Main Branch",
    "branchCode": "BR001",
    "grantedAt": "2024-01-10T10:00:00"
  }
}
```

**Response** (No Access): `200 OK`

```json
{
  "status": "success",
  "data": {
    "hasAccess": false,
    "userId": 2,
    "branchId": 1
  }
}
```

---

## Inter-Branch Transfers

### Base URL
```
/api/branches/transfers
```

### 1. Create Inter-Branch Transfer

**Endpoint**: `POST /api/branches/transfers`

**Description**: Creates a new inter-branch money transfer record. User must have access to the source branch.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "fromBranchId": 1,
  "toBranchId": 2,
  "amount": 50000.00,
  "transferDate": "2024-01-20",
  "paymentModeId": 3,
  "referenceNumber": "TXN123456789",
  "description": "Monthly fund transfer to North Branch",
  "status": "PENDING"
}
```

**Request Parameters** (Query):
- `createdBy` (optional, Long): User ID creating the record. Defaults to authenticated user.

**Response**: `201 Created`

```json
{
  "status": "success",
  "message": "Inter-branch transfer created successfully",
  "data": {
    "id": 1,
    "transferNumber": "TRF-2024-00001",
    "fromBranch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch"
    },
    "toBranch": {
      "id": 2,
      "code": "BR002",
      "name": "North Branch"
    },
    "amount": 50000.00,
    "transferDate": "2024-01-20",
    "paymentMode": {
      "id": 3,
      "code": "BANK_TRANSFER",
      "name": "Bank Transfer"
    },
    "referenceNumber": "TXN123456789",
    "description": "Monthly fund transfer to North Branch",
    "status": "PENDING",
    "isActive": true,
    "createdAt": "2024-01-20T10:30:00",
    "createdBy": {
      "id": 1,
      "username": "admin"
    }
  }
}
```

**Error Responses**:

- `400 Bad Request` - Validation errors (e.g., same branch for from/to)
- `403 Forbidden` - User does not have access to source branch
- `404 Not Found` - Branch or payment mode not found

---

### 2. Get Inter-Branch Transfers

**Endpoint**: `GET /api/branches/transfers`

**Description**: Retrieves inter-branch transfers with filtering. Users see only transfers involving branches they have access to.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `fromBranchId` | Long | No | null | Filter by source branch |
| `toBranchId` | Long | No | null | Filter by destination branch |
| `status` | String | No | null | Filter by status (PENDING, COMPLETED, CANCELLED) |
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter transfers from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter transfers to this date |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Page size |
| `sortBy` | String | No | "transferDate" | Sort field (transferDate, amount, createdAt) |
| `sortDir` | String | No | "DESC" | Sort direction (ASC, DESC) |

**Example Request**:
```
GET /api/branches/transfers?fromBranchId=1&status=PENDING&page=0&size=20
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "transferNumber": "TRF-2024-00001",
        "fromBranch": {
          "id": 1,
          "code": "BR001",
          "name": "Main Branch"
        },
        "toBranch": {
          "id": 2,
          "code": "BR002",
          "name": "North Branch"
        },
        "amount": 50000.00,
        "transferDate": "2024-01-20",
        "paymentMode": {
          "id": 3,
          "code": "BANK_TRANSFER",
          "name": "Bank Transfer"
        },
        "status": "PENDING",
        "createdAt": "2024-01-20T10:30:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "size": 20,
    "number": 0
  }
}
```

---

### 3. Update Transfer Status

**Endpoint**: `PATCH /api/branches/transfers/{id}/status`

**Description**: Updates the status of an inter-branch transfer.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Transfer ID

**Request Body**:
```json
{
  "status": "COMPLETED",
  "referenceNumber": "TXN123456789"
}
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Transfer status updated successfully",
  "data": {
    "id": 1,
    "transferNumber": "TRF-2024-00001",
    "status": "COMPLETED",
    "updatedAt": "2024-01-20T14:00:00"
  }
}
```

---

## Request/Response Formats

### BranchCreateDTO

```json
{
  "code": "string (required, max 50, unique, alphanumeric)",
  "name": "string (required, max 255)",
  "address": "string (optional, max 5000)",
  "city": "string (optional, max 100)",
  "state": "string (optional, max 100)",
  "pincode": "string (optional, max 10)",
  "phone": "string (optional, max 20)",
  "email": "string (optional, valid email format, max 255)",
  "contactPerson": "string (optional, max 255)",
  "isActive": "boolean (optional, default: true)"
}
```

### BranchUpdateDTO

```json
{
  "name": "string (optional, max 255)",
  "address": "string (optional, max 5000)",
  "city": "string (optional, max 100)",
  "state": "string (optional, max 100)",
  "pincode": "string (optional, max 10)",
  "phone": "string (optional, max 20)",
  "email": "string (optional, valid email format, max 255)",
  "contactPerson": "string (optional, max 255)",
  "isActive": "boolean (optional)"
}
```

**Note**: Branch `code` cannot be updated after creation.

### BranchDTO (Response)

```json
{
  "id": "long",
  "code": "string (unique, immutable)",
  "name": "string",
  "address": "string",
  "city": "string",
  "state": "string",
  "pincode": "string",
  "phone": "string",
  "email": "string",
  "contactPerson": "string",
  "isActive": "boolean",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "createdBy": {
    "id": "long",
    "username": "string",
    "email": "string (optional)"
  },
  "updatedBy": {
    "id": "long",
    "username": "string",
    "email": "string (optional)"
  }
}
```

### BranchAccessDTO

```json
{
  "id": "long",
  "userId": "long",
  "branchId": "long",
  "branchName": "string",
  "branchCode": "string",
  "grantedAt": "timestamp"
}
```

### InterBranchTransferCreateDTO

```json
{
  "fromBranchId": "long (required, must exist in branches)",
  "toBranchId": "long (required, must exist in branches, must be different from fromBranchId)",
  "amount": "decimal (required, > 0, max 15 digits, 2 decimal places)",
  "transferDate": "date (required, format: YYYY-MM-DD)",
  "paymentModeId": "long (required, must exist in payment_modes)",
  "referenceNumber": "string (optional, max 100)",
  "description": "string (optional, max 5000)",
  "status": "string (optional, enum: PENDING, COMPLETED, CANCELLED, default: PENDING)"
}
```

---

## Validation Rules

### Create Branch

1. **code**: Required, non-blank, maximum 50 characters, must be unique, alphanumeric with underscores
2. **name**: Required, non-blank, maximum 255 characters
3. **email**: Optional, if provided must be a valid email format
4. **phone**: Optional, if provided must be a valid phone number format
5. **pincode**: Optional, if provided must be a valid postal code format
6. **isActive**: Optional, defaults to true

### Update Branch

1. All validation rules from Create apply (for fields being updated)
2. Branch must exist and be active
3. Branch code cannot be updated (immutable)
4. Cannot update branch if it has active transactions (donations, expenses, events) - business rule

### Delete Branch

1. Branch must exist
2. Branch must not have active transactions (donations, expenses, events, users) - business rule

### Assign Branches to User

1. User must exist
2. All branch IDs must exist and be active
3. User cannot be assigned to the same branch twice (enforced by unique constraint)

### Inter-Branch Transfer

1. **fromBranchId**: Required, must exist in branches table and be active
2. **toBranchId**: Required, must exist in branches table, be active, and be different from fromBranchId
3. **amount**: Required, must be greater than 0, maximum 15 digits with 2 decimal places
4. **transferDate**: Required, must be a valid date in YYYY-MM-DD format
5. **paymentModeId**: Required, must exist in payment_modes table and be active
6. **referenceNumber**: Optional, maximum 100 characters
7. **description**: Optional, maximum 5000 characters
8. **status**: Optional, must be one of: PENDING, COMPLETED, CANCELLED. Defaults to PENDING

---

## Access Control & Security

### Role-Based Access

1. **Super User**: 
   - Can create, update, and delete branches
   - Can view all branches regardless of access
   - Can assign branch access to any user
   - Can view all inter-branch transfers

2. **Manager/Accountant**:
   - Can view only branches they have access to
   - Cannot create, update, or delete branches
   - Cannot assign branch access to users
   - Can view inter-branch transfers involving their accessible branches

### Data Isolation

1. **Transaction Isolation**: Users can only view/create transactions (donations, expenses, vouchers) for branches they have access to
2. **Event Isolation**: Users can only view/create events for branches they have access to (or global events)
3. **Report Isolation**: Reports are filtered by user's accessible branches
4. **Super User Override**: Super users bypass all branch access restrictions

### Security Rules

1. Branch access is checked on every API call that involves branch-specific data
2. Users cannot access branches they haven't been granted access to
3. Branch code is immutable after creation to maintain referential integrity
4. Soft delete is used for branches to maintain audit trail and referential integrity

---

## Error Handling

### Standard Error Response Format

```json
{
  "status": "error",
  "message": "Error message description",
  "errorCode": "ERROR_CODE",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ]
}
```

### Common Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `RESOURCE_NOT_FOUND` | 404 | Referenced resource not found |
| `DUPLICATE_RESOURCE` | 409 | Resource already exists (e.g., duplicate branch code) |
| `FORBIDDEN` | 403 | Insufficient permissions or branch access denied |
| `BRANCH_HAS_TRANSACTIONS` | 409 | Cannot delete/update branch with active transactions |
| `INVALID_BRANCH_TRANSFER` | 400 | Invalid inter-branch transfer (e.g., same branch) |
| `UNAUTHORIZED` | 401 | Authentication required |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

### Business Rules

1. **Branch Code Immutability**: Branch code cannot be changed after creation
2. **Transaction Association**: Branches with active transactions cannot be deleted
3. **User Access**: Users can only access data for branches they have been granted access to
4. **Super User Access**: Super users have access to all branches regardless of user_branch_access table
5. **Inter-Branch Transfer**: Source and destination branches must be different
6. **Transfer Number**: Auto-generated in format `TRF-YYYY-XXXXX` where YYYY is the year and XXXXX is a 5-digit sequential number

---

## Examples

### Example 1: Create a Branch

**Request**:
```bash
curl -X POST "http://localhost:8080/api/branches" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "BR001",
    "name": "Main Branch",
    "address": "123 Temple Street, Downtown",
    "city": "Bangalore",
    "state": "Karnataka",
    "pincode": "560001",
    "phone": "+91-80-12345678",
    "email": "main@trustapp.com",
    "contactPerson": "John Doe"
  }'
```

### Example 2: Assign Branches to User

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/users/2/branches?grantedBy=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '[1, 2, 3]'
```

### Example 3: Create Inter-Branch Transfer

**Request**:
```bash
curl -X POST "http://localhost:8080/api/branches/transfers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "fromBranchId": 1,
    "toBranchId": 2,
    "amount": 50000.00,
    "transferDate": "2024-01-20",
    "paymentModeId": 3,
    "referenceNumber": "TXN123456789",
    "description": "Monthly fund transfer"
  }'
```

### Example 4: Get Branch Statistics

**Request**:
```bash
curl -X GET "http://localhost:8080/api/branches/1/statistics?fromDate=2024-01-01&toDate=2024-01-31" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 5: Check User Branch Access

**Request**:
```bash
curl -X GET "http://localhost:8080/api/users/2/branches/1/check" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Notes

1. **Branch Code Format**: Branch codes should follow a consistent naming convention (e.g., `BR001`, `BR002`) for easy identification.

2. **Data Isolation**: The system enforces strict data isolation where users can only access data for branches they have been granted access to. This is enforced at the service/security layer.

3. **Super User Override**: Super users have access to all branches and can bypass branch access restrictions. This is checked at the security layer.

4. **Soft Delete**: Deleted branches are marked as inactive but remain in the database for audit purposes and to maintain referential integrity with historical transactions.

5. **Inter-Branch Transfers**: These transfers are tracked separately and can be used for financial reporting and reconciliation. The system can automatically create cash book entries for these transfers.

6. **Branch Access Management**: Branch access is managed through the `user_branch_access` table. Users without any branch access cannot access any branch-specific data (except super users).

7. **Transfer Number**: Inter-branch transfer numbers are auto-generated in the format `TRF-YYYY-XXXXX` where YYYY is the year and XXXXX is a 5-digit sequential number resetting each year.

---

## Related Documentation

- [Master Data Implementation](./01-MASTER-DATA-IMPLEMENTATION.md)
- [User Management](./02-USER-MANAGEMENT.md)
- [Donation Transaction APIs](./03-DONATION-TRANSACTION-APIS.md)
- [Event Management APIs](./04-EVENT-MANAGEMENT-APIS.md)
- [API Authentication Guide](../README.md#authentication)

