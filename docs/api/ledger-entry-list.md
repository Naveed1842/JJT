# List Ledger Entries

## URL
GET /children/{childId}/ledger-entries?fromMonth=2025-01&toMonth=2025-12

## Method
GET

## Auth / Roles
- Internal staff
- Sponsor (read-only) for assigned children

## Request JSON
None

## Response JSON
200 OK
```json
{
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "entries": [
    {
      "ledgerEntryId": "f83d6b30-6b1b-4f4a-9c23-6f6a6a1d8e5b",
      "month": "2026-01",
      "educationCost": {
        "amount": "125.00",
        "currency": "USD"
      },
      "coverageType": "EARLY_SUPPORT_POOL",
      "sponsorshipId": null,
      "createdAt": "2026-02-01T12:30:00Z"
    }
  ]
}
```

## Error format
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "child not found",
    "details": {
      "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a"
    }
  }
}
```

## Notes
- Results are limited to the requested month range (inclusive)
- Month format is `YYYY-MM`
- Sponsor access is read-only and limited to children assigned to the sponsor via a sponsorship record
- Possible errors: 401 unauthorized, 403 forbidden, 404 not found
