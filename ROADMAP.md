# JJT Platform — Backend Roadmap

Last updated: 2026-06-26

## Strategy

Backend First. Deliver a complete, production-ready backend API before investing in frontend implementation.

Authentication is handled entirely by the backend using Spring Security + JWT. The frontend is not the current priority.

---

## Milestone Status

| # | Milestone | Status | Priority | Effort |
|---|---|---|---|---|
| 1 | Auth Foundation (Spring Security + JWT) | NOT STARTED | CRITICAL | L |
| 2 | Admin API Completion | NOT STARTED | HIGH | M |
| 3 | CI/CD Pipeline + Docker | NOT STARTED | HIGH | S |
| 4 | Data Layer Hardening | NOT STARTED | HIGH | S |
| 5 | Testing Infrastructure | NOT STARTED | HIGH | L |
| 6 | API Polish + Documentation | NOT STARTED | MEDIUM | M |
| 7 | Production Readiness Validation | NOT STARTED | CRITICAL | M |

---

## Milestone 1 — Auth Foundation

**Status:** NOT STARTED  
**Priority:** CRITICAL  
**Effort:** L (1–2 weeks)

**Objective:** Replace X-ROLE header bypass with Spring Security + JWT. Real user identity with role-based access control.

**Removes:**
- `firebase-admin` dependency
- `FirebaseTokenVerifier`, `FirebaseAuthConfig`
- `AuthMode`, `AuthProperties`
- `SimpleSecurityFilter`
- Custom `SecurityContextHolder`, `AccessGuard`
- All `X-ROLE` / `X-SPONSOR-ID` / `X-ORG-ID` header logic

**Adds:**
- `spring-boot-starter-security` + `jjwt`
- V10 migration: `users` table
- V11 migration: `refresh_tokens` table (DB-backed revocation)
- `User` domain entity + `UserEntity` JPA entity
- `JwtTokenProvider` — generate, validate, extract claims
- `JwtAuthenticationFilter` — reads Bearer token, populates SecurityContext
- `UserDetailsService` — loads user by email
- `SecurityFilterChain` — stateless config, CORS, public/protected routes
- Auth endpoints: login, refresh, logout, me, change-password
- Security headers (CSP, X-Frame-Options, X-Content-Type-Options)
- `@PreAuthorize` on all protected endpoints

**Auth Endpoints:**
```
POST /api/auth/login          → { accessToken, refreshToken, expiresIn }
POST /api/auth/refresh        → { accessToken, expiresIn }
POST /api/auth/logout         → 204 No Content
GET  /api/auth/me             → { id, email, role, sponsorId, orgId }
PUT  /api/auth/change-password → 204 No Content
```

**Token Design:**
- Access token: 15 minute expiry, signed HS256
- Refresh token: 7 day expiry, stored hashed in DB with revocation support
- Sponsor's `sponsor_id` embedded in JWT claim — no extra DB lookup per request

**Definition of Done:**
- Login returns signed JWT
- Protected endpoints return 401 without token, 403 with wrong role
- Refresh token rotation works
- Logout revokes refresh token
- Firebase dependency fully removed, codebase compiles clean

---

## Milestone 2 — Admin API Completion

**Status:** NOT STARTED  
**Priority:** HIGH  
**Effort:** M (3–5 days)  
**Depends on:** Milestone 1

**Objective:** Complete the admin API. Currently write-only with no read, search, update, or bulk import.

**Missing Endpoints:**
```
GET  /api/admin/children                           → paginated + filterable
GET  /api/admin/children/{id}                      → single child detail
PUT  /api/admin/children/{id}                      → update child
GET  /api/admin/children/{id}/ledger               → admin ledger view
GET  /api/admin/children/{id}/progress             → admin progress view
POST /api/admin/children/import                    → bulk import
GET  /api/admin/sponsors                           → paginated + searchable
GET  /api/admin/sponsors/{id}                      → single sponsor + sponsorships
PUT  /api/admin/sponsors/{id}                      → update sponsor
GET  /api/admin/sponsors/{id}/sponsorships         → sponsor's commitments
GET  /api/admin/dashboard                          → summary stats
```

**Pagination Standard:**
```json
{
  "data": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

**Definition of Done:**
- All endpoints above implemented
- All list endpoints paginated
- Search/filter working on children (name, city, campus, roll number, status)
- Bulk import is transactional — partial failure rolls back the entire batch or returns per-record errors

---

## Milestone 3 — CI/CD Pipeline + Docker

**Status:** NOT STARTED  
**Priority:** HIGH  
**Effort:** S (1–2 days)  
**Depends on:** Milestone 1

**Objective:** Every commit builds automatically. Every push to main deploys to Heroku.

**Delivers:**
- `.github/workflows/ci.yml` — compile + test on every PR
- `.github/workflows/deploy.yml` — deploy to Heroku on main merge
- `Dockerfile` — multi-stage build, eclipse-temurin:17-jre-alpine runtime
- `docker-compose.yml` — local dev stack: app + PostgreSQL 16
- `.env.example` — all required env vars documented
- Structured JSON logging for log drain compatibility
- `spring.jpa.open-in-view=false` set (connection pool fix)

**Definition of Done:**
- Red CI blocks PR merge
- `docker-compose up` starts a working local environment with no manual steps
- Push to main deploys automatically

---

## Milestone 4 — Data Layer Hardening

**Status:** NOT STARTED  
**Priority:** HIGH  
**Effort:** S (1–2 days)  
**Depends on:** Milestones 1, 2

**Objective:** Production schema with proper indexes, audit timestamps, and soft delete.

**Migrations:**
- V12: Indexes on `children`, `sponsorships`, `ledger_entries`, `progress_updates`, `users`
- V13: `created_at`, `updated_at` on `children`, `ledger_entries`, `progress_updates`
- V14: `deleted_at` on `children` and `sponsors` for soft delete

**Definition of Done:**
- All common query columns indexed
- Soft delete implemented — no hard deletes on business records
- JPA auditing auto-populates timestamps
- `EXPLAIN ANALYZE` on list queries shows index scans

---

## Milestone 5 — Testing Infrastructure

**Status:** NOT STARTED  
**Priority:** HIGH  
**Effort:** L (1–2 weeks)  
**Depends on:** Milestones 1–4

**Objective:** Meaningful test coverage. Zero tests = zero deployment confidence.

**Scope:**
- Fix `contextLoads` — replace H2 with Testcontainers PostgreSQL
- Domain unit tests: `LedgerInvariant`, `SponsorshipLifecycle`
- Use case unit tests: all 5 use cases
- Repository integration tests (Testcontainers)
- Controller tests (`@WebMvcTest`): auth filter, role guards, validation
- Auth integration tests: full login → refresh → logout flow

**Definition of Done:**
- `mvn test` is green in CI
- Domain layer: 100% unit test coverage
- All controllers: at least one success test and one auth-failure test
- Security flows fully tested

---

## Milestone 6 — API Polish + Documentation

**Status:** NOT STARTED  
**Priority:** MEDIUM  
**Effort:** M (3–5 days)  
**Depends on:** Milestones 1–5

**Objective:** Consistent API, versioning strategy, OpenAPI docs, decision documentation.

**Delivers:**
- `springdoc-openapi` — Swagger UI at `/swagger-ui.html` (disabled in prod or protected)
- URL versioning: `/api/v1/` prefix
- `X-Request-ID` propagation for tracing
- `API_SPECIFICATION.md`
- `SECURITY_GUIDE.md` — JWT flow, role matrix, token lifecycle
- `TECHNICAL_DECISIONS.md` — architecture decisions with rationale

**Definition of Done:**
- All endpoints documented in Swagger with auth requirements
- Versioning applied consistently
- Three documentation files complete and accurate

---

## Milestone 7 — Production Readiness Validation

**Status:** NOT STARTED  
**Priority:** CRITICAL  
**Effort:** M (3–5 days)  
**Depends on:** All previous milestones

**Objective:** Final gate. Confirm the backend is secure, performant, and operable.

**Checklist:**
- Security audit: no accidentally public endpoints, CORS locked, headers present, BCrypt cost ≥ 12, JWT secrets ≥ 256 bits, refresh tokens hashed in DB
- Structured logging with request context (request_id, user_id, role, path, status, duration_ms)
- Heroku log drain configured
- Health check includes DB connectivity
- Load test: 50 concurrent users, p95 < 500ms, zero 5xx
- Fresh-database deploy verified
- Rollback procedure documented and tested
- `DEPLOYMENT_GUIDE.md` complete

**Definition of Done:**
- Security audit passes
- Load test passes
- Fresh deploy verified
- All documentation current

---

## Deferred

| Item | Reason |
|---|---|
| Frontend Angular auth integration | Backend first — frontend after backend is stable |
| Forgot Password / Reset Password | Requires email service integration — Phase 2 |
| Mobile app | Future consideration |
| Multi-tenancy (multiple orgs) | Not required for Phase 1 |
| Reporting / exports | Phase 2 |
| Pagination on sponsor endpoints | Included in Milestone 2 |
