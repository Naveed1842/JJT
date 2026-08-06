# Roadmap 5 — End-to-End Testing Guide

Financial Operations & Transparency Platform

---

## Prerequisites

- Backend running: `./mvnw spring-boot:run` (PostgreSQL must be up)
- Frontend running: `cd jjt-angular && npm start`
- Logged in as `admin@jjt.org` / `Admin@JJT2024!`

All new backend endpoints are under `/api/admin/finance/*`, `/api/admin/people`, `/api/admin/approvals`, `/api/admin/missions`, and `/api/admin/finance/executive`. The transparency endpoint is public: `/api/public/transparency`.

---

## 1. Chart of Accounts (CoA)

**Goal:** Verify the 25-row charity pack seed is present.

1. `GET /api/admin/finance/categories`
2. Expect 25 rows, codes `1000`–`8999`, reporting classes `INCOME / PROGRAMME / ADMIN / FUNDRAISING`.

**Frontend:** Admin → Finance → CoA tab.

---

## 2. Financial Periods

**Goal:** Create, close, and lock a period.

1. `POST /api/admin/finance/periods`
   ```json
   { "periodType": "MONTHLY", "label": "Jul 2026", "startDate": "2026-07-01", "endDate": "2026-07-31" }
   ```
2. Verify status is `OPEN`.
3. `POST /api/admin/finance/periods/{id}/close` → status becomes `CLOSED`.
4. `POST /api/admin/finance/periods/{id}/lock` → status becomes `LOCKED`.
5. Attempt to close an already-locked period → expect `400`.

**Frontend:** Admin → Finance → Periods tab.

---

## 3. Vendors

**Goal:** Create a vendor and deactivate it.

1. `POST /api/admin/finance/vendors`
   ```json
   { "name": "Al-Noor Stationers", "contactEmail": "alnoor@example.com", "taxReference": "NTN-12345" }
   ```
2. `GET /api/admin/finance/vendors` → vendor appears, `active: true`.
3. `POST /api/admin/finance/vendors/{id}/deactivate` → `active: false`.

**Frontend:** Admin → Finance → Vendors tab.

---

## 4. Expense Lifecycle

**Goal:** Draft → Submit → AI Approval Checks → Approve → Pay → verify Journal entry.

### 4a. Create Draft

`POST /api/admin/finance/expenses`
```json
{
  "title": "School supplies Q3",
  "vendorName": "Al-Noor Stationers",
  "amount": "15000.00",
  "currency": "PKR",
  "expenseDate": "2026-07-10",
  "categoryId": 5200,
  "description": "Notebooks and pencils for Lahore campus"
}
```
- Note: if `vendorName` is provided and no `vendorId`, the backend quick-creates a minimal vendor row.
- Response: `status: DRAFT`.

### 4b. Submit for Approval

`POST /api/admin/finance/expenses/{id}/submit`
- Creates an `ApprovalRequest` and runs 3 AI checks (duplicate heuristic, budget threshold, anomaly detection).
- Response: `status: SUBMITTED`.

### 4c. Check Approval Queue

`GET /api/admin/approvals`
- Find the request for this expense.
- Check `checks[]` array for `checkType` and `verdict` (PASS/WARN/FAIL).
- Check `aiRecommendation` field.

### 4d. Approve

`POST /api/admin/approvals/{approvalId}/resolve`
```json
{ "decision": "APPROVED", "notes": "Reviewed — looks fine" }
```
- Expense status becomes `APPROVED`.

### 4e. Pay

`POST /api/admin/finance/expenses/{id}/pay`
```json
{ "paymentMethod": "BANK_TRANSFER" }
```
- Expense status becomes `PAID`.
- A `financial_transactions` row is created with `tx_type: EXPENSE`.

### 4f. Verify Journal

`GET /api/admin/finance/transactions`
- The new transaction should appear with `source_type: EXPENSE` and the correct amount.

**Frontend:** Admin → Finance → Expenses tab (full lifecycle available via status buttons).

---

## 5. Budgets & Variance

**Goal:** Create a budget line and check variance.

1. Create a period (step 2 above) and note its `id`.
2. `POST /api/admin/finance/budgets`
   ```json
   { "periodId": "{periodId}", "categoryId": 5200, "budgetedAmount": "50000.00", "currency": "PKR" }
   ```
3. Pay one expense against the same period/category (step 4 above).
4. `GET /api/admin/finance/budgets/variance?periodId={periodId}`
   - Expect `actual` to be `15000`, `variance` to be `35000` (positive = under budget).

**Frontend:** Admin → Finance → Budgets tab → View Variance button.

---

## 6. People & Payroll

### 6a. Create Person

`POST /api/admin/people`
```json
{ "kind": "STAFF", "firstName": "Fatima", "lastName": "Shah", "email": "fatima@jjt.org" }
```

### 6b. Set Payroll Profile

`PUT /api/admin/people/{personId}/payroll`
```json
{ "salaryType": "MONTHLY_FIXED", "baseAmount": "45000.00", "currency": "PKR", "effectiveFrom": "2026-07-01" }
```

### 6c. Create Payroll Run

`POST /api/admin/finance/payroll/runs`
```json
{ "periodLabel": "Jul 2026", "currency": "PKR" }
```
- Auto-computes items from active payroll profiles.
- `GET /api/admin/finance/payroll/runs/{runId}/items` → should show Fatima's entry at 45,000 PKR.

### 6d. Process Run

`POST /api/admin/finance/payroll/runs/{runId}/process`
- Status → `PAID`.
- One `financial_transactions` row per payroll item is created.

**Frontend:** Admin → People section (create person, set payroll profile). Admin → Finance → Payroll tab (create + process run).

---

## 7. Missions & Programmes

**Goal:** Create a programme hierarchy and view financials.

1. `POST /api/admin/missions`
   ```json
   { "kind": "PROGRAMME", "name": "Lahore Education Initiative", "targetAmount": "500000.00", "startDate": "2026-01-01", "endDate": "2026-12-31" }
   ```
2. `POST /api/admin/missions`
   ```json
   { "kind": "PROJECT", "name": "Q3 Supplies", "parentId": "{programmeId}" }
   ```
3. `GET /api/admin/missions/{programmeId}/children` → Q3 Supplies appears.
4. `GET /api/admin/missions/{programmeId}/financials` → expense/income rolled up using recursive CTE.

**Frontend:** Admin → Missions section.

---

## 8. Executive Summary

`GET /api/admin/finance/executive/summary`

Expect:
- `mtdIncome`: total income this month.
- `mtdExpense`: total expenses this month.
- `pendingApprovalCount`: number of submissions awaiting human review.
- `programmePct`: from the latest published transparency snapshot (0 if none yet).
- `aiRecommendations[]`: any pending AI recommendations.

**Frontend:** Admin → Executive section.

---

## 9. Transparency Snapshot (Nightly Job)

The `TransparencyService` is scheduled `@Scheduled(cron = "0 0 2 * * *")` — runs at 02:00 server time.

**To trigger manually for testing:**

```bash
# Hit the exec endpoint (no direct trigger route, but you can call the service bean directly
# or temporarily lower the cron schedule in application.yml to run every minute)
```

Or via Postman: call `GET /api/public/transparency` — if no snapshot exists yet, expect `404`.

Once a snapshot exists:
- `GET /api/public/transparency` → public, unauthenticated.
- Verify `programmePct`, `adminPct`, `fundraisingPct`, `beneficiaryCount`, `costPerBeneficiary`.
- `published: true` only if `transactionCount >= 10` (minimum-data guard).

**Frontend Trust page:** Navigate to `/trust` — percentage bars now use live data with fallback to 87%/9%/4% when no snapshot is published.

---

## 10. Rejection & Void Paths

- Submit an expense → Reject via `POST /api/admin/approvals/{id}/resolve` with `"decision":"REJECTED"` → expense status = `REJECTED`.
- Create a draft expense → Void via `POST /api/admin/finance/expenses/{id}/void` → status = `VOIDED`.
- A voided or rejected expense creates no journal entry.

---

## 11. Security

- All `/api/admin/finance/*`, `/api/admin/people`, `/api/admin/approvals`, `/api/admin/missions` require `JJT_ADMIN` or `ORG_ADMIN` role.
- `GET /api/public/transparency` is unauthenticated — test without a token.
- Confirm a `SPONSOR` role JWT gets `403` on any finance endpoint.

---

## Postman Quick Reference

All new endpoints follow the same auth pattern as existing ones (Bearer token in Authorization header). Use the existing `JJT-dev.postman_environment.json` with `{{base_url}}` = `http://localhost:8080`.

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/admin/finance/categories` | List CoA |
| POST | `/api/admin/finance/periods` | Create period |
| POST | `/api/admin/finance/periods/{id}/close` | Close period |
| POST | `/api/admin/finance/periods/{id}/lock` | Lock period |
| GET | `/api/admin/finance/vendors` | List vendors |
| POST | `/api/admin/finance/vendors` | Create vendor |
| GET | `/api/admin/finance/expenses` | List expenses |
| POST | `/api/admin/finance/expenses` | Create expense |
| POST | `/api/admin/finance/expenses/{id}/submit` | Submit for approval |
| POST | `/api/admin/finance/expenses/{id}/approve` | Approve directly |
| POST | `/api/admin/finance/expenses/{id}/pay` | Mark paid |
| POST | `/api/admin/finance/expenses/{id}/void` | Void |
| GET | `/api/admin/approvals` | List approval requests |
| POST | `/api/admin/approvals/{id}/resolve` | Human decision |
| GET | `/api/admin/finance/transactions` | Journal entries |
| GET | `/api/admin/finance/budgets` | List budgets |
| POST | `/api/admin/finance/budgets` | Create budget |
| GET | `/api/admin/finance/budgets/variance?periodId=` | Variance report |
| GET | `/api/admin/people` | List people |
| POST | `/api/admin/people` | Create person |
| PUT | `/api/admin/people/{id}/payroll` | Upsert payroll profile |
| POST | `/api/admin/finance/payroll/runs` | Create payroll run |
| POST | `/api/admin/finance/payroll/runs/{id}/process` | Process run |
| GET | `/api/admin/missions` | List root mission nodes |
| POST | `/api/admin/missions` | Create mission node |
| GET | `/api/admin/missions/{id}/financials` | Mission financials (YTD) |
| GET | `/api/admin/finance/executive/summary` | Executive dashboard |
| GET | `/api/public/transparency` | Public trust data (no auth) |
