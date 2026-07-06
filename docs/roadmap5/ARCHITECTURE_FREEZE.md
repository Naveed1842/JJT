# Financial Operations & Transparency Platform
## Architecture Freeze — Complete Reference Package

> **Status:** FROZEN — 2026-07-07
> All structural decisions below are approved and locked for implementation.
> Changes require a new ADR entry. Enhancement proceeds through extension points only.

---

# SECTION 1 — FINAL ARCHITECTURE DOCUMENT

## 1.1 Vision Statement

The Financial Operations & Transparency Platform is a **generic, multi-layered financial engine** built atop JJT's existing donation and sponsorship core. It is not a charity module. It is a platform that expresses industry-specific behaviour through configuration, taxonomy packs, and business rules — with the charity pack being its first vertical.

The platform rests on five permanent business pillars:

| Pillar | What it owns |
|---|---|
| **People** | Identities, roles, relationships, payroll profiles |
| **Money** | Transactions, accounts, budgets, periods, reconciliation |
| **Mission** | Programmes, campaigns, impact hierarchy, objectives |
| **Impact** | Metrics, beneficiary counts, cost-per-outcome calculations |
| **AI** | Anomaly detection, forecasting, policy compliance, recommendations |

Every future module belongs to one or more pillars. This is the architectural north star.

## 1.2 Layering

```
┌─────────────────────────────────────────────────────┐
│  Layer 3 — Industry Packs                           │
│  Charity | School | SME | Healthcare | Retail       │
│  (configuration, templates, taxonomy seeds)         │
├─────────────────────────────────────────────────────┤
│  Layer 2 — Organisation Modules                     │
│  People · Vendor · Mission · Impact · Approval      │
│  Transparency · Payroll · Budget · Attachment       │
├─────────────────────────────────────────────────────┤
│  Layer 1 — Generic Financial Core                   │
│  financial_transactions · account_categories        │
│  fund_accounts · cost_centres · financial_periods   │
│  attachments · approval_requests                    │
└─────────────────────────────────────────────────────┘
│  Existing Platform Core (untouched)                 │
│  sponsorships · ledger_entries · donations          │
│  children · sponsors · organisations · users        │
└─────────────────────────────────────────────────────┘
```

Layer 1 has zero charity concepts. Layer 2 is generic business operations. Layer 3 is thin — almost entirely seed data and configuration rather than code.

## 1.3 Core Design Constraints (permanent)

1. **Append-only journal** — `financial_transactions` is never updated or deleted. Corrections = ADJUSTMENT rows. Reversals = REFUND rows. Same principle already proven in `ledger_entries`.
2. **No hardcoded public figures** — every percentage, ratio, and metric on the public Trust page is computed from real transaction data.
3. **AI never approves** — every `approval_requests` row requires a human `approved_by` FK to resolve. AI writes to `approval_check_results`, never to the approval decision column.
4. **Domain layer stays pure** — `core/domain` has zero Spring or JPA imports. The financial domain follows this same constraint.
5. **Flyway owns the schema** — `hibernate.ddl-auto=none` always. Every schema change = a numbered migration.
6. **One approval engine** — Finance is the first consumer. Procurement, HR, Leave, Campaigns reuse the same engine. No module gets its own approval tables.
7. **One attachment service** — generic `attachments` table with `owner_type`/`owner_id`. No finance-specific attachment tables.

---

# SECTION 2 — ARCHITECTURE DECISION RECORDS

---

## ADR-001: Evolve `fund_transactions` Rather Than Replace

**Context:** The existing `fund_transactions` table records all monetary movements. A new `financial_transactions` table with richer typing was proposed.

**Decision:** Introduce `financial_transactions` as the forward record of all new expense, transfer, and adjustment activity. Backfill existing `fund_transactions` rows into it via a Flyway migration with a `source_type='FUND_TX_LEGACY'` marker. The old table is kept read-only for six months, then dropped in a future migration.

**Alternatives Considered:**
- Alter `fund_transactions` in place — rejected; altering a live financial journal mid-flight is the highest-risk possible change.
- Shadow copy and cutover — accepted as the mechanism; the "backfill" migration IS the shadow copy.

**Rationale:** A clean cutover on an empty staging DB costs nothing. The backfill migration runs once and is idempotent. After it, all new code reads from one table.

**Consequences:** A two-week window where both tables exist. Application code reads from `financial_transactions` exclusively post-migration. Rollback = drop the new table and revert the backfill migration (safe, additive).

---

## ADR-002: People Domain Is Independent of Finance

**Context:** Payroll requires identity records (name, role classification, contact). Finance was tempted to own a `staff` table.

**Decision:** `people` is a first-class domain. It has its own entity, its own service, and its own admin section. `payroll_profile` (salary terms, cost centre allocation, payment method) is a 1:1 extension of `people`. Finance reads `people` via FK. Finance does not own `people`.

**Alternatives Considered:**
- Embed staff in payroll tables — rejected; HR will need the same records and would re-create them.
- Use existing `users` table — rejected; most workers are not system users (volunteers, teachers, contractors).

**Rationale:** HR is milestone H1 (after Roadmap 5). If `people` lives in Finance, HR pays a coupling debt. The separation costs three extra entity classes today and saves an extraction later.

**Consequences:** `people` becomes a shared kernel dependency. Modules that need person identity import from the People domain, not from Finance.

---

## ADR-003: Vendor Domain With Incremental Quality

**Context:** Current expenses use a free-text `payee` field. A `vendors` table was proposed.

**Decision:** `vendors` table is introduced in F1. Expenses gain a `vendor_id` FK (nullable). Free-text `payee` remains as a fallback and as the "quick create" path — entering a new payee name in the expense form silently creates a minimal `vendors` row, so data quality grows organically without a mandatory upfront vendor-registration gate.

**Alternatives Considered:**
- Mandatory vendor-first — rejected; operational friction kills adoption on first deploy.
- Keep free-text forever — rejected; kills vendor spend analysis and procurement readiness.

**Rationale:** The nullable FK with organic creation is the Pareto solution: zero friction now, structured data over time.

**Consequences:** Reports on vendor spend are possible once quick-created vendors are enriched. Procurement module slots in by changing the nullable FK to a lookup with additional vendor detail (bank account, contracts) — no schema change to `expenses` needed.

---

## ADR-004: Single Approval Engine

**Context:** Expenses need approval. Payroll runs need approval. Future procurement POs need approval. Three separate approval implementations were the naive path.

**Decision:** `approval_requests` + `approval_check_results` become a platform-level engine. Each request has: `entity_type` (EXPENSE, PAYROLL_RUN, PURCHASE_ORDER, …), `entity_id`, `status` (SUBMITTED → RECOMMENDED → APPROVED/REJECTED), `requested_by`, `approved_by`, `reviewed_at`. The check results table records each automated check with verdict, confidence, and explanation JSONB.

**Alternatives Considered:**
- Per-module status columns — rejected; duplicates workflow logic, no unified approval queue.
- External workflow engine (Camunda, Activiti) — rejected; introduces a new runtime dependency before the need is proven.

**Rationale:** The engine is simple today (two deterministic checks). Making it a platform table costs one extra JOIN. It prevents three future extractions.

**Consequences:** All future approval surfaces (HR, procurement, leave) are add-and-configure rather than build-from-scratch. The `AdminApprovalQueueComponent` becomes a reusable shell filtered by `entity_type`.

---

## ADR-005: Transparency Snapshots Not Live Queries

**Context:** The public Trust page needs programme/admin/fundraising percentages and cost-per-beneficiary figures.

**Decision:** `transparency_snapshots` table stores pre-computed figures per organisation per period. Snapshots are generated nightly via `@Scheduled` and on every period close. The public API reads snapshots, not live aggregates. A minimum-data-threshold guard prevents publishing snapshots from periods with fewer than N transactions.

**Alternatives Considered:**
- Live aggregation on every public request — rejected; unbounded query cost as transaction history grows; no caching story.
- Hardcoded percentages — already rejected by architectural principle.

**Rationale:** Snapshots are auditable (point-in-time record of what was published), fast (single-row read), and replayable (re-run the snapshot job to correct a figure after a late transaction is posted).

**Consequences:** A window of up to 24 hours where published figures are one day old. This is disclosed in the UI as "as of [date]." Period-close snapshots are always up-to-date.

---

## ADR-006: Mission Hierarchy as a Self-Referential Tree

**Context:** JJT operates "Education Mission → Sponsorship Programme → Ramadan Drive." A school client would have "Academics → Grade 5 → Science Lab." A generic tree is needed.

**Decision:** `mission_nodes(id, org_id, parent_id, kind, name, description, status, metadata JSONB)` where `kind ∈ {MISSION, PROGRAMME, PROJECT, CAMPAIGN, ACTIVITY}`. Depth is enforced by application logic (max 5 levels), not schema. Existing `campaigns` table gains `mission_node_id FK` (nullable) — campaigns become leaves of the mission tree.

**Alternatives Considered:**
- Separate tables per level — rejected; inflexible, breaks at hierarchy reuse across verticals.
- Materialized path — considered; deferred to F5 when reporting traversal becomes a performance concern.

**Rationale:** Self-referential trees are well understood, easy to query recursively in PostgreSQL (`WITH RECURSIVE`), and require no schema change when the tree depth policy changes.

**Consequences:** Reports that aggregate "everything under Programme X" use recursive CTEs. These are indexed by `parent_id`. Performance is acceptable for tree depths of ≤5 and orgs of ≤10,000 transaction rows per period.

---

## ADR-007: JSONB Metadata on Core Entities

**Context:** AI checks need entity-level feature bags. Business users need ad-hoc categorisation (tags, flags, notes).

**Decision:** `metadata JSONB` column (default `'{}'`) on: `financial_transactions`, `expenses`, `payroll_items`, `vendors`, `mission_nodes`, `approval_check_results`. No constraints on the JSONB content — it is a bag, not a schema. GIN index on `expenses.metadata` and `financial_transactions.metadata` for AI query patterns.

**Alternatives Considered:**
- EAV (entity-attribute-value) table — rejected; every lookup is a JOIN and a subquery.
- Application-level serialisation in a TEXT column — rejected; loses DB-native JSON operators.

**Rationale:** JSONB with GIN index is the PostgreSQL idiomatic answer to this problem. The field is invisible to normal application flows and costs one column per table.

**Consequences:** AI check implementations can query `metadata @> '{"flagged": true}'` without schema changes. Abuse potential (large blobs) is mitigated by an application-layer size guard (max 4KB per metadata object).

---

# SECTION 3 — DOMAIN MODEL

## 3.1 Core Entities

```
financial_transactions
  id              UUID PK
  org_id          FK → organisations
  fund_account_id FK → fund_accounts
  category_id     FK → account_categories
  cost_centre_id  FK → cost_centres (nullable)
  mission_node_id FK → mission_nodes (nullable)
  source_type     ENUM {DONATION, SPONSOR_PAYMENT, EXPENSE, PAYROLL_ITEM,
                        TRANSFER, FUND_TX_LEGACY, ADJUSTMENT, REFUND}
  source_id       UUID (nullable — points to source row)
  transfer_group  UUID (nullable — pairs TRANSFER rows)
  type            ENUM {INCOME, EXPENSE, TRANSFER, ADJUSTMENT, REFUND}
  amount          NUMERIC(15,2) NOT NULL
  currency        CHAR(3) DEFAULT 'GBP'
  effective_date  DATE NOT NULL
  description     TEXT
  created_by      FK → users
  created_at      TIMESTAMP DEFAULT now()
  metadata        JSONB DEFAULT '{}'

account_categories
  id              SERIAL PK
  org_id          FK → organisations
  code            VARCHAR(10)          -- e.g. '5100', '6200'
  name            VARCHAR(100)
  parent_id       FK → account_categories (nullable — tree)
  reporting_class ENUM {INCOME, PROGRAMME, ADMIN, FUNDRAISING, COGS, OPEX, …}
  is_system       BOOLEAN DEFAULT false
  active          BOOLEAN DEFAULT true

cost_centres
  id              UUID PK
  org_id          FK → organisations
  code            VARCHAR(20)
  name            VARCHAR(100)
  parent_id       FK → cost_centres (nullable)
  active          BOOLEAN DEFAULT true

people
  id              UUID PK
  org_id          FK → organisations
  kind            ENUM {EMPLOYEE, TEACHER, CONTRACTOR, VOLUNTEER, BOARD_MEMBER}
  first_name      VARCHAR(100)
  last_name       VARCHAR(100)
  email           VARCHAR(255) (nullable)
  phone           VARCHAR(50) (nullable)
  national_id     VARCHAR(50) (nullable, encrypted)
  active          BOOLEAN DEFAULT true
  created_at      TIMESTAMP DEFAULT now()

payroll_profiles
  id              UUID PK
  person_id       FK → people (UNIQUE)
  salary_type     ENUM {MONTHLY, HOURLY, DAILY, STIPEND}
  amount          NUMERIC(15,2)
  currency        CHAR(3)
  cost_centre_id  FK → cost_centres (nullable)
  payment_method  ENUM {BANK_TRANSFER, CASH, CHEQUE}
  bank_account    VARCHAR(50) (nullable, encrypted)
  active          BOOLEAN DEFAULT true

vendors
  id              UUID PK
  org_id          FK → organisations
  name            VARCHAR(255)
  contact_name    VARCHAR(100) (nullable)
  email           VARCHAR(255) (nullable)
  phone           VARCHAR(50) (nullable)
  bank_account    VARCHAR(50) (nullable, encrypted)
  tax_id          VARCHAR(50) (nullable)
  active          BOOLEAN DEFAULT true
  metadata        JSONB DEFAULT '{}'

expenses
  id              UUID PK
  org_id          FK → organisations
  vendor_id       FK → vendors (nullable)
  payee_text      VARCHAR(255) (nullable — fallback when vendor_id IS NULL)
  category_id     FK → account_categories
  cost_centre_id  FK → cost_centres (nullable)
  mission_node_id FK → mission_nodes (nullable)
  period_id       FK → financial_periods (nullable)
  invoice_ref     VARCHAR(100) (nullable — procurement seam)
  amount          NUMERIC(15,2) NOT NULL
  currency        CHAR(3) DEFAULT 'GBP'
  description     TEXT
  status          ENUM {DRAFT, SUBMITTED, APPROVED, PAID, REJECTED, VOIDED}
  paid_at         DATE (nullable)
  tx_id           FK → financial_transactions (nullable — set when PAID)
  created_by      FK → users
  created_at      TIMESTAMP
  metadata        JSONB DEFAULT '{}'

payroll_runs
  id              UUID PK
  org_id          FK → organisations
  period_id       FK → financial_periods
  run_date        DATE
  status          ENUM {DRAFT, APPROVED, PAID}
  total_gross     NUMERIC(15,2)
  created_by      FK → users

payroll_items
  id              UUID PK
  run_id          FK → payroll_runs
  person_id       FK → people
  gross_amount    NUMERIC(15,2)
  deductions      NUMERIC(15,2) DEFAULT 0
  net_amount      NUMERIC(15,2)
  tx_id           FK → financial_transactions (nullable — set when PAID)
  metadata        JSONB DEFAULT '{}'

financial_periods
  id              UUID PK
  org_id          FK → organisations
  label           VARCHAR(50)    -- e.g. 'FY2026-Q1'
  period_type     ENUM {MONTHLY, QUARTERLY, ANNUAL}
  start_date      DATE
  end_date        DATE
  status          ENUM {OPEN, CLOSED, LOCKED}
  closed_at       TIMESTAMP (nullable)
  closed_by       FK → users (nullable)

budgets
  id              UUID PK
  org_id          FK → organisations
  period_id       FK → financial_periods
  category_id     FK → account_categories (nullable — null = total budget)
  cost_centre_id  FK → cost_centres (nullable)
  mission_node_id FK → mission_nodes (nullable)
  amount          NUMERIC(15,2)
  notes           TEXT

mission_nodes
  id              UUID PK
  org_id          FK → organisations
  parent_id       FK → mission_nodes (nullable — NULL = root)
  kind            ENUM {MISSION, PROGRAMME, PROJECT, CAMPAIGN, ACTIVITY}
  name            VARCHAR(200)
  description     TEXT
  status          ENUM {DRAFT, ACTIVE, COMPLETED, ARCHIVED}
  start_date      DATE (nullable)
  end_date        DATE (nullable)
  target_amount   NUMERIC(15,2) (nullable)
  metadata        JSONB DEFAULT '{}'

impact_metrics
  id              UUID PK
  mission_node_id FK → mission_nodes
  metric_key      VARCHAR(100)   -- e.g. 'BENEFICIARIES', 'MEALS_SERVED'
  unit            VARCHAR(50)    -- e.g. 'children', 'meals'
  period_id       FK → financial_periods (nullable)
  value_source    ENUM {MANUAL, COUNT_QUERY}
  count_query_key VARCHAR(100) (nullable)  -- resolved by ImpactQueryRegistry
  manual_value    NUMERIC (nullable)
  recorded_at     TIMESTAMP

approval_requests
  id              UUID PK
  org_id          FK → organisations
  entity_type     ENUM {EXPENSE, PAYROLL_RUN, PURCHASE_ORDER, …}
  entity_id       UUID
  status          ENUM {SUBMITTED, RECOMMENDED_APPROVE, RECOMMENDED_REVIEW,
                        RECOMMENDED_REJECT, APPROVED, REJECTED}
  requested_by    FK → users
  approved_by     FK → users (nullable)
  reviewed_at     TIMESTAMP (nullable)
  notes           TEXT (nullable)

approval_check_results
  id              UUID PK
  request_id      FK → approval_requests
  check_type      ENUM {DUPLICATE_HEURISTIC, BUDGET_THRESHOLD, POLICY_COMPLIANCE,
                        ANOMALY_DETECTION, FORECAST_IMPACT}
  verdict         ENUM {APPROVE, REVIEW, REJECT}
  confidence      NUMERIC(4,3)   -- 0.000 – 1.000
  explanation     JSONB          -- {reason, evidence[], recommended_action}
  ran_at          TIMESTAMP

attachments
  id              UUID PK
  org_id          FK → organisations
  owner_type      VARCHAR(50)    -- EXPENSE, VENDOR, CHILD, SPONSOR, CAMPAIGN, …
  owner_id        UUID
  kind            ENUM {INVOICE, RECEIPT, PO, TRANSFER_SLIP, CONTRACT,
                        IMAGE, PDF, NOTE, OTHER}
  filename        VARCHAR(255)
  storage_ref     VARCHAR(500)   -- file store path / S3 key
  size_bytes      INTEGER
  content_hash    VARCHAR(64)    -- SHA-256, tamper evidence
  uploaded_by     FK → users
  uploaded_at     TIMESTAMP

transparency_snapshots
  id              UUID PK
  org_id          FK → organisations
  period_id       FK → financial_periods
  computed_at     TIMESTAMP
  programme_pct   NUMERIC(5,2)
  admin_pct       NUMERIC(5,2)
  fundraising_pct NUMERIC(5,2)
  total_income    NUMERIC(15,2)
  total_expense   NUMERIC(15,2)
  beneficiary_count INTEGER
  cost_per_beneficiary NUMERIC(10,2) (nullable)
  per_programme   JSONB    -- [{nodeId, name, amount, pct, beneficiaries}]
  published       BOOLEAN DEFAULT false
  UNIQUE (org_id, period_id)
```

---

# SECTION 4 — ENTITY RELATIONSHIP DIAGRAM

```
organisations ──────────────────────────────────────────────────────────┐
    │                                                                    │
    ├─ fund_accounts ──────────── financial_transactions ────────────────┤
    │                                     │                             │
    ├─ account_categories (tree)          │                             │
    │         └─ reports_to parent        │                             │
    ├─ cost_centres (tree)                │                             │
    │                                     │                             │
    ├─ people ──── payroll_profiles        │                             │
    │         └─ payroll_items ───────────┘                             │
    │                   └─ payroll_runs                                 │
    │                                                                    │
    ├─ vendors ──── expenses ─────────────┘                             │
    │                  │                                                 │
    ├─ mission_nodes (tree)               │                             │
    │         └─ impact_metrics           │                             │
    │         └─ campaigns (existing, FK)                               │
    │                                                                    │
    ├─ financial_periods                                                 │
    │         └─ budgets                                                 │
    │         └─ transparency_snapshots                                  │
    │                                                                    │
    ├─ approval_requests ──── approval_check_results                    │
    │                                                                    │
    └─ attachments (polymorphic: owner_type + owner_id) ────────────────┘

KEY FOREIGN KEYS (cross-cutting):
  financial_transactions.category_id     → account_categories
  financial_transactions.cost_centre_id  → cost_centres
  financial_transactions.mission_node_id → mission_nodes
  expenses.vendor_id                     → vendors
  expenses.category_id                   → account_categories
  expenses.cost_centre_id                → cost_centres
  expenses.mission_node_id               → mission_nodes
  expenses.period_id                     → financial_periods
  expenses.tx_id                         → financial_transactions
  approval_requests.entity_id            → (polymorphic)
  impact_metrics.mission_node_id         → mission_nodes
  transparency_snapshots.period_id       → financial_periods
```

---

# SECTION 5 — MODULE DEPENDENCY DIAGRAM

```
                    ┌─────────────────┐
                    │  Platform Core  │  ← untouched by Roadmap 5
                    │  (sponsorships, │
                    │   donations,    │
                    │   children,     │
                    │   users, orgs)  │
                    └────────┬────────┘
                             │ reads
           ┌─────────────────┼──────────────────┐
           │                 │                  │
    ┌──────┴───────┐  ┌──────┴───────┐  ┌──────┴───────┐
    │   Financial  │  │    People    │  │   Mission    │
    │     Core     │  │    Domain    │  │    Domain    │
    │  (Layer 1)   │  │  (Layer 2)   │  │  (Layer 2)   │
    └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
           │                 │                  │
           └────────┬────────┘                  │
                    │                           │
           ┌────────┴────────┐         ┌────────┴────────┐
           │    Expenses     │         │     Impact      │
           │    + Vendors    │         │    Metrics      │
           └────────┬────────┘         └────────┬────────┘
                    │                           │
           ┌────────┴────────┐                  │
           │    Payroll      │                  │
           └────────┬────────┘                  │
                    │                           │
           ┌────────┴───────────────────────────┴──┐
           │        Transparency Engine             │
           │     (aggregates Financial + Impact)    │
           └────────────────┬───────────────────────┘
                            │
               ┌────────────┴────────────┐
               │   Public Trust API      │
               │   Executive Dashboard   │
               └─────────────────────────┘

CROSS-CUTTING (no arrows shown; these serve everything):
  Approval Engine  ← consumed by Expenses, Payroll, (future: Procurement, Leave)
  Attachment Service ← consumed by Expenses, Vendors, People, Campaigns, Children
  Audit Service    ← consumed by all write operations (already exists)
  Alert Service    ← consumed by all threshold events (already exists)
  Policy Engine    ← (deferred; seam reserved in approval pipeline)
```

---

# SECTION 6 — SEQUENCE DIAGRAMS

## 6.1 Create and Approve an Expense

```
Controller          ExpenseService       ApprovalEngine      AuditService
    │                    │                    │                   │
    │─ createExpense() ──►│                   │                   │
    │                    │─ validate ─────────►│                  │
    │                    │  (budget check)     │                  │
    │                    │  (vendor lookup     │                  │
    │                    │   / quick-create)   │                  │
    │                    │                    │                   │
    │                    │─ save DRAFT ────────────────────────────────►│
    │                    │                                        │ log(EXPENSE_CREATED)
    │◄─ 201 expense ──────│                                        │
    │                    │                                        │
    │─ submitExpense() ──►│                                        │
    │                    │─ createApprovalRequest() ─────────────►│
    │                    │                    │─ runCheck(DUPLICATE)
    │                    │                    │─ runCheck(BUDGET_THRESHOLD)
    │                    │                    │─ computeRecommendation()
    │                    │                    │─ save check_results
    │                    │◄─ recommendation ───│
    │                    │─ status = SUBMITTED │
    │◄─ 200 ──────────────│                    │
    │                    │                    │                   │
    │─ approveExpense() ──►│                   │                   │
    │  (human)           │─ resolveApproval() ─────────────────────────►│
    │                    │  status = APPROVED │ log(EXPENSE_APPROVED)   │
    │◄─ 200 ──────────────│                    │                   │
    │                    │                    │                   │
    │─ payExpense() ──────►│                   │                   │
    │                    │─ debitFundAccount() │                   │
    │                    │─ insertFinancialTransaction(EXPENSE)   │
    │                    │─ expense.status = PAID                 │
    │                    │─ expense.tx_id = newTx.id              │
    │                    │──────────────────────────────────────────────►│
    │                    │                    │ log(EXPENSE_PAID)        │
    │◄─ 200 ──────────────│                   │                   │
```

## 6.2 Payroll Run

```
Controller          PayrollService       PeopleService       FinancialCore
    │                    │                    │                   │
    │─ createRun(period) ►│                   │                   │
    │                    │─ loadActiveProfiles() ───────────────►│
    │                    │◄─ [payroll_profiles + people] ─────────│
    │                    │─ computeItems() ─────────────────────────────►
    │                    │  (gross, deductions, net per person)   │
    │                    │─ save payroll_run DRAFT ───────────────────────►
    │◄─ 201 run ──────────│                   │                   │
    │                    │                    │                   │
    │─ approveRun() ──────►│                  │                   │
    │                    │─ createApprovalRequest(PAYROLL_RUN)    │
    │                    │  [same pipeline as expenses]           │
    │◄─ 200 ──────────────│                   │                   │
    │                    │                    │                   │
    │─ processRun() ──────►│                  │                   │
    │                    │  for each item:    │                   │
    │                    │   insertFinancialTransaction(EXPENSE)   │
    │                    │   item.tx_id = tx.id                   │
    │                    │─ run.status = PAID │                   │
    │◄─ 200 ──────────────│                   │                   │
```

## 6.3 Transparency Snapshot Generation

```
Scheduler       TransparencyService      FinancialCore        PublicAPI
    │                    │                    │                   │
    │─ @Scheduled ───────►│                   │                   │
    │  (nightly + period  │                   │                   │
    │   close event)      │                   │                   │
    │                    │─ queryExpenses(    │                   │
    │                    │   org, period) ────►│                  │
    │                    │◄─ by reporting_class│                   │
    │                    │                    │                   │
    │                    │─ queryIncome(period)───────────────────►│
    │                    │◄─ total income ─────│                   │
    │                    │                    │                   │
    │                    │─ queryImpact() ─────────────────────────────►
    │                    │◄─ beneficiary_count │                   │
    │                    │                    │                   │
    │                    │─ computeRatios()    │                   │
    │                    │  programmePct = programme / totalExpense│
    │                    │  adminPct = admin / totalExpense        │
    │                    │  fundraisingPct = fundraising / total   │
    │                    │  costPerBeneficiary = total / count     │
    │                    │                    │                   │
    │                    │─ minimumDataGuard() │                   │
    │                    │  (skip publish if  │                   │
    │                    │   totalTx < threshold)                  │
    │                    │                    │                   │
    │                    │─ upsertSnapshot()   │                   │
    │                    │  published = true  │                   │
    │◄────────────────────│                   │                   │
    │                    │                    │                   │
    │                (public request)         │                   │
    │────────────────────────────────────────────────────────────►│
    │                    │                    │ SELECT latest      │
    │                    │                    │ published snapshot │
    │◄────────────────────────────────────────────────────────────│
    │  {programmePct, adminPct, trends, costPerBeneficiary, …}    │
```

## 6.4 Period Close

```
Controller          PeriodService        TransparencyService    AuditService
    │                    │                    │                   │
    │─ closePeriod(id) ──►│                   │                   │
    │                    │─ validateOpenItems() (no SUBMITTED expenses)
    │                    │─ checkBudgetVariance()                 │
    │                    │─ period.status = CLOSED                │
    │                    │─ period.closed_at = now()              │
    │                    │─ triggerSnapshot() ─────────────────────►│
    │                    │                    │─ generateSnapshot(period)
    │                    │                    │─ snapshot.published = true
    │                    │◄─────────────────────│                 │
    │                    │──────────────────────────────────────────────►│
    │                    │                    │  log(PERIOD_CLOSED)     │
    │◄─ 200 ──────────────│                   │                   │
```

---

# SECTION 7 — API DESIGN SPECIFICATION

## 7.1 Finance Admin Endpoints

```
# Chart of Accounts
GET    /api/admin/finance/categories              ?orgId= &parentId= &active=
POST   /api/admin/finance/categories
PUT    /api/admin/finance/categories/{id}
DELETE /api/admin/finance/categories/{id}         (soft-delete: active=false)

# Cost Centres
GET    /api/admin/finance/cost-centres
POST   /api/admin/finance/cost-centres
PUT    /api/admin/finance/cost-centres/{id}

# Periods
GET    /api/admin/finance/periods                 ?status=OPEN|CLOSED|LOCKED
POST   /api/admin/finance/periods
POST   /api/admin/finance/periods/{id}/close
POST   /api/admin/finance/periods/{id}/lock

# Budgets
GET    /api/admin/finance/budgets?periodId=
POST   /api/admin/finance/budgets
PUT    /api/admin/finance/budgets/{id}
GET    /api/admin/finance/budgets/variance?periodId=   ← budget vs actuals report

# Expenses
GET    /api/admin/finance/expenses                ?status= &categoryId= &periodId= &vendorId= &page=
POST   /api/admin/finance/expenses                (DRAFT)
PUT    /api/admin/finance/expenses/{id}           (DRAFT only)
POST   /api/admin/finance/expenses/{id}/submit
POST   /api/admin/finance/expenses/{id}/approve
POST   /api/admin/finance/expenses/{id}/reject
POST   /api/admin/finance/expenses/{id}/pay
POST   /api/admin/finance/expenses/{id}/void
GET    /api/admin/finance/expenses/{id}/attachments
POST   /api/admin/finance/expenses/{id}/attachments

# Vendors
GET    /api/admin/finance/vendors                 ?active= &search=
POST   /api/admin/finance/vendors
PUT    /api/admin/finance/vendors/{id}
GET    /api/admin/finance/vendors/{id}/spend      ← spend by period/category

# People
GET    /api/admin/people                          ?kind= &active=
POST   /api/admin/people
PUT    /api/admin/people/{id}
GET    /api/admin/people/{id}/payroll-profile
PUT    /api/admin/people/{id}/payroll-profile

# Payroll
GET    /api/admin/finance/payroll/runs            ?periodId= &status=
POST   /api/admin/finance/payroll/runs
POST   /api/admin/finance/payroll/runs/{id}/approve
POST   /api/admin/finance/payroll/runs/{id}/process
GET    /api/admin/finance/payroll/runs/{id}/items

# Approvals Queue (unified)
GET    /api/admin/approvals                       ?entityType= &status= &page=
GET    /api/admin/approvals/{id}
POST   /api/admin/approvals/{id}/approve
POST   /api/admin/approvals/{id}/reject

# Financial Transactions (read-only — journal audit)
GET    /api/admin/finance/transactions            ?type= &sourceType= &periodId= &categoryId= &page=
GET    /api/admin/finance/transactions/{id}

# Reports
GET    /api/admin/finance/reports/cash-flow       ?periodId=
GET    /api/admin/finance/reports/income-expense  ?periodId=
GET    /api/admin/finance/reports/category-totals ?periodId=
GET    /api/admin/finance/reports/budget-variance ?periodId=
GET    /api/admin/finance/reports/mission-spend   ?missionNodeId= &periodId=

# Mission
GET    /api/admin/missions                        ?status= &kind=
POST   /api/admin/missions
PUT    /api/admin/missions/{id}
GET    /api/admin/missions/{id}/financials         ← spend + budget + impact
POST   /api/admin/missions/{id}/impact-metrics
```

## 7.2 Executive Dashboard Endpoint

```
GET /api/admin/finance/executive/summary
Response:
{
  "asOf": "2026-06-30",
  "cashPosition": [{ "fundName", "balance", "restricted" }],
  "mtdIncome": 0.00,
  "mtdExpense": 0.00,
  "pendingApprovals": { "count": 3, "totalValue": 0.00 },
  "criticalAlerts": [{ "id", "message", "severity", "raisedAt" }],
  "cashRunwayMonths": 8.3,
  "budgetBurnPct": 0.42,
  "programmePct": 0.87,
  "transparencySnapshotDate": "2026-06-30",
  "aiRecommendations": [{ "requestId", "entityType", "entityRef",
                          "recommendation", "confidence", "summary" }]
}
```

## 7.3 Public Transparency Endpoint

```
GET /api/public/transparency?orgId=
Response:
{
  "asOf": "2026-06-30",
  "periodLabel": "FY2026",
  "programmePct": 87.2,
  "adminPct": 9.1,
  "fundraisingPct": 3.7,
  "totalIncome": 0.00,
  "totalExpense": 0.00,
  "beneficiaryCount": 234,
  "costPerBeneficiary": 0.00,
  "trend": [{ "period", "programmePct", "totalIncome" }],
  "programmes": [{ "name", "amount", "pct", "beneficiaries" }],
  "published": true
}
```

---

# SECTION 8 — DATABASE MIGRATION PLAN

| Migration | Contents |
|---|---|
| **V25** | `account_categories` table + charity pack CoA seed (1xxx Assets, 2xxx Liabilities, 3xxx Equity, 4xxx Income, 5xxx Programme, 6xxx Admin, 7xxx Fundraising, 8xxx Transfers) |
| **V26** | `cost_centres` table + `fund_accounts.restricted BOOLEAN DEFAULT false` |
| **V27** | `financial_periods` table |
| **V28** | `people` + `payroll_profiles` tables |
| **V29** | `vendors` table |
| **V30** | `expenses` + `attachments` + `approval_requests` + `approval_check_results` tables |
| **V31** | `financial_transactions` table + backfill from `fund_transactions` with `source_type='FUND_TX_LEGACY'` + indexes |
| **V32** | `budgets` table |
| **V33** | `mission_nodes` + `impact_metrics` tables + `campaigns.mission_node_id FK` (nullable) |
| **V34** | `transparency_snapshots` table + UNIQUE(org_id, period_id) |
| **V35** | Hardening: partial indexes, GIN indexes on JSONB columns, ON DELETE RESTRICT on all financial FKs |

**Migration rules:**
- Every migration is idempotent (`CREATE TABLE IF NOT EXISTS`, `ADD COLUMN IF NOT EXISTS`).
- V31 backfill runs in a single transaction. Run on staging first with trial-balance check before/after.
- No migration ever DROPs a column from tables V1–V24. Addition only.
- Original `fund_transactions` table is kept read-only for 30 days post V31 before scheduling DROP.

---

# SECTION 9 — UI NAVIGATION MAP

```
/admin (shell, existing)
 ├── /admin/dashboard          (existing — executive KPI widgets added in F5)
 ├── /admin/children           (existing)
 ├── /admin/sponsors           (existing)
 ├── /admin/commitments        (existing)
 │
 ├── /admin/people             NEW — People directory
 │     └── /admin/people/:id   Person detail + payroll profile
 │
 ├── /admin/finance            NEW — Finance shell
 │     ├── /admin/finance/expenses       Expense list + approval queue
 │     │     └── /admin/finance/expenses/:id
 │     ├── /admin/finance/vendors        Vendor directory
 │     │     └── /admin/finance/vendors/:id  (spend history)
 │     ├── /admin/finance/payroll        Payroll runs list
 │     │     └── /admin/finance/payroll/:id  Run detail + items
 │     ├── /admin/finance/periods        Period management + close actions
 │     ├── /admin/finance/budgets        Budget planning grid
 │     ├── /admin/finance/transactions   Journal audit (read-only)
 │     ├── /admin/finance/reports        Reports hub
 │     │     ├── cash-flow
 │     │     ├── income-expense
 │     │     ├── budget-variance
 │     │     └── mission-spend
 │     └── /admin/finance/categories     Chart of accounts (advanced)
 │
 ├── /admin/missions           NEW — Mission tree
 │     └── /admin/missions/:id  Mission financials + impact metrics
 │
 ├── /admin/approvals          NEW — Unified approval queue (all entity types)
 │
 ├── /admin/executive          NEW (F5) — Executive dashboard
 │
 ├── /admin/funds              (existing)
 ├── /admin/reconciliation     (existing)
 ├── /admin/donors             (existing)
 ├── /admin/donations          (existing)
 ├── /admin/zakat              (existing)
 ├── /admin/campaigns          (existing)
 ├── /admin/early-support      (existing)
 ├── /admin/progress           (existing)
 ├── /admin/reports            (existing — augmented in F5)
 ├── /admin/settings           (existing)
 ├── /admin/users              (existing)
 ├── /admin/import             (existing)
 ├── /admin/alerts             (existing)
 └── /admin/audit              (existing)

Public site:
 /trust  → dynamic transparency figures from /api/public/transparency
           (replaces hardcoded 87% — ships in F2)
```

---

# SECTION 10 — DEVELOPMENT MILESTONES

## F1 — Financial Core + Expense Management (~2 weeks)

**Backend:** V25–V31 migrations · domain objects (Expense, Vendor, Person, ApprovalRequest, AccountCategory) · services (AdminExpenseService, AdminVendorService, AdminPeopleService, ApprovalEngineService) · two approval checks (DuplicateHeuristic, BudgetThreshold) · controllers (AdminFinanceController, AdminPeopleController) · full audit integration.

**Frontend:** `/admin/finance/expenses` with status filter + submit/approve/pay actions · approval recommendation badge · `/admin/finance/vendors` · `/admin/people` · attachment upload widget · pending approvals count badge in sidebar.

**Definition of done:** A new expense can be created → submitted → approved (with AI badge) → paid; the resulting `financial_transaction` row is visible in the journal.

---

## F2 — Transparency Engine (~4 days)

**Backend:** V34 migration · TransparencyService (snapshot generation, minimum-data guard) · nightly @Scheduled job + period-close trigger · `GET /api/public/transparency` · ImpactQueryRegistry (charity pack: BENEFICIARIES = live count of ACTIVE sponsorships).

**Frontend:** Trust page refactored to consume live API · ratio display · trend sparkline · per-programme breakdown cards · "as of [date]" disclosure.

**Definition of done:** Trust page shows ratios computed from real expense data. Hardcoded "87%" deleted.

---

## F3 — Budgets + Variance (~4 days)

**Backend:** V32 migration · BudgetService (create, variance computation) · BudgetThresholdCheck wired to live budget data · `GET /api/admin/finance/budgets/variance`.

**Frontend:** `/admin/finance/budgets` grid (category rows × period columns, budget vs actual, variance %) · over-budget visual indicator in expense approval.

---

## F4 — Payroll (~1 week)

**Backend:** PayrollService (createRun, computeItems from payroll_profiles, approve, process) · payroll approval request (entity_type=PAYROLL_RUN) · process posts one `financial_transaction` per item.

**Frontend:** `/admin/finance/payroll` list + run detail (items, totals, approve/process) · `/admin/people` full CRUD with payroll profile tab.

---

## F5 — Missions + Periods + Reports + Executive Dashboard (~1.5 weeks)

**Backend:** V33 migration · MissionService (tree traversal via recursive CTE) · ImpactMetricsService · period close workflow with snapshot trigger · report endpoints (cash-flow, income-expense, budget-variance, mission-spend) · `GET /api/admin/finance/executive/summary`.

**Frontend:** `/admin/missions` tree view · mission detail (spend + impact metrics) · `/admin/finance/periods` management · `/admin/finance/reports` hub with charts · `/admin/executive` dashboard (cash position, runway, burn, AI recommendations feed) · Trust page enhanced with trend sparkline.

---

## F6 — AI Seams (~2 days)

**Backend:** AnomalyDetectionCheck stub (expense > 2× 3-month category average → REVIEW) · ForecastImpactCheck stub (expense would breach minimum fund balance → REVIEW) · `v_monthly_category_totals` SQL view · reserved alert types (ANOMALOUS_EXPENSE, FORECAST_SHORTFALL, DUPLICATE_EXPENSE_SUSPECT) · `GET /api/admin/finance/ai/recommendations`.

**Frontend:** Executive dashboard AI recommendations feed wired to endpoint · each card shows entity reference, check type, confidence bar, explanation, link to approval.

> F6 ships seams and interfaces only. LLM/ML integration is Roadmap 6.

---

# SECTION 11 — RISK REGISTER

| ID | Risk | Probability | Impact | Mitigation |
|---|---|---|---|---|
| R1 | V31 backfill of fund_transactions corrupts financial history | Low | Critical | Run on staging first; trial-balance check before/after; single transaction; keep original table read-only 30 days post-migration |
| R2 | Scope gravity — platform keeps expanding before F1 ships | High | High | Architecture is frozen; new domain additions require a written ADR; F1 scope is fixed |
| R3 | Approval queue becomes a bottleneck if all entity types compete | Medium | Medium | `entity_type` filter on all approval queries; each module section has its own filtered view |
| R4 | Transparency snapshot published with insufficient data | Low | High | Minimum-data guard: skip publish if fewer than MIN_TX_THRESHOLD transactions in the period (configurable, default 10) |
| R5 | People domain conflict when HR module arrives | Medium | Medium | `people` owns identity only; payroll owns compensation; HR will FK to `people` — boundary documented in ADR-002 |
| R6 | `approval_check_results.explanation` JSONB becomes unstructured dump | Medium | Low | Enforced schema `{reason, evidence[], recommendedAction, confidence}` at application layer |
| R7 | `financial_transactions` query degradation after 1M rows | Low | Medium | Indexes on (org_id, effective_date) and (source_type, source_id); partitioning deferred until 500K rows |
| R8 | Second SaaS vertical before multi-tenant isolation hardening | Low | High | No second vertical planned; if one emerges, row-level security migration is prioritised before onboarding |
| R9 | JSONB metadata abused as schema-free dump | Medium | Low | 4KB application-layer size guard; GIN index only on high-query tables |
| R10 | Admin navigation too complex for operational users | Medium | Medium | Finance sections grouped under `/admin/finance` sub-nav; primary nav shows Finance, People, Missions, Approvals only |

---

# SECTION 12 — IMPLEMENTATION CHECKLIST

## Pre-Implementation Gate

- [ ] All ADRs acknowledged by lead developer
- [ ] V25–V35 migration file stubs created and sequenced in Flyway
- [ ] `AdminExpenseService` interface (method signatures only) written and reviewed
- [ ] Approval engine interfaces (`ApprovalCheck`, `ApprovalEngineService`) defined
- [ ] Angular route stubs for all new routes created (compile-checked, placeholder components)
- [ ] `api.models.ts` extended with all new DTO interfaces (zero new `any` permitted)

## F1 Checklist

**Migrations:**
- [ ] V25: account_categories + charity pack CoA seeds
- [ ] V26: cost_centres + fund_accounts.restricted
- [ ] V27: financial_periods
- [ ] V28: people + payroll_profiles
- [ ] V29: vendors
- [ ] V30: expenses + attachments + approval_requests + approval_check_results
- [ ] V31: financial_transactions + fund_transactions backfill

**Backend:**
- [ ] Domain: Expense, Vendor, Person, ApprovalRequest immutable value objects
- [ ] DuplicateHeuristicCheck (same vendor + amount + ±7 days window)
- [ ] BudgetThresholdCheck (expense > remaining budget for category/period)
- [ ] AdminExpenseService: create, submit, approve, reject, pay, void
- [ ] AdminVendorService: create (with quick-create path), list, update
- [ ] AdminPeopleService: create, list, update, upsert payroll profile
- [ ] ApprovalEngineService: createRequest, runChecks, computeRecommendation, resolve
- [ ] AdminFinanceController: all expense + vendor endpoints
- [ ] AdminPeopleController: all people + payroll profile endpoints
- [ ] Audit: EXPENSE_CREATED, EXPENSE_SUBMITTED, EXPENSE_APPROVED, EXPENSE_PAID, EXPENSE_VOIDED, EXPENSE_REJECTED
- [ ] `@Transactional(REQUIRES_NEW)` on audit + alert writes
- [ ] Integration tests: full expense lifecycle (create → submit → approve → pay → verify tx row)
- [ ] Integration tests: budget threshold check fires correctly
- [ ] Integration tests: duplicate heuristic fires on same-vendor same-amount

**Frontend:**
- [ ] `/admin/finance/expenses` list with status filter chips and pagination
- [ ] Expense detail drawer/modal with action buttons gated by status
- [ ] Approval recommendation badge (APPROVE/REVIEW/REJECT + confidence bar)
- [ ] `/admin/finance/vendors` list + create form (quick-create inline from expense form)
- [ ] `/admin/people` list + create + payroll profile tab
- [ ] Attachment upload widget (file input → POST → display list with kind label)
- [ ] Pending approvals count badge in admin sidebar
- [ ] OnPush strategy on all new section components with `markForCheck()` in subscribe paths
- [ ] Zero new `any` usages in api.models.ts or service calls

## F2 Checklist

- [ ] V34: transparency_snapshots
- [ ] TransparencyService: computeSnapshot(), minimumDataGuard(), publishSnapshot()
- [ ] ImpactQueryRegistry: BENEFICIARIES → count of ACTIVE sponsorships
- [ ] Nightly @Scheduled job (2am)
- [ ] Period-close event triggers snapshot generation with published=true
- [ ] GET /api/public/transparency returns latest published snapshot
- [ ] Trust page: remove hardcoded "87%", wire to transparency endpoint
- [ ] Trust page: "as of [date]" disclosure label
- [ ] Trust page: per-programme breakdown cards
- [ ] Trend sparkline (last 6 periods from snapshot history)

## F3 Checklist

- [ ] V32: budgets
- [ ] BudgetService: create, update, computeVariance (budgeted vs actual by category/period)
- [ ] BudgetThresholdCheck wired to live budget data (replaces placeholder from F1)
- [ ] GET /api/admin/finance/budgets/variance
- [ ] `/admin/finance/budgets` grid: category rows, period column, budget vs actual, variance %
- [ ] Over-budget indicator in expense approval (amber ≥90%, red ≥100%)

## F4 Checklist

- [ ] PayrollService: createRun, computeItems, approve, process
- [ ] Payroll approval request creation (entity_type=PAYROLL_RUN)
- [ ] Process posts one financial_transaction per payroll_item (source_type=PAYROLL_ITEM)
- [ ] `/admin/finance/payroll` list + run detail with approve/process actions
- [ ] People list: full CRUD + payroll profile tab

## F5 Checklist

- [ ] V33: mission_nodes + impact_metrics + campaigns.mission_node_id FK
- [ ] MissionService: create, tree traversal (recursive CTE), financials aggregate
- [ ] ImpactMetricsService: record manual, resolve COUNT_QUERY via ImpactQueryRegistry
- [ ] Period close workflow: validate no SUBMITTED expenses → close → trigger snapshot
- [ ] Report endpoints: cash-flow, income-expense, budget-variance, mission-spend
- [ ] GET /api/admin/finance/executive/summary
- [ ] `/admin/missions` tree view
- [ ] Mission detail: spend bar + impact metrics + linked campaigns
- [ ] `/admin/finance/periods` management with close UI
- [ ] `/admin/finance/reports` hub with chart visualisations
- [ ] `/admin/executive` dashboard: cash position, runway, burn, AI recommendations feed
- [ ] Trust page enhanced: trend sparkline, per-programme cost cards, beneficiary count

## F6 Checklist

- [ ] AnomalyDetectionCheck stub: expense > 2× 3-month category average → REVIEW
- [ ] ForecastImpactCheck stub: expense would breach MIN_FUND_BALANCE_PCT → REVIEW
- [ ] `v_monthly_category_totals` SQL view
- [ ] Alert type constants added to AlertType enum: ANOMALOUS_EXPENSE, FORECAST_SHORTFALL, DUPLICATE_EXPENSE_SUSPECT
- [ ] GET /api/admin/finance/ai/recommendations
- [ ] Executive dashboard AI recommendations feed wired to endpoint
- [ ] Each recommendation card: entity reference, check type, confidence, explanation, link to approval

---

# ARCHITECTURE FREEZE CONFIRMATION

The architecture is frozen as of **2026-07-07**. The following constraints apply from this point forward:

1. **No new tables** may be added to an F1–F6 migration without a written ADR entry. Extension points (nullable FKs, JSONB metadata, new enum values) do not require an ADR.
2. **The five-pillar model** (People, Money, Mission, Impact, AI) is the classification for all future modules. Any proposed module that does not fit must be discussed before implementation begins.
3. **The approval engine is the only approval engine.** No module may introduce `*_approval_status` columns or `*_approved_by` columns without routing through `approval_requests`.
4. **The attachment table is the only attachment table.** No module may introduce `*_attachments` tables.
5. **Layer 1 domain entities have zero framework imports.** Financial domain objects follow the same rule as the existing `core/domain` package.
6. **Every public financial figure is computed from data, never hardcoded.**

Implementation begins at **F1**. The sequence is fixed: F1 → F2 → F3 → F4 → F5 → F6.  
A milestone may only begin when the previous milestone's checklist is fully checked.
