-- Migration: V24__Alter_events_table_align_with_event_doc.sql
-- Purpose : Align existing events table with documentation in 04-EVENT-MANAGEMENT-APIS.md
-- Assumes : V14__Create_events_table.sql has already created the basic events table
-- Changes :
--   1) Add soft delete columns: deleted_at, deleted_by
--   2) Add foreign keys on created_by, updated_by, deleted_by to users(id)
--   3) Add indexes on is_active and created_at

ALTER TABLE events
    ADD COLUMN deleted_at TIMESTAMP NULL AFTER updated_at,
    ADD COLUMN deleted_by BIGINT NULL AFTER deleted_at;

ALTER TABLE events
    ADD CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_events_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_events_deleted_by
        FOREIGN KEY (deleted_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_events_active ON events (is_active);
CREATE INDEX idx_events_created_at ON events (created_at);


