# Role-Based Access Control (RBAC)

## Principles
- Least privilege
- Clear separation between internal staff and sponsors
- All writes require authenticated internal users
- Authentication is required for every request; unauthenticated -> 401, unauthorized -> 403
- Human-in-the-loop: no public self-registration for staff or sponsors in Phase-1
- Audit critical actions (child, ledger, progress, sponsorship) with actor identity

## Initial role categories
- Internal staff (full read/write): create and manage Child, LedgerEntry, ProgressUpdate, Sponsor, and Sponsorship records; view all data
- Sponsor (read-only): view assigned child profile, support status, ledger entries (list), and progress updates (list); no writes

## Notes
- Sponsor access is read-only in Phase-1
- Derived status is visible but not editable
- Sponsor visibility is limited to children linked via a Sponsorship record
- Future role expansion (e.g., ops auditor) requires a new ADR before implementation
