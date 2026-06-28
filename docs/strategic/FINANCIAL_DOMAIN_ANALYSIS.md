# Financial Domain Analysis
## Junior Jinnah Trust — Strategic Review

| | |
|---|---|
| **Document Version** | 1.0 |
| **Classification** | Strategic — Internal |
| **Prepared** | 2026-06-28 |
| **Status** | For Review |

---

## Executive Summary

The JJT Platform Phase-1 is a **commitment tracking system**, not a **financial management system**. It records that a sponsor *committed* to paying, but it has no model of money entering the organisation, no model of the central fund balance, no concept of actual payment receipt, and no financial reporting capability.

This is appropriate for Phase-1 — the goal was to eliminate spreadsheets and create a structured audit trail. However, as JJT grows, the gap between what the system records and what actually happens financially will become an operational and governance risk.

This document identifies every financial domain concept that is either absent or insufficiently modelled in the current system, and provides a structured framework for addressing each one in future phases.

---

## 1. The Critical Gap: Commitment vs Payment

The most important gap in the current model is that **a sponsorship commitment and an actual payment are treated as the same thing**.

When an admin clicks "Activate", the system assumes payment has been verified. But the system has no record of:
- What amount was actually received
- When it was received
- Which bank account received it
- What reference number it carried
- Whether it was for the correct month
- Whether it was the full amount or partial

**Business Risk:** A sponsor could be activated and remain ACTIVE for 12 months while actually paying nothing. The system will not detect this. The only detection mechanism today is a human admin reviewing a bank statement and cross-referencing with the sponsorship list — a process that does not scale.

**Financial Impact:** As the programme grows, this gap will result in children being recorded as "Sponsored" while the trust bears the cost from its central pool without knowing it.

**Recommended Concept:** A `PaymentRecord` entity that captures each offline payment received, linked to a sponsorship and a month. Activation of a sponsorship should be based on at least one payment record being logged, not just an admin assertion.

---

## 2. Fund Sources — What Is Missing

The current system recognises exactly two funding sources:

| Source | How Modelled |
|--------|-------------|
| Sponsor (named individual) | `SPONSOR` coverage type on ledger entry |
| Organisation central pool | `EARLY_SUPPORT` coverage type on ledger entry |

Every other funding source that a growing NGO handles is **completely absent**:

### 2.1 General Donations (Unrestricted)

A member of the public donates PKR 10,000 with no specific child in mind. This money should increase the central fund and be available for early support. Currently:

- There is no `Donation` entity
- The money is invisible to the platform
- It cannot be traced from receipt to use
- No donor receipt can be generated
- The donor has no way to see how their money was used

### 2.2 Zakat

Zakat is subject to specific Sharia rules: it may only be used for specific categories of beneficiaries, it cannot be used for certain types of expense (some scholars restrict it to personal needs, not institutional costs), and it should be spent within the same Islamic year it is collected.

If JJT collects Zakat:
- It must be held in a **restricted fund**, not pooled with general donations
- It must be spent on eligible categories only
- It must be tracked separately for religious compliance reporting
- Any unspent Zakat may need to be redistributed

The current system has no concept of restricted vs unrestricted funds. All money is implicitly one pool.

### 2.3 Sadaqah

Sadaqah is less restrictive than Zakat but donors often have intent (e.g. "for education" vs "for emergency support"). This intent should be capturable and honoured. Currently unmodelled.

### 2.4 Campaigns

A Ramadan Appeal collects money against a specific goal (e.g. "fund 50 children for one year"). This money:
- Is raised under a specific campaign identity
- Should be tracked against a campaign target
- Should show a progress bar (raised vs goal)
- Should be spent on campaign-defined purposes
- Remaining balances must be returned or redeployed with donor consent

The current system has no `Campaign` entity.

### 2.5 Corporate CSR

A company sponsors 10 children under a CSR programme. This is structurally different from individual sponsorship:
- One organisation may fund many children
- The corporate account manager is different from the contact for each child
- Corporate payments often come in bulk (quarterly, annually)
- Corporate donors need aggregate reporting for their own CSR documentation
- Tax receipts are often required at a corporate level

The current `Sponsor` entity is modelled as an individual. It has no concept of a corporate account with multiple child relationships under one billing identity.

### 2.6 Grants

External grants (government, foundation, international) are time-bounded, purpose-restricted, and subject to reporting requirements. A grant of PKR 500,000 "for education in Balochistan" must:
- Be held in a restricted fund
- Only be spent on the stated purpose
- Be reported against at the end of the grant period
- Be returned if conditions are not met

Entirely absent from the current model.

### 2.7 Anonymous Donations

Someone drops PKR 500 in a charity box or makes an anonymous online donation. This money:
- Should still increase the central fund
- Should be categorised (anonymous individual, anonymous corporate, etc.)
- Cannot be attributed to a donor for receipt purposes
- Still needs to appear in financial reports

### 2.8 One-Time vs Recurring

The current commitment type (`MONTHLY` / `YEARLY`) models the commitment horizon, not the payment frequency. These are different concepts:
- A YEARLY commitment sponsor might pay monthly via bank standing order
- A MONTHLY commitment sponsor might pay a full year in advance
- A one-time donor contributes once with no ongoing obligation

The payment mechanism is not captured. This means the platform cannot detect when a "monthly" sponsor has not paid for three months.

---

## 3. The Central Fund — Invisible Money

The `EARLY_SUPPORT` coverage type acknowledges that the organisation pays from a central pool. But this pool has no representation in the system. There is no:

- Central fund balance
- Record of how the central fund is replenished
- Record of how much has been drawn from it
- Alert when the central fund is insufficient to cover next month's early support

**Operational Implication:** An admin could add 20 new children on early support this month, drawing PKR 40,000 from the central fund, without knowing whether the central fund has PKR 40,000 in it.

**Recommended Concept:** A `FundAccount` with a running balance, debit/credit entries, and a category (`CENTRAL_POOL`, `ZAKAT_FUND`, `CAMPAIGN_FUND`, etc.). Every `EARLY_SUPPORT` ledger entry should debit the central fund. Every donation/campaign receipt should credit it.

This is the core of **fund accounting** — the accounting model appropriate for NGOs and charities.

---

## 4. Expense Categories — Everything is "Education Amount"

Every ledger entry records a single `educationAmount` in a single currency. The current model treats education support as a single undifferentiated expense. In reality, a child's educational support may include:

| Category | Example Monthly Cost |
|----------|-------------------|
| Tuition fee | PKR 1,500 |
| Books and stationery | PKR 200 |
| Uniform | PKR 150 (one-off) |
| Transport | PKR 500 |
| Exam fees | PKR 300 (quarterly) |
| Meals | PKR 600 |
| Emergency assistance | Variable |

**Why This Matters:**
1. A sponsor who is paying for "education" expects to know what their money covers
2. A corporate sponsor with a CSR focus on "books" cannot restrict their donation to books in the current model
3. Budget analysis is impossible without expense categories (cannot determine whether tuition costs are rising faster than donations)
4. Grant reporting often requires expense breakdown by category

**Recommended Concept:** An `ExpenseCategory` enum or lookup table attached to ledger entries. The total amount is still the same; the breakdown is an optional but increasingly important attribute.

---

## 5. The Payment Verification Problem

Currently: Admin verifies bank transfer manually → clicks Activate → sponsorship becomes ACTIVE.

This single human action conflates:
1. Receipt of payment
2. Verification that the payment is for the correct child
3. Verification that the payment is for the correct amount
4. Recording of the bank reference number
5. Updating the sponsorship status

If this process is not performed for one sponsor, that sponsor remains PENDING indefinitely. There is no:
- Automated reminder to the admin that a payment is overdue
- Record of when payment was expected
- Reconciliation report showing expected vs received payments

**Recommended Additions:**
- `payment_due_date` on sponsorship records
- `PaymentRecord` entity (date, amount, reference, bank, linked month)
- A "pending verification" report showing all PENDING sponsorships older than 7 days
- A "payment gap" report showing ACTIVE sponsorships with no payment in the current month

---

## 6. Currency Risk

The platform supports multiple currencies (PKR, USD, GBP, AED) on ledger entries. But:

- An overseas sponsor pays in GBP
- The ledger records PKR 2,000 for the child's monthly cost
- The exchange rate used for the conversion is not recorded
- If the GBP weakens, the PKR 2,000 might cost the sponsor more than they expected
- There is no mechanism to notify the sponsor of currency changes
- Historical ledger entries in PKR cannot be retrospectively valued in GBP

**For international operations this is a significant gap.** Donors in the UK need to know what they are contributing in GBP. The organisation needs to know its exposure if PKR devalues.

**Recommended Concept:**
- Store the `sponsorCurrency` and `exchangeRate` on payment records (not on the child's ledger entry, which should remain in the child's local currency)
- Record the GBP/PKR rate at the time of each payment

---

## 7. Donor Receipts and Tax Compliance

JJT's sponsors include individuals in Pakistan, the UK, UAE, and potentially other jurisdictions. Many sponsors want:

- A receipt confirming their donation for their own records
- A tax-deductible receipt if JJT is registered as a charity in their jurisdiction (e.g. HMRC Gift Aid in the UK, IRS 501(c)(3) for US donors)

The current system has no capability to:
- Generate a donation receipt
- Record that a receipt was issued
- Produce an annual donation summary for a sponsor
- Track Gift Aid eligibility for UK donors

**Financial Impact:** For UK donors, Gift Aid allows JJT to reclaim 25% of each donation from HMRC. On PKR 2,000/month (≈ GBP 6), that's GBP 1.50 per month per UK donor — small individually but significant at scale. Gift Aid requires a declaration per donor, which is a data element not captured anywhere in the current system.

---

## 8. Audit Trail — Designed But Not Implemented

The `docs/audit.md` file lists requirements for an audit trail. The current implementation does not meet them.

**What is documented as required:**
- Child creation and updates must be auditable
- Ledger entry creation must be auditable
- Progress update creation must be auditable
- Sponsorship creation must be auditable
- Access to sponsor views must be auditable
- All events must be attributable to a human actor

**What exists in the database:**
- `ledger_entries` — no `created_by`, no `created_at` (ROADMAP V13 adds these in a planned migration)
- `sponsorships` — has `created_at` but no `created_by`
- `children` — no `created_by`, no `created_at`
- `progress_updates` — no `created_by`, no `created_at`

Every financial record in the system is **currently unattributed**. An auditor asking "who created this early support entry and when?" cannot get an answer from the database.

**Regulatory Risk:** For any form of charity registration, regulatory oversight, or external audit, the absence of `created_by` on financial records is a compliance gap. It is not a matter of convenience — it is the minimum requirement for an auditable financial system.

---

## 9. Ledger Correction — An Unresolved Problem

Open Question OQ-02 in the BRD acknowledges the problem: what happens when a ledger entry has the wrong amount or currency?

The append-only constraint is correct for auditability. But the current system provides no correction mechanism at all. The options are:

| Option | Pros | Cons |
|--------|------|------|
| Allow admin delete with audit log | Simple to implement | Breaks immutability guarantee; removes historical truth |
| Add `CORRECTION` entry type | Preserves history; additive approach | Requires new domain logic; how does UI show net position? |
| Void + restate (like accounting journals) | Industry standard; fully auditable | More complex; needs debit/credit concepts |
| Soft-delete with reason | Balance between simplicity and auditability | Still modifies history |

**Recommended:** A `CORRECTION` coverage type with a mandatory reference to the entry being corrected. The corrected entry is not deleted but is marked as superseded. The net position is always calculated from active (non-superseded) entries.

This is the accounting approach — every correction is itself a new financial event.

---

## 10. Summary Gap Table

| Domain Concept | Current State | Gap Severity | Phase |
|---------------|--------------|-------------|-------|
| Central fund balance | Absent | Critical | Phase 2 |
| Payment receipt recording | Absent | Critical | Phase 2 |
| `created_by` on all financial records | Absent | Critical | Phase 2 |
| Donor/general donation entity | Absent | High | Phase 2 |
| Payment reconciliation report | Absent | High | Phase 2 |
| Expense categories | Absent | High | Phase 3 |
| Zakat fund (restricted) | Absent | High | Phase 3 |
| Campaign management | Absent | High | Phase 3 |
| Ledger correction mechanism | Absent | Medium | Phase 2 |
| Currency/exchange rate on payments | Absent | Medium | Phase 3 |
| Donor receipts / tax | Absent | Medium | Phase 3 |
| Co-sponsorship (shared child) | Absent | Medium | Phase 3 |
| Corporate sponsor account | Absent | Medium | Phase 3 |
| Annual budget per child | Absent | Low | Phase 3 |
| Gift Aid tracking (UK donors) | Absent | Low | Phase 4 |
| Bank statement import/reconciliation | Absent | Low | Phase 4 |
