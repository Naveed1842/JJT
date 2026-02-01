# Data Model (Conceptual)

## Database principles
- PostgreSQL
- Ledger entries are immutable
- No silent edits for critical data
- Audit all critical actions

## Relationships
- One Child -> one EducationSupportLedger
- One EducationSupportLedger -> many LedgerEntry
- One LedgerEntry -> one ProgressUpdate (per month)
- Sponsor -> many Sponsorship (future commitments)

## Constraints
- LedgerEntry month is unique per child
- ProgressUpdate month must match a ledger month
- Support status is derived, not stored
- Sponsorship start month must be in the future
