# Risk and Gap Analysis
## Junior Jinnah Trust — Phase 2 Assessment

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Part 1 — Current State Gap Analysis

### Gap 1: The Organisation Cannot Know Its Financial Position

**Severity:** Critical
**Current Impact:** Every day, the organisation operates without knowing:
- How much money is in the general fund
- How much of that money is committed to children already enrolled
- Whether the fund can sustain its current level of early support

**Root Cause:** No `FundAccount` or `FundTransaction` entities exist. The financial pool is entirely implicit.

**Consequence if not addressed:** The organisation could overcommit to early support (promising to cover children it cannot actually fund), leading to a governance emergency and potential breach of donor trust.

**Phase 2 Resolution:** M2.2 — Fund Accounting MVP (highest priority item)

---

### Gap 2: Sponsorship Payments Are Not Tracked After Activation

**Severity:** Critical
**Current Impact:** A sponsor who stops paying after month 3 of their commitment remains ACTIVE indefinitely. The child appears funded but has no ledger entries for months 4+. Nobody is alerted.

**Root Cause:** No `SponsorPayment` entity. No monthly cycle. ACTIVE is a permanent status with no ongoing verification.

**Consequence if not addressed:** JJT's core promise — "no child loses their education support" — can be silently broken. The platform gives false assurance.

**Phase 2 Resolution:** M2.3 — Payment Reconciliation (critical path)

---

### Gap 3: No Financial Attribution

**Severity:** High
**Current Impact:** For every financial record in the system (ledger entries, sponsorships), there is no record of who created it or when.

**Specific Problem:** "Who recorded this early support entry for Zainab Khalid in April 2026?" — currently unanswerable.

**Consequence if not addressed:** Cannot satisfy external audit. Cannot investigate data errors. Cannot demonstrate governance to a regulator.

**Phase 2 Resolution:** M2.1 — Data Integrity Sprint (first migration)

---

### Gap 4: Donation Management Does Not Exist

**Severity:** High
**Current Impact:** There is no way to record any incoming money. When JJT receives cash at a fundraising event, or a bank transfer from a general donor, there is no mechanism to record it. The only financial pathway that exists is the sponsorship commitment flow — and even that doesn't record actual payment receipt.

**Root Cause:** Phase 1 was explicitly scoped to sponsorship management. Donation management was deferred.

**Consequence if not addressed:** JJT cannot demonstrate to donors where their money went. Cannot produce a donor receipt. Cannot claim Gift Aid. Cannot report to its board on fundraising performance.

**Phase 2 Resolution:** M2.5 — Donation Management

---

### Gap 5: No Multi-Organisation Architecture

**Severity:** High (architectural — escalating cost with time)
**Current Impact:** All data is in one flat namespace. Adding `organisation_id` to every table later (when there are 50,000 rows) is a complex zero-downtime migration with risk.

**Consequence if not addressed:** If JJT grows, expands, or decides to offer the platform to other charities, the retrofitting cost will be significant.

**Phase 2 Resolution:** M2.1 / M2.4 — Multi-Organisation Foundation

---

### Gap 6: Child Lifecycle is Incomplete

**Severity:** Medium
**Current Impact:** Children have no `status` field beyond what can be derived from their sponsorship state. There is no "graduated," "withdrawn," or "transferred" status. Children who have left the programme remain in the active listing.

**Consequence:** The admin children list becomes increasingly cluttered with inactive records. Public browse page may show graduated children as available for sponsorship.

**Phase 2 Resolution:** M2.1 — add child lifecycle status; M2.3 — child archival workflow

---

### Gap 7: Restricted Funds Cannot Be Managed

**Severity:** Medium
**Current Impact:** If a donor specifies their donation is Zakat, the money goes into the same implicit pool as all other donations. There is no Zakat fund, no Zakat tracking, no Zakat compliance report.

**Consequence:** JJT cannot demonstrate Sharia compliance on Zakat funds, which may deter Zakat donors and expose the organisation to a religious compliance risk.

**Phase 2 Resolution:** M2.2 (fund accounts) + Sharia review

---

### Gap 8: No Notification System

**Severity:** Medium
**Current Impact:** All communication (payment reminders, receipt confirmations, progress updates) is manual. Admin must individually email sponsors, donors, and board members.

**Consequence:** At 50 sponsors this is manageable. At 500 sponsors it is impossible. Manual communication also means no delivery audit trail.

**Phase 2 Resolution:** M2.7 — Notifications

---

### Gap 9: No Campaigns

**Severity:** Low (currently) → High (as JJT grows)
**Current Impact:** JJT cannot run a fundraising campaign with a specific target and progress tracking. Every appeal is managed outside the platform (spreadsheets, social media posts).

**Consequence:** Ramadan, Eid, back-to-school campaigns — key fundraising moments — generate no on-platform data.

**Phase 2 Resolution:** M2.6 — Campaign Module

---

### Gap 10: No Executive Reporting

**Severity:** Low (currently) → High (as governance matures)
**Current Impact:** The board has no platform-provided visibility into programme performance or financial health.

**Phase 2 Resolution:** M2.8, M2.9 — Reporting and Dashboards

---

## Part 2 — Phase 2 Risk Register

### R-01: Fund Accounting Goes Live Without Sharia Review

**Category:** Compliance
**Likelihood:** High (easy to skip this step)
**Impact:** Critical (religious and donor trust)
**Description:** The Zakat Fund is the most legally and religiously sensitive feature. If JJT goes live with Zakat collection without a formal ruling on beneficiary eligibility and expense categories, donations may be collected incorrectly and disbursements may violate Sharia obligation.

**Mitigation:**
- Defer Zakat fund UI until a Sharia compliance review is complete
- Build the schema and backend now; gate the UI on a feature flag
- Assign responsibility: specific advisor, specific deadline

**Residual Risk if Mitigation Fails:** Zakat donors cannot be refunded if disbursements are found non-compliant. Reputational and religious risk.

---

### R-02: Monthly Reconciliation Job Misses Runs (Heroku Dynos Sleep)

**Category:** Technical
**Likelihood:** High on Heroku Free tier
**Impact:** Critical — if SponsorPayments are not auto-created, overdue detection fails
**Description:** Heroku free and eco dynos sleep after 30 minutes of inactivity. A scheduled job runs in the JVM — if the dyno is asleep when the 1st-of-month job should run, it doesn't run.

**Mitigation:**
- Upgrade to Heroku Hobby/Basic tier (no sleep) for production
- OR: use an external cron service (cron-job.org, EasyCron) to send an HTTP ping to a `/api/admin/jobs/monthly-cycle` endpoint
- The endpoint is idempotent: running it twice in the same month creates no duplicate records

**Detection:** Admin dashboard shows "last monthly cycle run: [date]". If this is not the current month, alert is shown.

---

### R-03: Database Migration Fails on Production

**Category:** Technical / Operational
**Likelihood:** Low
**Impact:** High — downtime, data risk
**Description:** Flyway migrations run on startup. A failed migration on Heroku causes the dyno to fail to start. If the migration has partially applied, the database may be in an inconsistent state.

**Mitigation:**
- All Phase 2 migrations are backward-compatible (add columns, add tables — no column drops)
- Test migrations on a staging database with a copy of production data before deploying
- Always take a Heroku database backup (`heroku pg:backups:capture`) before deploying a migration sprint
- Ensure each migration is idempotent where possible

---

### R-04: Fund Balance Goes Negative Before Phase 2 Launches

**Category:** Operational
**Likelihood:** Low (small scale currently)
**Impact:** Medium — financial management risk
**Description:** Between now and Phase 2 launch, JJT continues to record early support without any fund balance visibility. If many children are added on early support and no new donations come in, the organisation could unknowingly over-commit.

**Mitigation (interim):** Add a simple read-only "fund balance" field to the admin dashboard that admin manually updates with the current bank balance. Not a system-calculated value — just a manually entered number that serves as a reminder. Takes 2 hours to implement.

**Phase 2 Resolution:** M2.2 fully addresses this.

---

### R-05: Duplicate Payment Records

**Category:** Data Integrity
**Likelihood:** Medium (admin error)
**Impact:** Medium — inflated fund balance, incorrect reconciliation
**Description:** An admin could accidentally record the same payment twice — once when logging in the morning, once the next day while reviewing the bank statement.

**Mitigation:**
- Require bank reference number on all SponsorPayment records
- If a payment with the same bank reference + same sponsorship + same month already exists, show a warning: "A payment with this bank reference has already been recorded."
- Idempotency check is soft (warn, not block) because two payments might legitimately share a reference (though rare)

---

### R-06: Sponsor Contact Information Becomes Stale

**Category:** Operational
**Likelihood:** High (sponsors change email addresses)
**Impact:** Medium — notifications fail; receipts are undeliverable
**Description:** JJT sponsors are real people whose email addresses change. Without a self-service profile update, the email on record will become stale.

**Mitigation:**
- Phase 2: Allow sponsors to update their own display name, phone, and notification preferences (not email — email change requires admin to prevent account takeover)
- Admin-initiated email change: admin logs a reason and a new email verification is sent to the new address before it takes effect
- Email bounce tracking: when a notification bounces, flag the sponsor profile as "contact details need update" and alert admin

---

### R-07: Zakat Balance Not Disbursed By Hijri Year End

**Category:** Compliance
**Likelihood:** Medium (may happen in first year as processes are established)
**Impact:** High — Sharia compliance breach
**Description:** If Zakat funds are collected but not fully disbursed by the Hijri year end, the organisation must seek a Sharia ruling on the disposition of the remaining balance.

**Mitigation:**
- Zakat balance alert: when Hijri year end is 60 days away and Zakat balance > 0, alert admin
- Consider a planned disbursement event 30 days before year end (allocate remaining Zakat to next month's education costs for eligible children)

---

### R-08: Multi-Tenancy Migration Breaks Existing Data

**Category:** Technical
**Likelihood:** Low (careful migration design) / High (if skipped)
**Impact:** Critical — all existing data access could break
**Description:** The `organisation_id` migration (V13) adds a NOT NULL foreign key to all existing tables. If the migration doesn't properly backfill all existing rows with the JJT-001 UUID, the rows will violate the FK constraint and become inaccessible.

**Mitigation:**
- Two-step migration: (a) add `organisation_id` as nullable, backfill all existing rows to JJT-001; (b) in a subsequent migration, add NOT NULL constraint
- Never add FK constraint in the same migration as the column addition
- Test on a full copy of the production database before deploying

---

### R-09: Reporting Performance at Scale

**Category:** Technical
**Likelihood:** Low (currently) / High (at 5,000+ children)
**Impact:** Medium — reports time out; executives get no data
**Description:** Complex aggregate reports (annual statement, fund balance history) query millions of rows as the platform grows.

**Mitigation:**
- Design reports as proper SQL queries with indexes, not application-layer aggregation
- Add materialised views for frequently-used aggregates (monthly totals)
- Partition `fund_transactions` and `ledger_entries` by year at 1M+ rows
- Test report generation performance at 10x and 100x current scale in a staging environment before scaling

---

### R-10: Key Personnel Dependency

**Category:** Operational
**Likelihood:** High (small team, significant platform knowledge concentration)
**Impact:** High — critical operations cannot proceed if platform knowledge is with one person
**Description:** Currently, all platform knowledge and all admin access is concentrated in 2–3 people. If a key person leaves, important operations (month-end close, payment reconciliation) may fail.

**Mitigation:**
- Document all operational procedures in the platform's admin guide
- Create a month-end close checklist feature (M2.8 roadmap item)
- Ensure at least two people are trained on every operational workflow
- Admin credentials should be reset to known credentials and stored in a secure vault (LastPass, 1Password) accessible to the organisation, not individuals

---

## Part 3 — Business Rule Gaps Inherited from Phase 1

The following gaps were identified in `BUSINESS_RULE_GAP_ANALYSIS.md` (Phase 1 review) and are confirmed for Phase 2 resolution:

| Gap | Root Cause | Phase 2 Resolution |
|-----|-----------|-------------------|
| No DB-level append-only enforcement on ledger | Convention only | V15 migration (PostgreSQL rules) |
| No unique constraint on one ACTIVE per child | DB index dropped | V16 migration |
| Duplicate sponsor email warning missing | No check exists | M2.1 + BR-DON-05 |
| Education cost change not versioned | Scalar field | EducationCostHistory entity |
| Sponsorship renewal alerts absent | No expiry monitoring | M2.3 + BR-PAY-04 escalation |
| Orphaned children detection absent | No gap monitoring | At-Risk Dashboard (M2.3) |
| No `created_by` on financial records | Phase 1 deferred | V12 migration (M2.1) |
| YEARLY commitment protection missing | No early-expiry check | BR-PAY-07 |

---

## Part 4 — Phase 2 vs Phase 1 Comparison

| Capability | Phase 1 | Phase 2 |
|-----------|---------|---------|
| Fund balance visibility | None | Real-time, per fund |
| Payment verification | None (click Activate) | Full payment record with bank ref |
| Missed payment detection | None | Automated (OVERDUE in 15 days) |
| Donor management | None | Full donor profiles, receipts |
| Campaign fundraising | None | Full campaign lifecycle |
| Audit trail | Partial (no created_by) | Complete (AuditEvent log) |
| Multi-organisation | None | Foundation laid |
| Notifications | None | Email + in-app alerts |
| Reporting | None | 12-report catalogue |
| Executive dashboards | Admin panel only | Board, Finance, Sponsor, Campaign |
| Zakat management | None | Full fund accounting |
| Multi-currency | Implicit only | Recorded with exchange rate |
| Child lifecycle | AVAILABLE/RESERVED/ALLOCATED | Full 7-stage lifecycle |

---

## Part 5 — Risks That Phase 2 Does NOT Address (Phase 3+)

The following risks exist and are acknowledged but will not be resolved in Phase 2:

| Risk | Why Deferred |
|------|-------------|
| Online payment gateway (JazzCash, Stripe) | Regulatory complexity, PCI compliance; Phase 3 |
| Event-driven architecture for scale | Not needed at current volume |
| Database partitioning | Only needed at 1M+ rows |
| Public donor self-registration | Design decisions pending (OQ2-06) |
| Expense categories on ledger entries | Useful but not critical path |
| Gift Aid XML submission to HMRC | Phase 3; UK registration required first |
| Mobile app for sponsors | Phase 3 or 4 |
| Automated bank reconciliation via API | Phase 3; requires bank API integration |
