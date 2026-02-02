CREATE TABLE children (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    education_amount NUMERIC(12,2) NOT NULL,
    education_currency CHAR(3) NOT NULL
);

CREATE TABLE education_support_ledgers (
    id UUID PRIMARY KEY,
    child_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_ledger_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE RESTRICT
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    ledger_id UUID NOT NULL,
    child_id UUID NOT NULL,
    entry_month CHAR(7) NOT NULL,
    education_amount NUMERIC(12,2) NOT NULL,
    education_currency CHAR(3) NOT NULL,
    CONSTRAINT fk_entry_ledger FOREIGN KEY (ledger_id) REFERENCES education_support_ledgers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_entry_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE RESTRICT,
    CONSTRAINT uk_ledger_month UNIQUE (ledger_id, entry_month)
);

CREATE TABLE sponsors (
    id UUID PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL
);

CREATE TABLE sponsorships (
    id UUID PRIMARY KEY,
    sponsor_id UUID NOT NULL,
    child_id UUID NOT NULL,
    start_month CHAR(7) NOT NULL,
    CONSTRAINT fk_sponsorship_sponsor FOREIGN KEY (sponsor_id) REFERENCES sponsors(id) ON DELETE RESTRICT,
    CONSTRAINT fk_sponsorship_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE RESTRICT,
    CONSTRAINT uk_sponsor_child_start UNIQUE (sponsor_id, child_id, start_month)
);

CREATE TABLE progress_updates (
    id UUID PRIMARY KEY,
    child_id UUID NOT NULL,
    update_month CHAR(7) NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    CONSTRAINT fk_progress_child FOREIGN KEY (child_id) REFERENCES children(id) ON DELETE RESTRICT,
    CONSTRAINT uk_child_month UNIQUE (child_id, update_month)
);
