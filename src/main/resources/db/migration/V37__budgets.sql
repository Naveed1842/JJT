-- V37: Budget planning per period / category / cost centre / mission node

CREATE TABLE IF NOT EXISTS budgets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID          NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    period_id       UUID          NOT NULL REFERENCES financial_periods(id),
    category_id     INTEGER       REFERENCES account_categories(id),
    cost_centre_id  UUID          REFERENCES cost_centres(id),
    mission_node_id UUID,   -- FK to mission_nodes added in V38
    amount          NUMERIC(15,2) NOT NULL,
    notes           TEXT,
    UNIQUE (org_id, period_id, category_id, cost_centre_id)
);

CREATE INDEX IF NOT EXISTS idx_budgets_org_period ON budgets (org_id, period_id);
