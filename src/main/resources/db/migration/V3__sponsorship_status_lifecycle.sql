-- Add lifecycle columns
ALTER TABLE sponsorships ADD COLUMN status VARCHAR(16) DEFAULT 'PENDING' NOT NULL;
ALTER TABLE sponsorships ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE sponsorships ADD COLUMN expires_at TIMESTAMP NULL;

-- Add sponsor phone
ALTER TABLE sponsors ADD COLUMN phone VARCHAR(64);

-- Drop prior unique constraint/index if it exists (H2 ties the index to the constraint)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS uk_sponsorship_child_active;

-- Backfill existing rows: mark as ACTIVE (they were implicitly allocating)
UPDATE sponsorships SET status = 'ACTIVE' WHERE status IS NULL OR status = 'PENDING';
