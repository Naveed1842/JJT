-- Remove legacy unique index that enforced one sponsorship per child regardless of status
-- This index was introduced in V2__one_active_sponsorship_per_child.sql and now conflicts
-- with the lifecycle model (PENDING/ACTIVE/EXPIRED).

-- Drop the foreign key constraint first (H2 automatically created an index for it)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS fk_sponsorship_child;

-- Drop the unique index if it exists
DROP INDEX IF EXISTS uk_sponsorship_child_active;
