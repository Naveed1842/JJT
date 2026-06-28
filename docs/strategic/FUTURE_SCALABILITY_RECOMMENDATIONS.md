# Future Scalability Recommendations
## Junior Jinnah Trust — Architecture for Growth

| | |
|---|---|
| **Document Version** | 1.0 |
| **Classification** | Strategic — Internal |
| **Prepared** | 2026-06-28 |
| **Status** | For Review |

---

## Framing: What Scale Looks Like for JJT

| Horizon | Children | Sponsors | Monthly Transactions | Staff |
|---------|----------|---------|---------------------|-------|
| Today (Phase 1) | ~50 | ~30 | ~80 | 2–3 |
| 2 years | ~500 | ~400 | ~900 | 5–10 |
| 5 years | ~5,000 | ~4,000 | ~9,000 | 15–30 |
| 10 years | ~50,000 | ~40,000 | ~100,000 | 50+ |

These numbers are not unusual for a successful education charity. Many UK/US charities started where JJT is now and reached five-digit child counts within a decade.

The architectural decisions made today will either **enable** or **block** that growth at a cost that is orders of magnitude lower to address now than later.

---

## Decision 1 — UUID Primary Keys (Already Correct)

**Current state:** All entities use UUID primary keys. The admin provides UUID values at creation time for idempotent imports.

**Why this is right for scale:**
- UUIDs can be generated client-side without a round-trip to the database
- Idempotent imports mean bulk data from spreadsheets can be ingested without generating duplicates
- UUIDs are globally unique — if JJT ever merges with another organisation's dataset, there are no primary key collisions

**What to protect:** Never migrate to sequential auto-increment IDs. The current approach is correct and should be maintained. If performance concerns arise with UUID indexing, use UUID v7 (time-ordered) rather than UUID v4 — this eliminates B-tree fragmentation at scale.

**Action required now:** None. ✓

---

## Decision 2 — Append-Only Ledger (Already Correct)

**Current state:** Ledger entries are never updated or deleted. Progress can only move forward.

**Why this is right for scale:**
- As transaction volume grows to hundreds of thousands, the ability to audit any historical state is non-negotiable
- Append-only tables can be horizontally partitioned by month with no consistency issues
- Read replicas can serve historical queries without locking concerns
- Event-driven architectures (notifications, reporting) can consume the ledger as an event stream

**What to protect:** Never add UPDATE or DELETE to ledger_entries, even for corrections. The correction mechanism (a new `CORRECTION` entry type) must be additive.

**Action required now:** Add the `CORRECTION` coverage type (Phase 2) before the programme grows large enough that correction requests become a daily occurrence.

---

## Decision 3 — Modular Monolith (Correct For Now, Needs Boundaries)

**Current state:** A Spring Boot modular monolith with layered packages.

**The Recommendation:**

The modular monolith is the right choice for JJT today. Microservices would add operational complexity (service discovery, distributed transactions, network latency between services) that is not justified at current scale.

However, the current monolith has blurry module boundaries. For example:
- `AdminController` directly injects `SponsorJpaRepository` (we did this today for the list endpoint)
- Controllers reach into infrastructure repositories rather than going through use cases

**The risk:** As the codebase grows, the monolith becomes a "big ball of mud" — every class depends on every other class, and extracting any module becomes a multi-week refactor.

**Recommended boundaries to enforce now (costs nothing to do early, very expensive to fix later):**

```
Module 1: Identity & Access
  └── Users, roles, authentication, sessions, permissions

Module 2: Programme Management
  └── Children, schools, campuses, enrolment lifecycle

Module 3: Financial Ledger
  └── Ledger entries, fund transactions, payment records, reconciliation

Module 4: Sponsorship
  └── Sponsors, sponsorships, commitment lifecycle

Module 5: Content & Reporting
  └── Progress updates, reports, exports, dashboards

Module 6: Donor Engagement
  └── Campaigns, donations, donor receipts, communications
```

Each module should have:
- Its own package
- Its own repository interfaces (no cross-module repository injection)
- An explicit API (use cases) that other modules call
- No direct entity access across module boundaries

**If these boundaries are enforced from Phase 2 onwards**, extracting modules into separate services later is a matter of deploying the module separately — not a full rewrite.

---

## Decision 4 — Multi-Currency Architecture

**Current state:** `educationAmount` is stored as a decimal with a `educationCurrency` string. Each child has one currency.

**The problem at scale:**
- Pakistan operations will always be in PKR
- UK donors pay in GBP
- UAE donors pay in AED
- Reporting needs to show total programme cost in PKR and donor contributions in their native currencies
- Exchange rates change; historical reports must use the rate at the time of transaction

**Recommended changes:**

1. **Child records:** Always in local currency (PKR for Pakistan). This is correct today.

2. **Payment records:** Store in both sponsor currency and PKR:
   - `amountInSponsorCurrency: NUMERIC`
   - `sponsorCurrency: CHAR(3)`
   - `exchangeRateApplied: NUMERIC(10,4)`
   - `amountInPKR: NUMERIC` (= amountInSponsorCurrency × exchangeRateApplied)

3. **Fund accounts:** Each fund account operates in its base currency (PKR for Pakistan operations). Foreign currency amounts are converted on receipt.

4. **Exchange rate history:** A `ExchangeRate` lookup table:
   - `fromCurrency, toCurrency, effectiveDate, rate` — one row per rate per day
   - Used for historical reporting at the rate that was current on the transaction date

**Cost of not doing this now:** Every payment from an overseas sponsor is manually converted in someone's head. No historical record of what rate was used. Donor reports in GBP require manual spreadsheet work.

**Cost of doing this now:** One additional table, a few extra columns on payment records. Trivial.

---

## Decision 5 — Multi-Tenancy (Multiple Organisations)

**Current state:** BRD Section 5.2 says multi-organisation support is out of scope for Phase-1. The `org_id` column on users has no foreign key because there is no `organisations` table.

**The strategic question:** Should JJT expand the platform to serve other charities as a SaaS product?

**Arguments for (Product Officer perspective):**
- If the platform works for JJT, it works for any education charity in Pakistan
- Multi-tenancy turns a cost centre (the platform) into a revenue source
- Donors in the UK looking for Pakistan education charities could find multiple programmes on one platform
- Shared infrastructure reduces per-organisation cost

**Arguments against (current focus):**
- Multi-tenancy significantly increases complexity (tenant isolation, billing, onboarding)
- JJT should focus on proving the model before expanding it
- Premature multi-tenancy that isn't truly isolated is a security and data leak risk

**Architectural recommendation:**

Even if JJT never offers SaaS to other organisations, the platform should be designed for **multi-tenancy at the data level** from Phase 2. Specifically:

- Add an `organisations` table (Flyway V12)
- Add `organisation_id` FK to `children`, `sponsors`, `sponsorships`, `ledger_entries`, `progress_updates`
- All queries filter by `organisation_id` extracted from the authenticated user's JWT claims
- JJT itself is `organisation_id = "JJT-001"`

**Why do this now:**
- Adding `organisation_id` to existing tables with millions of rows later requires a zero-downtime migration that is technically complex
- Doing it at 50 children costs zero
- Doing it at 50,000 children requires careful planning, downtime, and risk

**This is the single most important architectural decision to make in Phase 2.**

---

## Decision 6 — Event-Driven Architecture (for the future)

**Current state:** Synchronous request/response only. No events published.

**Why this matters at scale:**

Consider these operations that should happen when a sponsor makes a payment:
1. Payment record created
2. Fund account credited
3. Child ledger entry created
4. Sponsorship status confirmed for the month
5. Email sent to sponsor ("payment received")
6. Email sent to sponsor ("here is your child's update")
7. Admin notification ("all payments received")
8. Reporting database updated
9. Donor receipt generated

In the current synchronous model, if any one of steps 1–9 fails, the entire HTTP request fails. The admin has to retry. At scale with 100+ payments per day, this is fragile.

**Recommended approach (Phase 3+):**

- Step 1 is synchronous (the HTTP response)
- Steps 2–9 are asynchronous events published to a message queue
- Each downstream concern consumes the event independently
- Failure in step 7 does not prevent step 5

This requires an event bus (Spring's `ApplicationEventPublisher` for the monolith phase, or a dedicated message broker like RabbitMQ/Kafka for the distributed phase).

**Implement internally first:** Within the monolith, use Spring's `@TransactionalEventListener` to fire events after transaction commit. This costs nothing to refactor later into external message queues.

---

## Decision 7 — Database Partitioning Strategy

**Current state:** Single PostgreSQL database, no partitioning.

**At 100,000 children × 12 months × 10 years = 12 million ledger entries:**
- A single unpartitioned table will still perform acceptably with proper indexes
- PostgreSQL handles tables of this size routinely
- BUT: queries like "give me all ledger entries for 2022" become full table scans without partition pruning

**Recommended partitioning strategy (implement at 1M+ rows):**

```sql
-- Partition ledger_entries by year
CREATE TABLE ledger_entries (...)
PARTITION BY RANGE (entry_month);

CREATE TABLE ledger_entries_2026 PARTITION OF ledger_entries
FOR VALUES FROM ('2026-01') TO ('2027-01');

CREATE TABLE ledger_entries_2027 PARTITION OF ledger_entries
FOR VALUES FROM ('2027-01') TO ('2028-01');
```

This means "show me all 2026 entries" only scans the 2026 partition — a 12x speed improvement for annual reports.

**When to implement:** When any single query on ledger_entries takes > 100ms. Not before.

---

## Decision 8 — Caching Strategy

**Current state:** No caching. Every request hits PostgreSQL.

**At scale, the following queries are expensive and rarely change:**
- Children listing (changes when new children added or sponsorship status changes)
- Sponsor's child list (changes only when sponsorship activated/expired)
- Ledger entries for a child (append-only — new entries only)

**Recommended approach:**
- Cache `GET /api/org/children` with a 60-second TTL (or invalidate on child/sponsorship write)
- Cache individual child profiles with a 5-minute TTL
- Do NOT cache ledger entries — they need to be real-time for financial accuracy

**Implementation:** Spring Boot's `@Cacheable` with Caffeine for Phase 2/3. Redis for distributed caching when scaling beyond single instance.

---

## Decision 9 — API Versioning (Already Planned)

**Current state:** ROADMAP Milestone 6 plans `/api/v1/` URL versioning.

**Importance at scale:** When JJT has 40,000 sponsors using the portal, a breaking API change cannot be deployed overnight. Versioning allows:
- Old clients to continue working on v1
- New clients to adopt v2
- Gradual migration with a deprecation window

**Recommendation:** Implement `/api/v1/` prefix in the next backend release (should have been in from the start). The longer this is deferred, the more clients need to update URLs simultaneously.

---

## Decision 10 — Observability

**Current state:** Basic Heroku logs only. No structured logging, no distributed tracing, no metrics.

**At scale, without observability:**
- "The app is slow" is impossible to diagnose
- "Which admin action is taking 3 seconds?" cannot be answered
- "Did the payment email actually send?" cannot be confirmed
- "How many API requests per second?" is unknown

**Phase 2 minimums:**
- Structured JSON logging (every request: method, path, status, duration_ms, user_id, role)
- Error tracking (Sentry — free tier is sufficient for years)
- Health monitoring with alerting (UptimeRobot — free)

**Phase 3 additions:**
- Application performance monitoring (Datadog, New Relic, or Prometheus + Grafana)
- Distributed tracing (OpenTelemetry)
- Database query performance logging (slow query log at > 100ms)

---

## Summary of Decisions Ranked by Urgency

| Decision | Do Now (Phase 2) | Do Later | Why |
|----------|:---:|:---:|-----|
| Multi-tenancy data foundation (org_id) | ✓ | | Adding to 50 children costs nothing; adding to 50,000 is risky |
| CORRECTION coverage type on ledger | ✓ | | Daily operational need once errors occur at scale |
| created_by on all financial records | ✓ | | Compliance requirement; trivial to add now |
| PaymentRecord entity | ✓ | | Financial integrity gap; closes the commitment-vs-payment problem |
| FundTransaction / Central Fund | ✓ | | Organisation cannot manage finances without this |
| UUID v7 (time-ordered) | | ✓ | Only relevant at millions of rows |
| Module boundary enforcement | ✓ | | Gets harder with every new feature added |
| Structured logging | ✓ | | Should have been in Phase 1 |
| API versioning (/api/v1/) | ✓ | | Deferred long enough |
| Event-driven architecture | | ✓ | Phase 3, when async needs emerge |
| Database partitioning | | ✓ | Only at 1M+ ledger entries |
| Multi-currency exchange rates | ✓ | | Any overseas sponsor creates this need |
| Redis caching | | ✓ | Only when single-instance hits limits |
