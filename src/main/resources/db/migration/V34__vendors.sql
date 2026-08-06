-- V34: Vendors directory (V30-V33 are media tables)

CREATE TABLE IF NOT EXISTS vendors (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID         NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    name          VARCHAR(255) NOT NULL,
    contact_name  VARCHAR(100),
    email         VARCHAR(255),
    phone         VARCHAR(50),
    bank_account  VARCHAR(100),  -- stored encrypted by application
    tax_id        VARCHAR(50),
    active        BOOLEAN NOT NULL DEFAULT true,
    metadata      JSONB   NOT NULL DEFAULT '{}',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_vendors_org_active ON vendors (org_id, active);
CREATE INDEX IF NOT EXISTS idx_vendors_org_name   ON vendors (org_id, LOWER(name));
