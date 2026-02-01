# Create Ledger Entry

## URL
POST /children/{childId}/ledger-entries

## Method
POST

## Auth / Roles
- Internal staff

## Request JSON
```json
{
  "month": "2026-01",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "coverageType": "EARLY_SUPPORT_POOL",
  "sponsorshipId": null
}
```

## Response JSON
201 Created
```json
{
  "ledgerEntryId": "f83d6b30-6b1b-4f4a-9c23-6f6a6a1d8e5b",
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "month": "2026-01",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "coverageType": "EARLY_SUPPORT_POOL",
  "sponsorshipId": null,
  "createdAt": "2026-02-01T12:30:00Z"
}
```

## Error format
```json
{
  "error": {
    "code": "CONFLICT",
    "message": "ledger entry already exists for month",
    "details": {
      "month": "2026-01"
    }
  }
}
```

## Notes
- One ledger entry per child per month (append-only)
- `coverageType` values: `EARLY_SUPPORT_POOL`, `SPONSORSHIP`
- If `coverageType` is `SPONSORSHIP`, `sponsorshipId` is required
- Month format is `YYYY-MM`
- Money uses string decimal with 2 fractional digits; currency is ISO 4217 uppercase
- Possible errors: 400 validation, 401 unauthorized, 403 forbidden, 404 not found, 409 conflict
