-- V28: Financial periods (monthly / quarterly / annual buckets for budgeting + reporting)

CREATE TABLE IF NOT EXISTS financial_periods (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    label       VARCHAR(50)  NOT NULL,
    period_type VARCHAR(20)  NOT NULL,   -- MONTHLY | QUARTERLY | ANNUAL
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'OPEN',  -- OPEN | CLOSED | LOCKED
    closed_at   TIMESTAMP WITH TIME ZONE,
    closed_by   UUID REFERENCES users(id),
    CONSTRAINT chk_period_dates CHECK (end_date >= start_date),
    UNIQUE (org_id, label)
);

CREATE INDEX IF NOT EXISTS idx_financial_periods_org_status
    ON financial_periods (org_id, status);
