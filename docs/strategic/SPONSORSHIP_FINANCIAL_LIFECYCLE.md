# Sponsorship Financial Lifecycle
## Junior Jinnah Trust — Complete Child Financial Journey

| | |
|---|---|
| **Document Version** | 1.0 |
| **Classification** | Strategic — Internal |
| **Prepared** | 2026-06-28 |
| **Status** | For Review |

---

## Overview

This document traces the complete financial lifecycle of a single child from the moment they are enrolled in the JJT programme to the moment their record is archived. At each stage, it identifies what financial events should occur, what ledger entries should exist, what fund movements should be recorded, and what reports should be produced.

The goal is to ensure that every rupee spent on every child is traceable from source to outcome.

---

## Stage 1 — Child Registered

**Trigger:** Admin creates a new child record.

**Current system behaviour:**
- Child record created
- Empty Education Support Ledger created (atomically)
- Child availability status = `AVAILABLE`

**What is missing:**
- No `created_by` timestamp (known gap, in ROADMAP V13)
- No annual education budget defined at registration
- No expected monthly cost validated against a school fee schedule
- No campaign association (if the child was added as part of a Back-to-School campaign)

**Recommended additions:**
- `enrolledBy: UUID → User` — who registered this child
- `enrolledAt: Instant`
- `annualBudget: Money` — approved education budget for the year (may differ from monthly cost × 12 if exam fees, uniforms etc. are included)
- `campaignId: UUID?` — if registered as part of a campaign

**Financial events at this stage:**
- None. The child is registered but no money moves yet.

**Reports produced:**
- "New enrollments this month" — shows children added, by campus, by admin

---

## Stage 2 — Early Support Period

**Trigger:** Admin records one or more `EARLY_SUPPORT` ledger entries for months before a sponsor is found.

**Current system behaviour:**
- `LedgerEntry` created with `coverageType = EARLY_SUPPORT`
- Progress update may be added for the same month
- No financial movement recorded

**What is missing:**
- No debit against the central fund
- No `created_by` on the ledger entry
- No detection of whether the same month was accidentally entered twice across different admin sessions
- No validation that the amount matches the child's registered monthly education cost

**Recommended additions:**

**At ledger entry creation:**
1. `createdBy: UUID → User` — who recorded this
2. `createdAt: Instant`
3. Automatic `FundTransaction`:
   ```
   DEBIT  General Education Fund  [amount]  [month]
   Linked to: ledger_entry_id
   Created by: [admin]
   ```
4. Warning if amount differs from child's registered `educationAmount` (may be intentional, should require explicit confirmation)

**Per month, the organisation is spending:**
- PKR 2,000 × number of children on early support

This is currently completely invisible to the organisation's financial picture.

**Financial events:**
- Fund debit for each month recorded
- Running balance of general fund decreases
- "Children on early support this month" count increases

**Reports produced:**
- "Early Support expenditure this month: PKR X across Y children"
- "Fund balance after this month's early support: PKR Z"
- "Children on early support for 3+ months without a sponsor: [list]" — operational risk alert

**Business rule gap:**
There is currently no maximum period for early support. A child could theoretically be on early support for 5 years with no sponsor — draining the central fund — without any automated alert. A policy decision is needed:

> Should the system warn (or require approval) when a child has been on early support for more than N consecutive months?

---

## Stage 3 — Sponsor Committed (PENDING)

**Trigger:** Either (a) a public visitor submits a sponsorship commitment, or (b) an admin creates a sponsorship directly.

**Current system behaviour:**
- `Sponsor` record created (or used existing for admin flow)
- `Sponsorship` record created with status = `PENDING`, start month = future
- Child availability status changes to `RESERVED`
- No financial record created

**What is missing:**
- No payment expected date set on the sponsorship
- No notification sent to admin to verify payment
- No record that the sponsor was informed of payment details
- If the sponsor never pays, the sponsorship remains PENDING indefinitely
- No automated escalation

**Recommended additions:**

- `paymentDueDate: LocalDate` — calculated from start month (e.g. 5th of the start month)
- `sponsorCurrency: Currency` — the currency the sponsor intends to pay in (may differ from child's PKR cost)
- `expectedAmountInSponsorCurrency: Money?` — what the sponsor should send in their currency
- `exchangeRateAtCommitment: BigDecimal?` — GBP/PKR rate at time of commitment

**Pending sponsorship monitoring (missing entirely):**
- "Pending sponsorships with payment due in 3 days" — alert
- "Pending sponsorships overdue by 7+ days" — escalation
- "Pending sponsorships with no payment activity for 14 days" — auto-expire candidate

**Financial events at this stage:**
- None (payment not yet received)
- The fund balance should show this as a "pending inflow" — money expected but not yet received

**Reports produced:**
- "Pending sponsorships awaiting payment verification: N"
- "Expected inflows this month from committed sponsors: PKR X"

---

## Stage 4 — Payment Received and Verified

**Trigger:** Admin verifies offline bank transfer received from sponsor.

**Current system behaviour:**
- Admin clicks "Activate"
- Sponsorship status changes to `ACTIVE`
- Child availability status changes to `ALLOCATED`

**What is missing:**
- No payment record created
- No bank reference number recorded
- No amount confirmed as received
- No record of who verified it
- No audit trail connecting "money in bank" to "sponsorship activated"

**Recommended additions:**

When admin activates a sponsorship, require them to create a `PaymentRecord`:

```
PaymentRecord
├── id: UUID
├── sponsorshipId: UUID
├── childId: UUID
├── month: YearMonth
├── amountReceived: Money
├── currencyReceived: Currency
├── exchangeRateApplied: BigDecimal?
├── amountInPKR: Money
├── bankReferenceNumber: String
├── receivedDate: LocalDate
├── verifiedBy: UUID → User
├── verifiedAt: Instant
└── notes: String?
```

On creation of `PaymentRecord`, automatically:
1. Create `FundTransaction`: CREDIT General Education Fund PKR 2,000 [linked to payment record]
2. Create `LedgerEntry`: `coverageType = SPONSOR, month = start month`
3. Set sponsorship status to `ACTIVE`

This creates a **four-way link**: PaymentRecord ↔ FundTransaction ↔ LedgerEntry ↔ Sponsorship.

An auditor can trace from the bank reference number to the child's ledger entry in one query.

**Financial events:**
- General fund credited with PKR 2,000 (net zero with the child's monthly cost — the fund was debited in early support months, and now the sponsor's payment covers future months)
- Or: if the child transitions directly from AVAILABLE to SPONSORED (no early support), the sponsor payment is the first financial event

---

## Stage 5 — Ongoing Sponsorship (Monthly Cycle)

**Trigger:** Each month, the sponsor is expected to pay.

**Current system behaviour:**
- Nothing. Once ACTIVE, the sponsorship is ACTIVE forever until manually expired.
- There is no monthly cycle, no payment tracking, no ledger entry for sponsor-covered months.

This is the most significant operational gap in the current system.

**What should happen monthly:**

1. On the 1st of each month, the system should identify all ACTIVE sponsorships.
2. For each, it should create a "payment expected" record or at minimum a dashboard alert.
3. When the admin confirms payment received, create a `PaymentRecord` and `LedgerEntry` (SPONSOR coverage).
4. If no payment is received by the Nth of the month, escalate to "payment overdue" status.
5. If overdue for 2 consecutive months, flag for admin review — possible auto-expire.

**Currently:** The ACTIVE state is a permanent designation with no monthly reconciliation. A sponsor who pays for 3 months then stops remains ACTIVE. The child's ledger for months 4+ will be empty — no entries are created, no alerts raised.

**This means the child's education continuity is invisible after sponsorship activation.** The whole point of the platform (ensuring no child loses educational support) is undermined.

**Recommended additions:**

Monthly payment tracking:
- `SponsorPayment` entity (one per sponsor per month per child)
- Auto-created at month start with status `EXPECTED`
- Admin updates to `RECEIVED` when payment arrives
- System flags as `OVERDUE` after payment due date
- If `OVERDUE` for 60 days, suggest expiring the sponsorship

This closes the most critical operational loop in the entire system.

---

## Stage 6 — Sponsorship Expired or Changed

**Scenarios:**

### 6a. Sponsor Cancels (Voluntary)
- Sponsor notifies JJT they wish to stop
- Admin expires the sponsorship
- Child transitions to `AVAILABLE` (no active sponsorship)
- Child should immediately be considered for the waiting list or early support

**Missing:** No "exit reason" on the expiry. The system cannot distinguish between:
- Sponsor voluntarily cancelled (happy, completed their commitment)
- Sponsor was forced off (financial hardship)
- Sponsor was removed due to non-payment
- Sponsorship expired naturally (YEARLY commitment completed)

This distinction matters for donor retention analysis.

### 6b. Sponsor Misses Payments (Involuntary)
- Currently: no detection, sponsorship remains ACTIVE
- Recommended: auto-flag → admin review → manual expire with reason
- Child should be immediately placed on early support (protected by the central fund)

### 6c. Sponsor Changes Child
- Not currently possible — a new sponsorship must be created for the new child
- The old sponsorship must be expired
- Correct behaviour, but creates administrative friction
- Recommended: an "transfer sponsorship" workflow that expires the old and creates a new PENDING for the new child atomically

### 6d. Corporate Sponsor Funds Multiple Children
- Currently impossible — one sponsor links to one child via one sponsorship
- A corporate sponsor may want to fund 50 children under one account
- Recommended: One `CorporateSponsor` may have many `Sponsorship` records (current schema supports this — it is purely an operational workflow gap)
- Corporate needs aggregate reporting across all 50 children

### 6e. Two Sponsors Share One Child (Co-Sponsorship)
- The unique constraint (one ACTIVE or PENDING sponsorship per child) prevents this entirely
- Some organisations allow "partial sponsorships" where two sponsors each cover half a child's cost
- Whether JJT wants this is a policy decision
- If yes: the unique constraint must be changed to "one sponsor per expense category" rather than "one sponsor per child"

---

## Stage 7 — Education Completed

**Trigger:** Child completes their education at the supported level (e.g. finishes primary, finishes matric).

**Current system behaviour:**
- No such lifecycle state exists
- Child records remain `AVAILABLE` or `ALLOCATED` indefinitely
- There is no "graduation" status

**What is missing:**
- `status` on the child entity (ENROLLED, GRADUATED, WITHDRAWN, TRANSFERRED, DECEASED)
- Graduation date
- What level of education was completed
- Whether the child continues into the next phase of education
- Impact report: X years of education funded, PKR Y total spent

**Financial events:**
- All active sponsorships for this child should be expired
- A graduation ledger entry (non-financial, status only) should be created
- Sponsor should receive a "thank you and impact" notification

**Reports produced:**
- "Children graduated this year: N"
- "Total education investment in graduated children: PKR X"
- "Average sponsorship duration: Y months"

---

## Stage 8 — Child Archived

**Trigger:** Child's record is no longer actively managed (graduated, aged out, transferred, or left the programme for any reason).

**Current system behaviour:**
- No archiving capability
- The child remains in the active children list indefinitely
- The public browse page will show archived children as AVAILABLE if their sponsorship has expired

**What is missing:**
- Soft delete (archiving) with reason code
- Archived children should not appear in public listings
- Archived children should still be accessible in historical reports
- Final ledger report should be produced at archive time

---

## Lifecycle Financial Summary

| Stage | Fund Movement | Ledger Entry | Report |
|-------|--------------|-------------|--------|
| 1. Registered | None | None | New enrollments |
| 2. Early Support | Debit general fund | EARLY_SUPPORT | Monthly expenditure |
| 3. Sponsor committed | None (pending inflow) | None | Pending payments |
| 4. Payment received | Credit general fund | SPONSOR | Payment confirmation |
| 5. Monthly ongoing | Credit per payment | SPONSOR per month | Monthly reconciliation |
| 6a. Sponsor cancels | None | None | Exit analysis |
| 6b. Non-payment | None, fund absorbs | EARLY_SUPPORT | At-risk report |
| 6c. Transfer | None | None | Admin audit |
| 7. Graduated | None | Graduation note | Impact report |
| 8. Archived | None | None | Historical summary |

---

## The At-Risk Detection System

The entire purpose of JJT's platform is stated in BRD Section 4: "no mechanism to determine which children were at risk of losing their educational support."

Phase-1 solved this for the commitment stage. But a child can still fall at risk at any of these points:

| Risk Event | Currently Detectable? | Recommended Detection |
|-----------|----------------------|----------------------|
| Child on early support 3+ months | No | Count consecutive months in ledger |
| Sponsor PENDING for 14+ days | No | `payment_due_date` + elapsed time |
| ACTIVE sponsor no payment in 45 days | No | `SponsorPayment` status check |
| No ledger entry for current month | No | Monthly coverage gap report |
| General fund balance below threshold | No | Fund balance alert |
| Child approaching graduation without renewal plan | No | Status-based alert |

**Recommended "At-Risk Dashboard":**
A dedicated admin view showing:
1. Children with no ledger entry for the current month
2. Children with active sponsorship but no payment received this month
3. Children on early support for 3+ consecutive months
4. General fund balance and projected runway (months remaining)

This dashboard is the most operationally valuable feature not yet built.
