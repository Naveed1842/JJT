# JJT Platform — Complete Onboarding Guide

> **Goal of this document:** After reading it, a new engineer understands the business problem, every layer of the codebase, every API endpoint, the security model, the data model, how to run it locally, and how it deploys. One document. One day.

---

## Table of Contents

1. [What Is JJT?](#1-what-is-jjt)
2. [Phase-1 Scope and Guardrails](#2-phase-1-scope-and-guardrails)
3. [System Architecture](#3-system-architecture)
4. [Domain Model](#4-domain-model)
5. [Database Schema](#5-database-schema)
6. [Authentication and Authorization](#6-authentication-and-authorization)
7. [Backend API Reference](#7-backend-api-reference)
8. [Frontend Application](#8-frontend-application)
9. [Key Business Flows End-to-End](#9-key-business-flows-end-to-end)
10. [Project Layout](#10-project-layout)
11. [Local Development Setup](#11-local-development-setup)
12. [Test Suite](#12-test-suite)
13. [Deployment](#13-deployment)
14. [Configuration Reference](#14-configuration-reference)
15. [Architectural Decisions](#15-architectural-decisions)

---

## 1. What Is JJT?

**Junior Jinnah Trust (JJT)** is a charitable organization that funds children's education in Pakistan. The platform exists to solve one core problem:

> **A child's education must not pause because sponsorship is delayed.**

Before this platform, education expenses were tracked in spreadsheets. When a sponsor was late or absent, there was no way to know which children were at risk of dropping out. The org was paying expenses from a central pool with no visibility into who owed what.

**The platform's job:**

- Register children and their monthly education cost
- Record who pays for each child, each month (the "ledger")
- Let sponsors see the children they support and their progress
- Let the public commit to sponsoring a child online
- Give admins a dashboard to manage all of this

**What the platform intentionally does NOT do (Phase-1):**

- Accept payments (no Stripe, no bank integration)
- Automate sponsor matching
- Send emails or notifications
- Track attendance or academic grades in detail

All financial transactions happen offline (bank transfer). The platform records the commitment and the outcome; money moves separately.

---

## 2. Phase-1 Scope and Guardrails

Phase-1 is deliberately small. These rules are not to be broken without a formal decision:

| Rule | Reason |
|------|--------|
| Ledger is append-only — no edits, no deletes | Auditability; errors get a correcting entry |
| One ledger entry per child per month | Prevents double-counting |
| Sponsorship start month must be in the future | Prevents backdating commitments |
| Progress updates must reference an existing ledger month | Ensures progress is tied to actual support |
| No payments in the platform | Scope; handled by trust's bank account |
| Sponsors have read-only access to their assigned children only | Privacy; donor isolation |

---

## 3. System Architecture

### 3.1 Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend language | Java | 17 |
| Backend framework | Spring Boot | 3.2.3 |
| Build tool | Maven | 3.9.x (BOM, not parent) |
| Database | PostgreSQL | 16 |
| DB migrations | Flyway | 9.22.3 |
| Security | Spring Security | 6.2.2 |
| JWT library | JJWT | 0.12.6 |
| Password hashing | BCrypt | cost factor 12 |
| Frontend framework | Angular | 18 (standalone components) |
| Frontend HTTP | Angular HttpClient | with functional interceptors |
| Deployment platform | Heroku | Standard dyno |
| Frontend hosting | sponsorone.app | (custom domain, CDN) |

### 3.2 High-Level Architecture

```
┌─────────────────────────────────────────────────┐
│                  Browser / Client               │
│                                                 │
│   ┌────────────────────────────────────────┐    │
│   │         Angular 18 SPA                 │    │
│   │      (sponsorone.app)                  │    │
│   │                                        │    │
│   │  Pages: Home, Children, Child Detail,  │    │
│   │         Sponsor Commit, Confirmation,  │    │
│   │         Admin, Login                   │    │
│   │                                        │    │
│   │  Auth: JWT in memory + refresh token   │    │
│   │         in localStorage                │    │
│   └──────────────────┬─────────────────────┘    │
└─────────────────────┼───────────────────────────┘
                       │ HTTPS / REST
                       ▼
┌─────────────────────────────────────────────────┐
│              Spring Boot Backend                │
│         (Heroku — jjt-platform.herokuapp.com)   │
│                                                 │
│  ┌─────────────────────────────────────────┐    │
│  │          Spring Security Filter Chain   │    │
│  │  JwtAuthenticationFilter (Bearer token) │    │
│  │  CORS (3 origins: localhost, prod app)  │    │
│  │  CSRF disabled (stateless JWT)          │    │
│  └──────────────────┬──────────────────────┘    │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐    │
│  │           API Layer (Controllers)        │    │
│  │  /api/auth    /api/admin  /api/org       │    │
│  │  /api/sponsor /api/public               │    │
│  └──────────────────┬──────────────────────┘    │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐    │
│  │       Application Layer (Use Cases)      │    │
│  │  CreateChildUseCase, CommitSponsorship,  │    │
│  │  RecordEarlySupport, AddProgress...      │    │
│  └──────────────────┬──────────────────────┘    │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐    │
│  │     Core Domain (Pure Java — no JPA)    │    │
│  │  Child, Sponsor, Sponsorship, Ledger,   │    │
│  │  LedgerEntry, ProgressUpdate            │    │
│  └──────────────────┬──────────────────────┘    │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐    │
│  │     Infrastructure (JPA + Flyway)       │    │
│  │  JPA Entities, Repositories, Mappers   │    │
│  └──────────────────┬──────────────────────┘    │
└─────────────────────┼───────────────────────────┘
                       │
                       ▼
             ┌─────────────────┐
             │  PostgreSQL 16  │
             │  (Heroku add-on)│
             └─────────────────┘
```

### 3.3 Architectural Pattern: Modular Monolith

The backend is a **modular monolith** — a single deployable JAR, but with strict package-level layering that could be split into services later without major refactoring.

```
com.jjt.platform
│
├── api/                    ← HTTP: controllers, DTOs, GlobalExceptionHandler
│   ├── admin/              ← Admin-only operations (create child, sponsor, ledger entries)
│   ├── auth/               ← Login, refresh, logout, me, change-password
│   ├── common/             ← Shared DTOs, DtoMapper, GlobalExceptionHandler
│   ├── org/                ← Org admin read-only: list/get children with ledger
│   ├── publics/            ← Unauthenticated: public sponsorship commitment
│   └── sponsor/            ← Sponsor read-only: their assigned children only
│
├── application/            ← Use cases: orchestrate domain + persistence
│   └── usecase/
│
├── config/                 ← Spring config: security, JWT properties
│   └── security/
│
├── core/                   ← Pure business logic: no Spring, no JPA
│   └── domain/
│       ├── entity/         ← Child, Sponsor, Sponsorship, Ledger, LedgerEntry, ProgressUpdate
│       ├── exceptions/     ← DomainException, LedgerInvariantViolation, SponsorshipInvariantViolation
│       └── value/          ← Money, YearMonthValue
│
└── infrastructure/         ← JPA entities, repositories, mappers, Flyway seed
    └── persistence/
```

**Key design principle:** The `core/domain` package has zero Spring dependencies and zero JPA annotations. It can be unit-tested with plain Java. JPA entities live in `infrastructure/persistence/entity/` and are converted to domain objects by mappers before any business logic runs.

---

## 4. Domain Model

### 4.1 Entity Relationship (conceptual)

```
Child ──────────────────────────────────┐
  │                                     │
  │ 1:1                                 │ 1:N
  ▼                                     ▼
EducationSupportLedger          ProgressUpdate
  │                              (one per child per month;
  │ 1:N                           month must exist in ledger)
  ▼
LedgerEntry
(one per child per month;
 coverageType = EARLY_SUPPORT | SPONSOR)


Child ──────────────────── Sponsorship ──── Sponsor
        (N:M via                             (1 sponsor,
         sponsorships                         many sponsorships)
         table; but only
         1 active per child)
```

### 4.2 Entities Explained

#### Child
Represents a school-age child enrolled in the programme. Created independently of any sponsorship.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | UUID | Stable identifier, provided by admin on creation |
| `rollNumber` | String | Unique school roll number |
| `fullName` | String | Child's name |
| `city` | String | City of the school |
| `campusName` | String | Campus identifier |
| `schoolName` | String | Optional school name |
| `educationCost` | Money | Monthly cost (amount + ISO-4217 currency, e.g. 2000 PKR) |

**Availability status** is derived at query time, never stored:
- `AVAILABLE` — no active or pending sponsorship
- `RESERVED` — has a pending (not yet activated) sponsorship
- `ALLOCATED` — has an active (approved) sponsorship

#### Sponsor
A person or organization that has committed to support a child. Created by admin either manually or when a public visitor submits the sponsorship form.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | UUID | Stable identifier |
| `displayName` | String | Name shown in the app |
| `contactEmail` | String | Primary contact |
| `phone` | String? | Optional phone |

#### Sponsorship
Links a Sponsor to a Child for a future period. Follows a lifecycle: `PENDING → ACTIVE → EXPIRED`.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | UUID | Stable identifier |
| `sponsorId` | UUID FK | The sponsor |
| `childId` | UUID FK | The child |
| `startMonth` | YYYY-MM | Must be in the future at creation time |
| `status` | Enum | PENDING / ACTIVE / EXPIRED |
| `commitmentType` | Enum | MONTHLY / YEARLY |
| `createdAt` | Instant | When committed |

**Business rule:** Only one ACTIVE sponsorship per child at any time. A new public commitment is rejected if the child already has PENDING or ACTIVE status.

#### EducationSupportLedger
One ledger per child, created automatically when the child is registered. It is a container for `LedgerEntry` records.

**Design:** The ledger is an **append-only aggregate** — calling `appendEntry()` returns a new ledger instance with the entry added. Modifications to existing entries are not permitted; corrections are new entries (this is intentional for auditability).

#### LedgerEntry
One per child per month. Records that education expenses for that month were covered, and by whom.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | UUID | Stable identifier |
| `childId` | UUID | The child |
| `month` | YYYY-MM | The month covered |
| `educationCost` | Money | Amount paid that month |
| `coverageType` | Enum | EARLY_SUPPORT (paid by org pool) / SPONSOR (paid by a sponsor) |

#### ProgressUpdate
A monthly written update on the child's academic progress. Must reference a month that already has a ledger entry — this enforces the invariant that progress is only recorded for months when the child was actually supported.

| Field | Type | Meaning |
|-------|------|---------|
| `id` | UUID | Stable identifier |
| `childId` | UUID | The child |
| `month` | YYYY-MM | Must match an existing ledger entry month |
| `summary` | String | Written update (max 2000 chars) |

#### Value Objects

**Money** — amount (`BigDecimal`) + currency (`Currency`). Never just a number.

**YearMonthValue** — wraps Java's `YearMonth`. Used for month-precision dates throughout the ledger.

### 4.3 Domain Invariants (enforced in code, not just in DB)

1. `startMonth` on a Sponsorship must be strictly after the current month — enforced in `Sponsorship.validateFutureStart()`
2. A `LedgerEntry` can only be appended to the ledger for its own `childId` — enforced in `EducationSupportLedger.appendEntry()`
3. No two ledger entries may share the same month for the same child — enforced by both domain logic and DB unique constraint
4. A `ProgressUpdate` month must exist in the ledger — enforced in `ProgressUpdate.create()`
5. Only one ACTIVE or PENDING sponsorship per child via the public endpoint — enforced in `PublicSponsorshipService`

---

## 5. Database Schema

### 5.1 Tables

All schema changes are managed by **Flyway** — never alter the DB manually in production. Migrations live in `src/main/resources/db/migration/` and are numbered V1 through V11.

```sql
-- children: every child in the programme
children (
  id               UUID PRIMARY KEY,
  full_name        VARCHAR(255) NOT NULL,
  roll_number      VARCHAR(255) NOT NULL UNIQUE,
  city             VARCHAR(255) NOT NULL,
  campus_name      VARCHAR(255) NOT NULL,
  school_name      VARCHAR(255),
  education_amount NUMERIC(12,2) NOT NULL,
  education_currency CHAR(3) NOT NULL
)

-- education_support_ledgers: one per child, created on child registration
education_support_ledgers (
  id       UUID PRIMARY KEY,
  child_id UUID NOT NULL UNIQUE REFERENCES children(id)
)

-- ledger_entries: one per child per month
ledger_entries (
  id                  UUID PRIMARY KEY,
  ledger_id           UUID NOT NULL REFERENCES education_support_ledgers(id),
  child_id            UUID NOT NULL REFERENCES children(id),
  entry_month         CHAR(7) NOT NULL,       -- YYYY-MM
  education_amount    NUMERIC(12,2) NOT NULL,
  education_currency  CHAR(3) NOT NULL,
  coverage_type       VARCHAR(20) NOT NULL,   -- EARLY_SUPPORT | SPONSOR
  UNIQUE (ledger_id, entry_month)             -- no two entries for same month
)

-- sponsors: individuals or orgs that commit to support
sponsors (
  id            UUID PRIMARY KEY,
  display_name  VARCHAR(255) NOT NULL,
  contact_email VARCHAR(255) NOT NULL,
  phone         VARCHAR(64)
)

-- sponsorships: the link between sponsor and child
sponsorships (
  id              UUID PRIMARY KEY,
  sponsor_id      UUID NOT NULL REFERENCES sponsors(id),
  child_id        UUID NOT NULL REFERENCES children(id),
  start_month     CHAR(7) NOT NULL,   -- YYYY-MM, must be future
  status          VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  commitment_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at      TIMESTAMP
)

-- progress_updates: monthly written update per child
progress_updates (
  id           UUID PRIMARY KEY,
  child_id     UUID NOT NULL REFERENCES children(id),
  update_month CHAR(7) NOT NULL,   -- YYYY-MM
  summary      VARCHAR(2000) NOT NULL,
  UNIQUE (child_id, update_month)
)

-- users: platform logins (admins, org admins, sponsors)
users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role          VARCHAR(20) NOT NULL,   -- JJT_ADMIN | ORG_ADMIN | SPONSOR
  sponsor_id    UUID REFERENCES sponsors(id),  -- set for SPONSOR role
  org_id        UUID,                           -- set for ORG_ADMIN role (no FK yet)
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_login_at TIMESTAMPTZ
)

-- refresh_tokens: stored hashed; revocable server-side
refresh_tokens (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash  VARCHAR(255) NOT NULL UNIQUE,
  issued_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked_at  TIMESTAMPTZ          -- null = valid; set = revoked
)
```

### 5.2 Migration History

| Migration | What it does |
|-----------|-------------|
| V1 | Initial schema: children, ledgers, ledger_entries, sponsors, sponsorships, progress_updates |
| V2 | Unique index: only one active sponsorship per child |
| V3 | Sponsorship lifecycle: adds `status`, `created_at`, `expires_at`; adds `phone` to sponsors |
| V4 | Drops the V2 unique index (replaced by application-level enforcement) |
| V5 | Seeds test data (PostgreSQL-specific) |
| V6 | Adds child identity columns: `roll_number`, `city`, `campus_name`, `school_name` |
| V7 | Adds `commitment_type` to sponsorships (MONTHLY / YEARLY) |
| V8 | Removes V5 seed data |
| V9 | Adds `coverage_type` to ledger_entries (EARLY_SUPPORT / SPONSOR) |
| V10 | Creates `users` table for JWT auth |
| V11 | Creates `refresh_tokens` table |

---

## 6. Authentication and Authorization

### 6.1 User Roles

| Role | Who they are | What they can do |
|------|-------------|-----------------|
| `JJT_ADMIN` | Platform super-admin | Everything: create children, sponsors, sponsorships, users, ledger entries, progress updates |
| `ORG_ADMIN` | Trust staff member | Everything JJT_ADMIN can do except manage users |
| `SPONSOR` | An individual sponsor | Read-only: their own assigned children, ledger, progress |
| *(anonymous)* | Public visitor | Submit a sponsorship commitment via the public form |

### 6.2 JWT Token Design

The platform uses **stateless JWT** (no sessions, no cookies).

**Access Token**
- Algorithm: HS512 with a 72-byte key (exceeds the 64-byte security threshold)
- Expiry: 15 minutes (`JWT_ACCESS_EXPIRY_MS=900000`)
- Stored: in Angular's memory (lost on page refresh — by design)
- Claims: `sub` (userId), `email`, `role`, `sponsorId` (nullable), `orgId` (nullable)

**Refresh Token**
- Format: UUID (random, opaque)
- Stored client-side: `localStorage` under key `rt`
- Stored server-side: SHA-256 hash in `refresh_tokens` table
- Expiry: 7 days (`JWT_REFRESH_EXPIRY_MS=604800000`)
- Revocable: `revoked_at` column is set on logout; checking `isValid()` = not revoked AND not expired

**Token lifecycle:**

```
User logs in
    │
    ▼
POST /api/auth/login
    │  username + password
    ▼
Spring's DaoAuthenticationProvider
  verifies BCrypt password hash
    │
    ▼
Generate access token (JWT, 15 min)
Generate refresh token (UUID → SHA-256 → store in DB)
    │
    ▼
Return { accessToken, refreshToken, expiresIn, tokenType }
    │
    ▼ (15 min later, access token expires)
    │
POST /api/auth/refresh
    │  { refreshToken: "<uuid>" }
    ▼
Hash the UUID → look up in DB → check not revoked, not expired
    │
    ▼
Revoke old refresh token (set revoked_at)
Issue new access token + new refresh token (rotation)
    │
    ▼
Return { accessToken, refreshToken, ... }
    │
    ▼ (user logs out)
    │
POST /api/auth/logout
    │  { refreshToken: "<uuid>" }
    ▼
Hash UUID → set revoked_at → done
Clear Angular memory + localStorage
```

### 6.3 Angular Token Handling

```
Page load / refresh
    │
    ▼
APP_INITIALIZER runs AuthService.restoreSession()
    │
    ├── reads 'rt' from localStorage
    ├── POSTs to /api/auth/refresh
    ├── if success → stores new access token in memory
    │             → stores new refresh token in localStorage
    │             → calls /api/auth/me → populates currentUser$
    └── if failure → clears localStorage → user is unauthenticated
```

Every outbound HTTP request goes through `authInterceptor`:
1. If URL matches a skip list (`/api/auth/login`, `/api/auth/refresh`, `/api/public/`), send as-is
2. Otherwise, attach `Authorization: Bearer <access token>` header
3. If response is 401: silently call `/api/auth/refresh`, then retry the original request once
4. If refresh also fails: call `AuthService.clearSession()` and navigate to `/login`

### 6.4 RBAC Enforcement

Authorization is enforced at two levels:

**Level 1 — Spring Security filter chain** (`SecurityConfig.java`)
```
Public (no auth required):
  POST /api/auth/login
  POST /api/auth/refresh
  /api/public/**
  /actuator/**

Everything else → must have valid JWT
```

**Level 2 — Method security** (`@PreAuthorize` on controllers)
```
@PreAuthorize("hasAnyRole('JJT_ADMIN', 'ORG_ADMIN')")  → AdminController, OrgChildrenController
@PreAuthorize("hasRole('JJT_ADMIN')")                  → UserManagementController
@PreAuthorize("hasRole('SPONSOR')")                    → SponsorChildrenController
@PreAuthorize("isAuthenticated()")                     → AuthController (me, logout, change-password)
```

**Level 3 — Data isolation** (`SponsorChildrenController`)
Sponsor users can only see children where a sponsorship record links `sponsor_id = principal.getSponsorId()`. This is enforced in `isChildSponsoredBy()` on every endpoint — a valid SPONSOR JWT for sponsor A cannot access sponsor B's children.

### 6.5 Default Admin User

On first startup, `AdminUserInitializer` creates a default admin:
- Email: `admin@jjt.org` (overridable via `ADMIN_EMAIL` env var)
- Password: `Admin@JJT2024!` (overridable via `ADMIN_PASSWORD` env var)
- **Change this password immediately on production** via `PUT /api/auth/change-password`

---

## 7. Backend API Reference

Base URL (production): `https://jjt-platform-23fdba49b06d.herokuapp.com`

Base URL (local): `http://localhost:8080`

All responses use JSON. Error shape: `{ "code": "ERROR_CODE", "message": "Human readable message" }`.

### 7.1 Auth Endpoints (`/api/auth`)

#### `POST /api/auth/login`
No authentication required.

Request:
```json
{ "email": "admin@jjt.org", "password": "Admin@JJT2024!" }
```
Response `200`:
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```
Errors: `401 UNAUTHORIZED` (wrong credentials), `401` (deactivated account)

---

#### `POST /api/auth/refresh`
No authentication required.

Request: `{ "refreshToken": "<uuid>" }`

Response `200`: same shape as login. Old refresh token is revoked; new pair is issued.

Errors: `401` (token not found, revoked, or expired)

---

#### `POST /api/auth/logout`
Requires: authenticated (any role).

Request: `{ "refreshToken": "<uuid>" }`

Response `200`. The server revokes the refresh token; the client clears local storage.

---

#### `GET /api/auth/me`
Requires: authenticated.

Response `200`:
```json
{
  "id": "uuid",
  "email": "sponsor@example.com",
  "role": "SPONSOR",
  "sponsorId": "uuid-or-null",
  "orgId": "uuid-or-null"
}
```

---

#### `PUT /api/auth/change-password`
Requires: authenticated.

Request: `{ "currentPassword": "...", "newPassword": "..." }` (newPassword min 8 chars)

Response `200`. BCrypt re-hashed immediately; existing sessions remain valid (stateless JWT).

---

### 7.2 Admin Endpoints (`/api/admin`)

Requires: `JJT_ADMIN` or `ORG_ADMIN` role.

#### `POST /api/admin/children`
Create a child and open their education support ledger simultaneously.

Request:
```json
{
  "rollNumber": "JJT-001",
  "fullName": "Ahmed Ali",
  "city": "Karachi",
  "campusName": "North Campus",
  "schoolName": "Government Primary School",
  "educationAmount": "2000.00",
  "educationCurrency": "PKR",
  "childId": "uuid (required — admin provides for idempotency)",
  "ledgerId": "uuid (required — admin provides for idempotency)"
}
```
Response `201`: `{ "childId": "uuid", "ledgerId": "uuid" }`

Note: `childId` and `ledgerId` are required, not auto-generated. This allows the admin to re-run imports without creating duplicates (idempotent import pattern).

---

#### `POST /api/admin/sponsors`
Create a named sponsor record for use in managed sponsorships.

Request: `{ "displayName": "...", "contactEmail": "...", "phone": "...", "sponsorId": "uuid (optional)" }`

Response `201`: `{ "id": "uuid", "displayName": "...", "contactEmail": "..." }`

---

#### `POST /api/admin/early-support`
Record that the org pool covered a child's expenses for a given month.

Request:
```json
{
  "childId": "uuid",
  "month": "2024-03",
  "educationAmount": "2000.00",
  "educationCurrency": "PKR",
  "ledgerEntryId": "uuid (optional, auto-generated if absent)"
}
```
Response `201`: `{ "id": "uuid", "childId": "uuid", "month": "2024-03" }`

This creates a `LedgerEntry` with `coverageType = EARLY_SUPPORT`. After a sponsorship is activated, future entries use `coverageType = SPONSOR` (not via this endpoint).

---

#### `POST /api/admin/children/{childId}/progress`
Record a monthly progress update for a child.

Request: `{ "month": "2024-03", "summary": "Ahmed passed his mid-term exams.", "progressUpdateId": "uuid (optional)" }`

Response `201`. The month must have an existing ledger entry.

---

#### `POST /api/admin/sponsorships`
Commit a managed sponsorship (admin creates it directly, linking an existing sponsor record).

Request:
```json
{
  "sponsorId": "uuid",
  "childId": "uuid",
  "startMonth": "2024-04",
  "commitmentType": "MONTHLY",
  "sponsorshipId": "uuid (optional)"
}
```
Response `201`: `{ "id": "uuid", "sponsorId": "...", "childId": "...", "startMonth": "..." }`

---

#### `GET /api/admin/sponsorships?status=PENDING`
List sponsorships by status. Status values: `PENDING`, `ACTIVE`, `EXPIRED`. Defaults to `PENDING`.

Response: array of `SponsorshipSummaryResponse` including sponsor name, email, phone, commitment type.

---

#### `POST /api/admin/sponsorships/{sponsorshipId}/activate`
Transition a PENDING sponsorship to ACTIVE.

Response `200`.

---

#### `POST /api/admin/sponsorships/{sponsorshipId}/expire`
Transition an ACTIVE sponsorship to EXPIRED.

Response `200`.

---

#### `GET /api/admin/children/{childId}/sponsorships`
All sponsorships for a child (all statuses, newest first).

---

#### `GET /api/admin/children/{childId}/sponsorships/active`
Returns `{ "active": true/false }`.

---

### 7.3 User Management Endpoints (`/api/admin/users`)

Requires: `JJT_ADMIN` only.

#### `POST /api/admin/users/sponsor`
Create a login account for a sponsor.

Request:
```json
{
  "sponsorId": "uuid (must exist in sponsors table)",
  "email": "sponsor@example.com",
  "password": "minimum8chars"
}
```
Response `201`: `UserResponse` (id, email, role, sponsorId, active, createdAt).

The user's JWT will contain `sponsorId`; `SponsorChildrenController` uses this to scope their data access.

---

#### `POST /api/admin/users/org`
Create a login account for an org admin staff member.

Request: `{ "email": "...", "password": "...", "orgId": "uuid (optional)" }`

Response `201`: `UserResponse`.

---

#### `GET /api/admin/users/`
List all users. Response: array of `UserResponse`.

---

#### `PUT /api/admin/users/{id}/activate`
Re-activate a deactivated user. Deactivated users cannot log in (`DisabledException` → 401).

#### `PUT /api/admin/users/{id}/deactivate`
Deactivate a user.

---

### 7.4 Org Endpoints (`/api/org`)

Requires: `JJT_ADMIN` or `ORG_ADMIN`.

These are the read endpoints the Angular frontend uses when displaying children to the public (via `SponsorService`).

#### `GET /api/org/children`
List all children with derived availability status.

Response: array of `ChildDto`:
```json
[
  {
    "id": "uuid",
    "rollNumber": "JJT-001",
    "fullName": "Ahmed Ali",
    "city": "Karachi",
    "campusName": "North Campus",
    "schoolName": "Government Primary School",
    "educationAmount": "2000.00",
    "educationCurrency": "PKR",
    "availabilityStatus": "AVAILABLE"
  }
]
```
`availabilityStatus` is derived at query time: `AVAILABLE` | `RESERVED` | `ALLOCATED`.

---

#### `GET /api/org/children/{childId}`
Single child detail.

#### `GET /api/org/children/{childId}/ledger`
Full ledger with all entries.

Response:
```json
{
  "childId": "uuid",
  "entries": [
    {
      "id": "uuid",
      "month": "2024-01",
      "educationAmount": "2000.00",
      "educationCurrency": "PKR",
      "coverageType": "EARLY_SUPPORT"
    }
  ]
}
```

#### `GET /api/org/children/{childId}/progress`
All progress updates for a child.

---

### 7.5 Sponsor Endpoints (`/api/sponsor`)

Requires: `SPONSOR` role. Sponsor can only see their own assigned children.

Every endpoint extracts `sponsorId` from the JWT claims (not from the URL or query params). A SPONSOR token for sponsor A will get 403 if they attempt to view sponsor B's children — even if they know the child's UUID — because `isChildSponsoredBy(sponsorId, childId)` fails.

#### `GET /api/sponsor/children`
All children assigned to the authenticated sponsor.

#### `GET /api/sponsor/children/{childId}`
One assigned child's details. Returns 403 if not assigned to this sponsor.

#### `GET /api/sponsor/children/{childId}/ledger`
That child's full ledger. 403 if not assigned.

#### `GET /api/sponsor/children/{childId}/progress`
That child's progress updates. 403 if not assigned.

---

### 7.6 Public Endpoints (`/api/public`)

No authentication. These are called by anonymous visitors completing the sponsorship flow.

#### `POST /api/public/sponsorships`
Submit a public sponsorship commitment. Creates a new Sponsor record and a PENDING Sponsorship.

Request (validated with `@Valid`):
```json
{
  "childId": "uuid",
  "commitmentType": "MONTHLY",
  "sponsor": {
    "name": "Fatima Khan",
    "email": "fatima@example.com",
    "phone": "0300-1234567"
  }
}
```

Validation:
- `childId`: required
- `commitmentType`: required, must be `MONTHLY` or `YEARLY`
- `sponsor.name`: required, max 200 chars
- `sponsor.email`: required, valid email format, max 200 chars
- `sponsor.phone`: optional, max 30 chars

Response `201`:
```json
{ "childId": "uuid", "startMonth": "2024-05" }
```

Errors:
- `400` if validation fails or child doesn't exist
- `409 SPONSORSHIP_CONFLICT` if child already has ACTIVE or PENDING sponsorship

Start month is auto-set to next calendar month.

---

### 7.7 Error Codes Reference

| HTTP | Code | When |
|------|------|------|
| 400 | `VALIDATION_ERROR` | Bean validation failed, malformed JSON, invalid enum, bad field |
| 400 | `DOMAIN_ERROR` | Business rule violated (e.g., ledger month mismatch) |
| 401 | `UNAUTHORIZED` | Missing/invalid/expired JWT, wrong password |
| 403 | `FORBIDDEN` | Valid JWT but wrong role or not your data |
| 404 | `NOT_FOUND` | Route doesn't exist |
| 409 | `CONFLICT` | Duplicate unique key |
| 409 | `LEDGER_CONFLICT` | Duplicate ledger entry for same month |
| 409 | `SPONSORSHIP_CONFLICT` | Child already has PENDING or ACTIVE sponsorship |
| 500 | `INTERNAL_ERROR` | Unexpected exception (check server logs) |

---

## 8. Frontend Application

### 8.1 Technology Choices

- **Angular 18** with standalone components (no `NgModule`)
- Lazy-loaded routes — each page is a separate chunk, improving initial load time
- Functional interceptors (`HttpInterceptorFn`) — the modern Angular pattern, replacing class-based interceptors
- No global state library (NgRx, etc.) — state is held in services with `BehaviorSubject`

### 8.2 Pages and Routes

| Route | Page | Who can access | Purpose |
|-------|------|---------------|---------|
| `/` | Home | Anyone | Landing / marketing page |
| `/login` | Login | Unauthenticated | Email + password login form |
| `/children` | One Child At A Time | Anyone | Browse available children |
| `/children/:childId` | Child Detail | Anyone | Full profile of one child |
| `/children/:childId/sponsor` | Sponsor Commit | Anyone | Fill in name/email to sponsor |
| `/sponsor/confirmation` | Confirmation | Anyone | Thank-you page after commitment |
| `/admin` | Admin Dashboard | JJT_ADMIN, ORG_ADMIN | Manage children, sponsorships, etc. |

### 8.3 Key Services

**`AuthService`** (`src/app/services/auth.service.ts`)

The central auth state. All components that need to know if the user is logged in inject this.

```typescript
interface CurrentUser {
  id: string;
  email: string;
  role: 'JJT_ADMIN' | 'ORG_ADMIN' | 'SPONSOR';
  sponsorId: string | null;
  orgId: string | null;
}
```

Key methods:
- `login(email, password)` — calls `/api/auth/login`, stores tokens
- `logout()` — revokes refresh token, clears session, navigates to `/login`
- `restoreSession()` — called by `APP_INITIALIZER` on page load to restore session from refresh token
- `refreshAccessToken()` — called by the interceptor when a 401 is received
- `currentUser$` — `Observable<CurrentUser | null>` for components that render differently based on auth state

**`SponsorService`** (`src/app/services/sponsor.service.ts`)

Calls the org and public API endpoints. Used by the public-facing pages (no authentication required for the org endpoints — note: `SponsorService` currently sends `X-ROLE: ORG_ADMIN` header as a transitional measure; this will be migrated to use the JWT interceptor fully in a future update).

Key methods: `getChildren()`, `getChild(id)`, `getLedger(id)`, `getProgress(id)`, `commitSponsorship(payload)`

### 8.4 Auth Interceptor

`src/app/interceptors/auth.interceptor.ts` runs on every outbound HTTP request:

1. Skip if URL contains `/api/auth/login`, `/api/auth/refresh`, or `/api/public/`
2. Attach `Authorization: Bearer <token>` from `AuthService.getAccessToken()`
3. On 401 response: call `AuthService.refreshAccessToken()`, retry once with new token
4. On refresh failure: call `AuthService.clearSession()`, navigate to `/login`

### 8.5 Route Guard

`src/app/guards/admin-auth.guard.ts`

```typescript
// Pseudocode:
if (!auth.isAuthenticated()) → redirect to /login
if (user.role is JJT_ADMIN or ORG_ADMIN) → allow
else → redirect to /  (sponsor gets home page, not admin)
```

Applied to the `/admin` route only.

### 8.6 Session Restore (APP_INITIALIZER)

On every page load (including hard refresh):

```
main.ts → APP_INITIALIZER → AuthService.restoreSession()
  → reads localStorage['rt']
  → POST /api/auth/refresh
  → if OK: store new access token in memory, new RT in localStorage
  → GET /api/auth/me → populate currentUser$
  → Angular bootstraps with session restored
```

If there's no refresh token (first visit) or it's expired/revoked, `restoreSession()` silently clears state and Angular bootstraps with the user as anonymous.

---

## 9. Key Business Flows End-to-End

### Flow 1: Child Registration (Admin)

```
Admin opens /admin → selects "Create Child" tab
→ fills rollNumber, fullName, city, campusName, educationAmount
→ clicks Submit
→ Angular POSTs to POST /api/admin/children (with JWT Bearer)
→ Backend creates Child domain object + EducationSupportLedger atomically
→ Returns childId + ledgerId
→ Admin sees success message
```

After this, the child appears on the public `/children` page with status `AVAILABLE`.

### Flow 2: Recording Early Support (Admin)

```
Each month, admin records that the org pool covered a child's expenses:

Admin opens /admin → selects "Record Early Support" tab
→ enters childId, month (YYYY-MM), amount, currency
→ POSTs to POST /api/admin/early-support
→ Backend: loads ledger → calls appendEntry() (creates new LedgerEntry with coverageType=EARLY_SUPPORT)
→ Saves to DB
→ Admin can then add a progress update for that month
```

### Flow 3: Public Sponsorship Commitment

```
Public visitor opens /children
→ SponsorService.getChildren() → GET /api/org/children
→ Sees a list of children (only AVAILABLE ones highlighted)
→ Clicks "Sponsor This Child"
→ Navigates to /children/:childId → GET /api/org/children/:childId (detail)
→ Clicks "Sponsor Now"
→ Navigates to /children/:childId/sponsor

Fills in name, email, phone, commitment type (MONTHLY / YEARLY)
→ Clicks Submit
→ SponsorService.commitSponsorship() → POST /api/public/sponsorships
→ Backend:
    1. Validates child exists
    2. Checks no ACTIVE or PENDING sponsorship already exists (→ 409 if yes)
    3. Creates Sponsor record
    4. Creates Sponsorship with status=PENDING, startMonth=next month
→ Returns { childId, startMonth }
→ Navigates to /sponsor/confirmation
→ Shows thank-you page with bank transfer details

Child's availabilityStatus is now RESERVED (has PENDING sponsorship)
```

### Flow 4: Admin Activates a Sponsorship

```
Admin opens /admin → selects "Pending Sponsorships" tab
→ GET /api/admin/sponsorships?status=PENDING
→ Sees the list with sponsor name, email, phone
→ Confirms payment received offline (bank transfer)
→ Clicks "Activate"
→ POST /api/admin/sponsorships/{id}/activate
→ Status changes PENDING → ACTIVE

Child's availabilityStatus is now ALLOCATED
From this month, ledger entries should use coverageType=SPONSOR
```

### Flow 5: Sponsor Login and Dashboard

```
Admin has already created a user account via POST /api/admin/users/sponsor
linking the sponsor record to the login credentials

Sponsor navigates to /login
→ Enters email + password
→ POST /api/auth/login → receives JWT with sponsorId claim
→ AuthService stores access token (memory) + refresh token (localStorage)
→ GET /api/auth/me → currentUser$ emits { role: 'SPONSOR', sponsorId: 'uuid' }
→ Navigates to / (home page — sponsors don't see /admin)

Sponsor navigates to their children (not yet in the UI as a dedicated page,
but available via API: GET /api/sponsor/children)
→ JWT interceptor attaches Bearer token
→ Backend extracts sponsorId from JWT claims
→ Returns only children where sponsorships.sponsor_id matches
```

---

## 10. Project Layout

```
JJT/
├── src/
│   └── main/
│       ├── java/com/jjt/platform/
│       │   ├── JjtPlatformApplication.java   ← Spring Boot entry point
│       │   ├── api/                          ← Controllers, DTOs
│       │   │   ├── admin/                    ← AdminController, UserManagementController
│       │   │   ├── auth/                     ← AuthController, AuthService
│       │   │   ├── common/                   ← GlobalExceptionHandler, shared DTOs, DtoMapper
│       │   │   ├── org/                      ← OrgChildrenController
│       │   │   ├── publics/                  ← PublicSponsorshipController
│       │   │   └── sponsor/                  ← SponsorChildrenController
│       │   ├── application/
│       │   │   └── usecase/                  ← CreateChildUseCase, CommitSponsorship, etc.
│       │   ├── config/
│       │   │   └── security/                 ← SecurityConfig, JwtTokenProvider,
│       │   │                                    JwtAuthenticationFilter, JwtUserDetails,
│       │   │                                    JwtUserDetailsService, JwtProperties
│       │   ├── core/
│       │   │   └── domain/
│       │   │       ├── entity/               ← Child, Sponsor, Sponsorship, Ledger, etc.
│       │   │       ├── exceptions/           ← DomainException, LedgerInvariant, etc.
│       │   │       └── value/                ← Money, YearMonthValue
│       │   └── infrastructure/
│       │       └── persistence/
│       │           ├── entity/               ← JPA entities (ChildEntity, SponsorEntity, etc.)
│       │           ├── mapper/               ← Entity ↔ Domain conversions
│       │           ├── repository/           ← Spring Data JPA repositories
│       │           └── seed/                 ← AdminUserInitializer
│       └── resources/
│           ├── application.yml               ← Config (datasource, JWT, admin defaults)
│           └── db/migration/                 ← V1–V11 Flyway SQL migrations
│
├── src/test/
│   ├── java/com/jjt/platform/
│   │   ├── TestcontainersConfiguration.java  ← Shared PostgreSQL container + HTTP client config
│   │   ├── JjtPlatformApplicationTests.java  ← Context loads test
│   │   ├── auth/AuthIntegrationTest.java     ← 9 auth flow tests
│   │   └── api/PublicSponsorshipIntegrationTest.java ← 6 public API tests
│   └── resources/
│       └── application-test.yml             ← Test profile config
│
├── jjt-angular/                              ← Angular 18 SPA
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.component.ts             ← Root component (router outlet)
│   │   │   ├── app.routes.ts                ← All routes
│   │   │   ├── components/                  ← Shared UI components (header, footer, cards)
│   │   │   ├── guards/
│   │   │   │   └── admin-auth.guard.ts      ← Protects /admin route
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts      ← JWT injection + 401 refresh
│   │   │   ├── pages/
│   │   │   │   ├── home/                    ← Landing page
│   │   │   │   ├── login/                   ← Login form
│   │   │   │   ├── one-child-at-a-time/     ← Children listing
│   │   │   │   ├── child-detail/            ← Child profile
│   │   │   │   ├── sponsor-commit/          ← Sponsor form
│   │   │   │   ├── sponsor-confirmation/    ← Thank-you page
│   │   │   │   └── admin/                   ← Admin dashboard
│   │   │   └── services/
│   │   │       ├── auth.service.ts          ← JWT auth state + token management
│   │   │       └── sponsor.service.ts       ← API calls for children/ledger/public
│   │   ├── environments/
│   │   │   ├── environment.ts               ← Dev: http://localhost:8080
│   │   │   └── environment.prod.ts          ← Prod: Heroku URL
│   │   └── main.ts                          ← Bootstrap + APP_INITIALIZER
│   └── package.json
│
├── pom.xml                                  ← Maven BOM (Spring Boot 3.2.3)
└── docs/                                    ← This document and other specs
```

---

## 11. Local Development Setup

### Prerequisites

| Tool | Required version | Install |
|------|-----------------|---------|
| Java | 17 | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| Node.js | 18+ | https://nodejs.org |
| Docker | any recent | https://docker.com |

### Step 1: Start PostgreSQL

```bash
docker run --name jjt-postgres \
  -e POSTGRES_DB=jjt \
  -e POSTGRES_USER=jjt \
  -e POSTGRES_PASSWORD=jjt \
  -p 5432:5432 \
  -d postgres:16-alpine
```

### Step 2: Start the Backend

```bash
cd /path/to/JJT

JAVA_HOME=/path/to/jdk17 \
mvn spring-boot:run
```

Flyway runs automatically on startup. The first run applies all 11 migrations and creates the default admin user (`admin@jjt.org` / `Admin@JJT2024!`).

Verify: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

### Step 3: Start the Angular Frontend

```bash
cd jjt-angular
npm install
npm start       # ng serve → http://localhost:4200
```

### Step 4: Login

Navigate to `http://localhost:4200/login` and use `admin@jjt.org` / `Admin@JJT2024!`.

### Environment Variables (local defaults in application.yml)

| Variable | Local default |
|----------|--------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/jjt` |
| `SPRING_DATASOURCE_USERNAME` | `jjt` |
| `SPRING_DATASOURCE_PASSWORD` | `jjt` |
| `JWT_SECRET` | `dev-only-secret-key-minimum-256-bits-long-must-change-in-production-now` |
| `JWT_ACCESS_EXPIRY_MS` | `900000` (15 min) |
| `JWT_REFRESH_EXPIRY_MS` | `604800000` (7 days) |
| `ADMIN_EMAIL` | `admin@jjt.org` |
| `ADMIN_PASSWORD` | `Admin@JJT2024!` |

---

## 12. Test Suite

### Running Tests

```bash
JAVA_HOME=/path/to/jdk17 mvn test
```

Tests require **Docker** — Testcontainers spins up a real PostgreSQL 16 container automatically. No mocking of the database. No H2.

### Test Classes

| Class | Tests | What it covers |
|-------|-------|---------------|
| `JjtPlatformApplicationTests` | 1 | Spring context starts, all 11 Flyway migrations apply successfully |
| `AuthIntegrationTest` | 9 | Login, wrong credentials, unknown email, `/me` with valid/no/invalid token, token refresh, refresh with bad token, RBAC cross-role rejection |
| `PublicSponsorshipIntegrationTest` | 6 | Valid submission, duplicate pending → 409, missing email → 400, malformed email → 400, missing name → 400, non-existent child → 400 |

**Total: 16 tests, all green.**

### Testing Architecture Decisions

- **Testcontainers with `@ServiceConnection`**: Spring Boot 3.2's idiomatic approach. The `@ServiceConnection` annotation on a `PostgreSQLContainer` bean auto-configures `spring.datasource.*` to point at the container. Zero manual property wiring.

- **Shared Spring context**: All three test classes import `TestcontainersConfiguration`. Spring Test's context cache means one PostgreSQL container and one Spring context serve all tests in a JVM run — fast startup.

- **`JdkClientHttpRequestFactory`**: Overrides the default HTTP client in `TestRestTemplate`. Apache HttpClient 4.x (which arrives transitively via the Cloud SQL socket factory) has a bug where it throws `IOException` on 401 POST responses due to auth-retry-in-streaming-mode. Java 11's `HttpClient` has no such behaviour.

- **`application-test.yml`**: Test profile suppresses most logging noise and explicitly sets the Flyway and driver config, preventing the main app's Cloud SQL socket factory from activating.

---

## 13. Deployment

### Backend (Heroku)

The backend is a standard Spring Boot JAR deployed to Heroku.

**Procfile** (root of project):
```
web: java -Dserver.port=$PORT -jar target/jjt-platform-0.0.1-SNAPSHOT.jar
```

**Build and deploy:**
```bash
# Build
JAVA_HOME=/path/to/jdk17 mvn clean package -DskipTests

# Deploy (Heroku Git or GitHub integration)
git push heroku main
```

Heroku auto-detects the Procfile and runs it. The `PORT` environment variable is set by Heroku automatically.

### Frontend

The Angular app is built and served from a static hosting provider at `sponsorone.app`.

```bash
cd jjt-angular
npm run build        # ng build --configuration=production
# Deploy dist/jjt-angular/ to your hosting
```

### Required Production Environment Variables

Set these on the Heroku dyno before going live. Missing or default values are security risks.

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL connection string (Heroku provides this via `DATABASE_URL` add-on — you may need to adapt the format) |
| `SPRING_DATASOURCE_USERNAME` | Yes | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | DB password |
| `JWT_SECRET` | **Critical** | Must be at least 64 characters of random data. Use: `openssl rand -hex 36` |
| `JWT_ACCESS_EXPIRY_MS` | Optional | Default 900000 (15 min). Adjust as needed. |
| `JWT_REFRESH_EXPIRY_MS` | Optional | Default 604800000 (7 days). |
| `ADMIN_EMAIL` | Yes | Email for the default admin account |
| `ADMIN_PASSWORD` | **Critical** | Strong password for the default admin. Change via API after first login. |
| `PORT` | Auto | Set by Heroku. Do not set manually. |

**Setting config vars via Heroku CLI:**
```bash
heroku config:set JWT_SECRET=$(openssl rand -hex 36) --app jjt-platform
heroku config:set ADMIN_EMAIL=youremail@org.com --app jjt-platform
heroku config:set ADMIN_PASSWORD="$(openssl rand -base64 18)" --app jjt-platform
```

### CORS Configuration

The backend allows cross-origin requests from these origins only (configured in `SecurityConfig`):
- `http://localhost:4200` (local Angular dev)
- `https://sponsorone.app`
- `https://www.sponsorone.app`

To add a new origin, update `SecurityConfig.corsConfigurationSource()` and redeploy.

---

## 14. Configuration Reference

### `src/main/resources/application.yml` (annotated)

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/jjt}
    username: ${SPRING_DATASOURCE_USERNAME:jjt}
    password: ${SPRING_DATASOURCE_PASSWORD:jjt}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: none              # Flyway manages the schema; Hibernate never touches it
    open-in-view: false           # No lazy-loading outside transactions; prevents N+1 bugs
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: ${PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health           # Only /actuator/health is public; nothing else exposed

jwt:
  secret: ${JWT_SECRET:dev-only-secret-key-minimum-256-bits-long-must-change-in-production-now}
  access-token-expiry-ms: ${JWT_ACCESS_EXPIRY_MS:900000}    # 15 minutes
  refresh-token-expiry-ms: ${JWT_REFRESH_EXPIRY_MS:604800000} # 7 days

admin:
  default-email: ${ADMIN_EMAIL:admin@jjt.org}
  default-password: ${ADMIN_PASSWORD:Admin@JJT2024!}
```

---

## 15. Architectural Decisions

### Why modular monolith and not microservices?
Single deployment target (Heroku), single team, Phase-1 scope. Microservices would add network latency, distributed transactions, and operational complexity with zero benefit at this scale. The package boundaries (`api`, `application`, `core`, `infrastructure`) make future extraction straightforward when the need arises.

### Why append-only ledger?
Auditability is non-negotiable for a charitable trust. If an entry was wrong, the corrective action must itself be recorded. An edit in-place would destroy the audit trail. This is documented in `docs/adr/0002-append-only-ledger.md`.

### Why pure domain model with no JPA?
The `core/domain` package has zero framework dependencies. This means:
- Business rules can be unit-tested in milliseconds without a database or Spring context
- The domain model can be reasoned about independently of persistence concerns
- JPA can be swapped for something else without touching business logic

The cost is the mapper boilerplate between JPA entities and domain objects. That cost is worth it.

### Why JWT instead of server-side sessions?
Stateless auth scales horizontally (multiple Heroku dynos without a shared session store) and fits the Angular SPA pattern where every request carries its own credential.

### Why BCrypt cost factor 12?
BCrypt 12 takes approximately 300ms to verify on a single core. This makes offline brute-force attacks impractical while staying under 1 second for a login request. Cost 10 (the default) would be too fast; cost 14 would make login noticeably slow for users.

### Why store refresh tokens hashed?
If the database is breached, hashed refresh tokens cannot be used directly to impersonate users. The raw UUID is only ever in memory or in the browser's `localStorage` — both of which require a live, exploited session to access.

### Why are `childId` and `ledgerId` required in `POST /api/admin/children`?
Admin data is often imported in bulk from spreadsheets. Requiring the admin to provide stable UUIDs makes the import idempotent — re-running the same import does not create duplicates. The use case auto-generates UUIDs only if the caller passes null, but the API DTO enforces them with `@NotNull`.

---

*This document covers the complete state of the JJT Platform as of the `feat/firebase-auth-jjt` branch (M1–M5 complete). For questions, read the code — it is the authoritative source of truth.*
