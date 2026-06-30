-- M2.1: Enforce append-only semantics on ledger_entries at the database level.
-- These rules prevent any UPDATE or DELETE against ledger_entries rows, regardless of caller.
-- The application layer already enforces this convention; this is the DB-level safety net.
-- PostgreSQL RULE is used rather than a TRIGGER so the block is silent (no exception noise in tests).

CREATE RULE no_update_ledger_entries AS
    ON UPDATE TO ledger_entries DO INSTEAD NOTHING;

CREATE RULE no_delete_ledger_entries AS
    ON DELETE TO ledger_entries DO INSTEAD NOTHING;
