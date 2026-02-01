# API Error Code Catalog (Phase-1)

Errors use the envelope:
```json
{ "error": { "code": "...", "message": "...", "details": { /* optional */ } } }
```

## Codes
- `UNAUTHORIZED` — HTTP 401 — authentication is missing or invalid (e.g., missing/expired token)
- `FORBIDDEN` — HTTP 403 — authenticated but role/ownership is insufficient
- `VALIDATION_ERROR` — HTTP 400 — request failed business or schema validation; `details` points to the field/constraint
- `NOT_FOUND` — HTTP 404 — requested resource does not exist or is not visible to caller
- `CONFLICT` — HTTP 409 — request violates uniqueness/append-only rules (e.g., duplicate ledger month)
- `INTERNAL_ERROR` — HTTP 500 — unexpected server error; avoid exposing internals in `message`

## Notes
- Always prefer specific codes above; reserve `INTERNAL_ERROR` for unhandled cases
- `details` is optional and should be structured JSON (field names, ids, constraint names)
- Messages should be user-readable, not stack traces
