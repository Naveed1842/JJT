# JJT Platform — End-to-End UAT Test Plan

**Version:** 1.0  
**Date:** 2026-07-07  
**Environment:** PostgreSQL (local), Spring Boot :8080, Angular :4200  
**Default Admin:** admin@jjt.org / Admin@JJT2024!

---

## Test Environment Setup

```bash
# Start PostgreSQL
docker compose -f docker-compose.postgres.yml up -d

# Start backend
./mvnw spring-boot:run

# Start frontend (separate terminal)
cd jjt-angular && npm start
```

**Postman:** Import `postman/JJT-dev.postman_environment.json` for API tests.

---

## Roles Used in This Plan

| Role | Username | Password | Access |
|---|---|---|---|
| JJT_ADMIN | admin@jjt.org | Admin@JJT2024! | Full system |
| ORG_ADMIN | (create via API) | set at creation | Org-scoped |
| SPONSOR | (create via API) | set at creation | Own child only |

---

## Legend

- **UI** — test via browser  
- **API** — call endpoint directly via Postman/curl  
- **DB** — query PostgreSQL to verify state  
- **N/A** — not applicable to this scenario  

---

---

# MODULE 1: Authentication & Roles

---

### TC-AUTH-001 — Admin Login (Happy Path)

**Business:** Admin logs in with correct credentials and receives JWT access token and refresh token.

**Preconditions:** App is running; admin seeded by `AdminUserInitializer`.

**Steps:**
1. POST `/api/auth/login` with `{ "email": "admin@jjt.org", "password": "Admin@JJT2024!" }`

**Expected API response:**
```json
{
  "accessToken": "<JWT>",
  "refreshToken": "<UUID>",
  "expiresIn": 900000,
  "tokenType": "Bearer"
}
```

**Expected DB changes:**
- `refresh_tokens` table: one new row with `token_hash = SHA256(refreshToken)`, `revoked_at = NULL`, `expires_at = now() + 7 days`

**Expected UI:** Dashboard loads; admin menu visible.

**Testing method:** API + DB

---

### TC-AUTH-002 — Sponsor Login

**Business:** Sponsor user logs in and can only see their child's data.

**Preconditions:** Sponsor user created and linked to a sponsor record.

**Steps:**
1. POST `/api/auth/login` with sponsor credentials
2. GET `/api/sponsor/children` (should return only their children)
3. GET `/api/admin/children` (should return 403)

**Expected API responses:**
- Login: 200 with tokens
- Sponsor children: 200 with child list
- Admin children: 403 FORBIDDEN

**Testing method:** API

---

### TC-AUTH-003 — Session Restore via Refresh Token

**Business:** After browser restart (access token lost), refresh token restores session.

**Preconditions:** User has logged in; `rt` key exists in localStorage.

**Steps:**
1. POST `/api/auth/refresh` with `{ "refreshToken": "<stored refresh token>" }`

**Expected API response:**
- 200 with new `accessToken` and new `refreshToken`
- Old refresh token is invalidated

**Expected DB changes:**
- Old `refresh_tokens` row: `revoked_at = now()`
- New `refresh_tokens` row: `revoked_at = NULL`

**Edge cases:**
- Submit revoked refresh token → 401
- Submit expired refresh token → 401

**Testing method:** API + DB

---

### TC-AUTH-004 — Invalid Credentials

**Business:** Wrong password returns 401, not 500.

**Steps:**
1. POST `/api/auth/login` with `{ "email": "admin@jjt.org", "password": "wrong" }`

**Expected:** 401 `{"code":"UNAUTHORIZED","message":"Authentication failed"}`

**Testing method:** API

---

### TC-AUTH-005 — Change Password

**Business:** Authenticated user changes their own password.

**Steps:**
1. Login as admin
2. POST `/api/auth/change-password` with `{ "currentPassword": "...", "newPassword": "NewPass@123!" }`
3. Login with old password → should fail
4. Login with new password → should succeed

**Expected DB changes:** `users.password_hash` updated

**Testing method:** API + DB

---

### TC-AUTH-006 — Logout

**Business:** Logout revokes refresh token; subsequent refresh fails.

**Steps:**
1. Login; note refresh token
2. POST `/api/auth/logout` with `{ "refreshToken": "<token>" }`
3. POST `/api/auth/refresh` with same token → should fail

**Expected DB:** `refresh_tokens.revoked_at` is set

**Testing method:** API + DB

---

### TC-AUTH-007 — Role-Based Page Access (UI)

**Business:** SPONSOR cannot access admin pages.

**Steps:**
1. Login as SPONSOR via UI
2. Navigate to `/admin` manually via URL bar

**Expected UI:** Redirected to home or shown 403/forbidden page.

**Testing method:** UI

---

---

# MODULE 2: Child Management

---

### TC-CHILD-001 — Create Child via Admin Form

**Business:** Admin creates a new child record. Child + ledger created atomically.

**Preconditions:** Logged in as JJT_ADMIN or ORG_ADMIN.

**Steps:**
1. Navigate to Admin → Children → Add Child
2. Fill: Roll Number = `TEST-001`, Full Name = `Ali Khan`, City = `Karachi`, Campus = `North Campus`, Education Amount = `2500`, Currency = `PKR`
3. Submit

**Expected UI:** Child appears in children list. Availability status = AVAILABLE.

**Expected API (if done via API):**
- POST `/api/admin/children` → 200 with `{ "childId": "<uuid>", "ledgerId": "<uuid>" }`

**Expected DB changes:**
- `children` row: all fields populated, `organisation_id` = org UUID
- `education_support_ledgers` row: `child_id` = new child's ID, `organisation_id` = org UUID

**Expected audit events:** `CHILD_CREATED` event with `entity_id = childId`

**Edge cases:**
- Roll number with leading/trailing spaces (should trim or reject)
- Very long full name (should respect DB column limit)

**Failure scenarios:**
- Duplicate roll number → 409 CONFLICT
- Missing required field → 400 VALIDATION_ERROR

**Testing method:** UI + DB

---

### TC-CHILD-002 — Bulk Import Children (CSV)

**Business:** Admin uploads a CSV and multiple children are created in one request.

**Preconditions:** CSV template downloaded from `/api/admin/children/import/template`.

**Steps:**
1. Download template
2. Fill CSV with 3 rows (valid) + 1 row with duplicate roll number + 1 row with blank name
3. Upload via Admin → Children → Bulk Import

**Expected API response:**
```json
{
  "totalRows": 5,
  "importedRows": 3,
  "skippedMissingName": 1,
  "skippedDuplicateRollNumber": 1,
  "failedRows": 0
}
```

**Expected UI:** Results table shows per-row status.

**Expected DB changes:**
- 3 new `children` rows
- 3 new `education_support_ledgers` rows
- No rows for skipped records

**Failure scenarios:**
- Empty CSV → 400
- All rows duplicates → all skipped, 0 imported

**Testing method:** UI + API + DB

---

### TC-CHILD-003 — Availability Status Derivation

**Business:** Availability status is computed from sponsorships, not stored.

**Steps:**
1. Create a child → status = AVAILABLE
2. Commit a sponsorship (PENDING) → status = RESERVED
3. Activate the sponsorship → status = ALLOCATED
4. Expire the sponsorship → status = AVAILABLE again

**Expected DB:** No `availability_status` column in `children` table — it's derived at query time.

**Testing method:** API + DB (verify status at each step)

---

---

# MODULE 3: Sponsor Management

---

### TC-SPONS-001 — Create Sponsor

**Business:** Admin registers a new sponsor.

**Preconditions:** Logged in as admin.

**Steps:**
1. POST `/api/admin/sponsors` with `{ "displayName": "Ahmed Ali", "contactEmail": "ahmed@example.com", "phone": "+92-300-0000000" }`

**Expected API:** 200 with `{ "sponsorId": "<uuid>", "displayName": "Ahmed Ali", "contactEmail": "ahmed@example.com" }`

**Expected DB:** New row in `sponsors` table.

**Failure scenarios:**
- Duplicate email → 409 CONFLICT (`uk_sponsor_contact_email`)
- Blank display name → 400

**Testing method:** API + DB

---

### TC-SPONS-002 — Create Sponsor User Account

**Business:** Admin creates a login account for a sponsor so they can view their child.

**Preconditions:** Sponsor record exists. Sponsor is linked to a child via active sponsorship.

**Steps:**
1. POST `/api/admin/users/sponsor` with `{ "sponsorId": "<uuid>", "email": "ahmed@example.com", "password": "SponsorPass@1!" }`
2. Login as the new sponsor user
3. GET `/api/sponsor/children` → should return sponsored child

**Expected DB:**
- `users` row with `role = 'SPONSOR'`, `sponsor_id = <sponsorId>`

**Failure scenarios:**
- Sponsor ID does not exist → 404 or 400
- Duplicate email → 409

**Testing method:** API + DB

---

---

# MODULE 4: Sponsorship Lifecycle

---

### TC-SLIFE-001 — Commit Future Sponsorship (Happy Path)

**Business:** Admin records a sponsor's commitment to fund a child starting from a future month.

**Preconditions:** Child with AVAILABLE status; sponsor exists.

**Steps:**
1. POST `/api/admin/sponsorships` with:
   ```json
   {
     "sponsorId": "<uuid>",
     "childId": "<uuid>",
     "startMonth": "<YYYY-MM of next month>",
     "commitmentType": "MONTHLY"
   }
   ```

**Expected API:** 200 with `{ "sponsorshipId", "sponsorId", "childId", "startMonth" }`

**Expected DB:**
- `sponsorships` row: `status = 'PENDING'`, `start_month = <future month>`, `commitment_type = 'MONTHLY'`

**Expected child availability:** RESERVED (not AVAILABLE)

**Expected audit event:** `SPONSORSHIP_COMMITTED`

**Failure scenarios:**
- `startMonth` = current month → 400 (must be future)
- `startMonth` = past month → 400
- Child already has PENDING/ACTIVE sponsorship → 409 (partial unique index violation)

**Testing method:** API + DB

---

### TC-SLIFE-002 — Activate Sponsorship

**Business:** Admin activates a PENDING sponsorship, making it ACTIVE.

**Preconditions:** Sponsorship in PENDING status.

**Steps:**
1. POST `/api/admin/sponsorships/{id}/activate`

**Expected API:** 200

**Expected DB:** `sponsorships.status = 'ACTIVE'`

**Expected child availability:** ALLOCATED

**Expected notification:** Sponsor welcome email queued (`SPONSORSHIP_ACTIVATED` template)

**Expected audit event:** `SPONSORSHIP_ACTIVATED`

**Failure scenarios:**
- Activating an already ACTIVE sponsorship → idempotent OR 400 depending on implementation
- Activating EXPIRED sponsorship → 400 (invalid state transition)

**Testing method:** API + DB

---

### TC-SLIFE-003 — Expire Sponsorship

**Business:** Admin expires an ACTIVE or PENDING sponsorship.

**Preconditions:** Sponsorship in ACTIVE or PENDING status.

**Steps:**
1. POST `/api/admin/sponsorships/{id}/expire`

**Expected DB:** `sponsorships.status = 'EXPIRED'`

**Expected child availability:** AVAILABLE (no more active/pending sponsorship)

**Verify:** The partial unique index `idx_one_active_pending_per_child` no longer blocks new sponsorship for the same child.

**After expiry:** Attempt to commit a new sponsorship for the same child → should succeed.

**Testing method:** API + DB

---

### TC-SLIFE-004 — Cannot Have Two Active Sponsorships for Same Child

**Business:** Database constraint prevents two non-expired sponsorships for the same child.

**Preconditions:** Child has ACTIVE sponsorship.

**Steps:**
1. Attempt to commit another sponsorship for the same child

**Expected:** 409 CONFLICT (PostgreSQL unique constraint violation on `idx_one_active_pending_per_child`)

**Expected API:** 409 with `{"code":"CONFLICT","message":"A record with the same key already exists."}`

**Testing method:** API + DB

---

### TC-SLIFE-005 — Same Sponsor, Different Children

**Business:** One sponsor can fund multiple children simultaneously.

**Steps:**
1. Sponsor A commits to Child 1 (ACTIVE)
2. Sponsor A commits to Child 2 (ACTIVE)

**Expected DB:** Two active sponsorship rows, both linked to same `sponsor_id`

**Testing method:** API + DB

---

---

# MODULE 5: Early Support (JJT-Funded)

---

### TC-EARLY-001 — Record Early Support (Sufficient Funds)

**Business:** JJT pays a child's education fee before a sponsor is found.

**Preconditions:** Child exists with no ledger entry for target month. Fund account balance ≥ education cost + min_reserve.

**Steps:**
1. Check fund balance: GET `/api/admin/fund-accounts`
2. Note balance (e.g., 50,000 PKR, min_reserve = 20,000 PKR)
3. POST `/api/admin/children/{childId}/early-support` with:
   ```json
   {
     "childId": "<uuid>",
     "month": "2026-06",
     "educationAmount": "2500",
     "educationCurrency": "PKR"
   }
   ```

**Expected API:** 200

**Expected DB changes:**
- `ledger_entries`: new row with `coverage_type = 'EARLY_SUPPORT'`, `entry_month = '2026-06'`, `education_amount = 2500`
- `fund_transactions`: new DEBIT row, `amount = 2500`, `reason = 'EARLY_SUPPORT'`, `ledger_entry_id = <new entry id>`

**Expected fund balance after:** 47,500 PKR (still above min_reserve of 20,000 PKR)

**Expected audit event:** `EARLY_SUPPORT_RECORDED`

**Testing method:** API + DB

---

### TC-EARLY-002 — Early Support Blocked by Insufficient Funds

**Business:** JJT cannot pay if balance would drop below min_reserve (unless forced).

**Preconditions:** Fund balance is exactly at or below min_reserve + education cost (e.g., balance = 22,000, min_reserve = 20,000, cost = 2,500 → after debit: 19,500 < 20,000).

**Steps:**
1. Attempt early support without force flag

**Expected API:** 400 or 422 with `INSUFFICIENT_FUNDS` error including `currentBalance`, `debitAmount`, `minReserve`

**Expected DB:** No new ledger entry. No fund transaction.

**Testing method:** API + DB

---

### TC-EARLY-003 — Early Support with Force Flag

**Business:** Admin can override the fund check with a reason.

**Preconditions:** Same as TC-EARLY-002 (balance would go below reserve).

**Steps:**
1. POST with `force: true` and `forceReason: "Emergency payment, sponsor incoming"`

**Expected API:** 200

**Expected DB:**
- Ledger entry created
- Fund transaction created (DEBIT)
- `admin_alerts`: new `FUND_BELOW_RESERVE` alert created

**Testing method:** API + DB

---

### TC-EARLY-004 — Duplicate Month Early Support Rejected

**Business:** Append-only ledger prevents two entries for the same month.

**Preconditions:** Early support already recorded for 2026-06.

**Steps:**
1. Attempt early support again for the same child, same month

**Expected API:** 409 CONFLICT (unique constraint `uk_ledger_month`)

**Expected DB:** Only one `ledger_entries` row for (ledger_id, 2026-06).

**Testing method:** API + DB

---

### TC-EARLY-005 — Fund Below Reserve Alert (Non-Blocking)

**Business:** Dropping below min_reserve raises an alert but does not roll back the transaction.

**Preconditions:** Fund balance is 21,000 PKR, min_reserve = 20,000 PKR, early support amount = 2,000 PKR (leaves 19,000, below 20,000).

**Steps:**
1. Record early support (balance check passes: 21,000 - 2,000 = 19,000 which fails reserve, but this scenario depends on implementation — test with force)
2. OR arrange balance to be just above threshold and record support

**Expected DB:**
- `admin_alerts`: new row with `alert_type = 'FUND_BELOW_RESERVE'`, `severity = 'CRITICAL'`, `dismissed_at = NULL`
- Fund transaction and ledger entry still created (alert is non-blocking)

**Testing method:** DB

---

---

# MODULE 6: Sponsor Payments & Reconciliation

---

### TC-PAY-001 — Generate Expected Payments for a Month

**Business:** System generates one EXPECTED payment record per ACTIVE sponsorship for the month.

**Preconditions:** At least 2 ACTIVE sponsorships exist with `start_month <= target_month`.

**Steps:**
1. POST `/api/admin/payments/generate?year=2026&month=6`

**Expected DB:**
- One `sponsor_payments` row per active sponsorship for 2026-06
- `status = 'EXPECTED'`
- `expected_amount` = child's education cost
- `received_amount = NULL`

**Idempotency:** Running again for same month inserts 0 new rows (skips existing).

**Edge cases:**
- PENDING sponsorships should NOT generate payments (only ACTIVE)
- Sponsorship start_month > target month should NOT generate payment

**Testing method:** API + DB

---

### TC-PAY-002 — Record Sponsor Payment Received (Full Amount)

**Business:** Sponsor pays in full; fund is credited; ledger entry created.

**Preconditions:** EXPECTED payment exists for sponsorship X, month 2026-06.

**Steps:**
1. Note current fund balance
2. POST `/api/admin/payments/{paymentId}/receive` with:
   ```json
   {
     "receivedAmount": "2500",
     "currency": "PKR",
     "bankReference": "TXN-123456",
     "receivedDate": "2026-07-03"
   }
   ```

**Expected API:** 200

**Expected DB changes:**
- `sponsor_payments`: `status = 'RECEIVED'`, `received_amount = 2500`, `bank_reference = 'TXN-123456'`, `received_date = 2026-07-03`, `fund_transaction_id` set, `ledger_entry_id` set
- `fund_transactions`: new CREDIT row, `amount = 2500`, `reason = 'SPONSOR_PAYMENT'`
- `ledger_entries`: new row with `coverage_type = 'SPONSOR'`, `entry_month = 2026-06`, `education_amount = 2500`

**Expected fund balance:** Previous balance + 2500

**Expected notification:** `PAYMENT_RECEIVED` email queued to sponsor

**Expected audit event:** `PAYMENT_RECEIVED`

**Testing method:** API + DB

---

### TC-PAY-003 — Record Sponsor Payment Received (Partial Amount)

**Business:** Sponsor pays less than expected; payment marked PARTIAL.

**Preconditions:** EXPECTED payment exists.

**Steps:**
1. POST receive with `receivedAmount = "1500"` (expected = 2500)

**Expected DB:**
- `sponsor_payments.status = 'PARTIAL'`
- `received_amount = 1500`
- Ledger entry and fund transaction still created for 1500

**Testing method:** API + DB

---

### TC-PAY-004 — Waive Payment

**Business:** Admin forgives a sponsor's payment for a month. Ledger entry still created; no fund credit.

**Preconditions:** EXPECTED payment exists.

**Steps:**
1. POST `/api/admin/payments/{paymentId}/waive` with `{ "reason": "Sponsor on leave" }`

**Expected DB:**
- `sponsor_payments.status = 'WAIVED'`, `waiver_reason = 'Sponsor on leave'`
- `ledger_entries`: new row with `coverage_type = 'SPONSOR'` (waived but education still covered)
- NO new `fund_transactions` row (no money received)

**Verify:** Fund balance unchanged after waiver.

**Testing method:** API + DB

---

### TC-PAY-005 — Cannot Record Duplicate Payment for Same Month

**Business:** Database constraint prevents two payments for the same (sponsorship, month).

**Preconditions:** EXPECTED payment exists for sponsorship X, month 2026-06.

**Steps:**
1. Attempt to generate expected payments again for same month (idempotent — should skip)
2. Attempt to directly POST a new payment for same sponsorship + month

**Expected:** 409 CONFLICT (`uk_sponsorship_payment_month`)

**Testing method:** API + DB

---

### TC-PAY-006 — Mark Overdue Payments

**Business:** Payments past the due date get marked OVERDUE; alerts raised; notifications sent.

**Preconditions:** EXPECTED payment for 2026-05 exists and today is 2026-07 (past due).

**Steps:**
1. POST `/api/admin/payments/mark-overdue?year=2026&month=5`

**Expected DB:**
- `sponsor_payments.status = 'OVERDUE'` for affected rows
- `admin_alerts`: new `PAYMENT_OVERDUE` alert per overdue payment
- `email_notifications`: new rows with `template = 'PAYMENT_OVERDUE'`

**Idempotency:** Running again does not duplicate alerts (idempotent alert creation).

**Testing method:** API + DB

---

### TC-PAY-007 — The JJT Reimbursement Scenario (Key Business Flow)

**Business:** JJT paid early support for June 2026. Sponsor subsequently pays for June 2026. Verify: ledger shows SPONSOR entry overriding EARLY_SUPPORT intent, fund is reimbursed.

**Preconditions:**
- Child has EARLY_SUPPORT ledger entry for 2026-06 (JJT already paid)
- Child has ACTIVE sponsorship starting 2026-06 or earlier

**Steps:**
1. Generate expected payments for 2026-06
2. Confirm `sponsor_payments` row created with `status = 'EXPECTED'`
3. Record payment received for 2026-06

**Expected DB:**
- `ledger_entries` for 2026-06 — there should be TWO entries: one `EARLY_SUPPORT` (original) and one `SPONSOR` (new from payment)
- OR: verify your business rule: does the system prevent this? Check if the unique constraint `uk_ledger_month` blocks the second entry.

**⚠️ CLARIFICATION NEEDED:** The DB constraint `UNIQUE(ledger_id, entry_month)` means there can be **only one** ledger entry per child per month. If JJT records early support for June, can the sponsor ALSO pay for June — and if so, what happens to the ledger?

**This is a critical business rule to clarify with the client before production.**

Possible behaviours:
- a) The system rejects the sponsor payment for a month that already has a ledger entry → sponsor effectively doesn't reimburse JJT for that month
- b) The ledger allows multiple entries per month (current DB constraint does NOT allow this)
- c) The EARLY_SUPPORT entry is replaced by the SPONSOR entry (not possible with append-only rules)

**Expected fund outcome:** If sponsor payment IS recorded for 2026-06 despite EARLY_SUPPORT: Fund gets a CREDIT. The net effect is JJT recoups its early payment.

**Testing method:** API + DB — and clarification with client required

---

### TC-PAY-008 — Cannot Receive Payment Already Received

**Business:** Once RECEIVED or WAIVED, payment cannot be modified.

**Preconditions:** Payment is RECEIVED.

**Steps:**
1. Attempt to POST receive again

**Expected:** 400 (invalid state — only EXPECTED and OVERDUE can be received)

**Testing method:** API

---

### TC-PAY-009 — Monthly Reconciliation Report

**Business:** Admin views per-month reconciliation showing expected, received, overdue, and waived counts.

**Steps:**
1. GET `/api/admin/reconciliation?year=2026&month=6`

**Expected API response includes:**
- `summary.expected`, `summary.received`, `summary.overdue`, `summary.waived`, `summary.total`
- `atRisk`: sponsorships with consecutive overdue months

**Testing method:** API

---

---

# MODULE 7: Ledger Management

---

### TC-LED-001 — Ledger Created Atomically with Child

**Business:** Every child must have exactly one education support ledger.

**Steps:**
1. Create a child
2. DB query: `SELECT * FROM education_support_ledgers WHERE child_id = '<new child id>'`

**Expected DB:** Exactly one row.

**Failure scenario:** Child creation without ledger → application bug (violates domain invariant).

**Testing method:** DB

---

### TC-LED-002 — Ledger Entries Are Append-Only

**Business:** PostgreSQL RULEs prevent UPDATE or DELETE on ledger_entries.

**Steps:**
1. Note a ledger_entry id
2. Run directly in PostgreSQL:
   ```sql
   UPDATE ledger_entries SET education_amount = 99999 WHERE id = '<uuid>';
   DELETE FROM ledger_entries WHERE id = '<uuid>';
   ```

**Expected:** Both statements execute 0 rows (silently ignored by PostgreSQL RULE, or error if rule uses INSTEAD with no-op).

**Testing method:** DB (direct SQL)

---

### TC-LED-003 — Fund Transactions Are Append-Only

**Business:** Same as above for fund_transactions.

**Steps:**
1. Run directly in PostgreSQL:
   ```sql
   UPDATE fund_transactions SET amount = 1 WHERE id = '<any uuid>';
   DELETE FROM fund_transactions WHERE id = '<any uuid>';
   ```

**Expected:** Both execute 0 rows.

**Testing method:** DB (direct SQL)

---

---

# MODULE 8: Fund Accounts

---

### TC-FUND-001 — Fund Balance Correctly Computed

**Business:** Balance is SUM(CREDIT) - SUM(DEBIT) — not stored as a column.

**Steps:**
1. Note reported balance via GET `/api/admin/fund-accounts`
2. Run in PostgreSQL:
   ```sql
   SELECT
     SUM(CASE WHEN transaction_type = 'CREDIT' THEN amount ELSE 0 END)
     - SUM(CASE WHEN transaction_type = 'DEBIT' THEN amount ELSE 0 END) AS computed_balance
   FROM fund_transactions
   WHERE fund_account_id = '<uuid>';
   ```
3. Verify API balance matches computed_balance

**Testing method:** API + DB

---

### TC-FUND-002 — Credit Fund via Donation

**Preconditions:** Fund account exists.

**Steps:**
1. Record a donation linked to the fund account
2. Check fund balance increased

**Expected DB:** New `fund_transactions` CREDIT row; balance increases.

**Testing method:** API + DB

---

### TC-FUND-003 — Min Reserve Enforced on Early Support

**Business:** Cannot debit fund to below min_reserve (without force).

Already covered in TC-EARLY-002.

---

---

# MODULE 9: Donations

---

### TC-DON-001 — Record One-Time Donation

**Business:** Admin records a cash donation from a donor.

**Preconditions:** Donor exists. Fund account exists.

**Steps:**
1. POST `/api/admin/donations` with:
   ```json
   {
     "donorId": "<uuid>",
     "donationType": "GENERAL",
     "amount": "10000",
     "currency": "PKR",
     "donationDate": "2026-07-07",
     "fundAccountId": "<uuid>"
   }
   ```

**Expected API:** 200 with receipt

**Expected DB:**
- `donations` row: `status = 'RECEIPTED'`, `receipt_number = 'JJT-2026-0001'` (auto-generated)
- `fund_transactions` CREDIT row: `amount = 10000`
- `donation_receipt_sequences` updated: `last_sequence` incremented

**Expected notification:** `DONATION_RECEIPT` email queued to donor (if donor has email)

**Testing method:** API + DB

---

### TC-DON-002 — IN_KIND Donation Does Not Credit Fund

**Business:** Non-cash donations are recorded but do not touch the fund balance.

**Steps:**
1. POST donation with `"donationType": "IN_KIND"`, no `fundAccountId`

**Expected DB:**
- `donations` row with `status = 'RECEIPTED'`, `fund_transaction_id = NULL`
- NO new `fund_transactions` row

**Testing method:** API + DB

---

### TC-DON-003 — Receipt Number Auto-Increments Per Year

**Business:** First donation of 2026 = JJT-2026-0001; second = JJT-2026-0002.

**Steps:**
1. Record donation 1 → receipt = `JJT-2026-0001`
2. Record donation 2 → receipt = `JJT-2026-0002`
3. Record donation for different year 2025 → receipt = `JJT-2025-0001`

**Expected DB:** `donation_receipt_sequences`: rows for (orgId, 2026) with `last_sequence = 2` and (orgId, 2025) with `last_sequence = 1`.

**Testing method:** API + DB

---

### TC-DON-004 — Reverse Donation

**Business:** Admin reverses an incorrect donation; fund is debited back.

**Preconditions:** `RECEIPTED` donation exists linked to a fund account.

**Steps:**
1. POST `/api/admin/donations/{id}/reverse`
2. Check fund balance decreased

**Expected DB:**
- `donations.status = 'REVERSED'`
- `fund_transactions`: new DEBIT row, `reason = 'DONATION_REVERSAL'`

**Verify:** Debit is never blocked by reserve check (compensation transaction always allowed).

**Verify:** If balance drops below min_reserve after reversal → `FUND_BELOW_RESERVE` alert raised.

**Testing method:** API + DB

---

### TC-DON-005 — Cannot Reverse an Already Reversed Donation

**Steps:**
1. Reverse donation
2. Attempt to reverse again

**Expected:** 400 (status is already REVERSED)

**Testing method:** API

---

### TC-DON-006 — Create Recurring Donation Schedule

**Business:** Donor commits to monthly donations.

**Steps:**
1. POST `/api/admin/donations/recurring` with:
   ```json
   {
     "donorId": "<uuid>",
     "donationType": "GENERAL",
     "amount": "5000",
     "currency": "PKR",
     "frequency": "MONTHLY",
     "startDate": "2026-08-01",
     "fundAccountId": "<uuid>"
   }
   ```

**Expected DB:**
- `recurring_donation_schedules`: `status = 'ACTIVE'`, `next_due_date = 2026-08-01`

**Testing method:** API + DB

---

### TC-DON-007 — Pause and Resume Recurring Schedule

**Steps:**
1. PATCH `/api/admin/donations/recurring/{id}/pause`
2. Verify `status = 'PAUSED'`
3. PATCH `/api/admin/donations/recurring/{id}/resume`
4. Verify `status = 'ACTIVE'`

**Testing method:** API + DB

---

---

# MODULE 10: Progress Updates

---

### TC-PROG-001 — Add Progress Update (Happy Path)

**Business:** Admin records monthly progress for a child after their education fee has been paid.

**Preconditions:** Child has a ledger entry for 2026-06 (EARLY_SUPPORT or SPONSOR coverage).

**Steps:**
1. POST `/api/admin/children/{childId}/progress` with:
   ```json
   { "month": "2026-06", "summary": "Ahmed performed well this month, ranked 3rd in class." }
   ```

**Expected API:** 200

**Expected DB:**
- `progress_updates` row: `child_id`, `update_month = 2026-06`, `summary = '...'`

**Expected notification:** `PROGRESS_UPDATE` email queued to active sponsor (if any)

**Testing method:** API + DB

---

### TC-PROG-002 — Progress Update for Month Without Ledger Entry Is Rejected

**Business:** Cannot add progress for a month where no one paid the child's education fee.

**Steps:**
1. Attempt progress update for a month with no ledger entry

**Expected:** 400 or 404 with error message indicating ledger entry must exist first

**Testing method:** API

---

### TC-PROG-003 — Duplicate Month Progress Update Rejected

**Steps:**
1. Add progress update for 2026-06
2. Attempt again for same child and month

**Expected:** 409 CONFLICT (`uk_child_month`)

**Testing method:** API + DB

---

### TC-PROG-004 — Sponsor Views Progress Updates

**Preconditions:** SPONSOR user logged in and linked to a sponsorship for this child.

**Steps:**
1. GET `/api/sponsor/children/{childId}/progress`

**Expected:** List of progress updates for this child only

**Negative test:** GET progress for a DIFFERENT child → 403 or empty

**Testing method:** API

---

---

# MODULE 11: Public Pages

---

### TC-PUB-001 — Children Visible Without Login

**Business:** Public-facing child list is available to unauthenticated visitors.

**Steps:**
1. Without auth: GET `/api/org/children`

**Expected:** 200 with child list (AVAILABLE children highlighted)

**Testing method:** API (no auth header)

---

### TC-PUB-002 — Public Sponsorship Commitment

**Business:** A visitor can express commitment to sponsor a child via public form.

**Steps:**
1. Without auth: POST `/api/public/sponsor-commitment` with:
   ```json
   {
     "childId": "<uuid>",
     "commitmentType": "MONTHLY",
     "sponsor": { "name": "New Donor", "email": "newdonor@example.com" }
   }
   ```

**Expected API:** 200 with `{ "childId": "<uuid>", "startMonth": "<next month>" }`

**Expected DB:**
- `sponsors` row created (or found by email)
- `sponsorships` row: `status = 'PENDING'`

**Business note:** Public commitment does NOT immediately ACTIVATE. Admin must review and activate.

**⚠️ CLARIFICATION NEEDED:** Does the public commitment create the sponsor record automatically if the email doesn't exist? Confirm with client.

**Testing method:** API + DB

---

---

# MODULE 12: Reports

---

### TC-REP-001 — Cash Flow Report

**Business:** Shows monthly credit, debit, and balance over last 6 months.

**Steps:**
1. GET `/api/admin/reports/cash-flow`

**Expected API:** `{ months: [...], currentBalance, totalCredits3Month, totalDebits3Month }`

**Verify:** `currentBalance` matches the computed balance (TC-FUND-001 verification method).

**Testing method:** API + DB

---

### TC-REP-002 — Portfolio Report

**Business:** Summary of all active, pending, and expired sponsorships.

**Steps:**
1. GET `/api/admin/reports/portfolio`

**Expected API:** `{ activeCount, pendingCount, expiredCount, totalMonthlyValue, currency, commitmentBreakdown }`

**Testing method:** API + DB (cross-check counts against sponsorships table)

---

### TC-REP-003 — Dashboard Summary

**Steps:**
1. GET `/api/admin/dashboard`

**Expected:** Fund accounts, children stats (total/available/enrolled), payment stats, active alert count.

**Verify counts match DB queries:**
```sql
SELECT COUNT(*), availability_status FROM children GROUP BY availability_status;
-- (NOTE: availability_status is derived, not stored — query may need joins)
```

**Testing method:** API + DB

---

---

# MODULE 13: Alerts

---

### TC-ALERT-001 — Fund Below Reserve Alert

Already covered in TC-EARLY-005 and TC-DON-004.

**Additional verification:**
- Alert is org-scoped: `organisation_id` in alert matches org
- Alert has `alert_type = 'FUND_BELOW_RESERVE'`, `severity = 'CRITICAL'`

---

### TC-ALERT-002 — Admin Dismisses Alert

**Steps:**
1. GET `/api/admin/alerts` — note active alert ID
2. POST `/api/admin/alerts/{id}/dismiss`
3. GET `/api/admin/alerts` — dismissed alert no longer appears in active list

**Expected DB:** `admin_alerts.dismissed_at` is populated, `dismissed_by` = admin user ID

**Failure scenario:** Dismiss already dismissed alert → idempotent or 400

**Testing method:** API + DB

---

### TC-ALERT-003 — Alert Creation Is Idempotent

**Business:** Creating the same alert type for the same entity twice should not create duplicate alerts.

**Steps:**
1. Drop fund balance below reserve (early support)
2. Drop again (more early support)
3. Check `admin_alerts` — should have only ONE undismissed `FUND_BELOW_RESERVE` alert

**Testing method:** DB

---

---

# MODULE 14: Audit Logs

---

### TC-AUDIT-001 — Audit Events Recorded for Key Operations

**Operations to verify:**

| Operation | Expected audit event_type |
|---|---|
| Create child | CHILD_CREATED |
| Commit sponsorship | SPONSORSHIP_COMMITTED |
| Activate sponsorship | SPONSORSHIP_ACTIVATED |
| Expire sponsorship | SPONSORSHIP_EXPIRED |
| Record early support | EARLY_SUPPORT_RECORDED |
| Record payment received | PAYMENT_RECEIVED |
| Waive payment | PAYMENT_WAIVED |
| Record donation | DONATION_RECORDED |
| Reverse donation | DONATION_REVERSED |

**DB query for verification:**
```sql
SELECT event_type, actor_email, entity_type, entity_id, description, created_at
FROM audit_events
WHERE organisation_id = '<org_uuid>'
ORDER BY created_at DESC
LIMIT 50;
```

**Testing method:** DB (after each API operation above)

---

### TC-AUDIT-002 — Audit Events Are Immutable

**Steps:**
```sql
UPDATE audit_events SET description = 'tampered' WHERE id = '<uuid>';
DELETE FROM audit_events WHERE id = '<uuid>';
```

**Expected:** Note whether PostgreSQL RULEs prevent this (design intent is immutability; verify if DB-enforced or only app-enforced).

**Testing method:** DB (direct SQL)

---

---

# MODULE 15: Email Notifications

---

### TC-NOTIF-001 — Notifications Queued (Not Sent in Dev)

**Business:** In dev environment, email notifications are queued as `QUEUED` status. Verify records are created even if not actually sent.

**Steps:**
1. Activate a sponsorship
2. Query DB: `SELECT * FROM email_notifications WHERE template = 'SPONSORSHIP_ACTIVATED' ORDER BY created_at DESC LIMIT 5`

**Expected:** Row with `status = 'QUEUED'` or `'SENT'`, `recipient_email = sponsor's email`

**Testing method:** DB

---

### TC-NOTIF-002 — Correct Templates Used

| Event | Template |
|---|---|
| Sponsorship activated | SPONSORSHIP_ACTIVATED |
| Payment received | PAYMENT_RECEIVED |
| Payment overdue | PAYMENT_OVERDUE |
| Donation recorded | DONATION_RECEIPT |
| Progress update added | PROGRESS_UPDATE |

**Testing method:** DB (after triggering each event via API)

---

---

# Cross-Module End-to-End Scenarios

---

### TC-E2E-001 — Complete Sponsorship Lifecycle

1. Create child → AVAILABLE
2. Record early support for Month 1 → fund debited
3. Create sponsor → sponsor record exists
4. Commit sponsorship (Month 2) → PENDING, child = RESERVED
5. Activate sponsorship → ACTIVE, child = ALLOCATED
6. Generate expected payments for Month 2 → EXPECTED payment
7. Record payment for Month 2 → RECEIVED, fund credited, ledger entry created
8. Add progress update for Month 2 → success
9. Expire sponsorship → EXPIRED, child = AVAILABLE
10. Verify audit events for each step above

**Testing method:** API + DB (end-to-end workflow)

---

### TC-E2E-002 — Overdue Payment → Alert → Resolution

1. Generate expected payments for 2026-05
2. Mark overdue (2026-05 is past due)
3. Verify PAYMENT_OVERDUE alert raised; notification queued
4. Record payment received for overdue payment
5. Verify payment transitions to RECEIVED
6. Verify fund credited; ledger entry created
7. Verify alert is still open (admin must dismiss manually)

**Testing method:** API + DB

---

### TC-E2E-003 — Donation → Fund Credit → Early Support → Reserve Alert

1. Record PKR 25,000 donation → fund = 25,000
2. Record early support (child 1, 2,500 PKR) → fund = 22,500
3. Record early support (child 2, 2,500 PKR) → fund = 20,000 (exactly at reserve)
4. Record early support (child 3, 2,500 PKR) → fund = 17,500 (BELOW reserve)
5. Verify `FUND_BELOW_RESERVE` alert raised after step 4

**Testing method:** API + DB

---

---

# Business Rules Requiring Client Clarification

These are ambiguities found in the codebase that cannot be resolved by code review alone:

| # | Question | Impact |
|---|---|---|
| 1 | If JJT records early support for June, and the sponsor later pays for June — can both entries co-exist in the ledger? The DB constraint `uk_ledger_month` allows only one entry per month. Does the sponsor payment record get blocked? Or does JJT only ever get "reimbursed" by the ledger showing SPONSOR instead of EARLY_SUPPORT? | Critical — affects TC-PAY-007 |
| 2 | The `PREPAID` sponsor payment status exists in the DB constraint but no business logic uses it. When does a payment become PREPAID? | Medium |
| 3 | Public commitment endpoint creates a sponsor if email is new. Does the client want a manual review step before a public commitment becomes a formal sponsorship record? | High — affects public UX |
| 4 | Are recurring donations expected to auto-generate `EXPECTED` donation records (forecasts), and who triggers this? Is there a scheduled job? | Medium |
| 5 | When a sponsorship expires, what happens to EXPECTED payments that were already generated for future months but not yet received? Should they be cancelled? | High |
| 6 | Is `ORG_ADMIN` role active and used, or is JJT currently single-tenant (only one org, only JJT_ADMIN)? | Medium |
| 7 | What is the business process when a sponsor dies or becomes unreachable — is there an "emergency transfer" flow, or just expire + new sponsorship? | Low (operational) |
| 8 | Are progress updates visible to the public, or only to the logged-in sponsor? | Medium |
| 9 | For partial payments, does JJT fund the shortfall automatically, or does the child's education remain partially funded? | High — financial integrity |
| 10 | What currency conversion logic applies when sponsor pays in a different currency than the education cost? (Currently, system seems to expect same-currency payments) | Medium |

---

---

# Test Execution Checklist

## Phase 1 — Authentication & Data Setup (Day 1)
- [ ] TC-AUTH-001 through TC-AUTH-007
- [ ] Create test data: 5 children, 3 sponsors, 1 fund account

## Phase 2 — Child & Sponsor Management (Day 1)
- [ ] TC-CHILD-001 through TC-CHILD-003
- [ ] TC-SPONS-001 through TC-SPONS-002

## Phase 3 — Sponsorship Lifecycle (Day 2)
- [ ] TC-SLIFE-001 through TC-SLIFE-005
- [ ] TC-EARLY-001 through TC-EARLY-005

## Phase 4 — Payments & Financial (Day 2–3)
- [ ] TC-PAY-001 through TC-PAY-009
- [ ] TC-FUND-001 through TC-FUND-003
- [ ] TC-LED-001 through TC-LED-003

## Phase 5 — Donations (Day 3)
- [ ] TC-DON-001 through TC-DON-007

## Phase 6 — Progress & Notifications (Day 3)
- [ ] TC-PROG-001 through TC-PROG-004
- [ ] TC-NOTIF-001 through TC-NOTIF-002

## Phase 7 — Reports, Alerts & Audit (Day 4)
- [ ] TC-ALERT-001 through TC-ALERT-003
- [ ] TC-REP-001 through TC-REP-003
- [ ] TC-AUDIT-001 through TC-AUDIT-002

## Phase 8 — End-to-End Scenarios (Day 4–5)
- [ ] TC-E2E-001 through TC-E2E-003
- [ ] TC-PUB-001 through TC-PUB-002

## Phase 9 — Client Sign-Off
- [ ] Present 10 business clarification questions
- [ ] Sign off on each module with client rep
