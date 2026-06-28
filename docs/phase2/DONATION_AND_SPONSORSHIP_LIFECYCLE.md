# Donation and Sponsorship Lifecycle
## Junior Jinnah Trust — Complete Financial Journey

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Overview

This document traces the complete lifecycle of every financial relationship JJT will have — from a one-time anonymous cash donation to a multi-year corporate sponsorship. Each lifecycle section maps the financial events, the system state changes, the notifications triggered, and the edge cases that must be handled.

---

## Lifecycle 1 — Sponsorship (Core Pathway)

### 1.1 Commitment

**Pathway A: Public Visitor (Unauthenticated)**
```
Visitor browses /browse → selects child → selects commitment type (MONTHLY/YEARLY)
→ completes contact form → submits

System:
  CREATE Donor (type=INDIVIDUAL)
  CREATE Sponsorship (status=PENDING, startMonth=next_month)
  Child availability: RESERVED
  
Notifications:
  → Admin: "New sponsorship commitment from [name] for [child]"
  → Sponsor: "Thank you for your commitment. Payment instructions: [bank details]"
```

**Pathway B: Admin Creates Directly**
```
Admin selects existing Sponsor → selects Child → selects startMonth → submits

System:
  SELECT or CREATE Sponsor
  CREATE Sponsorship (status=PENDING, startMonth=selected)
  Child availability: RESERVED
  
Note: Admin is creating on behalf of someone who likely already paid.
This path should prompt: "Has payment already been received?"
If yes → immediately proceed to 1.2 (Activation).
```

### 1.2 Payment Expected and Tracking

On `startMonth - 14 days` (14 days before the commitment month):
```
System creates SponsorPayment:
  - sponsorshipId: [id]
  - month: startMonth
  - status: EXPECTED
  - expectedAmount: child.currentEducationCost
  - paymentDueDate: startMonth's 15th day

Notification → Admin: "Payment expected from [Sponsor] for [child] by [date]"
```

### 1.3 Payment Received (Activation)

Admin receives bank notification, records payment:
```
Admin: POST /api/admin/payments/receive
  - sponsorPaymentId
  - receivedAmount
  - receivedCurrency
  - bankReferenceNumber
  - receivedDate

System (atomically):
  1. UPDATE SponsorPayment: status=RECEIVED, receivedAmount, bankRef
  2. CREATE Donation (type=SPONSORSHIP_PAYMENT, linked to SponsorPayment)
  3. CREATE FundTransaction: CREDIT General Fund [amount]
  4. CREATE LedgerEntry: coverageType=SPONSOR, month=startMonth
  5. UPDATE Sponsorship: status=ACTIVE
  6. Child availability: ALLOCATED

Notifications:
  → Sponsor: "Payment received. Your sponsorship of [child] is now active."
  → Donor receipt: auto-generated (JJT-2026-NNNN)
```

### 1.4 Ongoing Monthly Cycle

On the 1st of every month (for all ACTIVE sponsorships):
```
Scheduled Job:
  For each ACTIVE Sponsorship:
    CREATE SponsorPayment(month=currentMonth, status=EXPECTED)

On the payment due day (15th, configurable):
  For each SponsorPayment(status=EXPECTED, paymentDueDate < today):
    UPDATE status=OVERDUE
    Notification → Admin: "[Sponsor] payment overdue for [child]"
```

When admin records payment for an ongoing month:
```
Same as 1.3 but:
  - LedgerEntry.month = currentMonth
  - Sponsorship remains ACTIVE
```

### 1.5 Partial Payment

```
Admin receives less than the expected amount:

System:
  UPDATE SponsorPayment: status=PARTIAL, receivedAmount=[partial], shortfall=[expected-received]
  CREATE Donation for partial amount
  CREATE FundTransaction CREDIT for partial amount
  
Options:
  A. Create LedgerEntry anyway (child is considered covered, shortfall forgiven)
  B. Do NOT create LedgerEntry (child's month is uncovered; shortfall must be covered from General Fund)
  C. Create LedgerEntry (PARTIAL coverage type — future Phase)

Policy Decision Required: Does a partial payment constitute a covered month?
Default recommendation: Yes — create the LedgerEntry as SPONSOR, record the shortfall.
```

### 1.6 Overdue Payment — Escalation Path

```
Day 1–14: Status EXPECTED
Day 15: Status OVERDUE (payment due date passed)
Day 30: Alert → Admin: "30 days overdue — [Sponsor] [child]"
Day 45: Alert → Admin: "45 days overdue — consider contacting sponsor"
Month 2 (consecutive OVERDUE): Alert → Admin: "2 consecutive months overdue — consider action"
Month 3 (consecutive OVERDUE): Escalation Flag → "Required action: Renew, Waive, or Expire"

Admin choices at escalation:
  A. Expire Sponsorship (with reason) → child returns to AVAILABLE
  B. Waive months (with reason) → LedgerEntry created from General Fund (EARLY_SUPPORT)
  C. Record extended payment (sponsor catching up)
  D. Transfer to Early Support (child continues covered from fund, sponsor relationship suspended)
```

### 1.7 Prepayment

```
Sponsor pays multiple months in advance:

System:
  For each prepaid month M:
    CREATE SponsorPayment(month=M, status=PREPAID, receivedAmount=X)
    CREATE Donation
    CREATE FundTransaction CREDIT

  When month M arrives:
    Auto-transition SponsorPayment from PREPAID to RECEIVED
    CREATE LedgerEntry(month=M, coverageType=SPONSOR)
    Notification → Sponsor: "Your prepaid month has been applied to [child]'s education"
```

### 1.8 YEARLY Sponsorship — Annual Renewal

```
30 days before end of commitment period:
  Notification → Admin: "[Sponsor] YEARLY commitment expires in 30 days for [child]"

Admin initiates renewal:
  CREATE new Sponsorship(status=PENDING, startMonth=next month after expiry)
  Existing sponsorship transitions to EXPIRED at its end month

If no renewal:
  Sponsorship expires naturally
  Child returns to AVAILABLE
  Notification → Sponsor: "Your sponsorship of [child] has completed. Thank you."
  Notification → Sponsor: "Invite to renew or sponsor a new child"
```

### 1.9 Sponsorship Cancellation (Voluntary)

```
Admin receives notification from sponsor wishing to cancel.

System:
  UPDATE Sponsorship: status=EXPIRED, expiredAt=today, exitReason=VOLUNTARY_CANCELLATION
  Cancel all future SponsorPayment records (status=CANCELLED)
  Child: AVAILABLE (once current covered month completes)
  
Notifications:
  → Sponsor: "Your sponsorship has been cancelled as requested. Thank you for your support."
  → Admin: "Sponsor [name] has cancelled. [Child] needs a new sponsor."
```

---

## Lifecycle 2 — One-Time Donation

```
Pathway A: Admin records cash/bank donation from known donor

Admin: POST /api/admin/donations
  donorId (existing or create new)
  type: ONE_TIME
  amount, currency
  paymentMethod: BANK_TRANSFER | CASH
  fundAccountId: [which fund]
  receivedDate
  bankReferenceNumber?

System:
  CREATE Donation
  CREATE FundTransaction CREDIT [specified fund]
  CREATE DonorReceipt (status=PENDING)
  
Admin can then issue receipt: POST /api/admin/donations/{id}/issue-receipt
  System: UPDATE DonorReceipt(status=ISSUED)
  Notification → Donor: "Receipt attached" (with PDF)
```

---

## Lifecycle 3 — Recurring Monthly Donation

```
Donor commits to monthly donation (e.g. £10/month via standing order):

Admin:
  CREATE Donor
  CREATE RecurringDonationSchedule:
    - donorId
    - amount, currency
    - startMonth
    - frequency: MONTHLY
    - fundAccountId
    - paymentMethod: STANDING_ORDER | DIRECT_DEBIT

System (monthly):
  CREATE expected Donation(status=EXPECTED) for each month
  On admin confirmation → CREATE FundTransaction CREDIT
  If no confirmation by 30th → OVERDUE alert
  
At 12-month mark:
  Notification → Admin: "Recurring donor [name] approaching 1-year milestone"
  Optional: send thank-you message to donor
```

---

## Lifecycle 4 — Campaign Donation

```
Donor sees campaign (e.g. "Ramadan Appeal 2026"):

Pathway A: Via public campaign page (future)
  Donor fills form → submits amount → pays online (Phase 2b)

Pathway B: Admin records offline donation response
  Admin: POST /api/admin/campaigns/{id}/donations
    donorId, amount, currency, paymentMethod

System:
  Verify campaign.status = ACTIVE
  Verify (campaign.totalRaised + amount) <= campaign.targetAmount (warn if over target)
  CREATE Donation (type=CAMPAIGN, campaignId)
  CREATE FundTransaction CREDIT [campaign fund]
  
  If campaign is now at/above target:
    UPDATE Campaign: status=FUNDED
    Notification → Admin: "Campaign target reached!"
    Notification → Campaign donors: "Campaign funded — thank you!" (if email on record)
```

---

## Lifecycle 5 — Zakat Donation

```
Zakat donations require explicit designation and go directly to the Zakat Fund.

Pathway A: Donor designates at point of giving
  Admin records donation with type=ZAKAT
  
System:
  VERIFY: there IS a Zakat fund account
  CREATE Donation (type=ZAKAT, fundAccountId=Zakat Fund)
  CREATE FundTransaction CREDIT [Zakat Fund only — no other fund]
  
  Alert if Zakat Fund not yet created:
    "Please create a Zakat Fund Account before recording Zakat donations"

When Zakat is disbursed (education payment for eligible student):
  Admin: POST /api/admin/funds/{zakatFundId}/disburse
    childId, month, amount, eligibilityBasis

System:
  VERIFY disbursement is to eligible category
  CREATE FundTransaction DEBIT [Zakat Fund]
  CREATE LedgerEntry (coverageType=ZAKAT — new type needed)
  
  Note: LedgerEntry must differentiate EARLY_SUPPORT (from General Fund) from ZAKAT coverage.
  This distinction matters for annual Zakat compliance reporting.
```

---

## Lifecycle 6 — Anonymous Donation

```
Donor gives cash with no identity provided (e.g. at a community event):

System requirement: one "Anonymous Donor" placeholder per organisation
  CREATE Donor (type=ANONYMOUS, displayName="Anonymous") — seeded at setup

Admin records:
  POST /api/admin/donations
    donorId = [anonymous_donor_id]
    amount, currency
    paymentMethod: CASH
    fundAccountId: General Fund (or Sadaqah with purpose)
    
System:
  CREATE Donation (linked to anonymous donor)
  CREATE FundTransaction CREDIT
  No receipt generated (no email address; recipient is anonymous by choice)
  
Note: Anonymous donations should still appear in totals but not in any donor-identifying report.
```

---

## Lifecycle 7 — Gift Aid Reclaim (UK)

```
Applicable only when:
  - Donor is confirmed UK taxpayer
  - Gift Aid declaration has been signed
  - Amount is the donor's own money (not collected from third parties)

Standard reclaim: HMRC adds 25p for every £1 donated.

System requirement:
  Donor record must have: firstName, lastName, fullAddress, isUkTaxpayer=true, giftAidDeclarationDate

Admin process (annually or quarterly):
  1. Export all eligible donations for the period
  2. Submit to HMRC online
  3. When reclaim is received, admin records:
     POST /api/admin/donations
       type: GIFT_AID_RECLAIM
       amount: [reclaim amount]
       paymentMethod: BANK_TRANSFER
       notes: "HMRC Gift Aid claim ref: [ref]"

System:
  CREATE Donation (type=GIFT_AID_RECLAIM)
  CREATE FundTransaction CREDIT [General Fund]
```

---

## Lifecycle 8 — Corporate Sponsorship

```
A company funds multiple children under a named CSR programme.

Setup:
  Admin creates CorporateAccount (company name, CSR contact, budget, countries, restriction)
  Admin creates Donor (type=CORPORATE, linked to CorporateAccount)
  Admin creates multiple Sponsorships (one per child) linked to the CorporateSponsor

Payment flow:
  Corporate sponsor pays a single consolidated invoice covering all their children.
  Admin records a single Donation, then allocates it across multiple SponsorPayments.
  
System:
  CREATE Donation (type=CORPORATE_CSR, amount=[total])
  CREATE FundTransaction CREDIT
  For each SponsorPayment in scope:
    UPDATE status=RECEIVED, link to Donation
    CREATE LedgerEntry(SPONSOR) per child

Reporting:
  Corporate dashboard: "You funded 10 children for the month of July 2026. Total: PKR 20,000"
  Can be sent to CSR contact for their internal reporting.
```

---

## Edge Cases and Special Scenarios

### Scenario: Double Payment Recorded

```
Admin accidentally records the same payment twice.

Detection: System should check for duplicate (sponsorPaymentId + same external reference)
           before creating the second FundTransaction.

If caught: 
  Show warning: "A payment with this bank reference already exists for this month."
  Admin must confirm to proceed or discard.

If not caught (happens after the fact):
  Admin creates a CORRECTION FundTransaction (DEBIT) to reverse the erroneous credit.
  Notes: "Correction: duplicate payment entry. Original entry [id]."
  No physical records are deleted.
```

### Scenario: Sponsor Overpays

```
Sponsor sends PKR 2,500 when the cost is PKR 2,000.

Options:
  A. Record PKR 2,000 as received, note PKR 500 as credit balance
  B. Record full PKR 2,500 as received (fund shows 500 overage)
  C. Apply PKR 500 as a general donation

Recommendation: Record the full received amount as a Donation.
Create the LedgerEntry for PKR 2,000. The PKR 500 overage is recorded as a separate ONE_TIME donation.
This keeps the ledger entries at the correct education cost while accurately recording what was received.
```

### Scenario: Fee Increase During Active Sponsorship

```
Child's school fees increase from PKR 2,000 to PKR 2,500 from September 2026.

System:
  Admin creates EducationCostHistory(childId, amount=2500, effectiveFrom=2026-09, reason="Fee increase")
  
Impact:
  All existing LedgerEntries remain at their recorded amounts (immutable)
  All existing SponsorPayments at 2,000 remain as-is until their month
  
For September 2026 onwards:
  New SponsorPayment.expectedAmount = 2,500
  Sponsor must be notified of the increase BEFORE the September payment is due

Notification → Sponsor: 
  "The monthly cost for [child] has increased to PKR 2,500 from September 2026. 
   Please adjust your payment accordingly."
```

### Scenario: Sponsor Hardship — Reduced Payment Agreement

```
Sponsor contacts JJT: "I can only afford PKR 1,500 this month."

Options:
  A. Waive the remaining PKR 500 (General Fund covers the shortfall)
  B. Carry the shortfall as a balance due (not recommended for individuals)
  C. Record PKR 1,500 as received (PARTIAL status), cover shortfall from General Fund

Recommended: Option C
  1. Record partial payment: SponsorPayment(status=PARTIAL, received=1500)
  2. Manually create EARLY_SUPPORT top-up: LedgerEntry(type=EARLY_SUPPORT, amount=500)
     OR: Record 1,500 received + waive 500 with reason "Hardship accommodation"
  3. Proceed with creating the SPONSOR LedgerEntry for the full 2,000 (covered by two fund sources)
  
Note: No child's education is disrupted regardless of what the sponsor can afford in a given month.
The platform should make it easy to cover the gap from the General Fund rather than leaving the month uncovered.
```
