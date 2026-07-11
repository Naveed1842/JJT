# JJT Platform — Complete Project Summary
### Based on Actual Implementation · feat/firebase-auth-jjt Branch

---

## 1. EXECUTIVE SUMMARY

### What is JJT?

Junior Jinnah Trust (JJT) is a full-stack, cloud-deployed SaaS platform for managing child education sponsorships. It is live at **sponsorone.app** (frontend) and **jjt-platform.herokuapp.com** (API).

The platform provides three distinct digital surfaces: a public-facing website where sponsors can browse children and commit to monthly or yearly giving, a secured admin console for organisational staff to manage all operations, and a private sponsor portal where registered sponsors can track their children, view ledger entries, and read monthly progress updates.

### What Business Problem Does It Solve?

Most education-focused NGOs manage sponsorship programmes through spreadsheets, WhatsApp messages, cash envelopes, and paper receipts. This creates four compounding problems:

- **No audit trail** — payments arrive with no formal tracking, making compliance reporting impossible
- **No accountability** — donors cannot verify their money was applied to their sponsored child
- **Manual reconciliation** — matching payments to sponsorships happens offline, typically monthly
- **Missed communications** — progress updates reach sponsors inconsistently, or not at all

JJT replaces all of this with a single platform that records every action, every payment, and every update — with every record immutable and auditable.

### Target Users

| User Type | Role | Access |
|---|---|---|
| JJT Staff / Super Admin | JJT_ADMIN | Full platform control |
| Organisation Staff | ORG_ADMIN | Organisation-scoped full access |
| Registered Sponsor | SPONSOR | Private portal — own children only |
| Public Visitor | None | Browse children, commit sponsorship |

### Business Value

- Eliminates manual reconciliation of monthly sponsorship payments
- Provides a compliance-ready, append-only audit trail for board and regulatory review
- Gives sponsors real-time digital visibility into their commitment, education ledger, and child progress
- Separates Zakat tracking from general funds — enabling Islamic-compliant financial reporting
- Reduces operational overhead through automated email notifications, bulk imports, and one-click exports

---

## 2. PRODUCT OVERVIEW

JJT is structured as three logically separate products sharing one codebase and one API:

**1. Public Website (sponsorone.app)**
An anonymously accessible web application where potential sponsors discover children, learn about the trust, and complete a guided 5-step sponsorship commitment without creating an account. The public site shows real-time child availability based on live sponsorship data.

**2. Admin Console (/admin)**
A role-protected, feature-rich operations platform covering the full lifecycle of every child, sponsor, sponsorship, payment, donation, fund account, campaign, and user. The console contains 18 logical sections and is the operational backbone of the organisation. It is accessible to users with JJT_ADMIN or ORG_ADMIN roles.

**3. Sponsor Portal (/sponsor/portal)**
A private portal for registered sponsors. After logging in, sponsors see only their own sponsored children. Each child's card expands to show the complete month-by-month education ledger and all progress updates. Sponsors can also update their own profile information.

All three surfaces share a single Spring Boot REST API with role-based access enforced at every endpoint.

---

## 3. FEATURE INVENTORY

### Public Website

✅ **Home Page** — Hero section, up to 3 featured available children, live total/available children count, CTAs to browse children and learn why to give

✅ **Children Listing** (`/children`) — Full paginated browsable list; search by name, city, or campus; filter by availability status (All / Seeking / Bridged); load-more pagination; real-time availability badges

✅ **Child Detail Page** (`/children/:childId`) — Individual child profile with education cost, campus, city, current availability status, and trust signals panel

✅ **5-Step Sponsorship Wizard** (`/children/:childId/sponsor`) — Fully guided public commitment flow, no account required:
- Step 1: Set Intention (Sadaqah / Zakat / General)
- Step 2: Choose Plan (Monthly or Yearly; yearly carries a 10% discount)
- Step 3: Enter Personal Details (name, email, phone)
- Step 4: Bank Transfer Instructions (account title, number, IBAN, bank name — each field individually copyable to clipboard)
- Step 5: Confirmation page with selected start month

✅ **Sponsor Confirmation Page** (`/sponsor/confirmation`) — Post-commitment landing page

✅ **Trust & Accountability Page** (`/trust`) — Static transparency page

✅ **Why Give Page** (`/why-give`) — Static impact and motivation page

✅ **Login Page** (`/login`) — JWT-based authentication with remember-session via refresh token

---

### Child Management

✅ **Create Child** — Add individually via form (roll number, full name, city, campus, school name, education amount, education currency)

✅ **Bulk Import** — Upload Excel file for batch enrolment; row-by-row result report (imported / skipped / failed count); downloadable import template

✅ **Children List** — Searchable admin list with current sponsor name/email, availability badge, roll number, city, campus

✅ **Child Detail Panel** — Inline drill-down showing: complete sponsorship history (all time), month-by-month ledger entries, all progress updates

✅ **Individual PDF Report** — Printable per-child report generated server-side with OpenPDF

✅ **Export to Excel** — Full children list exported to `.xlsx` with Apache POI

---

### Sponsor Management

✅ **Create Sponsor** — Add sponsor with display name, email, phone

✅ **Sponsor List** — Searchable list of all sponsors

---

### Sponsorship Lifecycle

✅ **Commit Sponsorship** (Admin) — Assign a sponsor to a child with start month and commitment type (Monthly / Yearly)

✅ **Public Commitment** — Sponsor commits via public wizard; creates PENDING sponsorship and a new sponsor record (find-or-create by email to prevent duplicates)

✅ **Activate Sponsorship** — Admin reviews and activates a PENDING sponsorship; triggers welcome email to sponsor

✅ **Expire Sponsorship** — Admin manually expires an ACTIVE sponsorship; child becomes available again

✅ **At-Risk Detection** — Identifies sponsorships with consecutive overdue payment months

✅ **Filter by Status** — View PENDING, ACTIVE, or ALL sponsorships in the commitments section

✅ **Sponsorship uniqueness** — Partial unique index (PostgreSQL) enforces one active/pending sponsorship per sponsor-child-month combination; allows re-commitment after expiry

---

### Early Support (Pre-Sponsorship Funding)

✅ **Record Early Support** — Allows an org admin to cover a child's education cost from organisational funds before a sponsor is found; written directly to the child's education ledger

---

### Progress Updates

✅ **Add Progress Update** — Monthly written summary for a child; stored in database; triggers automatic email notification to the child's active sponsor

✅ **Progress History** — All updates visible in child detail panel (admin) and sponsor portal (sponsor)

---

### Donor Management

✅ **Create Donor** — Name, email, phone, donor type (Individual / Corporate / Trust / Anonymous), notes

✅ **Donor List** — Searchable list of all donors

---

### Donation Tracking

✅ **Record One-Time Donation** — Donor, type (General / Zakat / Sadaqah / Sponsorship Top-Up / Corporate / In-Kind), amount, currency, date, fund account, notes

✅ **Mark Donation Received** — Generates a formal receipt number (format: JJT-YYYY-NNNN); updates fund account balance

✅ **Reverse Donation** — Marks a received donation as reversed; debits fund account

✅ **Printable Receipt** — Opens a formatted printable receipt in a new browser window

✅ **Donation List** — Paginated list with status (Expected / Receipted / Reversed)

✅ **Export Donations to Excel** — Full donation history export

---

### Zakat Module

✅ **Dedicated Zakat Section** — Separate admin section for Zakat-only operations

✅ **Zakat Stats** — Headline metrics: total Zakat received (all time), this year, this month

✅ **Zakat Ledger** — Filterable table showing only Zakat-type donations with receive/reverse actions

---

### Recurring Donations

✅ **Create Recurring Schedule** — Donor, type, amount, currency, frequency (Monthly / Quarterly / Annual), start date, optional end date, linked fund account

✅ **Pause Schedule** — Temporarily suspends a recurring schedule

✅ **Cancel Schedule** — Permanently cancels a recurring schedule

✅ **Generate Expected Donations** — Creates expected donation records from active recurring schedules for the current or upcoming period

---

### Fund Accounts

✅ **Fund Account List** — List accounts with live balance, currency, minimum reserve threshold

✅ **Credit Fund Account** — Record cash received into a fund account; appends an immutable credit transaction

✅ **Fund Balance** — Live computed balance from append-only transaction log

✅ **Transaction History** — Paginated full transaction history per fund account

✅ **Minimum Reserve Warning** — Balance below reserve threshold triggers a visual indicator

---

### Payment Reconciliation

✅ **Generate Payment Records** — Creates one expected payment record per active sponsorship per month

✅ **Monthly Reconciliation View** — Navigate month-by-month; shows expected, received, overdue, partial, waived summary counts

✅ **Record Payment Received** — Marks a payment as RECEIVED; records bank reference, received date, actual amount; credits fund account; writes ledger entry

✅ **Waive Payment** — Marks a payment as WAIVED with a required reason

✅ **Payment Status Lifecycle** — EXPECTED → RECEIVED / OVERDUE / PARTIAL / WAIVED / PREPAID

✅ **At-Risk Sponsorships** — Flags sponsorships with consecutive overdue months

✅ **Export Reconciliation to Excel** — Monthly reconciliation data exported to `.xlsx`

---

### Campaigns

✅ **Create Campaign** — Name, description, target amount, currency, start/end date, linked fund account

✅ **Open Campaign** — Transitions from DRAFT to ACTIVE status

✅ **Close Campaign** — Transitions from ACTIVE to CLOSED status

✅ **Campaign List** — View all campaigns with status (DRAFT / ACTIVE / FUNDED / CLOSED)

---

### Reports

✅ **Cash Flow Report** — 6-month statement with opening balance, total credits, total debits, closing balance per month per fund account

✅ **Portfolio Report** — Active, pending, and expired sponsorship counts; total monthly commitment value; breakdown by commitment type (Monthly / Yearly)

---

### Audit Log

✅ **System-Wide Audit Trail** — Every create, update, and state-change operation writes an immutable audit event (event type, actor email, entity type, entity ID, description, timestamp)

✅ **Audit Log View** — Paginated table view with actor, event type, description, and relative timestamp

---

### Alerts

✅ **Severity-Tagged Alerts** — System raises alerts with severity: INFO / WARNING / CRITICAL (displayed as High/Medium/Low in UI)

✅ **Alert Types** — PAYMENT_OVERDUE, FUND_BELOW_RESERVE, SPONSORSHIP_AT_RISK, GENERAL

✅ **Sidebar Badge** — Active (undismissed) alert count visible in admin sidebar from any section

✅ **Dismiss Alert** — Marks an alert as dismissed (timestamped); dismissed alerts do not show in active count

---

### Email Notifications

✅ **Sponsorship Activated** — Welcome email to sponsor when commitment is activated by admin

✅ **Progress Update** — Notification email to active sponsor when a progress update is added for their child

✅ **Notification Log** — Full email notification history stored with status (QUEUED / SENT / FAILED), template used, and sent timestamp

✅ **Additional templates available** — PAYMENT_RECEIVED, PAYMENT_OVERDUE, DONATION_RECEIPT (template definitions exist; sending hooks implemented for the two above)

---

### User Management

✅ **Create Sponsor User** — Links a platform user account to a sponsor record; assigns SPONSOR role

✅ **Create Org Admin User** — Creates a user with ORG_ADMIN role

✅ **Activate / Deactivate User** — Enable or disable user login without deleting the record

✅ **User List** — List all platform users with role, linked sponsor, and active status

---

### Settings

✅ **Organisation Settings** — Update organisation name, base currency, payment due day

✅ **Change Password** — Authenticated users can change their own password

✅ **Profile View** — View current user information (read-only)

---

### Sponsor Portal

✅ **Sponsored Children List** — Shows all children linked to the logged-in sponsor's account

✅ **Child Detail Expansion** — Per-child: full month-by-month education ledger, all progress updates

✅ **Profile Management** — View and update display name and phone number

---

### Authentication & Security

✅ **JWT Authentication** — HS512 signed access tokens (15-minute expiry), stored in Angular memory only

✅ **Refresh Token Rotation** — Opaque UUID refresh tokens (7-day expiry); stored as SHA-256 hash in database; rotated on every `/refresh` call; stored in `localStorage` as `rt`

✅ **Role-Based Access Control** — Three roles enforced at every endpoint: JJT_ADMIN, ORG_ADMIN, SPONSOR

✅ **Session Restoration** — `APP_INITIALIZER` silently restores session from stored refresh token on every page load

✅ **Auth Interceptor** — Automatically attaches Bearer token to all API requests; handles 401 responses with token refresh retry

✅ **Route Guards** — `adminAuthGuard` protects `/admin`; `sponsorAuthGuard` protects `/sponsor/portal`

✅ **Claim-Scoped Data Access** — `SponsorChildrenController` extracts `sponsorId` from JWT claims, never from request parameters

---

### Import / Export

✅ **Bulk Child Import** (Excel) — Upload `.xlsx`; row-by-row import with full result report

✅ **Import Template Download** — Pre-formatted Excel template for child bulk import

✅ **Export Children** (Excel) — Full children list with sponsor info

✅ **Export Donations** (Excel) — Full donation history

✅ **Export Reconciliation** (Excel) — Monthly payment reconciliation data

✅ **Export Child Report** (PDF) — Individual child report generated server-side

---

### Media Management

🚧 **Backend Infrastructure** — StorageProvider interface, LocalStorageProvider, S3StorageProvider, MediaService, MediaController, MediaProcessingService, and Flyway migrations for media tables are implemented in the backend. Image variant generation (thumbnail/medium/full) is built using Thumbnailator.

🚧 **Angular UI** — Media upload and display components are not yet integrated. Child profiles on the public site and in the admin console do not yet display photos.

---

## 4. TECHNICAL HIGHLIGHTS

### Frontend — Angular 19

- **Framework:** Angular 19 with standalone components throughout; no NgModule pattern
- **Routing:** Lazy-loaded components via `loadComponent()`; all routes defined in `app.routes.ts`
- **State Management:** Angular Signals API (`signal<T>()`, `computed()`) for reactive state — no NgRx or RxJS-heavy store
- **Signal Stores:** `ChildrenStore` and `AlertsStore` implement TTL-based cache invalidation with signals; sidebar alert badge reacts to store state automatically
- **HTTP Interceptors:** `AuthInterceptor` (token attachment + 401 retry), `HttpErrorInterceptor` (centralised error handling)
- **Design System:** Custom inline CSS design system with CSS custom properties; no third-party UI framework; cream/forest-green brand palette applied consistently
- **Session Management:** `APP_INITIALIZER` calls `AuthService.restoreSession()` on boot; auth state managed as `BehaviorSubject<CurrentUser | null>`
- **Public Flow:** 5-step wizard is a standalone component with internal step state; no backend call until Step 5 confirmation
- **Code Volume:** ~4,631 lines of TypeScript across 43 source files (excluding specs)

### Backend — Spring Boot 3.2 / Java 17

- **Architecture:** Strict three-layer Clean Architecture: API layer → Application (use cases) → Domain (pure Java) → Infrastructure (JPA)
- **Domain Isolation:** `core/domain` has zero Spring or JPA dependencies; domain objects are immutable with state-transition methods returning new instances (`Sponsorship.withStatus()`)
- **JPA Separation:** JPA entities never cross into application or domain code; `DomainMapper` converts at the infrastructure boundary before use cases execute
- **Append-Only Ledger:** `EducationSupportLedger.appendEntry()` is the only permitted write operation; PostgreSQL database rules (`no_update`, `no_delete`) enforce this at the DB layer for fund transactions
- **Derived Availability:** Child availability status (`AVAILABLE / RESERVED / ALLOCATED`) is computed from sponsorships at query time — never stored as a column
- **Use Cases:** 5 formal use case classes (`CreateChildUseCase`, `CreateSponsorUseCase`, `CommitFutureSponsorshipUseCase`, `RecordEarlySupportUseCase`, `AddMonthlyProgressUseCase`); complex admin operations handled in service classes
- **Exception Handling:** Centralised in `GlobalExceptionHandler`; no per-controller `@ExceptionHandler`
- **Schema Management:** Flyway owns the schema entirely; `hibernate.ddl-auto=none` always; 25 migration files (V1–V25)
- **Code Volume:** ~10,929 lines of Java across 203 source files

### Database — PostgreSQL 16

- **Tables:** 20 tables created across 25 Flyway migrations
- **Immutability enforced at DB level:** PostgreSQL rules block `UPDATE` and `DELETE` on `fund_transactions` and `ledger_entries`
- **Partial Unique Index:** `uk_sponsor_child_start WHERE status IN ('ACTIVE','PENDING')` allows re-commitment after expiry while preventing duplicate active/pending sponsorships
- **Multi-tenancy:** `organisation_id` foreign key on every business table; all queries scoped by organisation
- **JSONB metadata:** `audit_events.metadata` stores contextual event data as JSONB
- **Indexed for performance:** Composite indexes on all high-read queries (sponsorship status + month, fund transactions by account + date, audit events by org + date)

### Security Architecture

- **Token Strategy:** Short-lived access token (15 min, memory-only) + long-lived refresh token (7 days, `localStorage`); prevents XSS-based token theft of access tokens
- **Refresh Token Hashing:** Stored as SHA-256 hash in `refresh_tokens` table; raw token never persisted
- **Token Rotation:** Every `/api/auth/refresh` call atomically revokes old token and issues new pair
- **CORS:** Hardcoded allowed origins in `SecurityConfig` (`localhost:4200`, `sponsorone.app`, `www.sponsorone.app`)
- **Claim Scoping:** Sponsor-role endpoints extract `sponsorId` from JWT claims only; request parameter manipulation cannot access another sponsor's data

### Deployment

- **Backend:** Heroku; `Procfile` runs the Spring Boot JAR; `system.properties` pins Java 17; continuous deployment via `git push heroku <branch>:main`
- **Frontend:** Firebase Hosting; build output `dist/jjt-angular/browser/`; `firebase.json` rewrites all routes to `index.html` for client-side routing; deployed via `firebase deploy --only hosting`
- **Database:** PostgreSQL on Heroku Postgres add-on (production); Docker Compose (`postgres:16-alpine`) for local development

### Testing

- **Integration Tests:** Testcontainers spins up a real `postgres:16-alpine` container per test run via `@ServiceConnection`; no mocking
- **Test Files:** 4 test classes — `AuthIntegrationTest`, `PublicSponsorshipIntegrationTest`, `TestcontainersConfiguration`, `JjtPlatformApplicationTests`
- **H2 Local Profile:** `application-local.yml` configures H2 in-memory database with `MODE=PostgreSQL`; activated with `-Dspring-boot.run.profiles=local` for fast local development without Docker

### Third-Party Libraries

| Library | Purpose |
|---|---|
| jjwt 0.12.6 | JWT generation, signing, and validation |
| Apache POI 5.2.5 | Excel import and export |
| OpenPDF 1.3.30 | PDF child report generation |
| Spring Boot Starter Mail | Email notification delivery via SMTP |
| Flyway Core | Database schema versioning |
| Testcontainers | Real PostgreSQL in integration tests |
| Thumbnailator (🚧) | Image variant generation for media |
| AWS SDK v2 (🚧) | S3 media storage (backend built, not deployed) |

---

## 5. BUSINESS WORKFLOWS

### Workflow 1: Child Onboarding

1. Admin opens Children section → "Add Child"
2. Fills form: roll number, full name, city, campus, school, education amount
3. System creates: `ChildEntity` (DB), `EducationSupportLedger` (DB), writes audit event
4. Child immediately appears in public listing with status "Seeking Sponsor"

**Bulk variant:** Admin uploads `.xlsx` → system processes row by row → returns import result report (N imported, N skipped, N failed)

### Workflow 2: Public Sponsorship Commitment

1. Visitor browses `/children`, finds available child
2. Completes 5-step wizard (Intention → Plan → Details → Payment → Confirm)
3. On Step 5, API call to `POST /api/public/sponsorships`:
   - Finds or creates Sponsor by email (prevents duplicate sponsor records)
   - Creates Sponsorship with status = PENDING
   - Writes audit event: `PUBLIC_SPONSORSHIP_COMMITTED`
4. Admin receives system alert: new commitment awaiting review
5. Admin navigates to Commitments, reviews, clicks Activate
6. Sponsorship status → ACTIVE
7. Welcome email sent to sponsor automatically
8. Child availability → "Bridged / Sponsored"

### Workflow 3: Monthly Payment Reconciliation

1. Admin navigates to Reconciliation → selects month → clicks "Generate Payments"
2. System creates one `SponsorPayment` record per active sponsorship for that month (status = EXPECTED)
3. As bank transfers arrive, admin clicks "Receive" on each record
4. Admin enters: bank reference, received date, actual amount
5. System: marks payment RECEIVED, credits fund account, writes immutable ledger entry, updates audit log
6. For missed payments: admin waives (with reason) or system flags as OVERDUE
7. At month-end, summary: expected / received / overdue / waived / partial

### Workflow 4: Early Support (Pre-Sponsorship)

1. Admin navigates to Early Support section
2. Selects child (not yet sponsored), month, education amount
3. System writes ledger entry directly (coverage type = EARLY_SUPPORT)
4. Child's education is recorded as covered from organisational funds for that month
5. Audit event written

### Workflow 5: Progress Update → Sponsor Notification

1. Admin navigates to Progress section
2. Selects child (must have at least one ledger entry), selects month, types summary
3. System saves `ProgressUpdate` to DB
4. System finds active sponsorship for child → fetches sponsor email
5. Email notification queued and sent: "Progress Update: [Child Name]"
6. Notification status tracked in `email_notifications` table (QUEUED → SENT / FAILED)
7. Update appears in sponsor portal under that child

### Workflow 6: Donation Tracking → Receipt

1. Admin navigates to Donations
2. Records donation: selects donor, type (incl. Zakat), amount, date, fund account
3. System creates donation record with status = EXPECTED (or directly RECEIPTED)
4. Admin clicks "Receive" → system:
   - Generates sequential receipt number (JJT-2026-0001 format)
   - Updates donation status = RECEIPTED
   - Credits linked fund account
   - Writes audit event
5. Admin prints receipt (new browser window with formatted receipt)

### Workflow 7: Sponsor Portal Session

1. Sponsor visits `sponsorone.app`, clicks "Sign In"
2. Enters credentials → JWT access token returned (memory) + refresh token (`localStorage`)
3. `APP_INITIALIZER` restores session on next visit via refresh token
4. Sponsor portal loads → `GET /api/sponsor/children` (claims-scoped, no parameter manipulation possible)
5. Sponsor expands a child card → ledger entries + progress updates loaded
6. Sponsor updates profile (display name, phone) → `PATCH /api/sponsor/profile`

---

## 6. ENGINEERING CHALLENGES SOLVED

### 1. Append-Only Financial Ledger with Two Enforcement Layers

The ledger invariant (no updates, no deletes) is enforced in two places: in the domain object (`EducationSupportLedger.appendEntry()` is the only write method) and at the PostgreSQL level via database rules that reject any `UPDATE` or `DELETE` statement against `fund_transactions` and `ledger_entries`. This double enforcement means the ledger cannot be corrupted even through direct database access.

### 2. Sponsorship Re-Commitment After Expiry

Naively preventing duplicate sponsorships blocked legitimate re-commitment: if a sponsor's commitment expired and they wanted to re-sponsor the same child, the unique constraint rejected the new record. Solved with a **partial unique index** (`WHERE status IN ('ACTIVE','PENDING')`), allowing a new commitment to be created after the previous one is expired. This was non-trivial because Hibernate's `@UniqueConstraint` annotation is not partial — removing the JPA annotation and relying purely on the Flyway-managed database index was required.

### 3. Find-or-Create Sponsor on Public Commitment

Public commitments arrive without authentication. Each submission includes a name and email. The naive implementation created a new sponsor record on every submission, causing a unique email constraint violation when the same email was used twice (e.g., re-committing after expiry). Solved with an `findByContactEmail().orElseGet(() -> createAndSave())` pattern — the sponsor record is reused if the email already exists.

### 4. Flyway Out-of-Order Migration

The production database had received migrations from a parallel branch (higher version numbers V26+) before V25 was merged. Flyway rejected V25 as "resolved but not applied to database." Solved by enabling `out-of-order: true` in Flyway configuration, which allows migrations to be applied in non-sequential order without requiring `repair`.

### 5. Multi-Tenancy via Organisation Scoping

The platform is designed to support multiple organisations sharing one database. Every business table carries an `organisation_id` column, and every query includes an organisation scope. This was introduced mid-project (V13 migration) and required a backfill of all existing data and an update to every query path — non-trivial in a system already in production.

### 6. Immutable Domain Objects in a JPA World

Spring Data JPA operates on mutable entities, but the domain layer requires immutable objects. The solution is a strict boundary: `DomainMapper` converts JPA entities to domain objects before they enter any use case, and converts domain objects back to JPA entities for persistence. Domain state transitions (`Sponsorship.withStatus()`) return new instances rather than mutating existing ones. JPA entities are only mutable — setters are restricted and their package visibility is limited.

### 7. JWT Claims-Scoped Sponsor Access

The sponsor portal must return only the children for the authenticated sponsor — preventing any parameter manipulation to access another sponsor's data. This is enforced by extracting `sponsorId` from the JWT claims inside the controller, never accepting it as a request parameter. If a user has a SPONSOR role, their `sponsorId` is baked into their token at issuance.

### 8. Angular Signal-Based Reactive Store with TTL Cache

The admin sidebar needs a live count of undismissed alerts, but polling the server on every navigation would be wasteful. Built a custom `AlertsStore` using Angular Signals with a TTL-based cache: the signal holds the last-fetched data and a fetch timestamp; a `computed()` derivation checks whether the TTL has expired and re-fetches automatically. This gives reactive, always-fresh data without manual subscription management or NgRx complexity.

---

## 7. PORTFOLIO SUMMARY (200–300 words)

**Junior Jinnah Trust Platform** is a production-deployed, full-stack SaaS application for managing child education sponsorship programmes. Built and architected end-to-end, the platform handles the complete operational lifecycle — from a child's first enrolment to monthly payment reconciliation — replacing manual spreadsheets and WhatsApp coordination with an auditable, accountable digital system.

The platform consists of three interconnected surfaces: a public-facing website (sponsorone.app) where potential sponsors discover children and complete a guided 5-step commitment without creating an account; a secured admin console with 18 functional modules covering children, sponsors, sponsorships, donations (including dedicated Zakat tracking), fund accounts, recurring giving schedules, campaigns, and a compliance-ready audit log; and a private sponsor portal where registered sponsors track their children's education ledger and receive monthly progress updates.

On the backend, the API is built with Spring Boot 3.2, following strict Clean Architecture — domain objects are pure Java with zero framework dependencies, JPA entities never cross into business logic, and the financial ledger is append-only with immutability enforced at both the domain and PostgreSQL levels. Role-based JWT authentication implements access token memory-only storage (XSS mitigation) with 7-day refresh token rotation. The frontend is Angular 19 with standalone components and the Signals API for reactive state management, deployed on Firebase Hosting.

The project involved solving genuine production problems: Flyway out-of-order migrations on a live database, partial unique indexes for sponsorship re-commitment, multi-tenancy backfills, and claims-scoped data access enforcement. The platform is live in production, deployed on Heroku (API) and Firebase (frontend), and actively used.

**Stack:** Java 17 · Spring Boot 3.2 · PostgreSQL 16 · Angular 19 · TypeScript · JWT · Flyway · Apache POI · OpenPDF · Testcontainers · Heroku · Firebase

---

## 8. RESUME EXPERIENCE

**Use this block as a starting point and adjust seniority language to match your target role.**

---

**Full-Stack Software Engineer** | Junior Jinnah Trust Platform | 2024–Present

- Architected and built a production SaaS platform (sponsorone.app) end-to-end for managing child education sponsorships, replacing manual NGO operations with a fully auditable digital system serving public visitors, organisation admins, and registered sponsors across three distinct application surfaces
- Designed and implemented a strict Clean Architecture with a framework-free domain layer, append-only financial ledger enforced at both application and PostgreSQL levels, and a JPA entity boundary preventing domain contamination — enabling confident schema evolution across 25 Flyway migrations on a live production database
- Built a complete payment reconciliation module with sponsor payment lifecycle tracking (Expected → Received / Overdue / Partial / Waived), bank reference recording, fund account balance computation, at-risk sponsorship detection, and month-by-month Excel export
- Implemented a Zakat management module with dedicated donation tracking, separate ledger, and headline statistics — enabling Islamic-compliant financial reporting for NGO and charitable compliance requirements
- Engineered JWT authentication with short-lived access tokens (memory-only, XSS-mitigation) and rotating refresh tokens stored as SHA-256 hashes; claims-scoped sponsor data access prevents any parameter manipulation across sponsor boundaries
- Developed a 5-step public sponsorship wizard in Angular 19 requiring no account creation, with find-or-create sponsor deduplication and a partial unique index solution enabling sponsorship re-commitment after expiry
- Built automated email notification system (Spring Boot Mail) that triggers on sponsorship activation and monthly progress updates, with full notification log tracking (QUEUED / SENT / FAILED) and template-based delivery
- Implemented bulk Excel import (Apache POI) for batch child enrolment with row-by-row result reporting, server-side PDF report generation (OpenPDF), and one-click Excel exports across children, donations, and reconciliation modules
- Implemented multi-tenancy with `organisation_id` scoping across 20 database tables, including a live production backfill migration; designed system to support future multi-organisation deployment
- Wrote integration tests using Testcontainers (real PostgreSQL, not mocks) and resolved a production Flyway out-of-order migration issue without data loss by enabling `out-of-order` mode and verifying migration idempotency

---

## 9. LINKEDIN PROJECT

**Project Title:** JJT — Child Education Sponsorship Platform

**Type:** Full-Stack Application (Production)

**URL:** sponsorone.app

---

**Overview**

JJT is a full-stack, cloud-deployed SaaS platform I built end-to-end for managing child education sponsorship programmes. The platform is live in production and serves as the complete operational backbone for the Junior Jinnah Trust — replacing manual spreadsheet tracking, WhatsApp coordination, and paper receipts with a formal, auditable, role-based digital system.

**Key Features**

- Public sponsorship commitment flow (5-step wizard; no account required) for sponsors to commit to monthly or yearly education support
- Admin console with 18 operational modules: child management, bulk Excel import, sponsorship lifecycle management (PENDING → ACTIVE → EXPIRED), monthly payment reconciliation, fund accounts, donations with Zakat tracking, recurring giving schedules, campaigns, donor management, user management, and compliance-ready audit log
- Append-only financial ledger enforced at both application and database level — every education payment is permanently recorded and cannot be altered
- Automatic email notifications to sponsors on activation and monthly progress updates
- Sponsor portal with claims-scoped access showing each sponsor's own children, month-by-month education ledger, and progress history
- Role-based access control: JJT_ADMIN, ORG_ADMIN, SPONSOR with JWT authentication using memory-only access tokens and rotating refresh tokens

**Technologies**

Java 17 · Spring Boot 3.2 · Spring Security · PostgreSQL 16 · Flyway · Angular 19 · TypeScript · Angular Signals · JWT (jjwt 0.12.6) · Apache POI · OpenPDF · Spring Boot Mail · Testcontainers · Heroku · Firebase Hosting

**Business Impact**

- Eliminated manual payment reconciliation — every sponsorship payment is now digitally recorded with bank reference and date
- Enabled Islamic-compliant Zakat fund tracking with dedicated reporting separate from general donations
- Provides a full immutable audit trail for every platform action, suitable for board and regulatory review
- Reduced sponsor communication overhead through automatic progress update email delivery

---

## 10. COMPANY PORTFOLIO DESCRIPTION

**JJT — Child Education Sponsorship Platform**

*End-to-end SaaS platform · Spring Boot · Angular · PostgreSQL · Live in Production*

---

JJT is a purpose-built digital operations platform for child education sponsorship NGOs, delivered as a fully functional, cloud-deployed product.

The platform solves a genuine operational problem: most education-focused charities manage sponsor relationships through spreadsheets, WhatsApp groups, and paper receipts — creating compliance risk, poor sponsor experience, and operational bottlenecks. JJT replaces all of this with a unified, role-secured, fully auditable system.

**What was built:**

A three-surface application consisting of a public sponsorship website, a comprehensive admin console, and a private sponsor portal — sharing a single Spring Boot REST API with JWT-based role enforcement across 76 endpoints.

The backend follows strict Clean Architecture: a framework-free domain layer, append-only financial ledger enforced at database level, JPA entity isolation from business logic, and Flyway-managed schema versioning across 25 migrations. The frontend is Angular 19 with standalone components, lazy loading, and reactive state via Angular Signals.

Key capabilities delivered: 5-step public sponsorship wizard, monthly payment reconciliation with lifecycle tracking, Zakat-separate donation management, recurring giving schedules, fund account balance management, bulk Excel import for child onboarding, PDF report generation, automated email notifications, and a complete immutable audit log.

**Deployment:** Heroku (API) · Firebase Hosting (Frontend) · PostgreSQL 16

**Outcome:** A production system actively used to manage child sponsorships, with every payment reconciled, every sponsor notified, and every action auditable.

---

## 11. PROJECT METRICS

These metrics are derived directly from the repository.

| Metric | Count |
|---|---|
| **Backend Java source files** | 203 |
| **Frontend TypeScript source files** | 43 |
| **Java lines of code** | ~10,929 |
| **TypeScript lines of code** | ~4,631 |
| **Total lines of code** | ~15,560 |
| **REST controllers** | 20 |
| **REST API endpoints** | ~76 |
| **JPA entities** | 20 |
| **Database tables** | 20 |
| **Flyway migrations** | 25 |
| **Spring Data repositories** | 20 |
| **Service classes** | 14 |
| **Use case classes** | 5 |
| **Angular page components** | 11 |
| **Angular shared components** | ~15 |
| **Angular signal stores** | 2 |
| **HTTP interceptors** | 2 |
| **Route guards** | 2 |
| **User roles** | 3 (JJT_ADMIN, ORG_ADMIN, SPONSOR) |
| **Admin console sections** | 18 |
| **Email notification templates** | 5 |
| **Donation types** | 6 |
| **Export types** | 4 (Excel ×3, PDF ×1) |
| **Integration test files** | 4 |
| **Supported payment statuses** | 6 |
| **Sponsorship statuses** | 3 (PENDING, ACTIVE, EXPIRED) |
| **Cloud integrations** | 2 (Heroku, Firebase) |

---

## 12. LESSONS LEARNED

### Engineering

**1. Database-level immutability is worth the extra step.**
Append-only semantics in the domain layer are easy to write but easy to bypass through direct DB access or future code paths. Adding PostgreSQL rules (`ON UPDATE → DO INSTEAD NOTHING`) as a second enforcement layer makes the invariant unconditional. The cost is a few lines of SQL. The benefit is a ledger you can genuinely guarantee.

**2. JPA annotations bleed into business concerns if not explicitly managed.**
The `@UniqueConstraint` on `@Table` enforces constraints unconditionally when Hibernate manages the schema (H2 + `create-drop`). When Flyway manages the schema (PostgreSQL), only the migration SQL runs. This means `@UniqueConstraint` and Flyway migration constraints can drift silently. The correct approach: remove constraint annotations from JPA entities entirely and let Flyway be the single source of schema truth.

**3. Partial unique indexes are underused in typical Spring + JPA stacks.**
Standard JPA doesn't model partial indexes — they must be defined in Flyway migrations. But they solve a class of business problems (allow re-use after terminal states) that full unique constraints cannot. Knowing when to reach for a partial index rather than restructuring the domain saves significant complexity.

**4. Flyway `out-of-order` is a legitimate operational tool, not a last resort.**
When feature branches add migrations and those branches merge in non-sequential order, `out-of-order: true` is the correct resolution — not migration renaming, not `flyway repair` with data risk. Understanding Flyway's versioning model early prevents production incidents.

**5. Clean Architecture's value is clearest at migration and refactoring time.**
When a domain rule changes, it changes in one place. When a persistence mechanism changes (PostgreSQL → H2 for local dev), no domain or use case code changes. The discipline of maintaining strict layer boundaries has a cost upfront and a significant benefit during maintenance.

### Product

**6. Public flows must handle re-use from day one.**
The first implementation of the public sponsorship endpoint assumed each submission was from a new sponsor. Reality: sponsors re-commit after expiry, or submit twice due to uncertainty. Find-or-create at the boundary is the correct default for any email-keyed resource.

**7. NGO workflows have implicit compliance requirements that need to be made explicit.**
Zakat is not just a donation category — it is a legally and religiously distinct fund that must be tracked, reported, and disbursed separately. Treating it as a simple label until an explicit module was requested cost a refactor. Surfacing implicit compliance needs early leads to better data models.

**8. The audit log is not a feature — it is infrastructure.**
Adding comprehensive audit logging after the fact requires touching every write path. Building it as infrastructure from the start (centralized `AuditService` called from every command) costs very little and enables compliance, debugging, and stakeholder trust with no retrofitting.

**9. State transitions need a defined lifecycle from the first model.**
Sponsorship status started as two states and grew to three with separate business rules for each transition. Payment status started at two and grew to six. Designing an explicit lifecycle (enum + allowed transitions) early prevents inconsistent state bugs and makes business rule changes safer.

**10. Two data sources for the same conceptual thing will always diverge.**
The admin component had two lists of children (`this.children` for dropdowns, `this.adminChildren` for the data table) sourced from different endpoints. Creating a child updated one but not the other, making the table stale. The root cause was two lists for one conceptual entity. The fix was adding the second reload call — but the real lesson is: maintain one source of truth per entity type at the UI layer.

---

*End of JJT Project Summary*
*Based on: feat/firebase-auth-jjt branch · Repository: Naveed1842/JJT*
*Prepared: July 2026*
