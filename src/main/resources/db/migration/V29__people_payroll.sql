-- V29: People directory + payroll profiles (independent of system users)

CREATE TABLE IF NOT EXISTS people (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID         NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    kind          VARCHAR(20)  NOT NULL,  -- EMPLOYEE | TEACHER | CONTRACTOR | VOLUNTEER | BOARD_MEMBER
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255),
    phone         VARCHAR(50),
    national_id   VARCHAR(100),           -- stored encrypted by application
    active        BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_people_org_active ON people (org_id, active);

CREATE TABLE IF NOT EXISTS payroll_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id       UUID NOT NULL UNIQUE REFERENCES people(id) ON DELETE CASCADE,
    salary_type     VARCHAR(20)  NOT NULL,  -- MONTHLY | HOURLY | DAILY | STIPEND
    amount          NUMERIC(15,2) NOT NULL,
    currency        CHAR(3)       NOT NULL DEFAULT 'GBP',
    cost_centre_id  UUID REFERENCES cost_centres(id),
    payment_method  VARCHAR(20)   NOT NULL DEFAULT 'BANK_TRANSFER',
    bank_account    VARCHAR(100),           -- stored encrypted by application
    active          BOOLEAN NOT NULL DEFAULT true
);
