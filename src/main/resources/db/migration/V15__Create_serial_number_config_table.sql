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

