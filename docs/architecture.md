# Architecture

## Style
- Modular monolith (Phase-1)
- Domain-first design
- Ledger-first design
- Append-only financial data

## Stack (Phase-1)
- Java 17+
- Spring Boot
- PostgreSQL

## Layers
- Controller: request/response handling only, no business logic
- Application services: use cases, transactions
- Domain: entities, rules, invariants
- Infrastructure: persistence, storage, integrations

## Transactions and data
- Explicit transactions for every write use case
- Ledger is the source of truth for support status
- One ledger entry per child per month
- Corrections append new entries, never edit existing entries

## Evolution constraints
- No microservices in Phase-1
- API contracts remain stable even if internals change
