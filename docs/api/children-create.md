# Create Child

## URL
POST /children

## Method
POST

## Auth / Roles
- Internal staff

## Request JSON
```json
{
  "fullName": "Amina Yusuf",
  "dateOfBirth": "2016-05-14",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "notes": "optional"
}
```

## Response JSON
201 Created
```json
{
  "childId": "9b9b4f2f-9e7c-4b5a-8b60-1c3c2f0e2a1a",
  "fullName": "Amina Yusuf",
  "dateOfBirth": "2016-05-14",
  "educationCost": {
    "amount": "125.00",
    "currency": "USD"
  },
  "supportStatus": "AT_RISK",
  "createdAt": "2026-02-01T12:00:00Z"
}
```

## Error format
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "educationCost.amount must be a string",
    "details": {
      "field": "educationCost.amount"
    }
  }
}
```

## Notes
- Creates the child's ledger implicitly
- `supportStatus` is derived from the ledger, not stored manually
- Money uses string decimal with 2 fractional digits; currency is ISO 4217 uppercase
- Dates use ISO 8601 (`YYYY-MM-DD`), timestamps use UTC ISO 8601
- `educationCost` represents the monthly education cost for the child
- Possible errors: 400 validation, 401 unauthorized, 403 forbidden
