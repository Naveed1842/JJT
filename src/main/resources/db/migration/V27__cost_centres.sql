-- V27: Cost centres + mark fund_accounts as restricted/unrestricted

CREATE TABLE IF NOT EXISTS cost_centres (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id    UUID NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    code      VARCHAR(20)  NOT NULL,
    name      VARCHAR(100) NOT NULL,
    parent_id UUID REFERENCES cost_centres(id),
    active    BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (org_id, code)
);

ALTER TABLE fund_accounts ADD COLUMN IF NOT EXISTS restricted BOOLEAN NOT NULL DEFAULT false;
