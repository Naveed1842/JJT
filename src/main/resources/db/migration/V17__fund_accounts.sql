-- M2.2: Fund Accounting MVP
-- Creates fund_accounts and fund_transactions tables.
-- fund_transactions is append-only (same pattern as ledger_entries).

CREATE TABLE fund_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200)   NOT NULL,
    currency        CHAR(3)        NOT NULL DEFAULT 'PKR',
    min_reserve     NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    organisation_id UUID REFERENCES organisations(id),
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE fund_transactions (
    id                 UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    fund_account_id    UUID           NOT NULL REFERENCES fund_accounts(id),
    transaction_type   VARCHAR(10)    NOT NULL CHECK (transaction_type IN ('CREDIT', 'DEBIT')),
    amount             NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    currency           CHAR(3)        NOT NULL DEFAULT 'PKR',
    reason             VARCHAR(100)   NOT NULL,
    description        TEXT,
    external_reference VARCHAR(200),
    ledger_entry_id    UUID REFERENCES ledger_entries(id),
    created_by         UUID REFERENCES users(id),
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fund_txn_account_date ON fund_transactions (fund_account_id, created_at DESC);

CREATE RULE no_update_fund_transactions AS ON UPDATE TO fund_transactions DO INSTEAD NOTHING;
CREATE RULE no_delete_fund_transactions AS ON DELETE TO fund_transactions DO INSTEAD NOTHING;
