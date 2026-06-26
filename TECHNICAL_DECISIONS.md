# JJT Platform — Technical Decisions

Last updated: 2026-06-26

---

## TD-001 — Authentication: Spring Security + JWT (not Firebase)

**Date:** 2026-06-26  
**Status:** APPROVED  
**Decided by:** Project Owner

**Decision:** Replace Firebase Authentication with Spring Security + JWT.

**Rationale:**
- Full ownership of auth logic — no third-party dependency
- Portable to any frontend (Angular, React, mobile)
- JWT is a well-understood, widely supported standard
- No Firebase project required for local development or new environments
- Avoids vendor lock-in

**Trade-offs accepted:**
- Must implement forgot-password and email verification ourselves (Phase 2)
- JWT tokens cannot be invalidated before expiry — mitigated by short access token lifetime (15 min) + DB-backed refresh token revocation

**Implementation:**
- Access tokens: HS256, 15 minute expiry
- Refresh tokens: 7 day expiry, hashed and stored in `refresh_tokens` table with `revoked_at` column
- BCrypt password hashing with cost factor 12
- `sponsor_id` embedded in JWT claims — avoids extra DB lookup per request

**Rejected alternatives:**
- Firebase Auth: vendor lock-in, requires internet for local dev, credentials management overhead
- Session-based auth: not suitable for stateless REST API consumed by multiple clients
- OAuth2/OIDC (self-hosted): too complex for Phase 1, can be added later

---

## TD-002 — Architecture: Backend First

**Date:** 2026-06-26  
**Status:** APPROVED  
**Decided by:** Project Owner

**Decision:** Deliver a complete, production-ready backend before investing in the Angular frontend.

**Rationale:**
- Backend stability enables any frontend to integrate without rework
- Avoids rebuilding APIs to fit frontend assumptions
- Backend can be validated and tested independently via API calls
- Frontend can evolve without blocking backend delivery

**Impact:**
- Angular Firebase auth removed
- Frontend development paused until backend is stable
- All backend APIs designed for general consumption, not Angular-specific

---

## TD-003 — Domain Model: Immutable Entities

**Date:** Pre-2026 (existing decision)  
**Status:** ACTIVE

**Decision:** Domain entities are immutable Java records/final classes. State transitions return new instances.

**Rationale:**
- Prevents accidental mutation of domain state
- Forces explicit state transitions through domain methods
- Append-only ledger invariant is enforced structurally

**Example:** `Sponsorship.withStatus(newStatus)` returns a new `Sponsorship` — it does not mutate the existing object.

**Constraint:** All status transitions must go through domain methods, not JPA entity setters. `entity.setStatus()` is banned in application/service code.

---

## TD-004 — Database Migrations: Flyway Only

**Date:** Pre-2026 (existing decision)  
**Status:** ACTIVE

**Decision:** All schema changes go through Flyway versioned migrations. Hibernate `ddl-auto` is set to `none`.

**Rationale:**
- Schema history is auditable and version-controlled
- Migrations run in the same order everywhere (local, CI, production)
- Prevents Hibernate from silently altering the production schema

**Convention:** 
- `V{n}__{description}.sql` for schema changes
- Never modify a migration after it has been applied to any environment
- If a migration was applied and needs correction, create a new migration

---

## TD-005 — Error Handling: Global Exception Handler

**Date:** 2026-06-26  
**Status:** ACTIVE

**Decision:** All exception handling is centralised in `GlobalExceptionHandler` (`@RestControllerAdvice`). No per-controller `@ExceptionHandler` methods.

**Response envelope:**
```json
{ "code": "ERROR_CODE", "message": "Human-readable message" }
```

**Mapping:**
| Exception | HTTP Status | Code |
|---|---|---|
| `UnauthorizedException` | 401 | `UNAUTHORIZED` |
| `ForbiddenException` | 403 | `FORBIDDEN` |
| `LedgerInvariantViolationException` | 409 | `LEDGER_CONFLICT` |
| `SponsorshipInvariantViolationException` | 409 | `SPONSORSHIP_CONFLICT` |
| `DomainException` | 400 | `DOMAIN_ERROR` |
| `IllegalArgumentException` | 400 | `VALIDATION_ERROR` |
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `DataIntegrityViolationException` | 409 | `CONFLICT` |
| `Exception` (catch-all) | 500 | `INTERNAL_ERROR` |

---

## TD-006 — API Versioning: URL Prefix

**Date:** 2026-06-26 (planned)  
**Status:** PLANNED (Milestone 6)

**Decision:** Version the API using URL prefix `/api/v1/`.

**Rationale:**
- Visible in logs and reverse proxy rules without header inspection
- Simple to route in load balancers and API gateways
- Industry standard for REST APIs

**Rejected:** Header-based versioning (`Accept: application/vnd.jjt.v1+json`) — invisible in logs, harder to test with curl/Postman.

---

## TD-007 — Build: Maven Wrapper (mvnw)

**Date:** 2026-06-26  
**Status:** ACTIVE

**Decision:** Use `./mvnw` (Maven Wrapper) for all build commands. Do not rely on system Maven version.

**Rationale:**
- Pins Maven version in the repository
- Works without Maven installed locally
- CI/CD and local environments use identical Maven version

**Java requirement:** Java 17. Set `JAVA_HOME` to JDK 17 or use SDKMAN.

---

## TD-008 — Test Environment: Testcontainers (not H2)

**Date:** 2026-06-26 (planned)  
**Status:** PLANNED (Milestone 5)

**Decision:** Integration tests use Testcontainers with a real PostgreSQL 16 container. H2 in-memory database is not used.

**Rationale:**
- H2 does not support all PostgreSQL features (enums, advisory locks, ON CONFLICT)
- Testcontainers ensures tests run against the same database engine as production
- Eliminates the class of bug where tests pass on H2 but fail on PostgreSQL

**Trade-off:** 30–60 seconds added to CI build time. Accepted.
