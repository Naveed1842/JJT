# JJT Platform — Phase 2.5 Implementation Roadmap

> Finance Engine: Programme & Expense Management
> Architecture frozen 2026-07-11. Scope: 4 new tables, 2 additive column changes, ~12 new endpoints.

---

## Overview

| Phase | Name | Effort | Scope |
|-------|------|--------|-------|
| 2.5.1 | Schema | 0.5 days | Flyway migrations (V26, V27) |
| 2.5.2 | Domain | 1 day | Domain entities, value objects, exceptions |
| 2.5.3 | Infrastructure | 1 day | JPA entities, mappers, repositories |
| 2.5.4 | Application | 2 days | Use cases, services |
| 2.5.5 | API | 1.5 days | Controllers, DTOs, request/response types |
| 2.5.6 | Frontend | 3 days | Angular components, routes, services |
| 2.5.7 | Testing | 1 day | Integration tests (Testcontainers) |
| **Total** | | **~10 days** | |

---

## Phase 2.5.1 — Schema Migrations

**Deliverables:** `V26__finance_engine.sql`, `V27__fund_account_purpose.sql`

### V26 — Finance Engine Tables

```sql
-- 1. programmes
CREATE TABLE programmes (
    id                      UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id         UUID           NOT NULL REFERENCES organisations(id),
    name                    VARCHAR(200)   NOT NULL,
    description             TEXT,
    classification          VARCHAR(100),      -- e.g. 'EDUCATION', 'COMMUNITY', 'ADMIN'
    objective               TEXT,
    primary_fund_account_id UUID           REFERENCES fund_accounts(id),
    status                  VARCHAR(20)    NOT NULL DEFAULT 'DRAFT'
                                CHECK (status IN ('DRAFT','ACTIVE','COMPLETED','SUSPENDED','CLOSED')),
    start_date              DATE,
    end_date                DATE,
    manager_id              UUID           REFERENCES users(id),
    created_by              UUID           NOT NULL REFERENCES users(id),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_programmes_org_status ON programmes (organisation_id, status);

-- 2. expense_categories
CREATE TABLE expense_categories (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID        NOT NULL REFERENCES organisations(id),
    code            VARCHAR(20) NOT NULL,
    name            VARCHAR(200) NOT NULL,
    category_type   VARCHAR(30) NOT NULL
                        CHECK (category_type IN (
                            'PERSONNEL','FACILITIES','PROGRAMME_DELIVERY',
                            'ADMINISTRATION','FUNDRAISING','OTHER')),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_expense_category_org_code UNIQUE (organisation_id, code)
);

-- 3. budget_lines
CREATE TABLE budget_lines (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    programme_id        UUID           NOT NULL REFERENCES programmes(id),
    expense_category_id UUID           NOT NULL REFERENCES expense_categories(id),
    fund_account_id     UUID           NOT NULL REFERENCES fund_accounts(id),
    budget_period       VARCHAR(7)     NOT NULL,   -- 'YYYY-MM'
    planned_amount      NUMERIC(14,2)  NOT NULL,
    currency            CHAR(3)        NOT NULL DEFAULT 'PKR',
    notes               TEXT,
    created_by          UUID           NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_budget_line UNIQUE (programme_id, expense_category_id, budget_period)
);

CREATE INDEX idx_budget_lines_programme ON budget_lines (programme_id, budget_period);

-- 4. programme_expenses
CREATE TABLE programme_expenses (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID           NOT NULL REFERENCES organisations(id),
    programme_id        UUID           NOT NULL REFERENCES programmes(id),
    expense_category_id UUID           NOT NULL REFERENCES expense_categories(id),
    fund_account_id     UUID           NOT NULL REFERENCES fund_accounts(id),
    fund_transaction_id UUID           REFERENCES fund_transactions(id),
    amount              NUMERIC(14,2)  NOT NULL,
    currency            CHAR(3)        NOT NULL DEFAULT 'PKR',
    expense_date        DATE           NOT NULL,
    description         TEXT           NOT NULL,
    reference           VARCHAR(200),
    status              VARCHAR(20)    NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN ('DRAFT','APPROVED','PAID','REVERSED')),
    approved_by         UUID           REFERENCES users(id),
    created_by          UUID           NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_programme_expenses_org     ON programme_expenses (organisation_id, expense_date DESC);
CREATE INDEX idx_programme_expenses_prog    ON programme_expenses (programme_id, expense_date DESC);
CREATE INDEX idx_programme_expenses_status  ON programme_expenses (status, expense_date);
```

### V27 — Additive Column Changes

```sql
-- fund_accounts: classify pot purpose
ALTER TABLE fund_accounts
    ADD COLUMN purpose_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL'
        CHECK (purpose_type IN ('EDUCATION','OPERATIONAL','RESERVE','GENERAL'));

-- fund_transactions: optional programme attribution
ALTER TABLE fund_transactions
    ADD COLUMN programme_id UUID REFERENCES programmes(id);

CREATE INDEX idx_fund_transactions_programme ON fund_transactions (programme_id)
    WHERE programme_id IS NOT NULL;
```

**Acceptance:** `./mvnw test -Dtest=FlywayMigrationTest` (all 27 migrations apply clean on fresh DB)

---

## Phase 2.5.2 — Domain Layer

**Package:** `com.jjt.platform.core.domain`
**Constraint:** Zero Spring, zero JPA. Plain Java only.

### New Domain Entities

**`Programme.java`**
```
fields: id, organisationId, name, description, classification, objective,
        primaryFundAccountId, status (ProgrammeStatus enum), startDate, endDate,
        managerId, createdBy, createdAt, updatedAt
transitions: withStatus(ProgrammeStatus) → new Programme (immutable)
```

**`ExpenseCategory.java`**
```
fields: id, organisationId, code, name, categoryType (ExpenseCategoryType enum),
        isActive, createdAt
```

**`BudgetLine.java`**
```
fields: id, programmeId, expenseCategoryId, fundAccountId, budgetPeriod (YearMonthValue),
        plannedAmount (Money), createdBy, createdAt
```

**`ProgrammeExpense.java`**
```
fields: id, organisationId, programmeId, expenseCategoryId, fundAccountId,
        fundTransactionId (nullable), amount (Money), expenseDate, description,
        reference, status (ExpenseStatus enum), approvedBy, createdBy, createdAt, updatedAt
transitions: approve(actingUserId) → new ProgrammeExpense
             pay(fundTransactionId) → new ProgrammeExpense
             reverse() → new ProgrammeExpense
```

### New Enums

```java
// ProgrammeStatus: DRAFT, ACTIVE, COMPLETED, SUSPENDED, CLOSED
// ExpenseCategoryType: PERSONNEL, FACILITIES, PROGRAMME_DELIVERY, ADMINISTRATION, FUNDRAISING, OTHER
// ExpenseStatus: DRAFT, APPROVED, PAID, REVERSED
```

### Update: `FundTransaction.java`

Add `programmeId` field (nullable UUID). Add factory method:
```java
static FundTransaction forProgrammeExpense(UUID programmeId, ...)
```

---

## Phase 2.5.3 — Infrastructure Layer

**Package:** `com.jjt.platform.infrastructure.persistence`

### JPA Entities (4 new)

| JPA Entity | Maps to |
|-----------|---------|
| `ProgrammeEntity` | `programmes` |
| `ExpenseCategoryEntity` | `expense_categories` |
| `BudgetLineEntity` | `budget_lines` |
| `ProgrammeExpenseEntity` | `programme_expenses` |

Add `programmeId` field to `FundTransactionEntity`.
Add `purposeType` field to `FundAccountEntity`.

### Repositories (4 new)

```java
ProgrammeJpaRepository
  findByOrganisationIdAndStatus(UUID orgId, ProgrammeStatus status)

ExpenseCategoryJpaRepository
  findByOrganisationIdAndIsActiveTrue(UUID orgId)

BudgetLineJpaRepository
  findByProgrammeIdAndBudgetPeriod(UUID programmeId, String period)
  findByProgrammeId(UUID programmeId)

ProgrammeExpenseJpaRepository
  findByOrganisationIdOrderByExpenseDateDesc(UUID orgId, Pageable pageable)
  findByProgrammeIdOrderByExpenseDateDesc(UUID programmeId, Pageable pageable)
  sumAmountByProgrammeIdAndStatus(UUID programmeId, ExpenseStatus status)  // @Query
```

### Mapper Updates

`DomainMapper` additions:
- `ProgrammeEntity ↔ Programme`
- `ExpenseCategoryEntity ↔ ExpenseCategory`
- `BudgetLineEntity ↔ BudgetLine`
- `ProgrammeExpenseEntity ↔ ProgrammeExpense`

---

## Phase 2.5.4 — Application Layer (Use Cases / Services)

**Package:** `com.jjt.platform.api.admin.service` (existing pattern)

### `AdminProgrammeService`

```
createProgramme(CreateProgrammeCommand) → Programme
updateProgramme(UUID id, UpdateProgrammeCommand) → Programme
changeProgrammeStatus(UUID id, ProgrammeStatus, actingUserId) → Programme
getProgramme(UUID id) → Programme
listProgrammes(UUID orgId, ProgrammeStatus filter) → List<Programme>
getProgrammeSummary(UUID id) → ProgrammeSummary  // includes P&L, expense count
```

### `AdminExpenseCategoryService`

```
createCategory(CreateExpenseCategoryCommand) → ExpenseCategory
listCategories(UUID orgId) → List<ExpenseCategory>
deactivateCategory(UUID id) → ExpenseCategory
```

### `AdminBudgetService`

```
setbudgetLine(SetBudgetLineCommand) → BudgetLine   // upsert by unique key
getBudgetLines(UUID programmeId) → List<BudgetLine>
getBudgetVariance(UUID programmeId, String period) → BudgetVarianceReport
```

### `AdminExpenseService`

```
recordExpense(RecordExpenseCommand, actingUserId) → ProgrammeExpense
approveExpense(UUID id, actingUserId) → ProgrammeExpense
payExpense(UUID id, actingUserId) → ProgrammeExpense
  // creates DEBIT fund_transaction, links back to expense, calls assertSufficientFunds
reverseExpense(UUID id, reason, actingUserId) → ProgrammeExpense
  // creates compensating CREDIT, skips reserve check
listExpenses(UUID orgId, UUID programmeId, Pageable) → Page<ProgrammeExpense>
```

### `AdminFinanceReportService`

```
getProgrammePnL(UUID programmeId) → ProgrammePnLReport
getTransparencyRatio(UUID orgId, String period) → TransparencyRatioReport
getOrganisationFinanceSummary(UUID orgId) → FinanceSummary
```

---

## Phase 2.5.5 — API Layer

**Base paths follow existing pattern:** `@RequestMapping("/api/admin/...")`

### New Controllers

**`AdminProgrammeController`** (`/api/admin/programmes`)
```
POST   /                           → 201 ProgrammeResponse
GET    /                           → 200 List<ProgrammeSummaryResponse>
GET    /{id}                       → 200 ProgrammeResponse
PUT    /{id}                       → 200 ProgrammeResponse
PATCH  /{id}/status                → 200 ProgrammeResponse
GET    /{id}/pnl                   → 200 ProgrammePnLResponse
GET    /{id}/expenses              → 200 Page<ProgrammeExpenseResponse>
GET    /{id}/budget                → 200 List<BudgetLineResponse>
```

**`AdminExpenseCategoryController`** (`/api/admin/expense-categories`)
```
POST   /                           → 201 ExpenseCategoryResponse
GET    /                           → 200 List<ExpenseCategoryResponse>
PATCH  /{id}/deactivate            → 200 ExpenseCategoryResponse
```

**`AdminBudgetController`** (`/api/admin/budgets`)
```
PUT    /lines                      → 200 BudgetLineResponse    (upsert)
GET    /variance/{programmeId}     → 200 BudgetVarianceResponse
```

**`AdminExpenseController`** (`/api/admin/expenses`)
```
POST   /                           → 201 ProgrammeExpenseResponse
GET    /                           → 200 Page<ProgrammeExpenseResponse>
GET    /{id}                       → 200 ProgrammeExpenseResponse
POST   /{id}/approve               → 200 ProgrammeExpenseResponse
POST   /{id}/pay                   → 200 ProgrammeExpenseResponse
POST   /{id}/reverse               → 200 ProgrammeExpenseResponse
```

**`AdminFinanceReportController`** (`/api/admin/finance`)
```
GET    /transparency               → 200 TransparencyRatioResponse
GET    /summary                    → 200 FinanceSummaryResponse
```

### Security

All new endpoints: `hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')` — consistent with existing admin pattern.

---

## Phase 2.5.6 — Frontend (Angular)

**New lazy-loaded routes under `/admin`**

### Services

**`programme.service.ts`**
```typescript
createProgramme(req): Observable<Programme>
listProgrammes(orgId, status?): Observable<ProgrammeSummary[]>
getProgramme(id): Observable<Programme>
getProgrammePnL(id): Observable<ProgrammePnL>
getProgrammeExpenses(id, page): Observable<Page<ProgrammeExpense>>
```

**`expense.service.ts`**
```typescript
recordExpense(req): Observable<ProgrammeExpense>
approveExpense(id): Observable<ProgrammeExpense>
payExpense(id): Observable<ProgrammeExpense>
reverseExpense(id, reason): Observable<ProgrammeExpense>
listExpenses(filters, page): Observable<Page<ProgrammeExpense>>
```

### Components / Pages

| Route | Component | Purpose |
|-------|-----------|---------|
| `/admin/programmes` | `ProgrammeListPage` | All programmes, status filter |
| `/admin/programmes/new` | `ProgrammeFormPage` | Create programme |
| `/admin/programmes/:id` | `ProgrammeDetailPage` | P&L, expenses, budget tabs |
| `/admin/programmes/:id/edit` | `ProgrammeFormPage` | Edit programme |
| `/admin/expense-categories` | `ExpenseCategoryPage` | Manage categories |
| `/admin/expenses` | `ExpenseListPage` | All expenses, filter by programme/status |
| `/admin/expenses/new` | `ExpenseFormPage` | Record new expense |
| `/admin/expenses/:id` | `ExpenseDetailPage` | Approve/Pay/Reverse actions |
| `/admin/finance/report` | `FinanceReportPage` | Transparency ratio, fund summary |

### State

**`ProgrammeStore`** (signals):
```typescript
programmes = signal<ProgrammeSummary[]>([])
selectedProgramme = signal<Programme | null>(null)
pnlData = signal<ProgrammePnL | null>(null)
lastRefreshed = signal<Date | null>(null)  // TTL cache
```

---

## Phase 2.5.7 — Testing

**Test class pattern:** extends `AbstractIntegrationTest` which imports `TestcontainersConfiguration`

### New Integration Test Classes

**`AdminProgrammeIntegrationTest`**
- Create programme → list → get → update status lifecycle
- Verify programme is scoped to organisation (multi-tenant)

**`AdminExpenseIntegrationTest`**
- Record DRAFT → approve → pay flow
- Verify fund_transaction DEBIT created on pay
- Verify assertSufficientFunds blocks pay below min_reserve
- Reverse expense → verify compensating CREDIT created
- Verify reverse bypasses reserve check

**`ProgrammePnLIntegrationTest`**
- Credit fund_transaction with programme_id → appears in P&L revenue
- Pay expense → debit appears in P&L cost
- Verify P&L formula: Revenue − Expenses

**`BudgetVarianceIntegrationTest`**
- Set budget_line → record expenses → verify variance calculation

**`TransparencyRatioIntegrationTest`**
- Mix EDUCATION and OPERATIONAL expenses → verify ratio

---

## Dependency Order

```
V26 migration
    └── V27 migration (fund_transactions.programme_id needs programmes table)
        └── Domain entities (Programme, ExpenseCategory, BudgetLine, ProgrammeExpense)
            └── JPA entities + mappers + repositories
                ├── AdminProgrammeService
                ├── AdminExpenseCategoryService
                ├── AdminBudgetService
                └── AdminExpenseService
                    └── AdminFinanceReportService
                        └── API Controllers + DTOs
                            └── Angular services + components
                                └── Integration tests (run against real schema)
```

---

## Rollback Plan

All Phase 2.5 changes are **additive only**:
- New tables can be dropped: `DROP TABLE programme_expenses, budget_lines, expense_categories, programmes CASCADE;`
- New columns can be dropped: `ALTER TABLE fund_accounts DROP COLUMN purpose_type;` `ALTER TABLE fund_transactions DROP COLUMN programme_id;`
- Zero changes to existing table structure, indexes, or constraints
- Existing 20 tables, all existing endpoints, all existing tests are untouched

---

## Definition of Done

- [ ] V26 and V27 migrate cleanly on a fresh PostgreSQL 16 database
- [ ] All existing 25-migration test suite still passes with no changes
- [ ] Programme CRUD lifecycle works end-to-end via API
- [ ] Expense DRAFT → APPROVED → PAID flow creates correct fund_transaction DEBIT
- [ ] Expense reversal creates compensating CREDIT and bypasses reserve check
- [ ] Programme P&L query returns correct Revenue − Expenses
- [ ] Transparency ratio endpoint returns correct Education % figure
- [ ] All new endpoints return 403 for SPONSOR role
- [ ] New Angular pages accessible under `/admin/programmes` and `/admin/expenses`
- [ ] No changes to existing controller, service, or domain tests
