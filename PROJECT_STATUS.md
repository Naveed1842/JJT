# JJT Platform — Project Status

Last updated: 2026-06-27

## Build Status

| Component | Status |
|---|---|
| Backend (Java/Spring Boot) | CLEAN (`./mvnw compile` — no errors) |
| Frontend (Angular 18) | CLEAN (`ng build --configuration=production` — no errors) |
| Tests | Pre-existing failure: H2/Flyway checksum mismatch in `contextLoads`. Does not affect PostgreSQL runtime. |
| Flyway | V1–V11 applied. Schema version: 11. |

---

## Architecture Decision: Firebase → Spring Security + JWT

**Decision date:** 2026-06-27 | **Status:** IMPLEMENTED (Milestone 1 complete)

Firebase Authentication has been fully removed and replaced with:
- Spring Security `SecurityFilterChain` (stateless, JWT-based)
- JJWT 0.12.x (HS512, 15-minute access tokens)
- Hashed refresh tokens in DB (7-day, revocable)
- BCrypt cost 12 for password hashing
- `@PreAuthorize` RBAC on all endpoints
- `users` + `refresh_tokens` tables (V10 + V11 migrations)

---

## Backend Auth Roadmap — Status

| Milestone | Description | Status |
|---|---|---|
| **M1** | Auth Foundation (Spring Security + JWT + RBAC) | ✅ **COMPLETE** |
| **M2** | Sponsor User Management (create sponsor users, link to sponsor records) | ✅ **COMPLETE** |
| **M3** | Org Admin User Management | ✅ **COMPLETE** |
| M4 | Import/Bulk Operations | PENDING |
| M5 | Test Infrastructure (Testcontainers PostgreSQL) | PENDING |
| M6 | Deployment Hardening (secrets, prod config) | PENDING |
| M7 | Angular Integration (replace Firebase SDK with JWT calls) | PENDING |

---

## Milestone 1 — Auth Foundation (COMPLETE)

### What was built

| Component | Files |
|---|---|
| Flyway V10 — `users` table | `V10__create_users_table.sql` |
| Flyway V11 — `refresh_tokens` table | `V11__create_refresh_tokens_table.sql` |
| `UserEntity` + `RefreshTokenEntity` | `infrastructure/persistence/entity/` |
| `UserRepository` + `RefreshTokenRepository` | `infrastructure/persistence/repository/` |
| `JwtProperties` (config binding) | `config/security/JwtProperties.java` |
| `JwtUserDetails` (UserDetails impl) | `config/security/JwtUserDetails.java` |
| `JwtTokenProvider` (sign/validate) | `config/security/JwtTokenProvider.java` |
| `JwtUserDetailsService` | `config/security/JwtUserDetailsService.java` |
| `JwtAuthenticationFilter` | `config/security/JwtAuthenticationFilter.java` |
| `SecurityConfig` (new — replaces old) | `config/security/SecurityConfig.java` |
| `AdminUserInitializer` (seed admin) | `infrastructure/persistence/seed/` |
| Auth DTOs | `api/auth/dto/` (5 records) |
| `AuthService` | `api/auth/AuthService.java` |
| `AuthController` | `api/auth/AuthController.java` |

### What was deleted

| File | Reason |
|---|---|
| `FirebaseTokenVerifier.java` | Firebase removed |
| `FirebaseAuthProperties.java` | Firebase removed |
| `AuthProperties.java` | Firebase removed |
| `AuthMode.java` | Firebase removed |
| `SimpleSecurityFilter.java` | Replaced by Spring Security |
| `AccessGuard.java` | Replaced by `@PreAuthorize` |
| `SecurityContext.java` (custom) | Replaced by Spring's `SecurityContext` |
| `SecurityContextHolder.java` (custom) | Replaced by Spring's `SecurityContextHolder` |

### Auth API endpoints

| Method | Path | Auth required | Description |
|---|---|---|---|
| POST | `/api/auth/login` | No | Email + password → access + refresh tokens |
| POST | `/api/auth/refresh` | No | Refresh token → new token pair |
| POST | `/api/auth/logout` | Yes | Revoke refresh token |
| GET | `/api/auth/me` | Yes | Current user profile |
| PUT | `/api/auth/change-password` | Yes | Update password |

### Test results (verified locally)

| Test | Result |
|---|---|
| `GET /actuator/health` | `{"status":"UP"}` |
| `GET /api/admin/sponsorships` (no token) | `HTTP 401` |
| `GET /api/admin/sponsorships` (valid JWT) | `HTTP 200` |
| `GET /api/auth/me` | `{"id":"...","email":"admin@jjt.org","role":"JJT_ADMIN"}` |
| Refresh token rotation | New `Bearer` token returned |
| Unknown path | `HTTP 404` `{"code":"NOT_FOUND"}` |

---

## Earlier Audit Fixes (Completed)

| Issue | Description |
|---|---|
| ISSUE-01 | Fixed `recordEarlySupport()` URL in admin component |
| ISSUE-02 | Added explicit `("childId")` to `@PathVariable` on `addProgress` |
| ISSUE-04 | Documented all required Heroku config vars |
| ISSUE-05 | V8 migration — removed V5 test seed data |
| ISSUE-07 | Removed SPONSOR role from OrgChildrenController access guards |
| ISSUE-08 | Removed SPONSOR role from commitSponsorship access guard |
| ISSUE-09 | Replaced full-table `sponsorshipRepo.findAll()` with scoped queries |
| ISSUE-10 | Fixed N+1 on `listChildren()` — 2 bulk queries |
| ISSUE-11 | `GlobalExceptionHandler` — structured JSON error envelope |
| ISSUE-12 | Input validation with `@Valid`, `@NotBlank`, `@Email` on all DTOs |
| ISSUE-13 | Fixed status transitions to go through domain `withStatus()` |
| ISSUE-14 | Deleted `ViewChildLedgerUseCase` (dead code) |
| ISSUE-15 | Removed dead `getPublicChildren()` / `getPublicChild()` from Angular service |
| ISSUE-16 | Deleted orphaned `ChildrenComponent` and `SponsorComponent` pages |
| ISSUE-17 | Added `coverageType` field end-to-end (enum → DB → API → Angular) |
| ISSUE-18 | Removed hardcoded `age = 8`; age hidden when unavailable |

---

## Deployment Checklist (Milestone 1)

Before deploying to production:

1. Set `JWT_SECRET` env var — minimum 32 characters, random, never commit to git
2. Optionally override `JWT_ACCESS_EXPIRY_MS` (default: 900000 = 15 min)
3. Optionally override `JWT_REFRESH_EXPIRY_MS` (default: 604800000 = 7 days)
4. Set `ADMIN_EMAIL` and `ADMIN_PASSWORD` env vars for the bootstrap admin account
5. **Immediately change the admin password** after first deploy via `PUT /api/auth/change-password`
6. V10 and V11 Flyway migrations run automatically on startup
7. Remove `firebase-admin` from any Heroku build cache / config vars — no longer needed

---

## Milestone 2 — Sponsor User Management (COMPLETE)

### Endpoints added

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/admin/users/sponsor` | JJT_ADMIN | Create login account for an existing sponsor record |
| GET | `/api/admin/users` | JJT_ADMIN | List all platform users |
| PUT | `/api/admin/users/{id}/activate` | JJT_ADMIN | Re-enable a deactivated account |
| PUT | `/api/admin/users/{id}/deactivate` | JJT_ADMIN | Disable an account without deleting it |

### RBAC matrix (verified)

| Caller | Target | Result |
|---|---|---|
| Unauthenticated | `/api/admin/*` | 401 |
| SPONSOR | `/api/admin/*` | 403 |
| JJT_ADMIN | `/api/admin/*` | 200 |
| SPONSOR | `/api/sponsor/*` | 200 |
| JJT_ADMIN | `/api/sponsor/*` | 403 |
| JJT_ADMIN | `/api/admin/users` | 200 |
| Deactivated account | `/api/auth/login` | 401 UNAUTHORIZED |

### Bug fixed in this milestone

`AccessDeniedException` thrown by `@PreAuthorize` is handled by Spring MVC's exception resolver chain before it reaches `ExceptionTranslationFilter`. Added explicit `@ExceptionHandler(AccessDeniedException.class)` in `GlobalExceptionHandler` to return `403` consistently. Also broadened `BadCredentialsException` handler to `AuthenticationException` to cover `DisabledException`, `LockedException`, and all other auth failure subtypes.

---

## Milestone 3 — Org Admin User Management (COMPLETE)

Single endpoint added to the existing `UserManagementController`:

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/admin/users/org` | JJT_ADMIN | Create an ORG_ADMIN login account (optional orgId field) |

**RBAC verified:**

| Caller | Target | Result |
|---|---|---|
| ORG_ADMIN | `/api/org/children` | 200 |
| ORG_ADMIN | `/api/admin/sponsorships` | 200 |
| ORG_ADMIN | `/api/sponsor/children` | 403 |
| ORG_ADMIN | `/api/admin/users` | 403 (user management is JJT_ADMIN only) |

`orgId` is stored as a plain UUID — no FK to an organizations table (none exists yet). Duplicate email returns `400 DOMAIN_ERROR` with a clear message.

---

## Known Pending Items

| Item | Priority |
|---|---|
| M5: Testcontainers PostgreSQL (replace broken H2 tests) | MEDIUM |
| M7: Angular — replace Firebase SDK auth with JWT calls | HIGH — needed for frontend login |
| Scheduled job to purge expired refresh tokens | LOW |
