# Create Sponsor

## URL
POST /sponsors

## Method
POST

## Auth / Roles
- Internal staff

## Request JSON
```json
{
  "displayName": "Green Valley Trust",
  "contactEmail": "contact@greenvalley.org"
}
```

## Response JSON
201 Created
```json
{
  "sponsorId": "5f0b9a91-1e5f-4f3c-9c0f-2cbd39b1c812",
  "displayName": "Green Valley Trust",
  "contactEmail": "contact@greenvalley.org",
  "createdAt": "2026-02-01T13:30:00Z"
}
```

## Error format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "displayName is required",
    "details": {
      "field": "displayName"
    }
  }
}
```

## Notes
- Sponsor records are commitments only; no payments are handled in Phase-1
- Possible errors: 400 validation, 401 unauthorized, 403 forbidden
