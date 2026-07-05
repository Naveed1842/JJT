-- M2.5: Donation Management
-- Creates donors, recurring schedules, receipt sequence tracker, and donations tables.

CREATE TABLE donors (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    display_name    VARCHAR(255) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(50),
    donor_type      VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL'
                        CHECK (donor_type IN ('INDIVIDUAL', 'CORPORATE', 'TRUST', 'ANONYMOUS')),
    notes           TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_donors_org_id ON donors(organisation_id);
CREATE INDEX idx_donors_email  ON donors(email) WHERE email IS NOT NULL;

-- Tracks the last-used sequential receipt number per org per year.
-- Receipt format: {ORG_SLUG_UPPER}-{YEAR}-{SEQUENCE:04d}  e.g. JJT-2026-0001
CREATE TABLE donation_receipt_sequences (
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    year            INT  NOT NULL,
    last_sequence   INT  NOT NULL DEFAULT 0,
    PRIMARY KEY (organisation_id, year)
);

CREATE TABLE recurring_donation_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    donor_id        UUID NOT NULL REFERENCES donors(id),
    donation_type   VARCHAR(30) NOT NULL
                        CHECK (donation_type IN ('GENERAL','ZAKAT','SADAQAH','SPONSORSHIP_TOP_UP','CORPORATE','IN_KIND')),
    amount          NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    currency        CHAR(3) NOT NULL DEFAULT 'PKR',
    frequency       VARCHAR(20) NOT NULL DEFAULT 'MONTHLY'
                        CHECK (frequency IN ('MONTHLY','QUARTERLY','ANNUAL')),
    start_date      DATE NOT NULL,
    end_date        DATE,
    next_due_date   DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','PAUSED','COMPLETED','CANCELLED')),
    fund_account_id UUID REFERENCES fund_accounts(id),
    notes           TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recurring_org      ON recurring_donation_schedules(organisation_id);
CREATE INDEX idx_recurring_next_due ON recurring_donation_schedules(next_due_date)
    WHERE status = 'ACTIVE';

CREATE TABLE donations (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id       UUID NOT NULL REFERENCES organisations(id),
    donor_id              UUID REFERENCES donors(id),
    donation_type         VARCHAR(30) NOT NULL
                              CHECK (donation_type IN ('GENERAL','ZAKAT','SADAQAH','SPONSORSHIP_TOP_UP','CORPORATE','IN_KIND')),
    amount                NUMERIC(14, 2) NOT NULL CHECK (amount > 0),
    currency              CHAR(3) NOT NULL DEFAULT 'PKR',
    donation_date         DATE NOT NULL,
    receipt_number        VARCHAR(30),
    notes                 TEXT,
    fund_account_id       UUID REFERENCES fund_accounts(id),
    fund_transaction_id   UUID REFERENCES fund_transactions(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'RECEIPTED'
                              CHECK (status IN ('EXPECTED','RECEIPTED','REVERSED')),
    recurring_schedule_id UUID REFERENCES recurring_donation_schedules(id),
    created_by            UUID REFERENCES users(id),
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by            UUID REFERENCES users(id),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_donations_org_id    ON donations(organisation_id);
CREATE INDEX idx_donations_donor_id  ON donations(donor_id) WHERE donor_id IS NOT NULL;
CREATE INDEX idx_donations_date      ON donations(organisation_id, donation_date DESC);
CREATE UNIQUE INDEX idx_donations_receipt ON donations(receipt_number) WHERE receipt_number IS NOT NULL;
CREATE INDEX idx_donations_recurring ON donations(recurring_schedule_id) WHERE recurring_schedule_id IS NOT NULL;
