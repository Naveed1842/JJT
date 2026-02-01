# Domain Model (Phase-1)

## Entities
- Child: a registered student whose education continuity is tracked
- EducationSupportLedger: per-child ledger that records monthly support
- LedgerEntry: append-only monthly record for a child
- Sponsor: a future-only commitment entity (no payments in Phase-1)
- Sponsorship: a commitment that starts in a future month
- ProgressUpdate: mandatory monthly update tied to a ledger month
- UserAccount: access identity used for RBAC

## Invariants
- Each child has exactly one ledger
- Each ledger has at most one entry per month
- Ledger entries are immutable (append-only)
- Corrections are new entries, not edits
- Progress updates must match a ledger month
- Support status is derived from ledger entries
- Sponsorship start month must be in the future

## Status derivation
Support status values:
- EARLY_SUPPORTED
- SPONSORED
- AT_RISK

Status is derived from ledger entries and sponsorship timing. It is not stored manually.
