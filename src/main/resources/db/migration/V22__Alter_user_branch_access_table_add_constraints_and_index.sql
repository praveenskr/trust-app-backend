-- Add missing foreign key constraints and index to user_branch_access table
-- as per 05-MULTI-BRANCH-MANAGEMENT-APIS.md documentation

-- Add foreign key constraint for branch_id
ALTER TABLE user_branch_access
ADD CONSTRAINT fk_user_branch_access_branch_id
FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE;

-- Add foreign key constraint for granted_by
ALTER TABLE user_branch_access
ADD CONSTRAINT fk_user_branch_access_granted_by
FOREIGN KEY (granted_by) REFERENCES users(id) ON DELETE SET NULL;

-- Add index for granted_at
ALTER TABLE user_branch_access
ADD INDEX idx_granted_at (granted_at);

