-- Add soft delete columns
ALTER TABLE branches
ADD COLUMN deleted_at TIMESTAMP NULL,
ADD COLUMN deleted_by BIGINT NULL;

-- Add foreign key constraints
ALTER TABLE branches
ADD CONSTRAINT fk_branches_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_branches_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add additional indexes
ALTER TABLE branches
ADD INDEX idx_city (city),
ADD INDEX idx_state (state),
ADD INDEX idx_created_at (created_at);

