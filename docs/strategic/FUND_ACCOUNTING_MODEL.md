# Fund Accounting Model
## Junior Jinnah Trust — Strategic Design

| | |
|---|---|
| **Document Version** | 1.0 |
| **Classification** | Strategic — Internal |
| **Prepared** | 2026-06-28 |
| **Status** | For Review |

---

## Why NGOs Use Fund Accounting (Not Commercial Accounting)

A commercial business tracks profit and loss against a single pool of capital. An NGO tracks **stewardship** — how money entrusted to it by donors is used in accordance with their stated intent.

**Fund accounting** segregates money into named funds based on donor intent and restriction:

| Fund Type | Definition | Example |
|-----------|-----------|---------|
| **Unrestricted** | Donor has placed no conditions on use | General donation, JJT discretion |
| **Restricted — Purpose** | Donor specifies the use | "For education in Balochistan only" |
| **Restricted — Time** | Money must be used within a period | Ramadan collection spent within Shawwal |
| **Restricted — Religious** | Sharia requirements (Zakat) | Cannot cross eligibility boundaries |
| **Endowment** | Principal preserved; income spent | Not applicable at current JJT scale |

The JJT Platform currently has zero fund accounting. All money is one implicit pool. This is the single largest financial domain gap.

---

## Proposed Fund Structure

### Fund 1: JJT General Education Fund (Unrestricted)

**Purpose:** Covers all EARLY_SUPPORT entries; bridges months when a child has no sponsor.

**Inflows:**
- Unrestricted donations (online, cash, bank transfer)
- General Sadaqah
- Unspent campaign balances returned to general fund
- Gift Aid reclaims (once implemented)
- Admin corrections (positive adjustments)

**Outflows:**
- Every `EARLY_SUPPORT` ledger entry debits this fund by the child's monthly education cost
- Administrative overhead apportionment (Phase 4)

**Invariants:**
- Balance must be ≥ 0 at all times (the fund cannot go negative — if it does, it is a governance emergency)
- If balance falls below `min_reserve` (configurable), an alert should be raised
- Admin should be warned before recording early support that would overdraw the fund

**Derived Metrics:**
- Available balance = Total inflows − Total outflows to date
- Reserved balance = Sum of all children on EARLY_SUPPORT × months of commitment (forward-looking)
- Available for new commitments = Available balance − Reserved balance

---

### Fund 2: Zakat Fund (Restricted — Religious)

**Purpose:** Holds Zakat contributions. Must be spent on Zakat-eligible expenses within the Islamic year.

**Inflows:**
- Zakat donations (flagged at point of collection as `ZAKAT`)

**Outflows:**
- Only to Zakat-eligible categories: personal education costs for eligible students, not institutional costs
- Must be spent within the same Islamic year (Hijri calendar tracking)
- Unspent balance at year-end is a governance issue requiring Sharia scholar guidance

**Invariants:**
- Cannot be pooled with the General Education Fund
- Cannot fund administrative costs
- Cannot be transferred to other funds without Sharia compliance sign-off
- A Zakat fund report must be available for annual review

**Key Design Decision:** Zakat funds should be tagged at the point of donation, not retrospectively. Once a donation enters the General Fund, it cannot be reclassified as Zakat.

---

### Fund 3: Sadaqah Fund (Lightly Restricted)

**Purpose:** Holds Sadaqah contributions where the donor has specified a purpose narrower than the general fund but less restrictive than Zakat.

**Examples:**
- "For books and stationery"
- "For transport costs"
- "For emergency assistance"

**Inflows:**
- Sadaqah donations with stated purpose

**Outflows:**
- Only against the donor's stated purpose
- Unspent balance at year-end: carry forward with donor consent or return

**Invariants:**
- Purpose must be recorded at donation time
- Spending against Sadaqah fund must match the purpose category

---

### Fund 4: Campaign Fund (Purpose-Restricted, Time-Limited)

**Purpose:** Receives campaign-specific donations. Exists only for the duration of the campaign.

**Examples:**
- Ramadan Appeal 2026
- Back to School 2026
- Winter Support 2026

**Inflows:**
- Donations explicitly made in response to the campaign

**Outflows:**
- Campaign-defined expenses (e.g. "fund 3 months of education for 10 children")

**Lifecycle:**
1. Campaign opens → Fund created with target amount and end date
2. Donations received → Fund balance increases
3. Campaign closes → Funds allocated to campaign purpose
4. Remaining balance → Return to donors, apply with consent to General Fund, or carry forward

**Invariants:**
- Campaign fund cannot exceed its target (prevents overcollection without donor consent)
- Campaign fund closes at campaign end date even if underfunded
- Underfunded campaign: either refund donors or apply to nearest matching purpose

---

### Fund 5: Emergency Assistance Fund (Restricted — Purpose)

**Purpose:** Holds donations designated for non-education emergencies affecting enrolled children.

**Examples:**
- Medical emergency
- Family crisis support
- Disaster relief for enrolled families

**Inflows:**
- Emergency appeal donations

**Outflows:**
- Emergency disbursements (not ledger entries — these are not education expenses)

**Design Note:** Emergency expenses should NOT appear in the Education Support Ledger. They require a separate `DisbursementRecord` entity.

---

## The Fund Accounting Entity Model

```
FundAccount
├── id: UUID
├── name: String
├── type: UNRESTRICTED | ZAKAT | SADAQAH | CAMPAIGN | EMERGENCY
├── description: String
├── openedAt: YearMonth
├── closedAt: YearMonth? (null = open)
├── targetAmount: Money? (campaigns)
├── minReserve: Money? (alerts when balance approaches)
└── currency: Currency (PKR by default)

FundTransaction (append-only, like ledger entries)
├── id: UUID
├── fundAccountId: UUID → FundAccount
├── type: CREDIT | DEBIT
├── category: DONATION | EARLY_SUPPORT | SPONSORSHIP_PAYMENT | CAMPAIGN_RECEIPT | CORRECTION
├── amount: Money
├── referenceDate: LocalDate
├── month: YearMonth? (for education-related debits)
├── childId: UUID? (for child-specific debits)
├── donorId: UUID? (for donation credits)
├── campaignId: UUID? (for campaign-related transactions)
├── description: String
├── createdBy: UUID → User
├── createdAt: Instant
└── externalReference: String? (bank reference, transaction ID)
```

**Derived Fund Balance** = `SUM(amount WHERE type=CREDIT) − SUM(amount WHERE type=DEBIT)` for a given fund account.

This is never stored — always calculated, following the same append-only principle as the child ledger.

---

## How EARLY_SUPPORT Maps to Fund Accounting

Currently: Admin creates `EARLY_SUPPORT` ledger entry. This is the end of the financial story.

**Proposed:** Creating an `EARLY_SUPPORT` ledger entry should **automatically** create a `FundTransaction`:

```
DEBIT  General Education Fund  PKR 2,000  2026-06  [child: Zainab Khalid]  [created_by: admin@jjt.org]
```

This debit is:
- Linked to the ledger entry (referential integrity)
- Attributed to the admin who created it
- Timestamped
- Reversible only via a compensating credit transaction (not by deletion)

The general fund balance decreases by PKR 2,000. The admin can see in real time whether the fund can sustain more early support entries.

---

## How Sponsorship Payments Map to Fund Accounting

When a sponsor's payment is received and verified:

**Option A (current):** Admin clicks Activate. No financial record created.

**Proposed Option B:**
1. Admin creates `PaymentRecord`:
   - Sponsor: MUHAMMAD NADEEM
   - Amount: PKR 2,000
   - Bank reference: TXN-20260701-001
   - Date received: 2026-07-03
   - Period: 2026-07
2. System creates `FundTransaction`:
   ```
   CREDIT  General Education Fund  PKR 2,000  2026-07  [sponsor: NADEEM]  [payment_ref: TXN-20260701-001]
   ```
3. System creates `LedgerEntry` for the child:
   ```
   SPONSOR  2026-07  PKR 2,000  [sponsorship: uuid]
   ```
4. Sponsorship status remains ACTIVE (payment confirmed for this month)

This creates a **complete audit chain**: from money received in the bank → fund credited → child's ledger updated.

---

## Funding Gap Detection

With fund accounting, the following become calculable automatically:

### Monthly Coverage Report (per child)
```
Child: Fatima Malik
Month: 2026-07
Education Cost: PKR 2,000
Sponsorship: ACTIVE (NADEEM) → Expected payment: PKR 2,000
Payment Received: PKR 2,000 (2026-07-03)
Ledger Status: COVERED
```

```
Child: Bilal Ahmed
Month: 2026-07
Education Cost: PKR 2,000
Sponsorship: ACTIVE (ADIL) → Expected payment: PKR 2,000
Payment Received: PKR 0
Ledger Status: AT_RISK — payment overdue
```

### Organization-Level Dashboard
```
General Education Fund Balance:     PKR 145,000
Reserved (early support running):   PKR  48,000 (24 children × 2 months avg)
Available for new commitments:      PKR  97,000
Expected payments this month:       PKR  40,000 (20 active sponsors)
Received payments this month:       PKR  32,000 (16 of 20)
Payment gaps this month:            PKR   8,000 (4 sponsors, see report)
```

None of these figures are currently calculable from the JJT Platform.

---

## Fund Transfer Rules

| Transfer | Permitted | Conditions |
|----------|----------|-----------|
| Zakat → General Fund | No | Sharia restriction |
| General Fund → Zakat | No | Cannot reclassify |
| Campaign Fund → General Fund | Conditional | Only unspent after campaign close, with donor consent |
| Emergency Fund → General Fund | Conditional | With governance approval |
| General Fund → Campaign Fund | No | Donations must be tagged at source |
| Sadaqah → General Fund | Conditional | Only if purpose is met |

---

## Minimum Viable Fund Accounting (Phase 2 Priority)

If full fund accounting is too complex for Phase 2, the minimum viable implementation is:

1. **Add `FundTransaction` table** (append-only) with DEBIT/CREDIT, amount, date, child reference, created_by
2. **Create one `FundAccount`** ("JJT General Education Fund") with starting balance set by admin
3. **Auto-debit the fund** when any `EARLY_SUPPORT` entry is created
4. **Show fund balance** on the admin dashboard
5. **Warn admin** if recording early support would reduce balance below PKR 20,000

This is the minimum that prevents the organisation from unknowingly running out of money.
