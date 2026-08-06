-- V35: Expenses, generic attachments, approval engine

-- Generic attachment table (polymorphic: owner_type + owner_id)
CREATE TABLE IF NOT EXISTS attachments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id       UUID         NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    owner_type   VARCHAR(50)  NOT NULL,  -- EXPENSE | VENDOR | CHILD | SPONSOR | CAMPAIGN | …
    owner_id     UUID         NOT NULL,
    kind         VARCHAR(30)  NOT NULL DEFAULT 'OTHER',
    filename     VARCHAR(255) NOT NULL,
    storage_ref  VARCHAR(500) NOT NULL,
    size_bytes   INTEGER,
    content_hash VARCHAR(64),
    uploaded_by  UUID REFERENCES users(id),
    uploaded_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_attachments_owner ON attachments (owner_type, owner_id);

-- Approval engine (platform-wide; consumed by expenses, payroll, future modules)
CREATE TABLE IF NOT EXISTS approval_requests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id       UUID        NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    entity_type  VARCHAR(30) NOT NULL,  -- EXPENSE | PAYROLL_RUN | …
    entity_id    UUID        NOT NULL,
    status       VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    requested_by UUID        NOT NULL REFERENCES users(id),
    approved_by  UUID REFERENCES users(id),
    reviewed_at  TIMESTAMP WITH TIME ZONE,
    notes        TEXT,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_approval_requests_org_status ON approval_requests (org_id, status);
CREATE INDEX IF NOT EXISTS idx_approval_requests_entity     ON approval_requests (entity_type, entity_id);

CREATE TABLE IF NOT EXISTS approval_check_results (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    request_id  UUID        NOT NULL REFERENCES approval_requests(id) ON DELETE CASCADE,
    check_type  VARCHAR(40) NOT NULL,
    verdict     VARCHAR(10) NOT NULL,  -- APPROVE | REVIEW | REJECT
    confidence  NUMERIC(4,3) NOT NULL DEFAULT 1.000,
    explanation JSONB NOT NULL DEFAULT '{}',
    ran_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Expenses
CREATE TABLE IF NOT EXISTS expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID          NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    vendor_id       UUID          REFERENCES vendors(id),
    payee_text      VARCHAR(255),
    category_id     INTEGER       REFERENCES account_categories(id),
    cost_centre_id  UUID          REFERENCES cost_centres(id),
    mission_node_id UUID,   -- FK to mission_nodes added in V38
    period_id       UUID          REFERENCES financial_periods(id),
    invoice_ref     VARCHAR(100),
    amount          NUMERIC(15,2) NOT NULL,
    currency        CHAR(3)       NOT NULL DEFAULT 'GBP',
    description     TEXT,
    status          VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    paid_at         DATE,
    tx_id           UUID,   -- FK to financial_transactions added in V36
    created_by      UUID          NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    metadata        JSONB         NOT NULL DEFAULT '{}',
    CONSTRAINT chk_expense_payee CHECK (vendor_id IS NOT NULL OR payee_text IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_expenses_org_status   ON expenses (org_id, status);
CREATE INDEX IF NOT EXISTS idx_expenses_org_period   ON expenses (org_id, period_id);
CREATE INDEX IF NOT EXISTS idx_expenses_vendor       ON expenses (vendor_id) WHERE vendor_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_expenses_metadata_gin ON expenses USING GIN (metadata);
