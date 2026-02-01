# Get Child Summary

## URL
GET /children/{childId}

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
  "fullName": "Amina Yusuf",
  "dateOfBirth": "2016-05-14",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "supportStatus": "EARLY_SUPPORTED"
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
- `supportStatus` is derived from the ledger, not stored manually
- Use ledger and progress update endpoints for monthly history
- Sponsor access is limited to children linked to the sponsor via a sponsorship record
- `educationCost` represents the monthly education cost for the child
- Possible errors: 401 unauthorized, 403 forbidden, 404 not found
