# Sponsor Child Detail (Read-Only)

## URL
GET /sponsors/{sponsorId}/children/{childId}

## Method
GET

## Auth / Roles
- Sponsor (read-only) for own sponsorId and children assigned via sponsorship
- Internal staff

## Request JSON
None

## Response JSON
200 OK
```json
{
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "fullName": "Amina Yusuf",
  "dateOfBirth": "2016-05-14",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "supportStatus": "SPONSORED",
  "sponsorshipStartMonth": "2026-03"
}
```

## Error format
```json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "not allowed to access this child for sponsor",
    "details": {
      "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812",
      "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a"
    }
  }
}
```

## Notes
- Read-only endpoint for sponsors
- `sponsorshipStartMonth` reflects the committed start month (format `YYYY-MM`)
- `educationCost` represents the monthly education cost for the child
- Use child ledger and progress update endpoints for monthly history
- Possible errors: 401 unauthorized, 403 forbidden, 404 not found
