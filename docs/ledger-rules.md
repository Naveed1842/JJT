# Ledger Rules

## Append-only
- Ledger entries are immutable
- No update or delete operations on ledger entries
- Corrections are recorded as new entries

## One entry per month
- Each child has at most one ledger entry per month
- Ledger month is the primary time unit for support
- Ledger month format is YYYY-MM

## Source of truth
- Ledger drives support status
- All reporting derives from the ledger

## Auditability
- Every ledger entry must be attributable to a human action
- Critical actions are audited
