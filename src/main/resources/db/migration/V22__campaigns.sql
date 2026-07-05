CREATE TABLE campaigns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    target_amount   NUMERIC(19,4),
    target_currency VARCHAR(10) NOT NULL DEFAULT 'PKR',
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','ACTIVE','FUNDED','CLOSED','ARCHIVED')),
    start_date      DATE,
    end_date        DATE,
    fund_account_id UUID REFERENCES fund_accounts(id),
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_campaigns_org_status ON campaigns(organisation_id, status);
CREATE INDEX idx_campaigns_org        ON campaigns(organisation_id);
