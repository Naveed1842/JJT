# Phase 2 Roadmap
## Junior Jinnah Trust — Delivery Plan

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Guiding Principles

1. **Never break Phase 1.** Every Phase 2 change must be backward-compatible at the database level. No Flyway migration may drop or rename a column used by the current application.
2. **Fund accounting before fundraising.** Financial governance (knowing what you have) must come before donation solicitation (asking for more). Never market a campaign you can't account for.
3. **Operational usefulness over completeness.** A working reconciliation workflow used daily is more valuable than a perfect architecture that takes 6 months to build.
4. **Small migrations, frequent deploys.** Each sprint should be independently deployable. No "big bang" migration that requires a maintenance window.

---

## Phase 2 Milestones Overview

| Milestone | Name | Focus Area | Priority |
|-----------|------|-----------|---------|
| M2.1 | Data Integrity Sprint | Foundation changes, no new UI | Critical |
| M2.2 | Fund Accounting MVP | Central fund + basic recording | Critical |
| M2.3 | Payment Reconciliation | Monthly payment lifecycle | Critical |
| M2.4 | Multi-Organisation Foundation | Organisation entity + scoping | High |
| M2.5 | Donation Management | All donation types + receipts | High |
| M2.6 | Campaign Module | Campaign fund lifecycle | Medium |
| M2.7 | Notifications | Email automation | High |
| M2.8 | Reporting Suite | Executive + operational reports | High |
| M2.9 | Executive Dashboards | Board-level KPI dashboards | Medium |
| M2.10 | Audit & Compliance Export | Four-eyes approval + annual export | Medium |
| M2.11 | Sponsor Self-Service | Portal enhancements + receipts | Medium |
| M2.12 | Budget Management | Per-child budgets + variance | Low |

---

## Milestone M2.1 — Data Integrity Sprint

**Duration estimate:** 1 sprint (2 weeks)
**Deploy:** Yes — backward-compatible changes only

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| V12 Migration | Add `created_by UUID` and `created_at TIMESTAMP` to `ledger_entries`, `sponsorships`, `progress_updates`. Nullable initially to preserve existing data. | Critical |
| V13 Migration | Add `organisations` table + `organisation_id` FK to all business tables. Seed JJT-001. Backfill all existing rows with JJT-001. | Critical |
| V14 Migration | Add `CORRECTION` to `coverage_type` enum for ledger entries. | High |
| V15 Migration | Add DB-level append-only enforcement: PostgreSQL rules preventing UPDATE/DELETE on `ledger_entries` and `fund_transactions` (once created). | High |
| V16 Migration | Add partial unique index: `CREATE UNIQUE INDEX idx_one_active_per_child ON sponsorships (child_id) WHERE status IN ('ACTIVE','PENDING')` | High |
| V17 Migration | Add `sponsor_contact_email_unique_partial` — unique index on `sponsors.contact_email` OR application-level duplicate check | Medium |
| Fix BR-08 | Allow SPONSOR role to update their own display name and phone via new `PATCH /api/sponsor/profile` endpoint | Medium |
| Fix BR-18 | Replace `educationAmount` scalar with an `EducationCostHistory` table (effective_from, amount) for cost change tracking | Medium |

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Admin duplicate warning | Show warning banner when creating a sponsor if another sponsor with same email already exists | High |
| Admin `created_by` display | Show who created each ledger entry and sponsorship on the detail views | Medium |

### Acceptance Criteria

- [ ] All existing data is preserved without modification
- [ ] `organisation_id = 'JJT-001'` on every existing row
- [ ] Attempting to UPDATE or DELETE a ledger entry via SQL returns an error
- [ ] Attempting to create two ACTIVE sponsorships for the same child returns a 409
- [ ] Admin creating a sponsor with a duplicate email sees a warning before proceeding

---

## Milestone M2.2 — Fund Accounting MVP

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes — new tables, no changes to existing

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| V18 Migration | Create `fund_accounts` table and `fund_transactions` table (append-only) | Critical |
| Seed | Create JJT General Education Fund (`fund_accounts`) with initial balance set via admin tool | Critical |
| Use case | When `EARLY_SUPPORT` ledger entry is created, automatically create a `FundTransaction DEBIT` | Critical |
| Use case | Fund balance query: `GET /api/admin/funds/{id}/balance` returns balance at any point in time | Critical |
| Use case | Fund balance check: before creating an early support entry, check if the debit would exceed available balance. Return 422 with "insufficient funds" warning if so (soft block — admin can override with reason) | Critical |
| Use case | Manual credit: `POST /api/admin/funds/{id}/credit` — allows admin to record a manual donation credit (e.g. cash received) against a fund. Requires amount, date, description, external reference | High |
| API endpoint | `GET /api/admin/funds` — list all fund accounts with current balance | Critical |
| API endpoint | `GET /api/admin/funds/{id}/transactions` — paginated transaction history | High |

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Admin funds tab | New "Funds" section in admin panel showing all fund accounts with balances | Critical |
| Fund dashboard widget | Add to admin home: "General Fund: PKR X available, PKR Y reserved" | Critical |
| Early support warning | When adding early support, show current fund balance and warn if balance will fall below minimum reserve | High |
| Manual credit form | Form to record an incoming general donation to the fund | High |

### Acceptance Criteria

- [ ] Admin can view current fund balance at any time
- [ ] Creating an early support entry debits the fund by the child's education amount
- [ ] Creating an early support entry when fund balance < minimum reserve shows a warning (not hard block)
- [ ] Admin can manually credit the fund with a donation
- [ ] Fund transaction history is queryable with full attribution (created_by, timestamp, amount, reason)

---

## Milestone M2.3 — Payment Reconciliation

**Duration estimate:** 3 sprints (6 weeks)
**Deploy:** Yes — critical operational feature

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| V19 Migration | Create `sponsor_payments` table: one row per sponsor × month × child. Status enum: `EXPECTED`, `RECEIVED`, `PARTIAL`, `OVERDUE`, `WAIVED`, `PREPAID` | Critical |
| Scheduled job | Monthly job (1st of month): for all ACTIVE sponsorships, create `SponsorPayment` with status = `EXPECTED` | Critical |
| Use case | `POST /api/admin/payments/receive` — admin records payment received for a given `SponsorPayment`. Creates: FundTransaction (CREDIT), LedgerEntry (SPONSOR), updates SponsorPayment to RECEIVED | Critical |
| Scheduled job | Daily job: for all EXPECTED payments past the payment due date, transition to `OVERDUE` | Critical |
| Alert rule | Detect sponsorships with 2 consecutive OVERDUE months → create admin alert | High |
| Alert rule | Detect sponsorships with 3 consecutive OVERDUE months → create escalation flag requiring admin action | High |
| Use case | `POST /api/admin/payments/waive` — admin waives a payment with reason. Creates compensating EARLY_SUPPORT entry funded from General Fund | Medium |
| Use case | `GET /api/admin/reconciliation/monthly?year=2026&month=07` — full monthly reconciliation report | Critical |
| Use case | Prepayment: allow admin to record payment for a future month (creates PREPAID SponsorPayment, does not create LedgerEntry until month arrives) | Should Have |

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Monthly reconciliation tab | New admin tab showing all active sponsorships with their current month payment status (EXPECTED/RECEIVED/OVERDUE) | Critical |
| Record payment form | Quick form to record a payment: sponsor, amount, bank reference, received date | Critical |
| At-risk indicator | Red badge on children whose active sponsorship has an OVERDUE payment | Critical |
| Payment history | Per-sponsorship payment timeline showing all months | High |
| Overdue alerts panel | Dashboard widget: "N sponsorships with overdue payments this month" | High |

### Acceptance Criteria

- [ ] On the 1st of each month, all active sponsorships have an EXPECTED payment record
- [ ] Admin can record a payment received with full details (amount, bank ref, date)
- [ ] Recording a payment creates a LedgerEntry (SPONSOR) and FundTransaction (CREDIT) atomically
- [ ] Payments not received by the 15th automatically become OVERDUE
- [ ] Admin dashboard shows all overdue payments prominently
- [ ] Admin can waive a payment with a recorded reason

---

## Milestone M2.4 — Multi-Organisation Foundation

**Duration estimate:** 1 sprint (2 weeks)
**Deploy:** Yes — transparent to existing users

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Activate `organisations` table | Complete the FK relationships started in V13. Add `organisation_id` to JWT claims. | Critical |
| API scoping | Add `@PreAuthorize` checks on all endpoints to filter by authenticated user's `organisation_id` | Critical |
| Org config endpoint | `GET /api/admin/org/config` — returns org-level settings: name, base currency, payment due day, min reserve | High |
| Org config update | `PATCH /api/admin/org/config` — allows JJT_ADMIN to update org settings | High |

### Acceptance Criteria

- [ ] Every API response is scoped to the authenticated user's organisation
- [ ] A user from ORG-001 cannot access data from ORG-002 by any means
- [ ] JWT claims include `orgId`
- [ ] Org configuration is queryable and editable

---

## Milestone M2.5 — Donation Management

**Duration estimate:** 2 sprints (4 weeks)

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| V20 Migration | Create `donors` table, `donations` table with type enum | Critical |
| Use case | `POST /api/admin/donations` — record any type of donation. Auto-creates FundTransaction CREDIT for the appropriate fund | Critical |
| Use case | `GET /api/admin/donations/{id}/receipt` — generate receipt PDF data | High |
| Use case | Recurring donation schedule: `POST /api/admin/donations/recurring` — creates monthly EXPECTED records | Should Have |
| Receipt number | Auto-generated sequential receipt number: JJT-2026-0001, JJT-2026-0002, etc. | High |

### Acceptance Criteria

- [ ] Any incoming donation can be recorded with full attribution
- [ ] Donation automatically credits the appropriate fund based on type
- [ ] A receipt can be generated for any donation
- [ ] Recurring donations generate monthly expected records

---

## Milestone M2.6 — Campaign Module

**Duration estimate:** 2 sprints (4 weeks)

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| V21 Migration | Create `campaigns` table with status lifecycle | High |
| Use case | Campaign CRUD: create, open, close, archive | High |
| Use case | Campaign donation: record donation against campaign fund | High |
| Use case | Campaign performance report | High |
| Public API | `GET /api/public/campaigns` — list active campaigns for donation page | High |

---

## Milestone M2.7 — Notifications

**Duration estimate:** 2 sprints (4 weeks)

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Email service | Integrate email provider (SendGrid/Mailgun). Template-based emails. | Critical |
| Notification events | Trigger emails on: payment received, sponsorship activated, progress update posted, payment overdue | Critical |
| Notification log | `email_notifications` table: recipient, template, sent_at, delivered, bounced | High |
| Admin alert system | In-app alerts for admin: at-risk children, overdue payments, fund below reserve | Critical |

---

## Milestone M2.8 — Reporting Suite

**Duration estimate:** 2 sprints (4 weeks)

Key reports (see `REPORTING_REQUIREMENTS.md` for full specifications):

| Report | Audience | Priority |
|--------|----------|---------|
| Monthly reconciliation | Finance admin | Critical |
| Fund balance summary | All admins | Critical |
| Donor summary | Finance admin | High |
| Cash flow (3-month) | Finance admin, Board | High |
| Per-child financial summary | Admin | High |
| Annual financial statement | Board, Auditors | High |
| Sponsor impact report | Sponsors | Medium |
| Campaign performance | Campaign manager | Medium |

---

## Milestone M2.9 — Executive Dashboards

**Duration estimate:** 1 sprint (2 weeks)

See `EXECUTIVE_DASHBOARD_SPECIFICATION.md` for full dashboard designs.

---

## Milestone M2.10 — Audit & Compliance Export

**Duration estimate:** 1 sprint (2 weeks)

| Task | Description | Priority |
|------|-------------|----------|
| Four-eyes approval | Transactions above PKR 50,000 require second approver | High |
| Audit event log | Immutable log of all financial actions | Critical |
| Annual export | JSON/CSV export of all transactions for a year, with checksum | High |

---

## Delivery Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Monthly reconciliation job not running (Heroku free tier sleep) | High | Critical | Upgrade to hobby dyno or use external cron (cron-job.org) |
| Email delivery failures (bounces, spam) | Medium | High | Use reputable provider (SendGrid) with bounce handling |
| Fund balance goes negative before M2.2 completes | Low | High | Admin workaround: manual tracking until M2.2. Add fund balance to Phase 1 admin dashboard ASAP |
| Multi-organisation migration breaks existing data | Medium | Critical | Full database backup before V13. Test migration on dev copy first. |
| Heroku costs increase with new tables and jobs | Low | Low | Monitor; current scale is well within hobby tier limits |
| Sharia compliance review for Zakat fund takes longer than expected | Medium | Medium | Defer Zakat fund UI until review is complete; schema can be ready ahead of launch |

---

## Dependencies Map

```
M2.1 (Data Integrity)
 └── enables everything below

M2.2 (Fund Accounting)
 └── M2.1 required first
 └── enables M2.3, M2.5

M2.3 (Payment Reconciliation) ← MOST CRITICAL OPERATIONAL PATH
 └── M2.2 required (fund credit on payment)

M2.4 (Multi-Org)
 └── M2.1 required (org_id migration)

M2.5 (Donations)
 └── M2.2 required (fund routing)
 └── enables M2.6

M2.6 (Campaigns)
 └── M2.5 required

M2.7 (Notifications)
 └── M2.3 required (payment received trigger)
 └── M2.5 optional (donation confirmation)

M2.8 (Reporting)
 └── M2.3 required (reconciliation data)
 └── M2.5 required (donation data)

M2.9 (Dashboards)
 └── M2.8 required (report data sources)

M2.10 (Audit Export)
 └── M2.1 required (created_by)
 └── M2.2 required (fund transactions)
```

---

## Recommended Sprint 1 Scope (Start Immediately)

The most valuable work that can start today, with the lowest risk:

1. **V12 Migration** — Add `created_by` and `created_at` to all tables (nullable, backward-compatible)
2. **V13 Migration** — Add `organisations` table + `organisation_id` FK to all tables (backfill JJT-001)
3. **V18 Migration** — Create `fund_accounts` and `fund_transactions` tables
4. **Seed** — Create JJT General Education Fund with an admin-configurable starting balance
5. **Auto-debit** — When early support entry created, auto-debit the fund
6. **Admin fund widget** — Show current fund balance on admin dashboard

These 6 items represent the minimum viable Phase 2 and can be delivered in 2 weeks. They close the most critical financial visibility gap with zero risk to Phase 1 functionality.
