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

