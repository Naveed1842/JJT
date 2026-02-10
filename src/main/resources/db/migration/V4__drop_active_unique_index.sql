-- Remove legacy unique index that enforced one sponsorship per child regardless of status
-- This index was introduced in V2__one_active_sponsorship_per_child.sql and now conflicts
-- with the lifecycle model (PENDING/ACTIVE/EXPIRED).

-- Drop constraint by name if it exists (covers H2 case)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS uk_sponsorship_child_active;

-- Some engines bind the index to the FK constraint; drop/recreate FK to allow index removal.
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS fk_sponsorship_child;
DROP INDEX IF EXISTS uk_sponsorship_child_active;
ALTER TABLE sponsorships
    ADD CONSTRAINT fk_sponsorship_child
    FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE RESTRICT;
