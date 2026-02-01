# JJT - Early Education Support Platform

JJT is a humanitarian platform focused on one problem: children's education must not stop because sponsorship is delayed.
This repository is Phase-1 documentation only. No code is included yet.

## Mission
- Education continuity comes first
- Sponsorship joins later
- Transparency is mandatory

## Phase-1 scope (locked)
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

## Architecture (Phase-1)
- Spring Boot (Java 17+)
- Modular monolith
- Domain-first, ledger-first design
- Append-only financial data
- Explicit transactions
- No microservices in Phase-1

## Repository docs
Start here:
- `docs/README.md`
- `docs/phase-1-guardrails.md`
- `docs/architecture.md`
- `docs/domain-model.md`
- `docs/ledger-rules.md`
- `docs/workflows.md`
- `docs/api/README.md`
- `docs/adr/README.md`

## Wiki
For quick, Phase-1 summaries, see the wiki: https://github.com/Naveed1842/JJT/wiki
