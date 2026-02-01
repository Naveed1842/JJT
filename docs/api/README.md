# API Contracts (Phase-1)

This folder defines stable API contracts. Internal implementation can change without breaking these contracts.

## Contract format
Each endpoint must define:
- URL
- Method
- Request JSON
- Response JSON
- Error format

## Status
Initial contracts drafted. Use the template in `contract-template.md` when drafting new endpoints.

## Conventions (applies to all endpoints)
- Money: `amount` is a string decimal with 2 fractional digits; `currency` is ISO 4217 uppercase (e.g., \"USD\").
- Month fields use `YYYY-MM` (UTC).
- Date fields use `YYYY-MM-DD` (ISO 8601).
- Timestamps use UTC ISO 8601 with `Z` suffix.
- Errors follow the envelope `{ \"error\": { \"code\", \"message\", \"details\" } }`.
- Sponsor access is read-only and limited to children they are assigned to via a sponsorship record; internal staff have full access.
- Auth errors: `401` when authentication is missing/invalid; `403` when authenticated but role/ownership is insufficient.

## Drafted endpoints
- `children-create.md`
- `children-get.md`
- `ledger-entry-create.md`
- `ledger-entry-list.md`
- `progress-update-create.md`
- `progress-update-list.md`
- `sponsors-create.md`
- `sponsorships-create.md`
- `sponsor-children-list.md`
- `sponsor-child-detail.md`
- `error-codes.md`
