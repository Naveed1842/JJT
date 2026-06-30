-- M2.1: Enforce that a child can have at most one non-terminal sponsorship at any time.
-- ACTIVE and PENDING are the non-terminal states. EXPIRED is terminal and is not constrained.
-- This index was originally introduced in V2 and incorrectly dropped in V4; now properly re-introduced.

CREATE UNIQUE INDEX idx_one_active_pending_per_child
    ON sponsorships (child_id)
    WHERE status IN ('ACTIVE', 'PENDING');
