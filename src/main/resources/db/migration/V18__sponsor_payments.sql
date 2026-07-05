-- M2.3: Payment Reconciliation
-- One row per sponsorship × payment_month, tracks the full lifecycle from EXPECTED to RECEIVED/OVERDUE/WAIVED.

CREATE TABLE sponsor_payments (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    sponsorship_id      UUID           NOT NULL REFERENCES sponsorships(id),
    sponsor_id          UUID           NOT NULL REFERENCES sponsors(id),
    child_id            UUID           NOT NULL REFERENCES children(id),
    payment_month       VARCHAR(7)     NOT NULL,
    status              VARCHAR(10)    NOT NULL DEFAULT 'EXPECTED'
                            CHECK (status IN ('EXPECTED', 'RECEIVED', 'PARTIAL', 'OVERDUE', 'WAIVED', 'PREPAID')),
    expected_amount     NUMERIC(14, 2) NOT NULL,
    expected_currency   CHAR(3)        NOT NULL DEFAULT 'PKR',
    received_amount     NUMERIC(14, 2),
    received_currency   CHAR(3),
    bank_reference      VARCHAR(200),
    received_date       DATE,
    waiver_reason       TEXT,
    fund_transaction_id UUID REFERENCES fund_transactions(id),
    ledger_entry_id     UUID REFERENCES ledger_entries(id),
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by          UUID REFERENCES users(id),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_sponsorship_payment_month UNIQUE (sponsorship_id, payment_month)
);

CREATE INDEX idx_sponsor_payments_status_month ON sponsor_payments (status, payment_month);
CREATE INDEX idx_sponsor_payments_sponsorship   ON sponsor_payments (sponsorship_id, payment_month DESC);
CREATE INDEX idx_sponsor_payments_child         ON sponsor_payments (child_id, payment_month DESC);
