# Donation Transaction APIs - Technical Documentation

## Overview

This document provides comprehensive API documentation for Donation Transaction CRUD operations in the Trust Management System. The donation transaction module allows users to record, retrieve, update, and manage donation transactions with donor details, payment modes, purposes, and event associations.

---

## Table of Contents

1. [Database Schema](#database-schema)
2. [API Endpoints](#api-endpoints)
3. [Request/Response Formats](#requestresponse-formats)
4. [Validation Rules](#validation-rules)
5. [Error Handling](#error-handling)
6. [Examples](#examples)

---

## Database Schema

### Donations Table

```sql
CREATE TABLE donations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receipt_number VARCHAR(100) UNIQUE,
    donor_name VARCHAR(255) NOT NULL,
    donor_address TEXT,
    pan_number VARCHAR(10),
    donor_phone VARCHAR(20),
    donor_email VARCHAR(255),
    amount DECIMAL(15, 2) NOT NULL,
    payment_mode_id BIGINT NOT NULL,
    purpose_id BIGINT NOT NULL,
    sub_category_id BIGINT,
    event_id BIGINT,
    branch_id BIGINT NOT NULL,
    donation_date DATE NOT NULL,
    notes TEXT,
    receipt_generated BOOLEAN DEFAULT FALSE,
    receipt_generated_at TIMESTAMP NULL,
    receipt_file_path VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    FOREIGN KEY (payment_mode_id) REFERENCES payment_modes(id) ON DELETE RESTRICT,
    FOREIGN KEY (purpose_id) REFERENCES donation_purposes(id) ON DELETE RESTRICT,
    FOREIGN KEY (sub_category_id) REFERENCES donation_sub_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_receipt_number (receipt_number),
    INDEX idx_donor_name (donor_name),
    INDEX idx_pan_number (pan_number),
    INDEX idx_donation_date (donation_date),
    INDEX idx_branch_id (branch_id),
    INDEX idx_purpose_id (purpose_id),
    INDEX idx_event_id (event_id),
    INDEX idx_payment_mode_id (payment_mode_id),
    INDEX idx_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Table Fields Description

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for donation record |
| `receipt_number` | VARCHAR(100) | UNIQUE | Auto-generated receipt number (format: YYYY-XXXXX) |
| `donor_name` | VARCHAR(255) | NOT NULL | Name of the donor |
| `donor_address` | TEXT | NULLABLE | Complete address of the donor |
| `pan_number` | VARCHAR(10) | NULLABLE | PAN card number (10 characters, alphanumeric) |
| `donor_phone` | VARCHAR(20) | NULLABLE | Contact phone number |
| `donor_email` | VARCHAR(255) | NULLABLE | Email address |
| `amount` | DECIMAL(15, 2) | NOT NULL | Donation amount (max 15 digits, 2 decimal places) |
| `payment_mode_id` | BIGINT | NOT NULL, FK | Reference to payment_modes table |
| `purpose_id` | BIGINT | NOT NULL, FK | Reference to donation_purposes table |
| `sub_category_id` | BIGINT | NULLABLE, FK | Reference to donation_sub_categories table |
| `event_id` | BIGINT | NULLABLE, FK | Reference to events table (if donation is event-specific) |
| `branch_id` | BIGINT | NOT NULL, FK | Reference to branches table |
| `donation_date` | DATE | NOT NULL | Date of donation |
| `notes` | TEXT | NULLABLE | Additional notes or remarks |
| `receipt_generated` | BOOLEAN | DEFAULT FALSE | Flag indicating if receipt has been generated |
| `receipt_generated_at` | TIMESTAMP | NULLABLE | Timestamp when receipt was generated |
| `receipt_file_path` | VARCHAR(500) | NULLABLE | File path to generated receipt PDF |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Record last update timestamp |
| `created_by` | BIGINT | NULLABLE, FK | User ID who created the record |
| `updated_by` | BIGINT | NULLABLE, FK | User ID who last updated the record |
| `deleted_at` | TIMESTAMP | NULLABLE | Soft delete timestamp |
| `deleted_by` | BIGINT | NULLABLE, FK | User ID who deleted the record |

### Indexes

- **Primary Key**: `id`
- **Unique Index**: `receipt_number`
- **Foreign Key Indexes**: `payment_mode_id`, `purpose_id`, `sub_category_id`, `event_id`, `branch_id`
- **Search Indexes**: `donor_name`, `pan_number`, `donation_date`, `created_at`
- **Filter Index**: `is_active`

---

## API Endpoints

### Base URL
```
/api/donations
```

### 1. Create Donation Transaction

**Endpoint**: `POST /api/donations`

**Description**: Creates a new donation transaction record.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "donorName": "John Doe",
  "donorAddress": "123 Main Street, City, State, 123456",
  "panNumber": "ABCDE1234F",
  "donorPhone": "+91-9876543210",
  "donorEmail": "john.doe@example.com",
  "amount": 5000.00,
  "paymentModeId": 1,
  "purposeId": 2,
  "subCategoryId": 5,
  "eventId": 3,
  "branchId": 1,
  "donationDate": "2024-01-15",
  "notes": "Monthly donation for temple maintenance"
}
```

**Request Parameters** (Query):
- `createdBy` (optional, Long): User ID creating the record. Defaults to authenticated user.

**Response**: `201 Created`

```json
{
  "status": "success",
  "message": "Donation transaction created successfully",
  "data": {
    "id": 1,
    "receiptNumber": "2024-00001",
    "donorName": "John Doe",
    "donorAddress": "123 Main Street, City, State, 123456",
    "panNumber": "ABCDE1234F",
    "donorPhone": "+91-9876543210",
    "donorEmail": "john.doe@example.com",
    "amount": 5000.00,
    "paymentMode": {
      "id": 1,
      "code": "CASH",
      "name": "Cash"
    },
    "purpose": {
      "id": 2,
      "code": "TEMPLE_MAINT",
      "name": "Temple Maintenance"
    },
    "subCategory": {
      "id": 5,
      "code": "MONTHLY",
      "name": "Monthly Donation"
    },
    "event": {
      "id": 3,
      "code": "FESTIVAL_2024",
      "name": "Annual Festival 2024"
    },
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch"
    },
    "donationDate": "2024-01-15",
    "notes": "Monthly donation for temple maintenance",
    "receiptGenerated": false,
    "receiptGeneratedAt": null,
    "receiptFilePath": null,
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "createdBy": {
      "id": 1,
      "username": "admin"
    }
  }
}
```

**Error Responses**:

- `400 Bad Request` - Validation errors
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    {
      "field": "donorName",
      "message": "Donor name is required"
    },
    {
      "field": "amount",
      "message": "Amount must be greater than 0"
    }
  ]
}
```

- `404 Not Found` - Referenced resource not found
```json
{
  "status": "error",
  "message": "Payment mode not found with id: 99",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

- `409 Conflict` - Duplicate receipt number (if manual entry)
```json
{
  "status": "error",
  "message": "Receipt number already exists: 2024-00001",
  "errorCode": "DUPLICATE_RESOURCE"
}
```

---

### 2. Get All Donation Transactions

**Endpoint**: `GET /api/donations`

**Description**: Retrieves a list of donation transactions with optional filtering and pagination.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `branchId` | Long | No | null | Filter by branch ID |
| `purposeId` | Long | No | null | Filter by donation purpose |
| `eventId` | Long | No | null | Filter by event |
| `paymentModeId` | Long | No | null | Filter by payment mode |
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter donations from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter donations to this date |
| `donorName` | String | No | null | Search by donor name (partial match) |
| `panNumber` | String | No | null | Search by PAN number |
| `receiptNumber` | String | No | null | Search by receipt number |
| `includeInactive` | Boolean | No | false | Include soft-deleted records |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Page size |
| `sortBy` | String | No | "donationDate" | Sort field (donationDate, amount, createdAt) |
| `sortDir` | String | No | "DESC" | Sort direction (ASC, DESC) |

**Example Request**:
```
GET /api/donations?branchId=1&fromDate=2024-01-01&toDate=2024-01-31&page=0&size=20&sortBy=donationDate&sortDir=DESC
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "receiptNumber": "2024-00001",
        "donorName": "John Doe",
        "donorAddress": "123 Main Street, City, State, 123456",
        "panNumber": "ABCDE1234F",
        "donorPhone": "+91-9876543210",
        "donorEmail": "john.doe@example.com",
        "amount": 5000.00,
        "paymentMode": {
          "id": 1,
          "code": "CASH",
          "name": "Cash"
        },
        "purpose": {
          "id": 2,
          "code": "TEMPLE_MAINT",
          "name": "Temple Maintenance"
        },
        "subCategory": {
          "id": 5,
          "code": "MONTHLY",
          "name": "Monthly Donation"
        },
        "event": {
          "id": 3,
          "code": "FESTIVAL_2024",
          "name": "Annual Festival 2024"
        },
        "branch": {
          "id": 1,
          "code": "BR001",
          "name": "Main Branch"
        },
        "donationDate": "2024-01-15",
        "notes": "Monthly donation for temple maintenance",
        "receiptGenerated": false,
        "receiptGeneratedAt": null,
        "receiptFilePath": null,
        "isActive": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 20
  }
}
```

---

### 3. Get Donation Transaction by ID

**Endpoint**: `GET /api/donations/{id}`

**Description**: Retrieves a specific donation transaction by its ID.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Donation transaction ID

**Example Request**:
```
GET /api/donations/1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "receiptNumber": "2024-00001",
    "donorName": "John Doe",
    "donorAddress": "123 Main Street, City, State, 123456",
    "panNumber": "ABCDE1234F",
    "donorPhone": "+91-9876543210",
    "donorEmail": "john.doe@example.com",
    "amount": 5000.00,
    "paymentMode": {
      "id": 1,
      "code": "CASH",
      "name": "Cash",
      "description": "Cash payment"
    },
    "purpose": {
      "id": 2,
      "code": "TEMPLE_MAINT",
      "name": "Temple Maintenance",
      "description": "Donations for temple maintenance"
    },
    "subCategory": {
      "id": 5,
      "code": "MONTHLY",
      "name": "Monthly Donation",
      "description": "Recurring monthly donations"
    },
    "event": {
      "id": 3,
      "code": "FESTIVAL_2024",
      "name": "Annual Festival 2024",
      "description": "Annual temple festival",
      "startDate": "2024-01-10",
      "endDate": "2024-01-20",
      "status": "ACTIVE"
    },
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch",
      "address": "123 Temple Street",
      "city": "City",
      "state": "State"
    },
    "donationDate": "2024-01-15",
    "notes": "Monthly donation for temple maintenance",
    "receiptGenerated": false,
    "receiptGeneratedAt": null,
    "receiptFilePath": null,
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

**Error Response**: `404 Not Found`

```json
{
  "status": "error",
  "message": "Donation transaction not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### 4. Update Donation Transaction

**Endpoint**: `PUT /api/donations/{id}`

**Description**: Updates an existing donation transaction. Only active (non-deleted) transactions can be updated.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Donation transaction ID

**Request Body**:
```json
{
  "donorName": "John Doe Updated",
  "donorAddress": "456 New Street, City, State, 123456",
  "panNumber": "ABCDE1234F",
  "donorPhone": "+91-9876543211",
  "donorEmail": "john.doe.updated@example.com",
  "amount": 6000.00,
  "paymentModeId": 2,
  "purposeId": 2,
  "subCategoryId": 5,
  "eventId": 3,
  "branchId": 1,
  "donationDate": "2024-01-16",
  "notes": "Updated monthly donation"
}
```

**Request Parameters** (Query):
- `updatedBy` (optional, Long): User ID updating the record. Defaults to authenticated user.

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Donation transaction updated successfully",
  "data": {
    "id": 1,
    "receiptNumber": "2024-00001",
    "donorName": "John Doe Updated",
    "donorAddress": "456 New Street, City, State, 123456",
    "panNumber": "ABCDE1234F",
    "donorPhone": "+91-9876543211",
    "donorEmail": "john.doe.updated@example.com",
    "amount": 6000.00,
    "paymentMode": {
      "id": 2,
      "code": "UPI",
      "name": "UPI"
    },
    "purpose": {
      "id": 2,
      "code": "TEMPLE_MAINT",
      "name": "Temple Maintenance"
    },
    "subCategory": {
      "id": 5,
      "code": "MONTHLY",
      "name": "Monthly Donation"
    },
    "event": {
      "id": 3,
      "code": "FESTIVAL_2024",
      "name": "Annual Festival 2024"
    },
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch"
    },
    "donationDate": "2024-01-16",
    "notes": "Updated monthly donation",
    "receiptGenerated": false,
    "receiptGeneratedAt": null,
    "receiptFilePath": null,
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
- `404 Not Found` - Donation transaction not found
- `409 Conflict` - Cannot update transaction with generated receipt (business rule)

```json
{
  "status": "error",
  "message": "Cannot update donation transaction with generated receipt. Please delete the receipt first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 5. Delete Donation Transaction (Soft Delete)

**Endpoint**: `DELETE /api/donations/{id}`

**Description**: Soft deletes a donation transaction by setting `is_active = false` and `deleted_at` timestamp. The record remains in the database for audit purposes.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Donation transaction ID

**Request Parameters** (Query):
- `deletedBy` (optional, Long): User ID deleting the record. Defaults to authenticated user.

**Example Request**:
```
DELETE /api/donations/1?deletedBy=1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Donation transaction deleted successfully"
}
```

**Error Responses**:

- `404 Not Found` - Donation transaction not found
```json
{
  "status": "error",
  "message": "Donation transaction not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

- `409 Conflict` - Cannot delete transaction with generated receipt
```json
{
  "status": "error",
  "message": "Cannot delete donation transaction with generated receipt. Please delete the receipt first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 6. Generate Receipt

**Endpoint**: `POST /api/donations/{id}/receipt`

**Description**: Generates a PDF receipt for a donation transaction. This endpoint creates the receipt file and updates the donation record with receipt information.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Donation transaction ID

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Receipt generated successfully",
  "data": {
    "receiptNumber": "2024-00001",
    "receiptFilePath": "/receipts/2024/2024-00001.pdf",
    "receiptGeneratedAt": "2024-01-15T12:00:00",
    "downloadUrl": "/api/donations/1/receipt/download"
  }
}
```

**Error Responses**:

- `404 Not Found` - Donation transaction not found
- `409 Conflict` - Receipt already generated
```json
{
  "status": "error",
  "message": "Receipt already generated for this donation transaction",
  "errorCode": "DUPLICATE_RESOURCE"
}
```

---

### 7. Download Receipt

**Endpoint**: `GET /api/donations/{id}/receipt/download`

**Description**: Downloads the generated PDF receipt for a donation transaction.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Donation transaction ID

**Response**: `200 OK`
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="receipt-2024-00001.pdf"`

**Error Responses**:

- `404 Not Found` - Donation transaction not found or receipt not generated
```json
{
  "status": "error",
  "message": "Receipt not found for this donation transaction",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### 8. Get Donation Summary/Statistics

**Endpoint**: `GET /api/donations/summary`

**Description**: Retrieves summary statistics for donations based on filters.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `branchId` | Long | No | null | Filter by branch ID |
| `purposeId` | Long | No | null | Filter by donation purpose |
| `eventId` | Long | No | null | Filter by event |
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter donations from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter donations to this date |
| `groupBy` | String | No | "none" | Group by: "purpose", "paymentMode", "event", "branch", "month" |

**Example Request**:
```
GET /api/donations/summary?branchId=1&fromDate=2024-01-01&toDate=2024-01-31&groupBy=purpose
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "totalAmount": 150000.00,
    "totalCount": 45,
    "averageAmount": 3333.33,
    "minAmount": 100.00,
    "maxAmount": 50000.00,
    "groupedData": [
      {
        "group": "Temple Maintenance",
        "count": 20,
        "totalAmount": 80000.00,
        "percentage": 53.33
      },
      {
        "group": "Education Fund",
        "count": 15,
        "totalAmount": 50000.00,
        "percentage": 33.33
      },
      {
        "group": "Charity",
        "count": 10,
        "totalAmount": 20000.00,
        "percentage": 13.33
      }
    ],
    "paymentModeBreakdown": [
      {
        "paymentMode": "Cash",
        "count": 10,
        "totalAmount": 30000.00
      },
      {
        "paymentMode": "UPI",
        "count": 15,
        "totalAmount": 50000.00
      },
      {
        "paymentMode": "Bank Transfer",
        "count": 20,
        "totalAmount": 70000.00
      }
    ]
  }
}
```

---

## Request/Response Formats

### DonationCreateDTO

```json
{
  "donorName": "string (required, max 255)",
  "donorAddress": "string (optional, max 5000)",
  "panNumber": "string (optional, 10 characters, alphanumeric)",
  "donorPhone": "string (optional, max 20)",
  "donorEmail": "string (optional, valid email format, max 255)",
  "amount": "decimal (required, > 0, max 15 digits, 2 decimal places)",
  "paymentModeId": "long (required, must exist in payment_modes)",
  "purposeId": "long (required, must exist in donation_purposes)",
  "subCategoryId": "long (optional, must exist in donation_sub_categories)",
  "eventId": "long (optional, must exist in events)",
  "branchId": "long (required, must exist in branches)",
  "donationDate": "date (required, format: YYYY-MM-DD)",
  "notes": "string (optional, max 5000)"
}
```

### DonationUpdateDTO

```json
{
  "donorName": "string (optional, max 255)",
  "donorAddress": "string (optional, max 5000)",
  "panNumber": "string (optional, 10 characters, alphanumeric)",
  "donorPhone": "string (optional, max 20)",
  "donorEmail": "string (optional, valid email format, max 255)",
  "amount": "decimal (optional, > 0, max 15 digits, 2 decimal places)",
  "paymentModeId": "long (optional, must exist in payment_modes)",
  "purposeId": "long (optional, must exist in donation_purposes)",
  "subCategoryId": "long (optional, must exist in donation_sub_categories, can be null)",
  "eventId": "long (optional, must exist in events, can be null)",
  "branchId": "long (optional, must exist in branches)",
  "donationDate": "string (optional, format: YYYY-MM-DD)",
  "notes": "string (optional, max 5000)"
}
```

### DonationDTO (Response)

```json
{
  "id": "long",
  "receiptNumber": "string (unique)",
  "donorName": "string",
  "donorAddress": "string",
  "panNumber": "string",
  "donorPhone": "string",
  "donorEmail": "string",
  "amount": "decimal",
  "paymentMode": {
    "id": "long",
    "code": "string",
    "name": "string",
    "description": "string (optional)"
  },
  "purpose": {
    "id": "long",
    "code": "string",
    "name": "string",
    "description": "string (optional)"
  },
  "subCategory": {
    "id": "long",
    "code": "string",
    "name": "string",
    "description": "string (optional)"
  },
  "event": {
    "id": "long",
    "code": "string",
    "name": "string",
    "description": "string (optional)",
    "startDate": "date",
    "endDate": "date",
    "status": "string"
  },
  "branch": {
    "id": "long",
    "code": "string",
    "name": "string",
    "address": "string (optional)",
    "city": "string (optional)",
    "state": "string (optional)"
  },
  "donationDate": "date",
  "notes": "string",
  "receiptGenerated": "boolean",
  "receiptGeneratedAt": "timestamp (nullable)",
  "receiptFilePath": "string (nullable)",
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

---

## Validation Rules

### Create Donation Transaction

1. **donorName**: Required, non-blank, maximum 255 characters
2. **amount**: Required, must be greater than 0, maximum 15 digits with 2 decimal places
3. **paymentModeId**: Required, must exist in `payment_modes` table and be active
4. **purposeId**: Required, must exist in `donation_purposes` table and be active
5. **subCategoryId**: Optional, if provided must exist in `donation_sub_categories` table, must belong to the specified `purposeId`, and be active
6. **eventId**: Optional, if provided must exist in `events` table and be active
7. **branchId**: Required, must exist in `branches` table and be active
8. **donationDate**: Required, must be a valid date in YYYY-MM-DD format, cannot be a future date (business rule)
9. **panNumber**: Optional, if provided must be exactly 10 characters, alphanumeric, uppercase
10. **donorEmail**: Optional, if provided must be a valid email format
11. **donorPhone**: Optional, if provided must be a valid phone number format
12. **notes**: Optional, maximum 5000 characters

### Update Donation Transaction

1. All validation rules from Create apply (for fields being updated)
2. Transaction must exist and be active
3. Transaction must not have a generated receipt (business rule - receipts cannot be modified)
4. If `subCategoryId` is being updated, it must belong to the specified `purposeId`

### Delete Donation Transaction

1. Transaction must exist
2. Transaction must not have a generated receipt (business rule - receipts cannot be deleted)

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
| `DUPLICATE_RESOURCE` | 409 | Resource already exists (e.g., duplicate receipt number) |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

### Business Rules

1. **Receipt Number Generation**: Auto-generated in format `YYYY-XXXXX` where YYYY is the year and XXXXX is a 5-digit sequential number resetting each year
2. **Receipt Modification**: Once a receipt is generated, the donation transaction cannot be updated or deleted
3. **Date Validation**: Donation date cannot be in the future
4. **Sub-Category Validation**: Sub-category must belong to the specified purpose
5. **Branch Access**: Users can only access donations for branches they have access to (enforced by security layer)

---

## Examples

### Example 1: Create a Cash Donation

**Request**:
```bash
curl -X POST "http://localhost:8080/api/donations" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "donorName": "Ramesh Kumar",
    "donorAddress": "456 Temple Road, Bangalore, Karnataka, 560001",
    "panNumber": "ABCDE5678G",
    "donorPhone": "+91-9876543210",
    "amount": 10000.00,
    "paymentModeId": 1,
    "purposeId": 2,
    "subCategoryId": 5,
    "branchId": 1,
    "donationDate": "2024-01-20",
    "notes": "Annual donation"
  }'
```

**Response**: `201 Created`
```json
{
  "status": "success",
  "message": "Donation transaction created successfully",
  "data": {
    "id": 2,
    "receiptNumber": "2024-00002",
    "donorName": "Ramesh Kumar",
    "amount": 10000.00,
    "paymentMode": {
      "id": 1,
      "code": "CASH",
      "name": "Cash"
    },
    "purpose": {
      "id": 2,
      "code": "TEMPLE_MAINT",
      "name": "Temple Maintenance"
    },
    "donationDate": "2024-01-20",
    "isActive": true
  }
}
```

### Example 2: Get Donations with Filters

**Request**:
```bash
curl -X GET "http://localhost:8080/api/donations?branchId=1&fromDate=2024-01-01&toDate=2024-01-31&page=0&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 3: Update Donation Amount

**Request**:
```bash
curl -X PUT "http://localhost:8080/api/donations/2" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "amount": 15000.00,
    "notes": "Updated amount - additional contribution"
  }'
```

### Example 4: Generate Receipt

**Request**:
```bash
curl -X POST "http://localhost:8080/api/donations/2/receipt" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response**: `200 OK`
```json
{
  "status": "success",
  "message": "Receipt generated successfully",
  "data": {
    "receiptNumber": "2024-00002",
    "receiptFilePath": "/receipts/2024/2024-00002.pdf",
    "receiptGeneratedAt": "2024-01-20T14:30:00",
    "downloadUrl": "/api/donations/2/receipt/download"
  }
}
```

### Example 5: Get Summary Statistics

**Request**:
```bash
curl -X GET "http://localhost:8080/api/donations/summary?branchId=1&fromDate=2024-01-01&toDate=2024-01-31&groupBy=purpose" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Notes

1. **Receipt Number Format**: The receipt number is auto-generated in the format `YYYY-XXXXX` where:
   - YYYY is the 4-digit year
   - XXXXX is a 5-digit sequential number (padded with zeros)
   - The sequence resets to 00001 at the start of each year
   - Receipt numbers are unique across all branches

2. **Soft Delete**: Deleted donations are marked as inactive but remain in the database for audit purposes. They can be retrieved by setting `includeInactive=true` in the query parameters.

3. **Branch Access Control**: Users can only access donations for branches they have been granted access to. This is enforced at the service/security layer.

4. **Receipt Generation**: Once a receipt is generated, the donation transaction becomes read-only (cannot be updated or deleted) to maintain data integrity and audit trail.

5. **Date Handling**: All dates are handled in ISO 8601 format (YYYY-MM-DD) for consistency.

6. **Amount Precision**: All monetary amounts are stored with 2 decimal places precision using DECIMAL(15,2) to avoid floating-point precision issues.

---

## Related Documentation

- [Master Data Implementation](./01-MASTER-DATA-IMPLEMENTATION.md)
- [User Management](./02-USER-MANAGEMENT.md)
- [API Authentication Guide](../README.md#authentication)

