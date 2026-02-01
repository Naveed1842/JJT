# ADR 0002: Append-Only Ledger and Derived Support Status

## Status
Accepted

## Context
The platform exists to guarantee education continuity and transparent reporting. Financial and support records must be trustworthy and auditable. Manual edits to ledger data would undermine transparency.

## Decision
- Ledger entries are append-only with no update or delete operations
- Each child has at most one ledger entry per month
- Corrections are recorded as new entries
- Support status is derived from the ledger and sponsorship timing and is not stored manually

## Consequences
- All reporting must read from the ledger and derive status
- Data correction workflows must append entries rather than edit
- Conflicts (duplicate month entries) are handled explicitly
