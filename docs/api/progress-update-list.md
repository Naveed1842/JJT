# List Progress Updates

## URL
GET /children/{childId}/progress-updates?fromMonth=2025-01&toMonth=2025-12

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
  "updates": [
    {
      "progressUpdateId": "2c7b1ce7-5329-4f78-8d90-4f6d0f4f8f2e",
      "month": "2026-01",
      "summary": "Attended all classes and completed assignments on time.",
      "createdAt": "2026-02-01T13:00:00Z"
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
