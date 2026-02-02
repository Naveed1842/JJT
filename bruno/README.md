# JJT Phase-1 Bruno Collection (dev)

## How to run
- Start the API with dev profile so seed data loads:  
  `SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run`
- Install Bruno and open the `bruno/` folder, or run via CLI.
- Select environment `dev.bruenv`.

## Environment values
- `baseUrl` default: http://localhost:8080  
- Months are prefilled for current context (previousMonth, currentMonth, nextMonth). Adjust if your calendar differs.  
- IDs: `CHILD-001`, `CHILD-002`, `SPONSOR-001`, roles/headers set for admin/org/sponsor.

## What the suites cover
- `00_health`: basic actuator health.
- `01_admin_write`: admin/org create child, record early support, add progress, commit future sponsorship (all 201).
- `02_org_read`: org-admin read-only views for children, ledger, progress (200).
- `03_sponsor_read`: sponsor can only view assigned child (CHILD-002) ledger/progress (200).
- `99_negative_cases`: trust boundaries — missing role (401), wrong role (403), sponsor forbidden on other child (403), duplicate ledger month (409), progress for non-ledger month (400).

## Expected outcomes / invariants checked
- Ledger is append-only; duplicate month returns 409.
- Sponsorship and progress align with ledger months.
- RBAC enforced via headers (X-ROLE, optional X-SPONSOR-ID).
- No write APIs for sponsors; org/sponsor endpoints are read-only.
