# Trust Management System - Implementation Plan

## Executive Summary

This document outlines the comprehensive implementation plan for a Trust Management System using **Spring Boot** (Backend) and **Angular** (Frontend). The system will manage donations, expenses, vouchers, subscriptions, multi-branch operations, user management, and accounting with double-entry cash book functionality.

---

## 1. Business Requirements Analysis

### 1.1 Core Modules

#### **Module 1: Donation Management**
- Collect donations with donor details (address, PAN number, amount, purpose, sub-category)
- Support multiple payment modes: Cash, UPI, Online Bank Transfer (80% are bank transfers)
- Link donations to specific events or global purposes
- Generate, download, and share receipts via mobile default share options
- Receipt numbering: Unique across all branches, auto-increment per year

#### **Module 2: Master Data Management**
- Configurable main purposes and sub-categories for donations
- Configurable main purposes and sub-categories for expenses/vouchers
- Configurable serial number formats for different entities (Trust, Vedhabharathi, Vedhabharathi News, etc.)

#### **Module 3: Event Management**
- Create and manage multiple events
- Associate donations, expenses, and vouchers with events
- Event-specific reporting

#### **Module 4: Expense & Voucher Management**
- Record expenses with main and sub-categories
- Manage vouchers with vendor details, contact information, event association, amounts
- Support cash and online payment modes for vouchers
- Expense/voucher tracking per event

#### **Module 5: Multi-Branch Management**
- Separate management for each branch
- Branch-specific data isolation (except for super users)
- Inter-branch money transfer tracking

#### **Module 6: User & Permission Management**
- User roles: Super User, Manager, Accountant
- Role-based access control (RBAC)
- Super user can view all branches
- Permission management by super user

#### **Module 7: Subscription Management (Magazine)**
- Subscription plans: Monthly, Quarterly, Yearly, Lifetime
- Discount management
- Validity and membership plan management
- Track upcoming subscription expiry users
- Separate accounting for Vedhabharathi sub-activity

#### **Module 8: Reporting & Analytics**
- Reports for donations, expenses, and vouchers
- Filters: Date range, branch, branches (multiple), events
- Export formats: PDF and Excel
- Cash book reports

#### **Module 9: Cash Book (Double Entry System)**
- Debit and credit entries
- Auto-generate entries for inter-branch transfers
- Track money transfers between branches and external organizations
- Cash flow reporting

#### **Module 10: Vedhabharathi Sub-Activity**
- Separate accounting for Vedhabharathi
- Manage expenses, vouchers separately
- Separate serial numbering

---

## 2. Technical Architecture

### 2.1 Technology Stack

#### **Backend (Spring Boot)**
- **Framework**: Spring Boot 3.x
- **Java Version**: Java 17+
- **Database**: MySQL 8.0+ (for production), H2 (for development)
- **Data Access**: Spring JDBC Client (not JPA)
- **Security**: Spring Security with JWT
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **PDF Generation**: Apache PDFBox or iText
- **Excel Export**: Apache POI
- **Build Tool**: Maven or Gradle
- **Testing**: JUnit 5, Mockito, Spring Boot Test

#### **Frontend (Angular)**
- **Framework**: Angular 17+ (or latest stable)
- **Language**: TypeScript
- **UI Library**: Angular Material or PrimeNG
- **State Management**: NgRx (optional) or RxJS
- **HTTP Client**: Angular HttpClient
- **PDF Viewer**: ng2-pdf-viewer or pdf.js
- **Excel Export**: xlsx library
- **Charts**: Chart.js or ng2-charts
- **Build Tool**: Angular CLI

#### **Infrastructure**
- **Containerization**: Docker
- **Version Control**: Git
- **CI/CD**: GitHub Actions / GitLab CI
- **API Documentation**: Swagger UI

### 2.2 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Angular Frontend                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  Admin   │  │ Manager  │  │Accountant│  │  Public  │ │
│  │  Module  │  │  Module  │  │  Module  │  │  Portal  │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │
└──────────────────────┬────────────────────────────────────┘
                        │ REST API (HTTPS)
┌───────────────────────▼────────────────────────────────────┐
│              Spring Boot Backend                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │   Auth   │  │ Business │  │ Reporting│  │  File    ││
│  │  Service │  │  Logic   │  │  Service  │  │  Service ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└───────────────────────┬────────────────────────────────────┘
                        │
┌───────────────────────▼────────────────────────────────────┐
│                      MySQL Database                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐│
│  │  Master  │  │Transaction│  │   User   │  │  Audit   ││
│  │   Data   │  │    Data   │  │   Data   │  │   Log    ││
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘│
└────────────────────────────────────────────────────────────┘
```

---

## 3. Database Design

### 3.1 Core Tables

#### **Master Data Tables**
- `branches` - Branch information
- `donation_purposes` - Main purposes (configurable)
- `donation_sub_categories` - Sub-categories (configurable)
- `expense_categories` - Expense main categories
- `expense_sub_categories` - Expense sub-categories
- `events` - Event information
- `subscription_plans` - Magazine subscription plans
- `serial_number_config` - Serial number configuration per entity
- `vendors` - Vendor master data

#### **Transaction Tables**
- `donations` - Donation records
- `donation_receipts` - Generated receipts
- `expenses` - Expense records
- `vouchers` - Voucher records
- `subscriptions` - Magazine subscriptions
- `cash_book_entries` - Double entry cash book
- `inter_branch_transfers` - Inter-branch money transfers

#### **User & Security Tables**
- `users` - User accounts
- `roles` - User roles (Super User, Manager, Accountant)
- `permissions` - Permission definitions
- `role_permissions` - Role-Permission mapping
- `user_branch_access` - User access to branches
- `audit_logs` - Audit trail for major actions

#### **Supporting Tables**
- `payment_modes` - Payment mode master (Cash, UPI, Bank Transfer)
- `subscription_discounts` - Discount configurations
- `file_attachments` - Receipt PDFs and other documents

### 3.2 Key Relationships

```
branches (1) ──< (M) donations
branches (1) ──< (M) expenses
branches (1) ──< (M) vouchers
events (1) ──< (M) donations
events (1) ──< (M) expenses
events (1) ──< (M) vouchers
donation_purposes (1) ──< (M) donations
donation_sub_categories (1) ──< (M) donations
users (1) ──< (M) donations (created_by)
cash_book_entries (M) ──< (1) branches (from_branch, to_branch)
```

---

## 4. Screen/UI Flow Design

### 4.1 Authentication & Authorization Screens

#### **Login Screen**
- Username/Email and Password
- Branch selection (for multi-branch users)
- Remember me option
- Forgot password link

#### **Dashboard (Role-based)**
- **Super User Dashboard**:
  - Overview of all branches (donations, expenses, vouchers)
  - Total collections across branches
  - Pending approvals
  - Upcoming subscription expiries
  - Cash book summary

- **Manager Dashboard**:
  - Branch-specific overview
  - Donation summary
  - Expense summary
  - Event-wise breakdown
  - Quick actions

- **Accountant Dashboard**:
  - Transaction entry shortcuts
  - Pending receipts to generate
  - Cash book entries
  - Report generation shortcuts

### 4.2 Donation Management Screens

#### **Donation Entry Form**
- Donor Information:
  - Name (required)
  - Address (required)
  - PAN Number (required, format validation)
  - Contact Number
  - Email (optional)
- Donation Details:
  - Amount (required)
  - Payment Mode (Cash/UPI/Bank Transfer) - dropdown
  - Purpose (Main) - dropdown from master
  - Sub-Category - dropdown (filtered by purpose)
  - Event Selection - dropdown (with "Global/General" option)
  - Branch - auto-selected based on user
  - Date - date picker
  - Remarks - text area
- Actions:
  - Save Draft
  - Submit & Generate Receipt
  - Cancel

#### **Donation List Screen**
- Filters:
  - Date Range (From/To)
  - Branch (multi-select for super users)
  - Event (multi-select)
  - Payment Mode
  - Purpose
  - Sub-Category
- Table Columns:
  - Receipt Number
  - Donor Name
  - Amount
  - Payment Mode
  - Purpose
  - Event
  - Date
  - Actions (View, Edit, Download Receipt, Share)
- Pagination
- Export to PDF/Excel button

#### **Receipt Generation & View**
- Receipt Template (PDF):
  - Trust Logo
  - Receipt Number (unique across branches)
  - Date
  - Donor Details (Name, Address, PAN)
  - Amount (in words and numbers)
  - Purpose & Sub-Category
  - Event (if applicable)
  - Payment Mode
  - Branch Name
  - Authorized Signatory
- Actions:
  - Download PDF
  - Share (opens mobile default share options)
  - Print
  - Email (future enhancement)

### 4.3 Expense Management Screens

#### **Expense Entry Form**
- Expense Details:
  - Expense Date
  - Event (dropdown with "General" option)
  - Main Category (dropdown from master)
  - Sub-Category (dropdown, filtered by category)
  - Amount
  - Payment Mode (Cash/Online)
  - Description
  - Attachments (receipts/bills)
- Actions:
  - Save
  - Save & Add Another
  - Cancel

#### **Expense List Screen**
- Similar filters as Donation List
- Group by Event/Category option
- Total summary row

### 4.4 Voucher Management Screens

#### **Voucher Entry Form**
- Vendor Information:
  - Vendor Name (with search/create new)
  - Contact Details (phone, email, address)
- Voucher Details:
  - Voucher Number (auto-generated)
  - Voucher Date
  - Event (dropdown)
  - Amount
  - Payment Mode (Cash/Online)
  - Description
  - Attachments
- Actions:
  - Save
  - Save & Print
  - Cancel

#### **Voucher List Screen**
- Similar to Expense List
- Vendor filter
- Print voucher option

### 4.5 Master Data Management Screens

#### **Purpose & Category Management**
- Main Purpose:
  - List view with Add/Edit/Delete
  - Name, Code, Description
  - Active/Inactive toggle
- Sub-Category:
  - List view with parent purpose
  - Add/Edit/Delete
  - Drag-drop for ordering

#### **Serial Number Configuration**
- Entity-wise configuration:
  - Entity Name (Trust, Vedhabharathi, Vedhabharathi News, etc.)
  - Prefix
  - Format (e.g., {PREFIX}-{YEAR}-{SEQUENCE})
  - Current Year
  - Last Sequence Number
  - Reset option (yearly)

#### **Event Management**
- Event List:
  - Name, Start Date, End Date, Status
  - Add/Edit/Delete
  - View associated transactions

### 4.6 Subscription Management Screens

#### **Subscription Entry Form**
- Subscriber Details:
  - Name, Address, Contact, Email
- Subscription Details:
  - Plan (Monthly/Quarterly/Yearly/Lifetime)
  - Start Date
  - End Date (auto-calculated based on plan)
  - Discount Applied (if any)
  - Amount
  - Payment Mode
- Actions:
  - Save
  - Generate Receipt

#### **Subscription List Screen**
- Filters:
  - Plan Type
  - Status (Active/Expired/Expiring Soon)
  - Date Range
- Columns:
  - Subscriber Name
  - Plan
  - Start Date
  - End Date
  - Days Remaining
  - Status
  - Actions (Renew, View, Edit)

#### **Expiry Management Screen**
- Upcoming Expiries (next 30/60/90 days)
- Expired Subscriptions
- Bulk renewal option
- Send reminder option (future)

### 4.7 Cash Book Screens

#### **Cash Book Entry Form**
- Transaction Type:
  - Inter-Branch Transfer
  - External Transfer (to/from outside organization)
- Entry Details:
  - Date
  - From Branch/Account
  - To Branch/Account
  - Amount
  - Description
  - Reference Number
- Auto-generate:
  - Debit Entry (From Branch)
  - Credit Entry (To Branch)
- Actions:
  - Save
  - View Generated Entries

#### **Cash Book View/Report**
- Filters:
  - Date Range
  - Branch
  - Transaction Type
- Display:
  - Date, Description, Debit, Credit, Balance
  - Running Balance
- Export to PDF/Excel

### 4.8 Reporting Screens

#### **Report Generator**
- Report Type Selection:
  - Donation Report
  - Expense Report
  - Voucher Report
  - Cash Book Report
  - Combined Financial Report
- Filters Panel:
  - Date Range (required)
  - Branch/Branches (multi-select)
  - Event (multi-select)
  - Purpose/Category
  - Payment Mode
- Output Options:
  - Preview
  - Export PDF
  - Export Excel
- Report Preview:
  - Summary totals
  - Detailed breakdown
  - Charts (optional)

### 4.9 User Management Screens

#### **User List**
- Columns:
  - Username, Name, Role, Branch Access, Status
  - Actions (Edit, Deactivate, Reset Password)

#### **User Create/Edit Form**
- Basic Info:
  - Username, Email, Name, Phone
- Role Assignment:
  - Role dropdown
  - Branch Access (multi-select for managers/accountants)
- Permissions (for super users):
  - Granular permission checkboxes
- Actions:
  - Save
  - Cancel

#### **Permission Management (Super User Only)**
- Role-based permission matrix
- Permission groups:
  - Donation Management
  - Expense Management
  - Voucher Management
  - Reporting
  - User Management
  - Master Data Management
  - Cash Book
  - Subscription Management

### 4.10 Branch Management Screens

#### **Branch List**
- Columns:
  - Branch Name, Code, Address, Contact, Status
  - Actions (Edit, View Details)

#### **Branch Create/Edit Form**
- Branch Details:
  - Name, Code, Address, Contact Info
  - Active/Inactive status

---

## 5. API Design (Spring Boot)

### 5.1 Authentication APIs

```
POST   /api/auth/login              - User login
POST   /api/auth/logout              - User logout
POST   /api/auth/refresh-token       - Refresh JWT token
GET    /api/auth/current-user        - Get current user info
POST   /api/auth/forgot-password     - Forgot password
POST   /api/auth/reset-password      - Reset password
```

### 5.2 Donation APIs

```
GET    /api/donations                - List donations (with filters)
GET    /api/donations/{id}          - Get donation details
POST   /api/donations                - Create donation
PUT    /api/donations/{id}          - Update donation
DELETE /api/donations/{id}          - Delete donation (soft delete)
POST   /api/donations/{id}/receipt  - Generate receipt
GET    /api/donations/{id}/receipt  - Download receipt PDF
GET    /api/donations/export        - Export to Excel/PDF
GET    /api/donations/summary       - Get summary statistics
```

### 5.3 Expense APIs

```
GET    /api/expenses                 - List expenses
GET    /api/expenses/{id}            - Get expense details
POST   /api/expenses                 - Create expense
PUT    /api/expenses/{id}            - Update expense
DELETE /api/expenses/{id}            - Delete expense
GET    /api/expenses/export          - Export to Excel/PDF
```

### 5.4 Voucher APIs

```
GET    /api/vouchers                 - List vouchers
GET    /api/vouchers/{id}            - Get voucher details
POST   /api/vouchers                 - Create voucher
PUT    /api/vouchers/{id}            - Update voucher
DELETE /api/vouchers/{id}            - Delete voucher
GET    /api/vouchers/{id}/print     - Print voucher
GET    /api/vouchers/export          - Export to Excel/PDF
```

### 5.5 Master Data APIs

```
# Purposes & Categories
GET    /api/master/donation-purposes        - List purposes
POST   /api/master/donation-purposes       - Create purpose
PUT    /api/master/donation-purposes/{id}   - Update purpose
DELETE /api/master/donation-purposes/{id}   - Delete purpose
GET    /api/master/donation-sub-categories - List sub-categories
POST   /api/master/donation-sub-categories - Create sub-category
# Similar for expense categories

# Events
GET    /api/master/events           - List events
POST   /api/master/events           - Create event
PUT    /api/master/events/{id}      - Update event
DELETE /api/master/events/{id}      - Delete event

# Serial Number Config
GET    /api/master/serial-config    - List configurations
PUT    /api/master/serial-config/{id} - Update configuration
GET    /api/master/serial-config/next/{entity} - Get next serial number
```

### 5.6 Subscription APIs

```
GET    /api/subscriptions           - List subscriptions
GET    /api/subscriptions/{id}     - Get subscription details
POST   /api/subscriptions          - Create subscription
PUT    /api/subscriptions/{id}     - Update subscription
POST   /api/subscriptions/{id}/renew - Renew subscription
GET    /api/subscriptions/expiring  - Get expiring subscriptions
GET    /api/subscriptions/export   - Export to Excel/PDF
```

### 5.7 Cash Book APIs

```
GET    /api/cashbook                - List cash book entries
GET    /api/cashbook/{id}           - Get entry details
POST   /api/cashbook/transfer       - Create inter-branch transfer
GET    /api/cashbook/balance        - Get branch balances
GET    /api/cashbook/export         - Export to Excel/PDF
```

### 5.8 Reporting APIs

```
POST   /api/reports/donations       - Generate donation report
POST   /api/reports/expenses        - Generate expense report
POST   /api/reports/vouchers        - Generate voucher report
POST   /api/reports/cashbook        - Generate cash book report
POST   /api/reports/combined        - Generate combined report
```

### 5.9 User Management APIs

```
GET    /api/users                    - List users
GET    /api/users/{id}               - Get user details
POST   /api/users                    - Create user
PUT    /api/users/{id}               - Update user
DELETE /api/users/{id}               - Delete user
GET    /api/users/roles              - List roles
GET    /api/users/permissions        - List permissions
PUT    /api/users/{id}/permissions   - Update user permissions
```

### 5.10 Branch APIs

```
GET    /api/branches                 - List branches
GET    /api/branches/{id}            - Get branch details
POST   /api/branches                 - Create branch
PUT    /api/branches/{id}            - Update branch
```

---

## 6. Implementation Phases

### **Phase 1: Foundation (Weeks 1-3)**
- Project setup (Spring Boot + Angular)
- MySQL database schema design and creation
- JDBC Client configuration and setup
- Authentication & Authorization (JWT)
- User management basic structure (using JDBC)
- Branch management
- Master data tables setup

**Deliverables:**
- Working login/logout
- User CRUD operations
- Branch CRUD operations
- Master data management screens

### **Phase 2: Core Donation Module (Weeks 4-6)**
- Donation entry form
- Donation list with filters
- Receipt generation (PDF)
- Receipt download and share
- Serial number generation logic
- Donation reporting

**Deliverables:**
- Complete donation workflow
- Receipt generation and sharing
- Basic donation reports

### **Phase 3: Expense & Voucher Module (Weeks 7-9)**
- Expense entry and management
- Voucher entry and management
- Category management
- Expense/voucher reporting

**Deliverables:**
- Expense management complete
- Voucher management complete
- Related reports

### **Phase 4: Event Management (Week 10)**
- Event CRUD operations
- Link donations/expenses/vouchers to events
- Event-wise reporting

**Deliverables:**
- Event management module
- Event association in transactions

### **Phase 5: Subscription Module (Weeks 11-13)**
- Subscription plan management
- Subscription entry and renewal
- Discount management
- Expiry tracking and alerts
- Subscription reporting

**Deliverables:**
- Complete subscription management
- Expiry tracking dashboard

### **Phase 6: Cash Book & Accounting (Weeks 14-16)**
- Double-entry cash book implementation
- Inter-branch transfer logic
- Auto-generation of debit/credit entries
- Cash book reporting
- Balance calculations

**Deliverables:**
- Cash book module
- Inter-branch transfer functionality
- Cash book reports

### **Phase 7: Advanced Reporting (Weeks 17-18)**
- Combined financial reports
- Advanced filters
- PDF and Excel export
- Dashboard enhancements
- Charts and visualizations

**Deliverables:**
- Comprehensive reporting module
- Export functionality

### **Phase 8: Vedhabharathi Sub-Activity (Week 19)**
- Separate accounting setup
- Entity-specific serial numbers
- Separate reporting

**Deliverables:**
- Vedhabharathi module isolation

### **Phase 9: Permission Management (Week 20)**
- Granular permission system
- Role-based access control refinement
- Permission management UI for super users

**Deliverables:**
- Complete RBAC system

### **Phase 10: Testing & Optimization (Weeks 21-22)**
- Unit testing
- Integration testing
- Performance optimization
- Security audit
- Bug fixes

**Deliverables:**
- Tested and optimized system

### **Phase 11: Deployment & Training (Weeks 23-24)**
- Production deployment
- User training
- Documentation
- Support setup

**Deliverables:**
- Production-ready system
- User documentation
- Training materials

---

## 7. Key Technical Implementation Details

### 7.1 Serial Number Generation

**Logic (JDBC):**
```java
// Using JDBC Client for serial number generation
@Transactional
public String generateSerialNumber(String entityType, Long branchId) {
    int currentYear = LocalDate.now().getYear();
    
    // Get or create config with row-level locking
    String selectSql = """
        SELECT id, prefix, current_year, last_sequence 
        FROM serial_number_config 
        WHERE entity_type = ? 
        FOR UPDATE
        """;
    
    SerialNumberConfig config = jdbcClient.sql(selectSql)
        .param(entityType)
        .query(SerialNumberConfig.class)
        .optional()
        .orElseGet(() -> createNewConfig(entityType, currentYear));
    
    // Reset sequence if year changed
    if (config.getCurrentYear() != currentYear) {
        updateConfigYear(entityType, currentYear, 0);
        config.setCurrentYear(currentYear);
        config.setLastSequence(0);
    }
    
    // Increment and update sequence
    int newSequence = config.getLastSequence() + 1;
    updateSequence(entityType, newSequence);
    
    String sequence = String.format("%04d", newSequence);
    return String.format("%s-%d-%s", 
        config.getPrefix(), 
        currentYear, 
        sequence);
}
```

**Database:**
- `serial_number_config` table tracks current year and last sequence
- Auto-reset on year change
- Unique constraint on entity type

### 7.2 Double Entry Cash Book

**Auto-generation Logic (JDBC):**
```java
// When creating inter-branch transfer using JDBC
@Transactional
public void createInterBranchTransfer(TransferDTO dto) {
    String sql = """
        INSERT INTO cash_book_entries 
        (branch_id, entry_type, amount, description, reference_number, transaction_date, created_at)
        VALUES (?, ?, ?, ?, ?, ?, NOW())
        """;
    
    // Create debit entry (from branch)
    jdbcClient.sql(sql)
        .param(dto.getFromBranchId())
        .param("DEBIT")
        .param(dto.getAmount())
        .param("Transfer to " + toBranchName)
        .param(dto.getReferenceNumber())
        .param(LocalDate.now())
        .update();
    
    // Create credit entry (to branch)
    jdbcClient.sql(sql)
        .param(dto.getToBranchId())
        .param("CREDIT")
        .param(dto.getAmount())
        .param("Transfer from " + fromBranchName)
        .param(dto.getReferenceNumber())
        .param(LocalDate.now())
        .update();
}
```

### 7.3 Receipt Generation

**PDF Generation:**
- Use Apache PDFBox or iText
- Template-based approach
- Include all required fields
- Digital signature placeholder
- Unique receipt number

**Share Functionality:**
- Use Web Share API (for mobile browsers)
- Fallback to download option
- Generate shareable link (future enhancement)

### 7.4 Audit Trail

**Track Major Actions:**
- Create/Update/Delete operations
- User login/logout
- Permission changes
- Financial transactions
- Report generation

**Implementation:**
- Spring AOP for automatic logging
- Custom annotation `@AuditLog`
- Store in `audit_logs` table

### 7.5 Multi-Branch Data Isolation

**Implementation:**
- Application-level filtering using JDBC queries with WHERE clauses
- Branch context in JWT token
- Super users bypass filters (no branch filter applied)
- All queries include branch_id filter for non-super users

### 7.6 JDBC Client Implementation Patterns

**Repository Pattern with JDBC:**
```java
@Repository
public class DonationRepository {
    private final JdbcClient jdbcClient;
    
    public DonationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
    
    public List<Donation> findByBranchId(Long branchId, LocalDate fromDate, LocalDate toDate) {
        String sql = """
            SELECT id, donor_name, amount, payment_mode, purpose_id, 
                   sub_category_id, event_id, branch_id, donation_date
            FROM donations
            WHERE branch_id = ? 
            AND donation_date BETWEEN ? AND ?
            ORDER BY donation_date DESC
            """;
        
        return jdbcClient.sql(sql)
            .param(branchId)
            .param(fromDate)
            .param(toDate)
            .query(Donation.class)
            .list();
    }
    
    public Optional<Donation> findById(Long id) {
        String sql = "SELECT * FROM donations WHERE id = ?";
        return jdbcClient.sql(sql)
            .param(id)
            .query(Donation.class)
            .optional();
    }
    
    @Transactional
    public Long save(Donation donation) {
        String sql = """
            INSERT INTO donations 
            (donor_name, donor_address, pan_number, amount, payment_mode, 
             purpose_id, sub_category_id, event_id, branch_id, donation_date, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcClient.sql(sql)
            .param(donation.getDonorName())
            .param(donation.getDonorAddress())
            .param(donation.getPanNumber())
            .param(donation.getAmount())
            .param(donation.getPaymentMode())
            .param(donation.getPurposeId())
            .param(donation.getSubCategoryId())
            .param(donation.getEventId())
            .param(donation.getBranchId())
            .param(donation.getDonationDate())
            .param(donation.getCreatedBy())
            .update(keyHolder);
        
        return keyHolder.getKey().longValue();
    }
}
```

**Key JDBC Client Features:**
- Type-safe parameter binding with `.param()`
- Fluent query API
- Row mapping to POJOs
- Transaction support with `@Transactional`
- Batch operations support
- Connection pooling via HikariCP (default in Spring Boot)

---

## 8. Security Considerations

### 8.1 Authentication & Authorization
- JWT-based authentication
- Password encryption (BCrypt)
- Session management
- Role-based access control (RBAC)

### 8.2 Data Security
- HTTPS for all communications
- SQL injection prevention (parameterized queries with JDBC - always use ? placeholders)
- XSS prevention (input sanitization)
- CSRF protection
- Sensitive data encryption at rest
- Prepared statements for all database operations

### 8.3 API Security
- Rate limiting
- Input validation
- Output sanitization
- CORS configuration

---

## 9. Testing Strategy

### 9.1 Unit Testing
- Service layer unit tests
- Repository layer tests
- Utility function tests
- Coverage target: 80%+

### 9.2 Integration Testing
- API endpoint testing
- Database integration tests (using Testcontainers with MySQL)
- JDBC query testing
- External service mocking

### 9.3 User Acceptance Testing (UAT)
- Test scenarios for each module
- User workflow testing
- Performance testing

---

## 10. Deployment Strategy

### 10.1 Environment Setup
- Development
- Staging
- Production

### 10.2 Deployment Steps
1. MySQL database setup and migration scripts
2. Application deployment (Docker containers)
3. Configuration management (JDBC connection pooling)
4. Health checks (database connectivity)
5. Monitoring setup

### 10.3 Monitoring
- Application logs
- Error tracking
- Performance metrics
- Database monitoring

---

## 11. User Experience Best Practices

### 11.1 UI/UX Guidelines
- Responsive design (mobile, tablet, desktop)
- Intuitive navigation
- Clear error messages
- Loading indicators
- Confirmation dialogs for critical actions
- Keyboard shortcuts for power users
- Accessibility (WCAG 2.1 compliance)

### 11.2 Performance
- Lazy loading for large lists
- Pagination
- Caching strategies
- Optimized database queries
- CDN for static assets

### 11.3 Mobile Experience
- Touch-friendly buttons
- Mobile-optimized forms
- Native share functionality
- Responsive tables

---

## 12. Future Enhancements (Post-MVP)

1. **Payment Gateway Integration**
   - Razorpay/PayU integration
   - Online payment processing
   - Payment status tracking

2. **WhatsApp API Integration**
   - Automated receipt sharing
   - Subscription reminders
   - Event notifications

3. **Email Integration**
   - Automated receipt emails
   - Report distribution
   - Notification system

4. **Mobile App**
   - Native Android/iOS app
   - Offline capability
   - Push notifications

5. **Advanced Analytics**
   - Dashboard with charts
   - Trend analysis
   - Predictive analytics

6. **Document Management**
   - Cloud storage integration
   - Document versioning
   - Bulk upload

---

## 13. Assumptions & Constraints

### 13.1 Assumptions
- Users have basic computer literacy
- Internet connectivity available
- Browsers support modern JavaScript
- Payment gateway integration not required initially
- WhatsApp API integration not required initially

### 13.2 Constraints
- Manual payment entry (no gateway integration)
- Manual WhatsApp sharing (no API integration)
- No real-time PAN validation
- Audit trail for major actions only

---

## 14. Success Criteria

1. All 18 business requirements implemented
2. System handles 1000+ transactions per day
3. Response time < 2 seconds for 95% of requests
4. 99.9% uptime
5. User satisfaction score > 4/5
6. Zero data loss
7. Complete audit trail
8. All reports exportable in PDF/Excel

---

## 15. Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data Loss | High | Regular backups, transaction logging |
| Security Breach | High | Security audits, penetration testing |
| Performance Issues | Medium | Load testing, optimization |
| User Adoption | Medium | Training, intuitive UI |
| Scope Creep | Medium | Phased approach, change management |

---

## 16. Conclusion

This implementation plan provides a comprehensive roadmap for building a Trust Management System using Spring Boot and Angular. The phased approach ensures systematic development while maintaining quality and meeting all business requirements.

**Next Steps:**
1. Review and approve this plan
2. Set up development environment
3. Begin Phase 1 implementation
4. Regular progress reviews

---

**Document Version:** 1.0  
**Last Updated:** [Current Date]  
**Prepared By:** Development Team  
**Approved By:** [To be filled]

