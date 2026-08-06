-- V39: Transparency snapshots (pre-computed nightly; served to public Trust page)

CREATE TABLE IF NOT EXISTS transparency_snapshots (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id               UUID          NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    period_id            UUID          NOT NULL REFERENCES financial_periods(id),
    computed_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    programme_pct        NUMERIC(5,2),
    admin_pct            NUMERIC(5,2),
    fundraising_pct      NUMERIC(5,2),
    total_income         NUMERIC(15,2) NOT NULL DEFAULT 0,
    total_expense        NUMERIC(15,2) NOT NULL DEFAULT 0,
    beneficiary_count    INTEGER       NOT NULL DEFAULT 0,
    cost_per_beneficiary NUMERIC(10,2),
    per_programme        JSONB         NOT NULL DEFAULT '[]',
    published            BOOLEAN       NOT NULL DEFAULT false,
    UNIQUE (org_id, period_id)
);

CREATE INDEX IF NOT EXISTS idx_transparency_org_published
    ON transparency_snapshots (org_id, published, computed_at DESC);
