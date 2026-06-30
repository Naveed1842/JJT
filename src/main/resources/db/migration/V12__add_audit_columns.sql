-- M2.1: Add created_by + created_at to all financial records.
-- created_by is nullable: Phase 1 historical rows have no author. All Phase 2+ rows will be non-null.
-- created_at on ledger_entries and progress_updates defaults to NOW() to backfill existing rows safely.
-- sponsorships already has created_at (added in V3); only created_by is added here.

ALTER TABLE ledger_entries
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

ALTER TABLE progress_updates
    ADD COLUMN created_by UUID REFERENCES users(id),
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

ALTER TABLE sponsorships
    ADD COLUMN created_by UUID REFERENCES users(id);
