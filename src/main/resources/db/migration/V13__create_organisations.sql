-- M2.1: Introduce the organisations table and backfill all existing business entities with JJT-001.
-- organisation_id is added as nullable here. JPA entities do not map this column yet (M2.4).
-- All existing rows are backfilled. NOT NULL enforcement is deferred to M2.4 once every code
-- path that inserts rows has been updated to supply an organisation_id.

-- 1. Create the organisations table
CREATE TABLE organisations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(64)  NOT NULL,
    base_currency   CHAR(3)      NOT NULL DEFAULT 'PKR',
    payment_due_day INT          NOT NULL DEFAULT 15,
    min_fund_reserve NUMERIC(14,2) NOT NULL DEFAULT 20000.00,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_org_slug UNIQUE (slug)
);

-- 2. Seed JJT as the first and only organisation
INSERT INTO organisations (name, slug, base_currency, payment_due_day, min_fund_reserve, active)
VALUES ('Junior Jinnah Trust', 'jjt', 'PKR', 15, 20000.00, TRUE);

-- 3. Add organisation_id (nullable -- NOT NULL deferred to M2.4) to all business tables
ALTER TABLE children                  ADD COLUMN organisation_id UUID REFERENCES organisations(id);
ALTER TABLE education_support_ledgers ADD COLUMN organisation_id UUID REFERENCES organisations(id);
ALTER TABLE ledger_entries            ADD COLUMN organisation_id UUID REFERENCES organisations(id);
ALTER TABLE sponsors                  ADD COLUMN organisation_id UUID REFERENCES organisations(id);
ALTER TABLE sponsorships              ADD COLUMN organisation_id UUID REFERENCES organisations(id);
ALTER TABLE progress_updates          ADD COLUMN organisation_id UUID REFERENCES organisations(id);

-- 4. Backfill all existing rows with the JJT organisation id
UPDATE children                  SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');
UPDATE education_support_ledgers SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');
UPDATE ledger_entries            SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');
UPDATE sponsors                  SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');
UPDATE sponsorships              SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');
UPDATE progress_updates          SET organisation_id = (SELECT id FROM organisations WHERE slug = 'jjt');

-- 5. Performance indexes
CREATE INDEX idx_children_org_id       ON children(organisation_id);
CREATE INDEX idx_sponsors_org_id       ON sponsors(organisation_id);
CREATE INDEX idx_sponsorships_org_id   ON sponsorships(organisation_id);
CREATE INDEX idx_ledger_entries_org_id ON ledger_entries(organisation_id);
