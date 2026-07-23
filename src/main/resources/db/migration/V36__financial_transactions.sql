-- V36: Financial transactions journal + payroll tables + backfill from fund_transactions
-- Append-only: no UPDATE or DELETE ever issued against this table.

CREATE TABLE IF NOT EXISTS financial_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID          NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    fund_account_id UUID          REFERENCES fund_accounts(id),
    category_id     INTEGER       REFERENCES account_categories(id),
    cost_centre_id  UUID          REFERENCES cost_centres(id),
    mission_node_id UUID,   -- FK to mission_nodes added in V38
    source_type     VARCHAR(30)   NOT NULL,
    source_id       UUID,
    transfer_group  UUID,
    type            VARCHAR(20)   NOT NULL,  -- INCOME | EXPENSE | TRANSFER | ADJUSTMENT | REFUND
    amount          NUMERIC(15,2) NOT NULL,
    currency        CHAR(3)       NOT NULL DEFAULT 'GBP',
    effective_date  DATE          NOT NULL,
    description     TEXT,
    created_by      UUID          REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    metadata        JSONB         NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_fin_tx_org_date      ON financial_transactions (org_id, effective_date DESC);
CREATE INDEX IF NOT EXISTS idx_fin_tx_source        ON financial_transactions (source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_fin_tx_fund_account  ON financial_transactions (fund_account_id) WHERE fund_account_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_fin_tx_metadata_gin  ON financial_transactions USING GIN (metadata);

-- Wire expense.tx_id FK now that financial_transactions exists
ALTER TABLE expenses ADD CONSTRAINT IF NOT EXISTS fk_expenses_tx
    FOREIGN KEY (tx_id) REFERENCES financial_transactions(id);

-- Backfill fund_transactions into financial_transactions as legacy rows
INSERT INTO financial_transactions (id, org_id, fund_account_id, source_type, source_id, type,
                                    amount, currency, effective_date, description, created_by, created_at)
SELECT
    gen_random_uuid(),
    fa.organisation_id,
    ft.fund_account_id,
    'FUND_TX_LEGACY',
    ft.id,
    CASE WHEN ft.transaction_type = 'CREDIT' THEN 'INCOME' ELSE 'EXPENSE' END,
    ft.amount,
    ft.currency,
    COALESCE(ft.created_at::date, CURRENT_DATE),
    COALESCE(ft.description, ft.reason),
    ft.created_by,
    ft.created_at
FROM fund_transactions ft
JOIN fund_accounts fa ON ft.fund_account_id = fa.id
WHERE NOT EXISTS (
    SELECT 1 FROM financial_transactions ft2
    WHERE ft2.source_type = 'FUND_TX_LEGACY' AND ft2.source_id = ft.id
);

-- Payroll
CREATE TABLE IF NOT EXISTS payroll_runs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID          NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    period_id   UUID          NOT NULL REFERENCES financial_periods(id),
    run_date    DATE          NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',  -- DRAFT | APPROVED | PAID
    total_gross NUMERIC(15,2) NOT NULL DEFAULT 0,
    created_by  UUID          NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS payroll_items (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id       UUID          NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    person_id    UUID          NOT NULL REFERENCES people(id),
    gross_amount NUMERIC(15,2) NOT NULL,
    deductions   NUMERIC(15,2) NOT NULL DEFAULT 0,
    net_amount   NUMERIC(15,2) NOT NULL,
    tx_id        UUID          REFERENCES financial_transactions(id),
    metadata     JSONB         NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_payroll_items_run ON payroll_items (run_id);
