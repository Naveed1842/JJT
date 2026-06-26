# Project Purpose & Current State (as-is)

## Purpose (Phase-1)
JJT is an **Early Education Support Platform** focused on one core problem: **a child’s education should not pause because sponsorship is delayed**.

Phase‑1 is intentionally “small and strict”:
- **Child registration is independent of sponsorship**
- **Education support is ledger-first and append-only**
- **Workflows are manual and human-controlled**
- **Sponsors have read-only visibility (limited to assigned children)**

For the product intent and guardrails, see `README.md` and `docs/phase-1-guardrails.md`.

---

## Repository layout
- **Backend**: Spring Boot (Java 17), Maven, modular monolith under `src/main/java/com/jjt/platform`
- **DB**: PostgreSQL + Flyway migrations under `src/main/resources/db/migration`
- **Frontend**: Angular 18 app under `jjt-angular/`

---

## Backend (Spring Boot) – what exists today

### Architecture shape
Code is structured along the documented layering:
- **API** (`com.jjt.platform.api.*`): controllers + request/response DTOs
- **Application** (`com.jjt.platform.application.*`): use cases / orchestration (transactions)
- **Core domain** (`com.jjt.platform.core.*`): domain entities, invariants, value objects
- **Infrastructure** (`com.jjt.platform.infrastructure.*`): persistence (JPA entities/repos), mappers, seed data

### Persistence & migrations
Flyway is enabled by default (see `src/main/resources/application.yml`). The initial schema includes:
- `children`
- `education_support_ledgers`
- `ledger_entries` (unique per ledger+month)
- `sponsors`
- `sponsorships`
- `progress_updates`

Additional migrations evolve sponsorship constraints/status lifecycle and seed Postgres test data.

### Auth & roles (implemented)
The backend uses a **custom servlet filter** (`SimpleSecurityFilter`) and supports three modes:
- **LEGACY**: `X-ROLE` header (and `X-SPONSOR-ID`/`X-ORG-ID` as needed)
- **DUAL** (default): Bearer token first, fallback to legacy headers
- **FIREBASE**: Bearer token required

Public endpoints are those under **`/api/public/**`** (bypass auth in the filter).

Roles implemented (`Role` enum):
- `JJT_ADMIN`
- `ORG_ADMIN`
- `SPONSOR`

### HTTP API surface (implemented in code)
The controllers currently present these route groups:

- **Admin** (`/api/admin`)
  - Create child: `POST /children`
  - Create sponsor: `POST /sponsors`
  - Record early support ledger entry: `POST /early-support`
  - Add monthly progress update: `POST /children/{childId}/progress`
  - Commit sponsorship: `POST /sponsorships`
  - List sponsorships by status: `GET /sponsorships?status=PENDING|ACTIVE|...`
  - Activate/expire sponsorship: `POST /sponsorships/{id}/activate`, `POST /sponsorships/{id}/expire`
  - Sponsorships for a child: `GET /children/{childId}/sponsorships` and `GET /children/{childId}/sponsorships/active`

- **Org** (`/api/org`)
  - List children: `GET /children`
  - Child detail: `GET /children/{childId}`
  - Child ledger: `GET /children/{childId}/ledger`
  - Child progress: `GET /children/{childId}/progress`

- **Sponsor** (`/api/sponsor`) (read-only; access is constrained by sponsorId)
  - My sponsored children: `GET /children`
  - Sponsored child detail: `GET /children/{childId}`
  - Sponsored child ledger: `GET /children/{childId}/ledger`
  - Sponsored child progress: `GET /children/{childId}/progress`

- **Public** (`/api/public`)
  - Public sponsorship commitment: `POST /sponsorships`

Notes:
- The `docs/api/*` contracts exist as Phase‑1 targets; some endpoints match closely, but **the code is the source of truth** for “current state”.
- Actuator health is exposed (see `application.yml`): `GET /actuator/health`.

---

## Frontend (Angular) – what exists today
- Angular 18 project with pages for home/children/child-detail/admin/sponsor flows.
- A global HTTP interceptor (`roleInterceptor`) supports:
  - Bearer token via Firebase Auth (preferred)
  - Legacy `X-ROLE`/`X-SPONSOR-ID` fallback (migration support)
- `environment.ts` currently points `apiBaseUrl` to a deployed Heroku backend (and includes a hard-coded `sponsorId` for legacy mode/testing).

Practical status (from code + README):
- Landing/marketing flows appear implemented.
- Admin and child listing flows exist and call backend routes.
- Some UI areas may still be “in progress” depending on which routes/pages you rely on; verify by running the app and checking navigation paths.

---

## Deployment & operations (documented)
There are docs for Phase‑1 ops (low-cost, simple):
- Backend deployment guidance exists (Heroku + Postgres) in the root `README.md`.
- Additional operational notes are in `docs/deployment.md`.

---

## Current repo state (git)
At the time of writing this doc (2026‑04‑23), `git status` shows **untracked files**:
- `docs/excelData.svg`
- `src/main/java/com/jjt/platform/api/admin/dto/ImportChildrenResponse.java`

---

## What to do next (recommended)
- Align `docs/api/*` contracts with the **actual** controller routes/DTOs (or update code to match contracts).
- Decide whether Phase‑1 auth should stay **DUAL** (migration-friendly) or move to **FIREBASE** only.
- Remove/replace hard-coded IDs in Angular environments and document local dev environment setup.

