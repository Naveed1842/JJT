# Business Rules — Phase 2
## Junior Jinnah Trust — Complete Rule Catalogue

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Scope

This document defines all business rules for Phase 2. It includes:
- Revised Phase 1 rules (where Phase 2 changes them)
- All new Phase 2 rules
- Governance rules that the platform should enforce or support

Rules from Phase 1 that are unchanged are not repeated here. See Phase 1 BRD BR-01 through BR-12.

---

## Rule Naming Convention

Rules are numbered with a prefix indicating their domain:

- `BR-FM-*` — Fund Management
- `BR-DON-*` — Donation Management
- `BR-PAY-*` — Sponsorship Payment
- `BR-CAMP-*` — Campaign
- `BR-CHILD-*` — Child lifecycle
- `BR-AUD-*` — Audit and Compliance
- `BR-ORG-*` — Organisation and Access
- `BR-NOTIF-*` — Notifications
- `BR-GOV-*` — Governance

---

## Fund Management Rules

### BR-FM-01: Fund Balance Immutability

**Rule:** A fund account's balance is always derived by summing its `FundTransaction` records. It is never stored as a scalar.

**Rationale:** Storing a scalar balance risks drift between the balance and the transaction log. Derivation ensures the balance is always consistent with the full audit trail.

**Implementation:** `FundAccount.balance` is a derived query, never a database column.

---

### BR-FM-02: Non-Negative Fund Balance (Soft Block)

**Rule:** The system shall prevent any `FundTransaction DEBIT` that would reduce a Fund Account balance below zero. The admin may override this block with a recorded justification (max 1 override per transaction).

**Rationale:** A fund account with a negative balance represents money spent that was never received. This is a governance emergency.

**User Story:** As a Finance Admin, when I try to record early support for a child that would overdraw the fund, I see a warning with the current balance, the requested debit, and a field to enter a justification if I need to proceed.

**Acceptance Criteria:**
- Transaction is blocked if it would create a negative balance
- Admin sees the balance before and after the proposed debit
- If admin provides justification and confirms, the transaction proceeds
- The override is logged as an `AuditEvent` with the justification

---

### BR-FM-03: Minimum Reserve Alert

**Rule:** When a fund account's balance falls below `organisation.minFundReserve`, the admin dashboard shall display a persistent warning banner.

**Rationale:** Early warning before the fund is exhausted enables proactive fundraising.

**Alert Levels:**
- 80% of threshold: Yellow warning (informational)
- 100% of threshold (balance = minReserve): Orange alert (action recommended)
- Balance < minReserve: Red alert (action required)

---

### BR-FM-04: Zakat Fund Isolation

**Rule:** Funds in a `ZAKAT` Fund Account may never be:
- Transferred to any other Fund Account
- Debited for administrative expenses
- Debited for non-Zakat-eligible purposes

**Rationale:** Sharia obligation. Breach constitutes a religious and legal violation.

**Implementation:** The UI and API shall block fund transfers from the Zakat Fund. An attempt returns: `"ZAKAT funds cannot be transferred or repurposed. Contact your Sharia compliance advisor."` The attempt is logged as an `AuditEvent`.

---

### BR-FM-05: Campaign Fund Cannot Exceed Target

**Rule:** A Campaign Fund Account shall not accept donations that would cause the total raised to exceed the campaign's `targetAmount`, unless the campaign is explicitly reopened by an admin.

**Rationale:** Overcollection without donor consent misrepresents how their donation will be used.

**Implementation:** When recording a campaign donation, the system shall calculate `currentBalance + newAmount` and reject with a warning if it exceeds the target.

---

### BR-FM-06: Fund Transaction Attribution

**Rule:** Every `FundTransaction` must have `createdBy` (user UUID) and `createdAt` (timestamp). No anonymous financial transactions are permitted.

**Rationale:** Audit requirement. Every financial event must be traceable to a specific user.

---

### BR-FM-07: Large Transaction Approval (Four-Eyes)

**Rule:** Any single `FundTransaction` with `amount > org.largeTransactionThreshold` (default: PKR 50,000) requires a second approver (`approvedBy`) different from `createdBy`.

**Rationale:** Prevents insider fraud and single-point-of-failure for large disbursements.

**User Story:** As a Finance Admin, when I record a large transaction, I submit it for approval. It enters a PENDING_APPROVAL state. A second admin with FINANCE_ADMIN or JJT_ADMIN role reviews and approves or rejects it with a reason.

**Acceptance Criteria:**
- Transaction cannot proceed without a second approver above the threshold
- `createdBy` and `approvedBy` must be different user UUIDs
- Rejected transactions are recorded with rejection reason (not deleted)

---

### BR-FM-08: Correction Mechanism (No Deletions)

**Rule:** Errors in financial records shall be corrected via a compensating `FundTransaction` of opposite type, never by modifying or deleting the original transaction.

**Example:** If a CREDIT was recorded for PKR 2,000 in error, the correction is a DEBIT of PKR 2,000 with `category=CORRECTION` and a description referencing the original transaction ID.

---

## Donation Rules

### BR-DON-01: Donation Fund Routing

**Rule:** Each donation type has a mandatory target fund. The system enforces this routing.

| Donation Type | Target Fund |
|--------------|-------------|
| SPONSORSHIP_PAYMENT | General Education Fund |
| ONE_TIME | As specified by admin |
| RECURRING_MONTHLY | As specified by admin |
| CAMPAIGN | Campaign's dedicated Fund Account |
| ZAKAT | Zakat Fund Account only |
| SADAQAH | Sadaqah Fund or General Fund per donor intent |
| ANONYMOUS | General Fund only |
| GIFT_AID_RECLAIM | General Fund |

---

### BR-DON-02: Receipt Number Uniqueness

**Rule:** Receipt numbers follow the format `{ORG_SLUG}-{YEAR}-{NNNN}` and are unique per organisation per calendar year. The sequence is monotonically increasing.

**Implementation:** The system generates the next available sequence number at time of issuance. Gaps are permitted (a voided receipt leaves a gap). Numbers are never reused.

---

### BR-DON-03: Immutable Issued Receipt

**Rule:** Once a receipt is issued (`receiptStatus = ISSUED`), the associated `Donation` record is immutable. If an error is discovered, a new correcting donation must be created and the original donation annotated with a reference to the correction.

**Rationale:** Issued receipts are legal documents. Modifying the underlying record misrepresents the receipt that was sent to the donor.

---

### BR-DON-04: Anonymous Donation Privacy

**Rule:** Anonymous donations shall not appear in any report that identifies donors. They contribute to aggregate totals (fund balance, monthly total) but not to any named donor list.

---

### BR-DON-05: Gift Aid Declaration Requirement

**Rule:** Gift Aid eligibility requires ALL of: `isUkTaxpayer=true`, `firstName`, `lastName`, `fullAddress`, `giftAidDeclarationDate`. The system shall prevent marking a donation as Gift Aid eligible unless all five fields are present on the donor record.

---

## Sponsorship Payment Rules

### BR-PAY-01: Monthly Payment Record Required

**Rule:** For every ACTIVE sponsorship and every calendar month from `startMonth` until expiry, exactly one `SponsorPayment` record must exist. This record is auto-created at the start of each month with status `EXPECTED`.

**Rationale:** Without this record, the system cannot detect missed payments. It is the heartbeat of the financial lifecycle.

---

### BR-PAY-02: Payment Uniqueness Per Sponsorship Per Month

**Rule:** A unique constraint on `(sponsorship_id, month)` in the `sponsor_payments` table ensures only one payment record per sponsorship per calendar month. Attempts to create a duplicate return a 409 CONFLICT.

---

### BR-PAY-03: Atomic Payment Recording

**Rule:** Recording a payment as RECEIVED is an atomic operation that must create ALL of the following in a single database transaction:
1. UPDATE `SponsorPayment.status` to RECEIVED
2. CREATE `Donation` record
3. CREATE `FundTransaction` CREDIT
4. CREATE `LedgerEntry` (coverageType=SPONSOR)

If any of these four operations fails, all must be rolled back.

**Rationale:** Partial state (payment received but no ledger entry created, or fund credited but no donation record) is a financial integrity violation.

---

### BR-PAY-04: Overdue Transition Timing

**Rule:** A `SponsorPayment` transitions from `EXPECTED` to `OVERDUE` when the current date exceeds `paymentDueDate` (org.paymentDueDay of the payment month). This transition is performed by a scheduled daily job.

---

### BR-PAY-05: Waiver Requirements

**Rule:** A payment can only be WAIVED with a mandatory reason (minimum 10 characters). A waived payment must be recorded — it does not simply disappear. The child's education month must still be covered via a compensating EARLY_SUPPORT entry from the General Fund.

**Rationale:** Waiving a payment must not leave the child uncovered. The financial impact is absorbed by the organisation, not the child.

---

### BR-PAY-06: Consecutive Overdue Escalation

**Rule:**
- 2 consecutive OVERDUE months → Admin alert (low priority)
- 3 consecutive OVERDUE months → Admin escalation (required action — cannot dismiss without choosing an outcome)

**Implementation:** The system counts consecutive months where `status = OVERDUE` for the same sponsorship. The count resets to zero when a RECEIVED payment is recorded.

---

### BR-PAY-07: No Backdated Payment for Expired Sponsorship

**Rule:** A payment cannot be recorded for a month that falls after the `Sponsorship.expiredAt` date. Attempting to do so returns a validation error.

**Exception:** A Finance Admin may record a backdated payment for an expired sponsorship if an audit justification is provided. This is an exceptional flow, not a normal operation.

---

## Campaign Rules

### BR-CAMP-01: Campaign Status Machine

**Valid transitions:**
- DRAFT → ACTIVE (admin opens campaign)
- ACTIVE → FUNDED (target met)
- ACTIVE → CLOSED (admin closes, or endDate reached)
- FUNDED → ACTIVE (admin reopens with justification — stretch goal)
- FUNDED → CLOSED (admin closes)
- CLOSED → ARCHIVED (after financial disposition is recorded)

**Invalid transitions:**
- Any status → DRAFT (a launched campaign cannot be un-launched)
- ARCHIVED → any (terminal state)

---

### BR-CAMP-02: Campaign Donation Lock

**Rule:** A campaign in status CLOSED or ARCHIVED does not accept new donations. Any API attempt returns: `"This campaign is closed. Donations cannot be accepted."`

---

### BR-CAMP-03: Unspent Campaign Fund Disposition

**Rule:** When a campaign is CLOSED, the admin must record a disposition for any remaining fund balance before the campaign can be ARCHIVED. Valid dispositions: `RETURN_TO_GENERAL`, `REFUND_DONORS`, `ROLLOVER`.

- `RETURN_TO_GENERAL`: Creates a FUND_TRANSFER FundTransaction from campaign fund to General Fund
- `REFUND_DONORS`: Admin must process refunds manually; records a DEBIT from campaign fund
- `ROLLOVER`: Admin creates a new campaign and transfers balance to new campaign fund

---

## Child Lifecycle Rules

### BR-CHILD-01: Child Archival Prerequisite

**Rule:** A child record cannot be archived while any of the following exist: ACTIVE or PENDING sponsorships, open `SponsorPayment` records (EXPECTED), current month LedgerEntry not yet created.

**Implementation:** The archive operation validates these conditions and returns a descriptive error listing which conditions must be resolved.

---

### BR-CHILD-02: Education Cost History

**Rule:** When a child's education cost changes, a new `EducationCostHistory` entry is created with an `effectiveFrom` date. The previous cost entry remains intact. Only the most recent entry whose `effectiveFrom` is ≤ current month is used for new SponsorPayment expected amounts.

---

### BR-CHILD-03: Coverage Gap Detection

**Rule:** A child with an ACTIVE sponsorship and no `SponsorPayment` record for the current month (after auto-creation) — or a child with no current-month LedgerEntry after the 15th of the month — shall be flagged as "coverage at risk" in the admin dashboard.

---

### BR-CHILD-04: Graduated Child Protection

**Rule:** Once a child is marked GRADUATED, no new LedgerEntry records may be appended to their ledger. All existing sponsorships are auto-expired. Historical records remain accessible.

---

## Audit Rules

### BR-AUD-01: Full Attribution

**Rule:** All financial records (`FundTransaction`, `Donation`, `SponsorPayment`, `LedgerEntry`, `DonorReceipt`) must have `createdBy` and `createdAt` fields populated. System-generated records (e.g. auto-created SponsorPayment by monthly job) shall use a designated system user UUID.

---

### BR-AUD-02: Immutable Audit Log

**Rule:** `AuditEvent` records are INSERT-only. No application role has UPDATE or DELETE permissions on the `audit_events` table. This is enforced at the database level via row-level security.

---

### BR-AUD-03: Audit Checksum Verification

**Rule:** The annual audit export tool shall verify that all `AuditEvent.checksum` values are consistent with their fields. Any checksum mismatch is a tamper indicator and must be flagged prominently in the export report.

---

### BR-AUD-04: Sensitive Action Logging

**Rule:** The following actions must always generate an `AuditEvent`:
- Any FundTransaction above PKR 10,000
- Any sponsorship status change (PENDING → ACTIVE, ACTIVE → EXPIRED)
- Any SponsorPayment status change
- Any fund account balance override (forced debit below zero)
- Any four-eyes approval (both the creation and the approval)
- Any user role change
- Any login failure (3 or more in 1 hour from same IP)
- Any audit export generation

---

## Organisation Rules

### BR-ORG-01: Cross-Organisation Isolation

**Rule:** All API endpoints must filter results by the authenticated user's `organisation_id`. A user from Organisation A must never be able to read, write, or infer data from Organisation B.

**Implementation:** Every query includes `WHERE organisation_id = :currentUserOrgId`. This is enforced at the repository/use-case level, not relying on URL parameters alone.

---

### BR-ORG-02: Organisation Configuration Immutability

**Rule:** `Organisation.baseCurrency` cannot be changed once any `FundTransaction` has been recorded. Changing the base currency after financial history exists would make all historical amounts meaningless.

---

## Notification Rules

### BR-NOTIF-01: Notification Delivery Log

**Rule:** Every notification email attempted must create an `EmailNotificationLog` entry recording: recipient, template, sent_at, and outcome (SENT, FAILED, BOUNCED, DELIVERED).

**Rationale:** If a sponsor claims they never received a receipt or overdue notice, the delivery log is the evidence.

---

### BR-NOTIF-02: Notification on Critical Events (Mandatory)

**Rule:** The following events must always trigger a notification, regardless of notification preferences:
- Payment receipt (to sponsor)
- Donation receipt (to donor)
- Sponsorship expiry warning at 30 days (to admin)
- Overdue escalation at 3 consecutive months (to admin, high priority)

These cannot be suppressed by the user's notification preferences because they are financial acknowledgements, not marketing.

---

## Governance Rules

### BR-GOV-01: Board Report Availability

**Rule:** The quarterly board report (fund balances, programme summary, cash flow, payment collection rate) must be producible in under 60 seconds for the current period.

---

### BR-GOV-02: Annual Audit Export Completeness

**Rule:** The annual audit export must include:
- All `FundTransactions` for the calendar year
- All `Donations` for the calendar year
- All `SponsorPayments` for the calendar year
- All `LedgerEntries` for the calendar year
- All `AuditEvents` for the calendar year
- Fund account opening and closing balances
- A checksum of the entire export

Any year for which the export cannot be produced should be flagged as an audit concern.

---

### BR-GOV-03: Data Retention Policy

**Rule:** Child and financial records must be retained for a minimum of 7 years after the child exits the programme. After 7 years, personal identifying information (name, address) may be anonymised while financial records are retained in anonymised form.

**Implementation:** A `retentionDate` field on the Child record, calculated as `exitDate + 7 years`. A periodic job flags records past their retention date for review.

---

## Summary Table

| Rule ID | Domain | Priority | Phase |
|---------|--------|----------|-------|
| BR-FM-01 through FM-08 | Fund Management | Critical | M2.2 |
| BR-DON-01 through DON-05 | Donations | High | M2.5 |
| BR-PAY-01 through PAY-07 | Payments | Critical | M2.3 |
| BR-CAMP-01 through CAMP-03 | Campaigns | High | M2.6 |
| BR-CHILD-01 through CHILD-04 | Child Lifecycle | High | M2.1 |
| BR-AUD-01 through AUD-04 | Audit | Critical | M2.1 |
| BR-ORG-01 through ORG-02 | Organisation | Critical | M2.4 |
| BR-NOTIF-01 through NOTIF-02 | Notifications | High | M2.7 |
| BR-GOV-01 through GOV-03 | Governance | Medium | M2.10 |
