-- Fix: uk_sponsor_child_start was an unconditional unique constraint on (sponsor_id, child_id, start_month).
-- This incorrectly prevented a sponsor from re-committing to the same child after a previous
-- sponsorship had expired, even for a different future month or the same month.
--
-- Business rule: uniqueness should only apply to non-terminal (ACTIVE, PENDING) sponsorships.
-- EXPIRED sponsorships are historical records and must never block future commitments.

-- Drop the existing unconditional constraint (may be named as a constraint or as an index)
ALTER TABLE sponsorships DROP CONSTRAINT IF EXISTS uk_sponsor_child_start;
DROP INDEX IF EXISTS uk_sponsor_child_start;

-- Recreate as a partial unique index covering only non-terminal statuses.
-- Two ACTIVE or PENDING sponsorships for the same (sponsor, child, month) are still blocked.
-- EXPIRED sponsorships are excluded and do not prevent future commitments.
CREATE UNIQUE INDEX uk_sponsor_child_start
    ON sponsorships (sponsor_id, child_id, start_month)
    WHERE status IN ('ACTIVE', 'PENDING');
