# Create Progress Update

## URL
POST /children/{childId}/progress-updates

## Method
POST

## Auth / Roles
- Internal staff

## Request JSON
```json
{
  "month": "2026-01",
  "summary": "Attended all classes and completed assignments on time."
}
```

## Response JSON
201 Created
```json
{
  "progressUpdateId": "2c7b1ce7-5329-4f78-8d90-4f6d0f4f8f2e",
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "month": "2026-01",
  "summary": "Attended all classes and completed assignments on time.",
  "createdAt": "2026-02-01T13:00:00Z"
}
```

## Error format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "month must match an existing ledger entry",
    "details": {
      "month": "2026-01"
    }
  }
}
```

## Notes
- Progress update month must match a ledger month for the child
- Month format is `YYYY-MM`
- Possible errors: 400 validation, 401 unauthorized, 403 forbidden, 404 not found
