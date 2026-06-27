# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Commands

### Backend (Spring Boot)

```bash
# Run the application (requires PostgreSQL on localhost:5432)
./mvnw spring-boot:run

# Run all tests (Docker required — Testcontainers spins up PostgreSQL 16)
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthIntegrationTest

# Build a deployable JAR (skipping tests)
./mvnw clean package -DskipTests

# Deploy to Heroku
git push heroku <branch>:main
```

**Java 17 is required.** If your default JVM is newer, prefix commands with `JAVA_HOME=/path/to/jdk17`.

### Local PostgreSQL (Docker)

```bash
# Start via Docker Compose (recommended)
docker compose -f docker-compose.postgres.yml up -d

# Or run directly
docker run --name jjt-postgres \
  -e POSTGRES_DB=jjt -e POSTGRES_USER=jjt -e POSTGRES_PASSWORD=jjt \
  -p 5432:5432 -d postgres:16-alpine
```

### Frontend (Angular)

```bash
cd jjt-angular
npm install
npm start          # ng serve → http://localhost:4200
npm run build      # production build to dist/jjt-angular/browser/
firebase deploy --only hosting   # deploy to Firebase Hosting
```

---

## Architecture

### Backend Package Structure

```
com.jjt.platform
├── api/                    HTTP layer only — no business logic in controllers
│   ├── admin/              JJT_ADMIN + ORG_ADMIN: write operations
│   ├── auth/               Login, refresh, logout, /me, change-password
│   ├── common/             GlobalExceptionHandler, shared DTOs, DtoMapper
│   ├── org/                JJT_ADMIN + ORG_ADMIN: read-only child/ledger/progress
│   ├── publics/            Unauthenticated: public sponsorship commitment
│   └── sponsor/            SPONSOR role: scoped read-only access
├── application/usecase/    Use cases — orchestrate domain + persistence, own transactions
├── config/security/        SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter,
│                           JwtUserDetails, JwtUserDetailsService, JwtProperties, Role
├── core/domain/            Pure Java — zero Spring, zero JPA
│   ├── entity/             Child, Sponsor, Sponsorship, EducationSupportLedger,
│   │                       LedgerEntry, ProgressUpdate, CommitmentType,
│   │                       CoverageType, SponsorshipStatus
│   ├── exceptions/         DomainException, LedgerInvariantViolationException,
│   │                       SponsorshipInvariantViolationException
│   └── value/              Money, YearMonthValue
└── infrastructure/persistence/
    ├── entity/             JPA entities — mapped separately from domain objects
    ├── mapper/             DomainMapper: JPA entity ↔ domain object conversions
    ├── repository/         Spring Data JPA repositories
    └── seed/               AdminUserInitializer (creates default admin on first boot)
```

### Key Design Constraints

- **Domain layer has zero framework dependencies.** `core/domain` can be unit-tested with plain Java.
- **JPA entities never enter application or domain code.** Mappers convert them at the infrastructure boundary before use cases run.
- **Immutable domain objects.** State transitions (e.g. `Sponsorship.withStatus()`) return new instances rather than mutating in place.
- **Append-only ledger.** `EducationSupportLedger.appendEntry()` is the only permitted write. No UPDATE or DELETE is ever issued against `ledger_entries`.
- **Derived availability status.** `AVAILABLE / RESERVED / ALLOCATED` is computed from `sponsorships` at query time — never stored.
- **Flyway owns the schema.** `hibernate.ddl-auto=none` always. New schema changes require a `V{n}__description.sql` migration in `src/main/resources/db/migration/`.
- **All exception handling is centralised** in `GlobalExceptionHandler`. No per-controller `@ExceptionHandler` methods.

### Authentication Flow

- Access tokens: HS512 JWT, 15-minute expiry, stored in Angular memory only.
- Refresh tokens: opaque UUID, 7-day expiry, stored in `localStorage` as `rt`, stored server-side as a SHA-256 hash in `refresh_tokens` table with a `revoked_at` column.
- Rotation: each `/api/auth/refresh` call revokes the old token and issues a new pair atomically.
- JWT claims include `sub` (userId), `email`, `role`, `sponsorId` (nullable), `orgId` (nullable).
- `SponsorChildrenController` extracts `sponsorId` from JWT claims to scope data access — never from request parameters.

### CORS

Allowed origins are hardcoded in `SecurityConfig.corsConfigurationSource()`:
- `http://localhost:4200`
- `https://sponsorone.app`
- `https://www.sponsorone.app`

Adding a new origin requires a code change and redeployment.

### Frontend Structure

```
jjt-angular/src/app/
├── components/             Shared UI components (header, footer, cards, tables)
├── guards/                 admin-auth.guard.ts — protects /admin route
├── interceptors/           auth.interceptor.ts — attaches Bearer token, handles 401 retry
├── pages/                  One folder per route
├── services/
│   ├── auth.service.ts     Central auth state (BehaviorSubject<CurrentUser | null>)
│   └── sponsor.service.ts  API calls for children, ledger, public commitment
└── app.routes.ts           All routes with lazy-loaded components
```

`AuthService.restoreSession()` is called by `APP_INITIALIZER` in `main.ts` on every page load to silently restore sessions from the stored refresh token.

### Configuration

Backend environment variables (with local defaults in `application.yml`):

| Variable | Default |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/jjt` |
| `SPRING_DATASOURCE_USERNAME` | `jjt` |
| `SPRING_DATASOURCE_PASSWORD` | `jjt` |
| `JWT_SECRET` | `dev-only-secret-key-...` (must be 64+ chars in production) |
| `JWT_ACCESS_EXPIRY_MS` | `900000` (15 min) |
| `JWT_REFRESH_EXPIRY_MS` | `604800000` (7 days) |
| `ADMIN_EMAIL` | `admin@jjt.org` |
| `ADMIN_PASSWORD` | `Admin@JJT2024!` |

Frontend environment: `jjt-angular/src/environments/environment.ts` (dev) and `environment.prod.ts` (prod) — both export `{ apiBaseUrl }`.

### Test Setup

- Tests require Docker. Testcontainers starts a real `postgres:16-alpine` container automatically via `@ServiceConnection` — no manual wiring.
- All test classes import `TestcontainersConfiguration` to share one Spring context and one PostgreSQL container across the full test run.
- `RestTemplateBuilder` in `TestcontainersConfiguration` overrides the default HTTP client to `JdkClientHttpRequestFactory`, avoiding an Apache HttpClient 4.x bug on 401 POST responses.
- Test profile config lives in `src/test/resources/application-test.yml`.

### Deployment

- Backend: Heroku, `git push heroku <branch>:main`. `system.properties` pins Java 17. `Procfile` runs `java -Dserver.port=$PORT -jar target/jjt-platform-0.0.1-SNAPSHOT.jar`.
- Frontend: Firebase Hosting. Build output is `dist/jjt-angular/browser/`. `firebase.json` rewrites all routes to `index.html` for Angular client-side routing.

### Postman Collections

Pre-built collections are in `postman/`. Use `JJT-dev.postman_environment.json` for local testing against `http://localhost:8080`.
