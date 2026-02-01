# Phase-1 Guardrails

## Mission
- Children's education must not stop because sponsorship is delayed
- Education continuity comes first
- Sponsorship joins later
- Transparency is mandatory

## Platform intent
- This is not a fundraising platform
- This is a continuity and trust platform

## Scope (locked)
In scope:
- Child registration without sponsor dependency
- Monthly education cost per child
- Early Support Pool (temporary coverage)
- Immutable monthly education ledger
- Support status (EARLY_SUPPORTED, SPONSORED, AT_RISK)
- Sponsor read-only visibility
- Monthly progress updates
- Role-based access control
- Manual, human-controlled workflows
- Low-cost, simple deployment

Out of scope:
- Online payments
- Payment gateways
- Automated sponsor sourcing
- AI / analytics
- Mobile apps
- Government integrations
- Adoption / legal workflows
- Marketing automation

## Core constraints
- No microservices in Phase-1
- Domain-first, ledger-first design
- Append-only financial data
- Explicit transactions
- Status is derived from the ledger
- Derived data must not be stored manually
- API-first contracts: define URL/method/JSON/error format before implementation

## Protected core
- Child
- Education Support Ledger
- Ledger Entry rules
- Domain invariants

## Governance
- Phase-1 scope is frozen
- ADRs required for architectural changes
- Documentation is part of infrastructure
- No shortcuts, even if slower
