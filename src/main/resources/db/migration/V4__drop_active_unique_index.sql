-- Remove legacy unique index that enforced one sponsorship per child regardless of status
-- This index was introduced in V2__one_active_sponsorship_per_child.sql and now conflicts
-- with the lifecycle model (PENDING/ACTIVE/EXPIRED).

-- Drop constraint by name if it exists (covers H2 case)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS uk_sponsorship_child_active;

-- Drop the Postgres index if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relkind = 'i'
          AND c.relname = 'uk_sponsorship_child_active'
    ) THEN
        EXECUTE 'DROP INDEX IF EXISTS uk_sponsorship_child_active';
    END IF;
END$$;
