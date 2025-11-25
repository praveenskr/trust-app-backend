CREATE TABLE tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL DEFAULT 'BEARER',
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expired BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME,
    user_id BIGINT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_token_type (token_type),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

