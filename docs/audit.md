# Audit Requirements

## Audit scope
Critical actions must be audited and attributable to a human actor.

## Events to audit
- Child creation and updates
- Ledger creation
- Ledger entry creation
- Progress update creation
- Sponsorship and sponsor record creation
- Access to sponsor read-only views

## Audit principles
- No silent edits
- Append-only logs for critical actions
- Timestamps required for all audited events
