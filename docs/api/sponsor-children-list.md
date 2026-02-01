# List Sponsor Children (Read-Only)

## URL
GET /sponsors/{sponsorId}/children

## Method
GET

## Auth / Roles
- Sponsor (read-only) for own sponsorId
- Internal staff

## Request JSON
None

## Response JSON
200 OK
```json
{
  "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812",
  "children": [
    {
      "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
      "fullName": "Amina Yusuf",
      "supportStatus": "SPONSORED",
      "sponsorshipStartMonth": "2026-03"
    }
  ]
}
```

## Error format
```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "not allowed to access this sponsor",
    "details": {
      "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812"
    }
  }
}
```

## Notes
- Read-only endpoint for sponsors
- Children returned are those linked to the sponsor via a sponsorship record
- Use child ledger and progress update endpoints for monthly history
- Possible errors: 401 unauthorized, 403 forbidden, 404 not found
