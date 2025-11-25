# Event Management APIs - Technical Documentation

## Overview

This document provides comprehensive API documentation for Event Management in the Trust Management System. The Event Management module allows users to create, manage, and track events that can be associated with donations, expenses, and vouchers. Events have a lifecycle with statuses (PLANNED, ACTIVE, COMPLETED, CANCELLED) and can be branch-specific or global.

---

## Table of Contents

1. [Database Schema](#database-schema)
2. [API Endpoints](#api-endpoints)
3. [Request/Response Formats](#requestresponse-formats)
4. [Validation Rules](#validation-rules)
5. [Event Lifecycle Management](#event-lifecycle-management)
6. [Event Reporting & Statistics](#event-reporting--statistics)
7. [Error Handling](#error-handling)
8. [Examples](#examples)

---

## Database Schema

### Events Table

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
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT NULL,
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_code (code),
    INDEX idx_branch_id (branch_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date),
    INDEX idx_active (is_active),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Table Fields Description

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for event record |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE | Unique event code (e.g., "FESTIVAL_2024") |
| `name` | VARCHAR(255) | NOT NULL | Event name |
| `description` | TEXT | NULLABLE | Detailed event description |
| `start_date` | DATE | NOT NULL | Event start date |
| `end_date` | DATE | NULLABLE | Event end date (can be same as start_date for single-day events) |
| `status` | ENUM | DEFAULT 'PLANNED' | Event status: PLANNED, ACTIVE, COMPLETED, CANCELLED |
| `branch_id` | BIGINT | NULLABLE, FK | Reference to branches table (NULL for global events) |
| `is_active` | BOOLEAN | DEFAULT TRUE | Soft delete flag |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMP | AUTO UPDATE | Record last update timestamp |
| `created_by` | BIGINT | NULLABLE, FK | User ID who created the record |
| `updated_by` | BIGINT | NULLABLE, FK | User ID who last updated the record |
| `deleted_at` | TIMESTAMP | NULLABLE | Soft delete timestamp |
| `deleted_by` | BIGINT | NULLABLE, FK | User ID who deleted the record |

### Indexes

- **Primary Key**: `id`
- **Unique Index**: `code`
- **Foreign Key Indexes**: `branch_id`
- **Search Indexes**: `status`, `start_date`, `end_date`
- **Filter Index**: `is_active`

### Related Tables

Events are referenced by:
- `donations` table (event_id)
- `expenses` table (event_id)
- `vouchers` table (event_id)

---

## API Endpoints

### Base URL
```
/api/master/events
```

### 1. Create Event

**Endpoint**: `POST /api/master/events`

**Description**: Creates a new event record.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Request Body**:
```json
{
  "code": "FESTIVAL_2024",
  "name": "Annual Temple Festival 2024",
  "description": "Annual temple festival with cultural programs, food stalls, and religious ceremonies",
  "startDate": "2024-01-10",
  "endDate": "2024-01-20",
  "status": "PLANNED",
  "branchId": 1,
  "isActive": true
}
```

**Request Parameters** (Query):
- `createdBy` (optional, Long): User ID creating the record. Defaults to authenticated user.

**Response**: `201 Created`

```json
{
  "status": "success",
  "message": "Event created successfully",
  "data": {
    "id": 1,
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024",
    "description": "Annual temple festival with cultural programs, food stalls, and religious ceremonies",
    "startDate": "2024-01-10",
    "endDate": "2024-01-20",
    "status": "PLANNED",
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch",
      "address": "123 Temple Street",
      "city": "City",
      "state": "State"
    },
    "isActive": true,
    "createdAt": "2024-01-05T10:30:00",
    "updatedAt": "2024-01-05T10:30:00",
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
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    {
      "field": "code",
      "message": "Event code is required"
    },
    {
      "field": "startDate",
      "message": "Start date is required"
    },
    {
      "field": "endDate",
      "message": "End date must be after or equal to start date"
    }
  ]
}
```

- `404 Not Found` - Referenced resource not found
```json
{
  "status": "error",
  "message": "Branch not found with id: 99",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

- `409 Conflict` - Duplicate event code
```json
{
  "status": "error",
  "message": "Event code already exists: FESTIVAL_2024",
  "errorCode": "DUPLICATE_RESOURCE"
}
```

---

### 2. Get All Events

**Endpoint**: `GET /api/master/events`

**Description**: Retrieves a list of events with optional filtering and sorting.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `branchId` | Long | No | null | Filter by branch ID |
| `status` | String | No | null | Filter by status (PLANNED, ACTIVE, COMPLETED, CANCELLED) |
| `includeInactive` | Boolean | No | false | Include soft-deleted records |
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter events starting from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter events ending before this date |
| `search` | String | No | null | Search by event name or code (partial match) |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Page size |
| `sortBy` | String | No | "startDate" | Sort field (startDate, endDate, name, createdAt) |
| `sortDir` | String | No | "DESC" | Sort direction (ASC, DESC) |

**Example Request**:
```
GET /api/master/events?branchId=1&status=ACTIVE&fromDate=2024-01-01&toDate=2024-12-31&page=0&size=20
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "code": "FESTIVAL_2024",
        "name": "Annual Temple Festival 2024",
        "description": "Annual temple festival with cultural programs",
        "startDate": "2024-01-10",
        "endDate": "2024-01-20",
        "status": "ACTIVE",
        "branch": {
          "id": 1,
          "code": "BR001",
          "name": "Main Branch"
        },
        "isActive": true,
        "createdAt": "2024-01-05T10:30:00",
        "updatedAt": "2024-01-10T08:00:00"
      },
      {
        "id": 2,
        "code": "CHARITY_2024",
        "name": "Charity Drive 2024",
        "description": "Annual charity drive for underprivileged",
        "startDate": "2024-02-01",
        "endDate": "2024-02-28",
        "status": "PLANNED",
        "branch": null,
        "isActive": true,
        "createdAt": "2024-01-15T14:20:00",
        "updatedAt": "2024-01-15T14:20:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 20
  }
}
```

---

### 3. Get Event by ID

**Endpoint**: `GET /api/master/events/{id}`

**Description**: Retrieves a specific event by its ID with full details.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Example Request**:
```
GET /api/master/events/1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "id": 1,
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024",
    "description": "Annual temple festival with cultural programs, food stalls, and religious ceremonies",
    "startDate": "2024-01-10",
    "endDate": "2024-01-20",
    "status": "ACTIVE",
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch",
      "address": "123 Temple Street",
      "city": "City",
      "state": "State",
      "pincode": "560001",
      "phone": "+91-80-12345678",
      "email": "main@trustapp.com"
    },
    "isActive": true,
    "createdAt": "2024-01-05T10:30:00",
    "updatedAt": "2024-01-10T08:00:00",
    "createdBy": {
      "id": 1,
      "username": "admin",
      "email": "admin@trustapp.com"
    },
    "updatedBy": {
      "id": 2,
      "username": "manager",
      "email": "manager@trustapp.com"
    }
  }
}
```

**Error Response**: `404 Not Found`

```json
{
  "status": "error",
  "message": "Event not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

---

### 4. Update Event

**Endpoint**: `PUT /api/master/events/{id}`

**Description**: Updates an existing event. Note: Event code cannot be updated after creation.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Request Body**:
```json
{
  "name": "Annual Temple Festival 2024 - Updated",
  "description": "Updated description with additional programs",
  "startDate": "2024-01-12",
  "endDate": "2024-01-22",
  "status": "ACTIVE",
  "branchId": 1,
  "isActive": true
}
```

**Request Parameters** (Query):
- `updatedBy` (optional, Long): User ID updating the record. Defaults to authenticated user.

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Event updated successfully",
  "data": {
    "id": 1,
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024 - Updated",
    "description": "Updated description with additional programs",
    "startDate": "2024-01-12",
    "endDate": "2024-01-22",
    "status": "ACTIVE",
    "branch": {
      "id": 1,
      "code": "BR001",
      "name": "Main Branch"
    },
    "isActive": true,
    "createdAt": "2024-01-05T10:30:00",
    "updatedAt": "2024-01-12T11:45:00",
    "updatedBy": {
      "id": 1,
      "username": "admin"
    }
  }
}
```

**Error Responses**:

- `400 Bad Request` - Validation errors
- `404 Not Found` - Event not found
- `409 Conflict` - Cannot update event with associated transactions (business rule)

```json
{
  "status": "error",
  "message": "Cannot update event status to CANCELLED. Event has associated donations. Please remove associations first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 5. Delete Event (Soft Delete)

**Endpoint**: `DELETE /api/master/events/{id}`

**Description**: Soft deletes an event by setting `is_active = false` and `deleted_at` timestamp. The record remains in the database for audit purposes.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Request Parameters** (Query):
- `deletedBy` (optional, Long): User ID deleting the record. Defaults to authenticated user.

**Example Request**:
```
DELETE /api/master/events/1?deletedBy=1
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Event deleted successfully"
}
```

**Error Responses**:

- `404 Not Found` - Event not found
```json
{
  "status": "error",
  "message": "Event not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

- `409 Conflict` - Cannot delete event with associated transactions
```json
{
  "status": "error",
  "message": "Cannot delete event. Event has associated donations, expenses, or vouchers. Please remove associations first.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 6. Update Event Status

**Endpoint**: `PATCH /api/master/events/{id}/status`

**Description**: Updates the status of an event. This endpoint handles event lifecycle transitions with validation.

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Request Body**:
```json
{
  "status": "ACTIVE",
  "reason": "Event has started"
}
```

**Status Transition Rules**:
- `PLANNED` → `ACTIVE`: Allowed when current date >= start_date
- `PLANNED` → `COMPLETED`: Allowed when current date >= end_date
- `PLANNED` → `CANCELLED`: Always allowed
- `ACTIVE` → `COMPLETED`: Allowed when current date >= end_date
- `ACTIVE` → `CANCELLED`: Always allowed
- `COMPLETED` → `ACTIVE`: Not allowed (one-way transition)
- `CANCELLED` → Any status: Not allowed (cancelled events cannot be reactivated)

**Response**: `200 OK`

```json
{
  "status": "success",
  "message": "Event status updated successfully",
  "data": {
    "id": 1,
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024",
    "status": "ACTIVE",
    "previousStatus": "PLANNED",
    "updatedAt": "2024-01-10T08:00:00"
  }
}
```

**Error Responses**:

- `400 Bad Request` - Invalid status transition
```json
{
  "status": "error",
  "message": "Invalid status transition from COMPLETED to ACTIVE. Completed events cannot be reactivated.",
  "errorCode": "VALIDATION_ERROR"
}
```

---

### 7. Get Event Statistics

**Endpoint**: `GET /api/master/events/{id}/statistics`

**Description**: Retrieves comprehensive statistics for a specific event including donations, expenses, vouchers, and financial summaries.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Example Request**:
```
GET /api/master/events/1/statistics
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "event": {
      "id": 1,
      "code": "FESTIVAL_2024",
      "name": "Annual Temple Festival 2024",
      "startDate": "2024-01-10",
      "endDate": "2024-01-20",
      "status": "ACTIVE"
    },
    "donations": {
      "totalCount": 150,
      "totalAmount": 500000.00,
      "averageAmount": 3333.33,
      "minAmount": 100.00,
      "maxAmount": 50000.00,
      "byPaymentMode": [
        {
          "paymentMode": "Cash",
          "count": 50,
          "totalAmount": 150000.00
        },
        {
          "paymentMode": "UPI",
          "count": 60,
          "totalAmount": 200000.00
        },
        {
          "paymentMode": "Bank Transfer",
          "count": 40,
          "totalAmount": 150000.00
        }
      ]
    },
    "expenses": {
      "totalCount": 25,
      "totalAmount": 200000.00,
      "averageAmount": 8000.00,
      "byCategory": [
        {
          "category": "Food & Catering",
          "count": 10,
          "totalAmount": 100000.00
        },
        {
          "category": "Decoration",
          "count": 8,
          "totalAmount": 60000.00
        },
        {
          "category": "Sound System",
          "count": 7,
          "totalAmount": 40000.00
        }
      ]
    },
    "vouchers": {
      "totalCount": 15,
      "totalAmount": 75000.00,
      "averageAmount": 5000.00
    },
    "financialSummary": {
      "totalIncome": 500000.00,
      "totalExpenses": 275000.00,
      "netAmount": 225000.00,
      "profitMargin": 45.00
    },
    "timeline": {
      "daysRemaining": 5,
      "daysElapsed": 5,
      "totalDays": 10,
      "completionPercentage": 50.00
    }
  }
}
```

---

### 8. Get Event Transactions

**Endpoint**: `GET /api/master/events/{id}/transactions`

**Description**: Retrieves all transactions (donations, expenses, vouchers) associated with an event.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Path Parameters**:
- `id` (Long, required): Event ID

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `transactionType` | String | No | "ALL" | Filter by type: DONATIONS, EXPENSES, VOUCHERS, ALL |
| `fromDate` | String (YYYY-MM-DD) | No | null | Filter transactions from this date |
| `toDate` | String (YYYY-MM-DD) | No | null | Filter transactions to this date |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `size` | Integer | No | 20 | Page size |

**Example Request**:
```
GET /api/master/events/1/transactions?transactionType=DONATIONS&page=0&size=20
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "event": {
      "id": 1,
      "code": "FESTIVAL_2024",
      "name": "Annual Temple Festival 2024"
    },
    "donations": {
      "content": [
        {
          "id": 1,
          "receiptNumber": "2024-00001",
          "donorName": "John Doe",
          "amount": 5000.00,
          "paymentMode": "Cash",
          "donationDate": "2024-01-10"
        }
      ],
      "totalElements": 150,
      "totalPages": 8
    },
    "expenses": {
      "content": [
        {
          "id": 1,
          "expenseNumber": "EXP-2024-00001",
          "vendorName": "ABC Catering",
          "amount": 10000.00,
          "category": "Food & Catering",
          "expenseDate": "2024-01-10"
        }
      ],
      "totalElements": 25,
      "totalPages": 2
    },
    "vouchers": {
      "content": [
        {
          "id": 1,
          "voucherNumber": "VCH-2024-00001",
          "vendorName": "XYZ Decorations",
          "amount": 5000.00,
          "voucherDate": "2024-01-10"
        }
      ],
      "totalElements": 15,
      "totalPages": 1
    }
  }
}
```

---

### 9. Get Upcoming Events

**Endpoint**: `GET /api/master/events/upcoming`

**Description**: Retrieves upcoming events (status PLANNED or ACTIVE) within a specified date range.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `branchId` | Long | No | null | Filter by branch ID |
| `daysAhead` | Integer | No | 30 | Number of days ahead to look for events |
| `includeActive` | Boolean | No | true | Include currently active events |

**Example Request**:
```
GET /api/master/events/upcoming?branchId=1&daysAhead=60
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": [
    {
      "id": 2,
      "code": "CHARITY_2024",
      "name": "Charity Drive 2024",
      "startDate": "2024-02-01",
      "endDate": "2024-02-28",
      "status": "PLANNED",
      "daysUntilStart": 15,
      "branch": {
        "id": 1,
        "name": "Main Branch"
      }
    },
    {
      "id": 3,
      "code": "EDUCATION_2024",
      "name": "Education Fundraiser 2024",
      "startDate": "2024-03-01",
      "endDate": "2024-03-15",
      "status": "PLANNED",
      "daysUntilStart": 44,
      "branch": null
    }
  ]
}
```

---

### 10. Get Event Dashboard

**Endpoint**: `GET /api/master/events/dashboard`

**Description**: Retrieves a comprehensive dashboard view with event summaries, statistics, and quick insights.

**Request Headers**:
```
Authorization: Bearer {JWT_TOKEN}
```

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `branchId` | Long | No | null | Filter by branch ID |
| `year` | Integer | No | Current year | Filter by year |

**Example Request**:
```
GET /api/master/events/dashboard?branchId=1&year=2024
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "data": {
    "summary": {
      "totalEvents": 12,
      "plannedEvents": 5,
      "activeEvents": 2,
      "completedEvents": 4,
      "cancelledEvents": 1
    },
    "upcomingEvents": [
      {
        "id": 2,
        "code": "CHARITY_2024",
        "name": "Charity Drive 2024",
        "startDate": "2024-02-01",
        "status": "PLANNED",
        "daysUntilStart": 15
      }
    ],
    "activeEvents": [
      {
        "id": 1,
        "code": "FESTIVAL_2024",
        "name": "Annual Temple Festival 2024",
        "startDate": "2024-01-10",
        "endDate": "2024-01-20",
        "status": "ACTIVE",
        "totalDonations": 500000.00,
        "totalExpenses": 200000.00,
        "netAmount": 300000.00
      }
    ],
    "financialOverview": {
      "totalEventIncome": 1500000.00,
      "totalEventExpenses": 800000.00,
      "netEventProfit": 700000.00,
      "averageEventProfit": 175000.00
    },
    "monthlyBreakdown": [
      {
        "month": "January",
        "eventCount": 2,
        "totalIncome": 500000.00,
        "totalExpenses": 200000.00
      },
      {
        "month": "February",
        "eventCount": 1,
        "totalIncome": 300000.00,
        "totalExpenses": 150000.00
      }
    ]
  }
}
```

---

## Request/Response Formats

### EventCreateDTO

```json
{
  "code": "string (required, max 50, unique)",
  "name": "string (required, max 255)",
  "description": "string (optional, max 5000)",
  "startDate": "date (required, format: YYYY-MM-DD)",
  "endDate": "date (optional, format: YYYY-MM-DD, must be >= startDate)",
  "status": "string (optional, enum: PLANNED, ACTIVE, COMPLETED, CANCELLED, default: PLANNED)",
  "branchId": "long (optional, must exist in branches table if provided)",
  "isActive": "boolean (optional, default: true)"
}
```

### EventUpdateDTO

```json
{
  "name": "string (optional, max 255)",
  "description": "string (optional, max 5000)",
  "startDate": "date (optional, format: YYYY-MM-DD)",
  "endDate": "date (optional, format: YYYY-MM-DD, must be >= startDate)",
  "status": "string (optional, enum: PLANNED, ACTIVE, COMPLETED, CANCELLED)",
  "branchId": "long (optional, must exist in branches table if provided, can be null)",
  "isActive": "boolean (optional)"
}
```

**Note**: Event `code` cannot be updated after creation.

### EventDTO (Response)

```json
{
  "id": "long",
  "code": "string (unique, immutable)",
  "name": "string",
  "description": "string",
  "startDate": "date",
  "endDate": "date (nullable)",
  "status": "string (PLANNED, ACTIVE, COMPLETED, CANCELLED)",
  "branch": {
    "id": "long",
    "code": "string",
    "name": "string",
    "address": "string (optional)",
    "city": "string (optional)",
    "state": "string (optional)"
  },
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

### EventStatusUpdateDTO

```json
{
  "status": "string (required, enum: PLANNED, ACTIVE, COMPLETED, CANCELLED)",
  "reason": "string (optional, max 500)"
}
```

---

## Validation Rules

### Create Event

1. **code**: Required, non-blank, maximum 50 characters, must be unique, alphanumeric with underscores
2. **name**: Required, non-blank, maximum 255 characters
3. **startDate**: Required, must be a valid date in YYYY-MM-DD format
4. **endDate**: Optional, if provided must be a valid date in YYYY-MM-DD format, must be greater than or equal to startDate
5. **status**: Optional, must be one of: PLANNED, ACTIVE, COMPLETED, CANCELLED. Defaults to PLANNED
6. **branchId**: Optional, if provided must exist in `branches` table and be active
7. **description**: Optional, maximum 5000 characters
8. **isActive**: Optional, defaults to true

### Update Event

1. All validation rules from Create apply (for fields being updated)
2. Event must exist and be active
3. Event code cannot be updated (immutable)
4. If updating status, must follow valid status transition rules
5. Cannot update event to CANCELLED if it has associated transactions (donations, expenses, vouchers)

### Delete Event

1. Event must exist
2. Event must not have associated transactions (donations, expenses, vouchers) - business rule to maintain data integrity

### Status Update

1. Status must be one of: PLANNED, ACTIVE, COMPLETED, CANCELLED
2. Status transition must follow valid lifecycle rules (see Event Lifecycle Management section)
3. Event must exist and be active

---

## Event Lifecycle Management

### Status Definitions

- **PLANNED**: Event is scheduled but has not started yet
- **ACTIVE**: Event is currently ongoing (current date is between start_date and end_date)
- **COMPLETED**: Event has finished successfully
- **CANCELLED**: Event has been cancelled and will not occur

### Status Transition Rules

| From Status | To Status | Condition | Allowed |
|-------------|-----------|-----------|---------|
| PLANNED | ACTIVE | Current date >= start_date | ✅ Yes |
| PLANNED | COMPLETED | Current date >= end_date | ✅ Yes |
| PLANNED | CANCELLED | Always | ✅ Yes |
| ACTIVE | COMPLETED | Current date >= end_date | ✅ Yes |
| ACTIVE | CANCELLED | Always | ✅ Yes |
| COMPLETED | ACTIVE | Never | ❌ No |
| COMPLETED | PLANNED | Never | ❌ No |
| CANCELLED | Any | Never | ❌ No |

### Automatic Status Updates

The system can automatically update event statuses based on dates:
- Events with `start_date <= current_date` and status `PLANNED` can be auto-updated to `ACTIVE`
- Events with `end_date < current_date` and status `ACTIVE` can be auto-updated to `COMPLETED`

**Note**: Automatic status updates should be implemented as a scheduled job or triggered during event retrieval.

---

## Event Reporting & Statistics

### Available Reports

1. **Event Summary Report**: Overview of all events with financial summaries
2. **Event Detail Report**: Detailed report for a specific event with all transactions
3. **Event Financial Report**: Income vs expenses analysis per event
4. **Event Timeline Report**: Chronological view of events and their statuses
5. **Event Performance Report**: Comparison of events by financial performance

### Statistics Endpoints

- `GET /api/master/events/{id}/statistics` - Event-specific statistics
- `GET /api/master/events/dashboard` - Dashboard with aggregated statistics
- `GET /api/master/events/upcoming` - Upcoming events list

### Export Options

Events and their associated data can be exported in:
- **PDF**: Formatted reports for printing
- **Excel**: Spreadsheet format for analysis
- **CSV**: Raw data export

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
| `DUPLICATE_RESOURCE` | 409 | Resource already exists (e.g., duplicate event code) |
| `INVALID_STATUS_TRANSITION` | 400 | Invalid event status transition |
| `EVENT_HAS_TRANSACTIONS` | 409 | Cannot delete/update event with associated transactions |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

### Business Rules

1. **Event Code Immutability**: Event code cannot be changed after creation
2. **Transaction Association**: Events with associated donations, expenses, or vouchers cannot be deleted or cancelled
3. **Date Validation**: End date must be greater than or equal to start date
4. **Status Transitions**: Status changes must follow valid lifecycle rules
5. **Branch Access**: Users can only access events for branches they have access to (enforced by security layer)
6. **Global Events**: Events with `branch_id = NULL` are global events accessible to all users (subject to permissions)

---

## Examples

### Example 1: Create a Branch-Specific Event

**Request**:
```bash
curl -X POST "http://localhost:8080/api/master/events" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024",
    "description": "Annual temple festival with cultural programs",
    "startDate": "2024-01-10",
    "endDate": "2024-01-20",
    "status": "PLANNED",
    "branchId": 1
  }'
```

**Response**: `201 Created`
```json
{
  "status": "success",
  "message": "Event created successfully",
  "data": {
    "id": 1,
    "code": "FESTIVAL_2024",
    "name": "Annual Temple Festival 2024",
    "startDate": "2024-01-10",
    "endDate": "2024-01-20",
    "status": "PLANNED",
    "branch": {
      "id": 1,
      "name": "Main Branch"
    }
  }
}
```

### Example 2: Create a Global Event

**Request**:
```bash
curl -X POST "http://localhost:8080/api/master/events" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "code": "CHARITY_2024",
    "name": "Charity Drive 2024",
    "description": "Annual charity drive for underprivileged",
    "startDate": "2024-02-01",
    "endDate": "2024-02-28",
    "status": "PLANNED"
  }'
```

**Response**: `201 Created`
```json
{
  "status": "success",
  "message": "Event created successfully",
  "data": {
    "id": 2,
    "code": "CHARITY_2024",
    "name": "Charity Drive 2024",
    "startDate": "2024-02-01",
    "endDate": "2024-02-28",
    "status": "PLANNED",
    "branch": null
  }
}
```

### Example 3: Update Event Status

**Request**:
```bash
curl -X PATCH "http://localhost:8080/api/master/events/1/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "status": "ACTIVE",
    "reason": "Event has started"
  }'
```

### Example 4: Get Event Statistics

**Request**:
```bash
curl -X GET "http://localhost:8080/api/master/events/1/statistics" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 5: Get Upcoming Events

**Request**:
```bash
curl -X GET "http://localhost:8080/api/master/events/upcoming?branchId=1&daysAhead=60" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 6: Get Event Dashboard

**Request**:
```bash
curl -X GET "http://localhost:8080/api/master/events/dashboard?branchId=1&year=2024" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Notes

1. **Event Code Format**: Event codes should follow a consistent naming convention (e.g., `FESTIVAL_2024`, `CHARITY_2024`) for easy identification and filtering.

2. **Global vs Branch Events**: 
   - Events with `branch_id = NULL` are global events accessible across all branches
   - Events with `branch_id` set are branch-specific events
   - Branch-specific events can be viewed by users with access to that branch

3. **Soft Delete**: Deleted events are marked as inactive but remain in the database for audit purposes. They can be retrieved by setting `includeInactive=true` in query parameters.

4. **Date Handling**: All dates are handled in ISO 8601 format (YYYY-MM-DD) for consistency.

5. **Status Management**: Event status transitions are validated to ensure data integrity. Automatic status updates can be implemented as scheduled jobs.

6. **Transaction Association**: Events are linked to donations, expenses, and vouchers. This association is used for event-specific reporting and financial analysis.

7. **Event Reporting**: Event statistics include aggregated data from associated transactions (donations, expenses, vouchers) to provide comprehensive financial insights.

---

## Related Documentation

- [Master Data Implementation](./01-MASTER-DATA-IMPLEMENTATION.md)
- [Donation Transaction APIs](./03-DONATION-TRANSACTION-APIS.md)
- [User Management](./02-USER-MANAGEMENT.md)
- [API Authentication Guide](../README.md#authentication)

