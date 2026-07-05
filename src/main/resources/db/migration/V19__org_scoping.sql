-- M2.4: Enforce NOT NULL on organisation_id across all business tables.
-- sponsor_payments was created in V18 without organisation_id; add and backfill it here.

ALTER TABLE sponsor_payments
    ADD COLUMN organisation_id UUID REFERENCES organisations(id);

UPDATE sponsor_payments
    SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt')
    WHERE organisation_id IS NULL;

ALTER TABLE sponsor_payments
    ALTER COLUMN organisation_id SET NOT NULL;

-- Enforce NOT NULL on tables that received organisation_id as nullable in V13.
ALTER TABLE children                  ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE education_support_ledgers ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE ledger_entries            ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE sponsors                  ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE sponsorships              ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE progress_updates          ALTER COLUMN organisation_id SET NOT NULL;
ALTER TABLE fund_accounts             ALTER COLUMN organisation_id SET NOT NULL;

-- Backfill users.org_id for any rows created before M2.4 (e.g. admin seeded without orgId).
UPDATE users
    SET org_id = (SELECT id FROM organisations WHERE slug = 'jjt')
    WHERE org_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_sponsor_payments_org_id ON sponsor_payments(organisation_id);
