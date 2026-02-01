# ADR 0001: Modular Monolith with Layered Architecture

## Status
Accepted

## Context
Phase-1 requires low-cost, simple deployment with a protected core domain. The platform must remain stable and auditable, with explicit transactions and no microservices.

## Decision
Use a modular monolith with clear layers:
- Controller (no business logic)
- Application services (use cases, transactions)
- Domain (entities, rules, invariants)
- Infrastructure (persistence, storage)

There will be a single deployable application and a single PostgreSQL database in Phase-1.

## Consequences
- Operations are simpler and cheaper in Phase-1
- Domain boundaries are enforced in-process rather than via network boundaries
- Future service extraction will require new ADRs and careful migration
