-- Remove legacy unique index that enforced one sponsorship per child regardless of status
-- This index was introduced in V2__one_active_sponsorship_per_child.sql and now conflicts
-- with the lifecycle model (PENDING/ACTIVE/EXPIRED).

-- Drop constraint by name if it exists (covers H2 case)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS uk_sponsorship_child_active;

-- Drop index by name if it exists (works for PostgreSQL and H2)
DROP INDEX IF EXISTS uk_sponsorship_child_active;
