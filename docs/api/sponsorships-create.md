# Create Sponsorship (Future Commitment)

## URL
POST /sponsorships

## Method
POST

## Auth / Roles
- Internal staff

## Request JSON
```json
{
  "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812",
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "startMonth": "2026-03"
}
```

## Response JSON
201 Created
```json
{
  "sponsorshipId": "d3a6e9b6-3c89-4a62-8f93-1c2b3d4e5f6a",
  "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812",
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "startMonth": "2026-03",
  "createdAt": "2026-02-01T14:00:00Z"
}
```

## Error format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "startMonth must be in the future",
    "details": {
      "startMonth": "2026-03"
    }
  }
}
```

## Notes
- `startMonth` must be in the future
- Sponsorships are commitments only; no payments are handled in Phase-1
- Ledger entries use `sponsorshipId` when coverage is SPONSORSHIP
- Possible errors: 400 validation, 401 unauthorized, 403 forbidden, 404 not found
