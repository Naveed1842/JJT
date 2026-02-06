# JJT Project Status (as of 2026-02-06)

This is a factual snapshot of what exists in the repo right now. No assumptions about alignment or future scope.

## Repo state
- Branch: `dev`
- Uncommitted changes present (see `git status --short` for full list).
- `bruno/` folder is deleted locally (was previously generated; now missing).
- `postman/` folder exists with collections/environments.

## Backend (Spring Boot)
Location: `src/main/java/com/jjt/platform`

### Core domain
Implemented classes:
- Entities: `Child`, `EducationSupportLedger`, `LedgerEntry`, `Sponsor`, `Sponsorship`, `ProgressUpdate`
- Value objects: `Money`, `YearMonthValue`
- Exceptions: `DomainException`, `LedgerInvariantViolationException`

### Application layer (use cases)
Implemented classes under `src/main/java/com/jjt/platform/application/usecase`:
- `CreateChildUseCase`
- `RecordEarlySupportUseCase`
- `CommitFutureSponsorshipUseCase`
- `AddMonthlyProgressUseCase`
- `ViewChildLedgerUseCase`
- `CreateSponsorUseCase`

### Persistence
Implemented under `src/main/java/com/jjt/platform/infrastructure/persistence`:
- JPA entities: `ChildEntity`, `EducationSupportLedgerEntity`, `LedgerEntryEntity`, `SponsorEntity`, `SponsorshipEntity`, `ProgressUpdateEntity`
- Repositories: `ChildJpaRepository`, `EducationSupportLedgerJpaRepository`, `LedgerEntryRepository`, `SponsorJpaRepository`, `SponsorshipJpaRepository`, `ProgressUpdateRepository`
- Mappers: `ChildMapper`, `EducationSupportLedgerMapper`, `LedgerEntryMapper`, `MoneyMapper`, `YearMonthMapper`, `SponsorMapper`, `SponsorshipMapper`, `ProgressUpdateMapper`
- Seed data: `TestDataInitializer` (dev profile only)

### API layer
Implemented under `src/main/java/com/jjt/platform/api`:
- Admin write endpoints (in `AdminController`):
  - `POST /api/admin/children`
  - `POST /api/admin/early-support`
  - `POST /api/admin/children/{childId}/progress`
  - `POST /api/admin/sponsorships`
  - `POST /api/admin/sponsors`
- Org read endpoints (in `OrgChildrenController`):
  - `GET /api/org/children`
  - `GET /api/org/children/{childId}`
  - `GET /api/org/children/{childId}/ledger`
  - `GET /api/org/children/{childId}/progress`
- Sponsor read endpoints (in `SponsorChildrenController`):
  - `GET /api/sponsor/children`
  - `GET /api/sponsor/children/{childId}`
  - `GET /api/sponsor/children/{childId}/ledger`
  - `GET /api/sponsor/children/{childId}/progress`

### Security (Phase-1 header-based)
- Filter: `SimpleSecurityFilter`
- Roles: `JJT_ADMIN`, `ORG_ADMIN`, `SPONSOR`
- Headers required:
  - `X-ROLE` (always)
  - `X-SPONSOR-ID` when `X-ROLE=SPONSOR`

### Dev seed data (deterministic)
`TestDataInitializer` (profile `dev`) creates:
- Children:
  - `CHILD-001` → `1f0464a8-7b42-3c1d-9fca-526bc4f21265`
  - `CHILD-002` → `3096dee8-0986-39e2-a381-cb0ad8af1860`
- Sponsor:
  - `SPONSOR-001` → `7ba67472-9df7-3bde-a2f5-fd2083a31f0a`
- Ledgers:
  - `LEDGER-001` → `bf5a746d-d64a-3c3d-8b2d-b2cab7ab203b`
  - `LEDGER-002` → `11659044-19c2-36a4-a729-1c9a2dad33f3`
- Ledger entries and progress updates for previous/current months
- Sponsorship created for CHILD-002 starting next month

## Frontend (Angular)
Location: `jjt-angular/`

### Routes
`jjt-angular/src/app/app.routes.ts`:
- `/` → Home
- `/children` → list
- `/children/:childId` → detail
- `/children/:childId/sponsor` → sponsorship commit
- `/sponsor/confirmation` → confirmation

### Services & interceptors
- `SponsorService` (reads children/ledger/progress via `/api/org/*`, commits sponsorship via `/api/admin/sponsorships`)
- `roleInterceptor` auto-injects headers, adds `X-SPONSOR-ID` when role is SPONSOR
- `environment.ts` uses:
  - `apiBaseUrl: http://localhost:8080`
  - `sponsorId: 7ba67472-9df7-3bde-a2f5-fd2083a31f0a`

### Components/pages
- Pages: `ChildrenComponent`, `ChildDetailComponent`, `SponsorCommitComponent`, `SponsorConfirmationComponent`, `HomeComponent`
- Presentational components: `ChildCardComponent`, `ChildSnapshotComponent`, `LedgerTableComponent`, `ProgressListComponent`

### Landing page
- `HomeComponent` contains static layout and a role switcher (stores role in localStorage).
- Tailwind is used for styling.

## Postman
Location: `postman/`
- Collections:
  - `JJT-Phase1.postman_collection.json`
  - `JJT-Platform-API.postman_collection.json`
- Environments:
  - `JJT-Phase1.postman_environment.json`
  - `JJT-dev.postman_environment.json`

Note: `JJT-Phase1.postman_environment.json` currently stores `SPONSOR-001` and `CHILD-001/002` as strings, while the backend uses UUIDs. Requests will fail unless updated to UUIDs listed above.

## Known issues observed
1) **403 for sponsor requests**
   - Cause: `X-ROLE=SPONSOR` requires `X-SPONSOR-ID` header. Missing header triggers `ForbiddenException` in `SimpleSecurityFilter`.

2) **Duplicate sponsorship (409/500)**
   - Cause: seed data already inserts a sponsorship for CHILD-002 starting next month. Re-posting the same sponsor/child/startMonth violates the unique constraint.

3) **PathVariable name error**
   - `AdminController.addProgress` uses `@PathVariable` without a name and the build is not compiled with `-parameters`. This can throw:
     `IllegalArgumentException: Name for argument of type [UUID] not specified...`

## Pending (as of now)
- Resolve the `@PathVariable` name issue in `AdminController.addProgress` or enable `-parameters` in build.
- Decide how to handle duplicate sponsorship submissions in the UI (prevent or surface 409).
- Align Postman environment values with actual UUIDs from seed data.
- Bruno collection is currently removed; if needed, regenerate or restore.

## Uncommitted changes snapshot (high-level)
- Backend controllers updated: `AdminController`, `OrgChildrenController`, `SponsorChildrenController`
- Angular sponsor flow and related components/services added/modified
- Bruno files deleted locally
- Postman collections present

