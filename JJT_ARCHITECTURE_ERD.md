# JJT Platform — Final Entity Relationship Diagram

> Architecture frozen 2026-07-11. 20 existing tables + 4 new tables (Phase 2.5 Finance Engine).
> New tables are marked **[NEW]**. Modified columns are marked **[+]**.

---

## Table Legend

| Marker | Meaning |
|--------|---------|
| `PK` | Primary key |
| `FK` | Foreign key |
| `UK` | Unique constraint |
| `IX` | Index |
| **[NEW]** | Added in Phase 2.5 |
| **[+]** | Column added to existing table in Phase 2.5 |

---

## ERD — Full Schema (Mermaid)

```mermaid
erDiagram

    %% ─── CORE IDENTITY ──────────────────────────────────────────────
    organisations {
        UUID id PK
        VARCHAR name
        VARCHAR slug UK
        VARCHAR contact_email
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    users {
        UUID id PK
        UUID organisation_id FK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR role
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    refresh_tokens {
        UUID id PK
        UUID user_id FK
        VARCHAR token_hash UK
        TIMESTAMP expires_at
        TIMESTAMP revoked_at
        TIMESTAMP created_at
    }

    %% ─── CHILD & SPONSORSHIP ────────────────────────────────────────
    children {
        UUID id PK
        UUID organisation_id FK
        VARCHAR full_name
        DATE date_of_birth
        VARCHAR status
        NUMERIC education_amount
        CHAR education_currency
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    education_support_ledgers {
        UUID id PK
        UUID child_id FK UK
        TIMESTAMP created_at
    }

    ledger_entries {
        UUID id PK
        UUID ledger_id FK
        VARCHAR year_month
        NUMERIC education_cost
        CHAR currency
        VARCHAR coverage_type
        UUID created_by FK
        TIMESTAMP created_at
    }

    sponsors {
        UUID id PK
        UUID organisation_id FK
        VARCHAR display_name
        VARCHAR contact_email
        UUID user_id FK
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    sponsorships {
        UUID id PK
        UUID organisation_id FK
        UUID child_id FK
        UUID sponsor_id FK
        VARCHAR commitment_type
        VARCHAR status
        DATE start_date
        DATE end_date
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    progress_updates {
        UUID id PK
        UUID child_id FK
        UUID organisation_id FK
        VARCHAR academic_year
        INT term
        TEXT narrative
        UUID created_by FK
        TIMESTAMP created_at
    }

    sponsor_payments {
        UUID id PK
        UUID sponsorship_id FK
        UUID sponsor_id FK
        UUID child_id FK
        VARCHAR payment_month
        VARCHAR status
        NUMERIC expected_amount
        CHAR expected_currency
        NUMERIC received_amount
        VARCHAR bank_reference
        DATE received_date
        UUID fund_transaction_id FK
        UUID ledger_entry_id FK
        UUID created_by FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% ─── FUND MANAGEMENT ────────────────────────────────────────────
    fund_accounts {
        UUID id PK
        UUID organisation_id FK
        VARCHAR name
        CHAR currency
        NUMERIC min_reserve
        VARCHAR purpose_type
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    fund_transactions {
        UUID id PK
        UUID fund_account_id FK
        VARCHAR type
        NUMERIC amount
        CHAR currency
        VARCHAR reason
        TEXT description
        VARCHAR external_reference
        UUID ledger_entry_id FK
        UUID programme_id FK
        UUID created_by FK
        TIMESTAMP created_at
    }

    %% ─── DONATIONS ──────────────────────────────────────────────────
    donors {
        UUID id PK
        UUID organisation_id FK
        VARCHAR display_name
        VARCHAR contact_email
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    donation_receipt_sequences {
        UUID id PK
        UUID organisation_id FK UK
        INT next_sequence_number
        TIMESTAMP updated_at
    }

    recurring_donation_schedules {
        UUID id PK
        UUID donor_id FK
        UUID organisation_id FK
        NUMERIC amount
        CHAR currency
        VARCHAR frequency
        VARCHAR status
        DATE next_due_date
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    donations {
        UUID id PK
        UUID donor_id FK
        UUID organisation_id FK
        UUID fund_transaction_id FK
        NUMERIC amount
        CHAR currency
        VARCHAR receipt_number UK
        DATE donation_date
        VARCHAR status
        UUID created_by FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% ─── CAMPAIGNS ──────────────────────────────────────────────────
    campaigns {
        UUID id PK
        UUID organisation_id FK
        VARCHAR name
        TEXT description
        DATE start_date
        DATE end_date
        NUMERIC target_amount
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% ─── NOTIFICATIONS & AUDIT ──────────────────────────────────────
    email_notifications {
        UUID id PK
        UUID organisation_id FK
        VARCHAR recipient_email
        VARCHAR template_type
        VARCHAR status
        JSONB payload
        TIMESTAMP sent_at
        TIMESTAMP created_at
    }

    admin_alerts {
        UUID id PK
        UUID organisation_id FK
        VARCHAR alert_type
        VARCHAR severity
        VARCHAR title
        TEXT body
        UUID entity_id
        VARCHAR entity_type
        VARCHAR status
        TIMESTAMP resolved_at
        TIMESTAMP created_at
    }

    audit_events {
        UUID id PK
        UUID organisation_id FK
        UUID actor_id FK
        VARCHAR action
        VARCHAR entity_type
        UUID entity_id
        JSONB metadata
        TIMESTAMP created_at
    }

    %% ─── PHASE 2.5: FINANCE ENGINE [NEW] ────────────────────────────
    programmes {
        UUID id PK
        UUID organisation_id FK
        VARCHAR name
        TEXT description
        VARCHAR classification
        TEXT objective
        UUID primary_fund_account_id FK
        VARCHAR status
        DATE start_date
        DATE end_date
        UUID manager_id FK
        UUID created_by FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    expense_categories {
        UUID id PK
        UUID organisation_id FK
        VARCHAR code UK
        VARCHAR name
        VARCHAR category_type
        BOOLEAN is_active
        TIMESTAMP created_at
    }

    budget_lines {
        UUID id PK
        UUID programme_id FK
        UUID expense_category_id FK
        UUID fund_account_id FK
        VARCHAR budget_period
        NUMERIC planned_amount
        CHAR currency
        TEXT notes
        UUID created_by FK
        TIMESTAMP created_at
    }

    programme_expenses {
        UUID id PK
        UUID organisation_id FK
        UUID programme_id FK
        UUID expense_category_id FK
        UUID fund_account_id FK
        UUID fund_transaction_id FK
        NUMERIC amount
        CHAR currency
        DATE expense_date
        TEXT description
        VARCHAR reference
        VARCHAR status
        UUID approved_by FK
        UUID created_by FK
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    %% ─── RELATIONSHIPS ──────────────────────────────────────────────

    organisations ||--o{ users : "has"
    organisations ||--o{ children : "manages"
    organisations ||--o{ sponsors : "has"
    organisations ||--o{ fund_accounts : "owns"
    organisations ||--o{ campaigns : "runs"
    organisations ||--o{ donors : "receives from"
    organisations ||--o{ programmes : "runs"
    organisations ||--o{ expense_categories : "defines"
    organisations ||--o{ programme_expenses : "incurs"

    users ||--o{ refresh_tokens : "holds"
    users ||--o| sponsors : "may be"

    children ||--|| education_support_ledgers : "has one"
    education_support_ledgers ||--o{ ledger_entries : "contains"
    children ||--o{ sponsorships : "receives"
    children ||--o{ sponsor_payments : "associated with"
    children ||--o{ progress_updates : "has"

    sponsors ||--o{ sponsorships : "makes"
    sponsors ||--o{ sponsor_payments : "owes"

    sponsorships ||--o{ sponsor_payments : "generates"

    fund_accounts ||--o{ fund_transactions : "records"
    fund_accounts ||--o{ budget_lines : "funds"
    fund_accounts ||--o{ programme_expenses : "debited by"
    fund_accounts ||--o{ programmes : "primary fund for"

    fund_transactions ||--o| ledger_entries : "linked to"
    fund_transactions ||--o| programmes : "attributed to"
    fund_transactions ||--o| donations : "backs"
    fund_transactions ||--o| sponsor_payments : "backs"
    fund_transactions ||--o| programme_expenses : "backs"

    donors ||--o{ recurring_donation_schedules : "has"
    donors ||--o{ donations : "makes"
    organisations ||--|| donation_receipt_sequences : "has sequence"

    programmes ||--o{ budget_lines : "has"
    programmes ||--o{ programme_expenses : "incurs"
    expense_categories ||--o{ budget_lines : "used in"
    expense_categories ||--o{ programme_expenses : "classifies"
```

---

## Table Inventory

### Group 1 — Identity & Auth (3 tables)

| Table | Purpose |
|-------|---------|
| `organisations` | Multi-tenant root entity |
| `users` | Platform users (JJT_ADMIN, ORG_ADMIN, SPONSOR) |
| `refresh_tokens` | Opaque refresh tokens stored as SHA-256 hash |

### Group 2 — Child & Sponsorship (6 tables)

| Table | Purpose |
|-------|---------|
| `children` | Children under care; `education_amount` drives expected payment |
| `education_support_ledgers` | One-per-child container; append-only |
| `ledger_entries` | Immutable monthly record: SPONSOR, EARLY_SUPPORT, CORRECTION |
| `sponsors` | Sponsors linked optionally to a user account |
| `sponsorships` | Child ↔ Sponsor relationship with lifecycle status |
| `progress_updates` | Term-by-term academic narrative reports |

### Group 3 — Payment Reconciliation (1 table)

| Table | Purpose |
|-------|---------|
| `sponsor_payments` | One row per sponsorship × month; EXPECTED → RECEIVED/OVERDUE/WAIVED |

### Group 4 — Fund Management (2 tables)

| Table | Purpose |
|-------|---------|
| `fund_accounts` | Named fund pots; balance = SUM(CREDIT) − SUM(DEBIT) |
| `fund_transactions` | Append-only journal; `programme_id` FK added in Phase 2.5 **[+]** |

### Group 5 — Donations (4 tables)

| Table | Purpose |
|-------|---------|
| `donors` | One-off and recurring donors |
| `donations` | Individual donation records with receipt number |
| `donation_receipt_sequences` | Per-org auto-increment for receipt numbering |
| `recurring_donation_schedules` | Scheduled recurring commitments |

### Group 6 — Campaigns (1 table)

| Table | Purpose |
|-------|---------|
| `campaigns` | Fundraising campaigns with targets and date ranges |

### Group 7 — Notifications & Audit (3 tables)

| Table | Purpose |
|-------|---------|
| `email_notifications` | Outbound email log with template type and JSONB payload |
| `admin_alerts` | In-app alerts (PAYMENT_OVERDUE, FUND_BELOW_RESERVE, etc.) |
| `audit_events` | Immutable event log with JSONB metadata |

### Group 8 — Finance Engine **[NEW]** (4 tables)

| Table | Purpose |
|-------|---------|
| `programmes` | Named activities/projects under an organisation |
| `expense_categories` | Reusable taxonomy: PERSONNEL, FACILITIES, PROGRAMME_DELIVERY, etc. |
| `budget_lines` | Planned spend per programme × category × period |
| `programme_expenses` | Actual expenses linking to fund_transactions journal |

---

## Phase 2.5 — Additive Column Changes

```sql
-- fund_accounts: classify the purpose of each fund pot
ALTER TABLE fund_accounts
    ADD COLUMN purpose_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL'
        CHECK (purpose_type IN ('EDUCATION', 'OPERATIONAL', 'RESERVE', 'GENERAL'));

-- fund_transactions: optional attribution to a programme
-- Nullable: existing rows are unaffected; income tagged post-recording
ALTER TABLE fund_transactions
    ADD COLUMN programme_id UUID REFERENCES programmes(id);
```

---

## Key Invariants

| Invariant | Enforcement |
|-----------|------------|
| Ledger entries are append-only | Domain (`appendEntry()` returns new instance) + PostgreSQL RULE |
| Fund transactions are append-only | PostgreSQL RULE; no UPDATE/DELETE ever issued |
| One ledger entry per child per month | `UNIQUE (ledger_id, year_month)` in `ledger_entries` |
| One payment record per sponsorship per month | `UNIQUE (sponsorship_id, payment_month)` |
| Only one active/pending sponsorship per child | Partial unique index: `WHERE status IN ('ACTIVE','PENDING')` |
| Fund balance never goes below `min_reserve` | `assertSufficientFunds()` before every debit |
| Budget lines unique per programme × category × period | `UNIQUE (programme_id, expense_category_id, budget_period)` |
| Income is a single CREDIT — never split | Architecture decision; `programme_expenses` records costs separately |

---

## Programme P&L Formula

```
Programme Revenue  = SUM(fund_transactions.amount WHERE programme_id = X AND type = 'CREDIT')
Programme Expenses = SUM(fund_transactions.amount WHERE programme_id = X AND type = 'DEBIT')
Programme P&L      = Revenue − Expenses
```

```
Transparency Ratio = SUM(programme_expenses.amount WHERE fund_account.purpose_type = 'EDUCATION')
                     / SUM(programme_expenses.amount)
```
