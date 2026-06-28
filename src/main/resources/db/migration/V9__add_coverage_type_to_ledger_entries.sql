-- Add coverage type to distinguish org-funded vs sponsor-funded ledger entries.
-- Existing rows default to EARLY_SUPPORT (all pre-existing entries were org-funded).
ALTER TABLE ledger_entries
    ADD COLUMN coverage_type VARCHAR(20) NOT NULL DEFAULT 'EARLY_SUPPORT';
