# Business Requirements Document (Phase-1)

## Purpose
Guarantee education continuity even when sponsorship is delayed, with full transparency and human-controlled workflows. Scope is locked to Phase-1 guardrails (no payments, no automation beyond RBAC-authenticated staff actions).

## Actors
- Internal Staff (read/write): manage Child, EducationSupportLedger, LedgerEntry, ProgressUpdate, Sponsor, Sponsorship; view all.
- Sponsor (read-only): view assigned children, support status, ledger entries (list), progress updates (list).

## Access Matrix (Phase-1)
| Capability | Internal Staff | Sponsor |
| --- | --- | --- |
| Register child | R/W | - |
| Create ledger entry | R/W | - |
| List ledger entries (assigned child) | R/W | R |
| Create progress update | R/W | - |
| List progress updates (assigned child) | R/W | R |
| Create sponsor | R/W | - |
| Create sponsorship (future) | R/W | - |
| View child summary (assigned) | R/W | R |
| View sponsor's children | R/W | R (own) |

## Core Use Cases
1) Child registration (creates ledger; sets monthly education cost).
2) Monthly ledger entry (one per child per month; coverage = Early Support Pool or Sponsorship).
3) Monthly progress update (must match an existing ledger month).
4) Sponsorship commitment (startMonth in the future; no payments).
5) Sponsor read-only visibility (assigned children only).

## Key Business Rules
- Ledger is append-only; corrections append new entries; no edits/deletes.
- One ledger entry per child per month (month format `YYYY-MM`).
- Education cost stored as `educationCost` (amount string with 2 decimals + ISO 4217 currency).
- Support status is derived from ledger + sponsorship timing; never manually set.
- Progress update month must match a ledger entry month for the child.
- Sponsorship startMonth must be in the future.
- Authentication required for all calls; 401 for missing/invalid, 403 for insufficient role/ownership.
- Audit all critical writes with actor identity and timestamp.

## Data Overview (business-facing)
- Child: identity, dateOfBirth, educationCost, derived supportStatus.
- EducationSupportLedger: per-child log of LedgerEntry.
- LedgerEntry: month, educationCost, coverageType (EARLY_SUPPORT_POOL | SPONSORSHIP), optional sponsorshipId, createdAt.
- Sponsor: displayName, contactEmail.
- Sponsorship: sponsorId, childId, startMonth, createdAt.
- ProgressUpdate: childId, month, summary, createdAt.

## Workflows (happy path)
- Register child: staff authenticates → submits child data + educationCost → ledger created → status derives as AT_RISK until coverage recorded.
- Add ledger entry: staff authenticates → checks month uniqueness → records entry (coverageType + optional sponsorshipId) → status derives.
- Add progress update: staff authenticates → validates month exists in ledger → records update.
- Create sponsorship: staff authenticates → validates startMonth is future → records sponsorship.
- Sponsor view: sponsor authenticates → sees assigned children summaries and lists of ledger entries and progress updates.

## Error Behaviors (mapped to codes)
- 400 VALIDATION_ERROR: schema/business rule failures (e.g., duplicate month in payload before insert, missing required fields).
- 401 UNAUTHORIZED: missing/invalid auth.
- 403 FORBIDDEN: authenticated but role/ownership insufficient (e.g., sponsor requesting non-assigned child).
- 404 NOT_FOUND: resource absent or not visible.
- 409 CONFLICT: append-only or uniqueness violation (ledger entry month exists).
- 500 INTERNAL_ERROR: unexpected.
See `docs/api/error-codes.md` for envelope and details guidance.

## Non-Functional (Phase-1)
- Simplicity/low cost: single deployable Spring Boot app, single PostgreSQL DB.
- Auditability: append-only ledger, audited critical actions, timestamps in UTC.
- Stability: API-first contracts; no microservices.
- Manual control: no automated sponsor sourcing or payments.

## Assumptions & Open Questions
- Identity/SSO provider TBD; must support role claims for staff vs sponsor.
- Time zone handling: ledger months use UTC; confirm display/localization needs before Phase-2.
- Data retention/backups: standard PostgreSQL backups; retention policy to be defined.
