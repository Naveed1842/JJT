# Financial Domain Model
## Junior Jinnah Trust — Phase 2 Entity Design

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Design Philosophy

This model follows three principles inherited from the Phase 1 architecture:

1. **Append-only for financial records.** No financial transaction is ever modified or deleted. Corrections are new entries.
2. **Domain entities are zero-dependency.** Every entity described here should be implementable as a pure Java record with no Spring or JPA annotations.
3. **Derivation over storage.** Balances, totals, and statuses are always calculated from the transaction log — never stored and risking staleness.

---

## Entity Map — Overview

```
Organisation
│
├── FundAccount (1..*)
│   └── FundTransaction (0..*) [append-only]
│
├── Child (1..*)
│   ├── EducationSupportLedger (1)
│   │   └── LedgerEntry (0..*) [append-only]
│   ├── ProgressUpdate (0..*)
│   └── EducationCostHistory (1..*) [effective date based]
│
├── Sponsor (1..*)
│   ├── Sponsorship (0..*)
│   │   └── SponsorPayment (0..*) [one per month when ACTIVE]
│   └── User (0..1)
│
├── Donor (0..*)
│   └── Donation (0..*)
│       └── DonorReceipt (0..1)
│
└── Campaign (0..*)
    ├── FundAccount (1) [dedicated campaign fund]
    └── CampaignDonation (0..*)
```

---

## Core Entities

### Organisation

```
Organisation
├── id:              UUID
├── name:            String           "Junior Jinnah Trust"
├── slug:            String           "jjt"
├── country:         String           "PK"
├── baseCurrency:    String           "PKR"
├── zakatYearType:   HIJRI | GREGORIAN
├── paymentDueDay:   Int              15  (day of month payment is due)
├── minFundReserve:  BigDecimal       20000  (PKR — alert threshold)
├── createdAt:       Instant
└── active:          Boolean
```

**Invariants:**
- `slug` is globally unique (used in multi-tenancy URL routing)
- `baseCurrency` cannot change after first financial transaction
- `paymentDueDay` must be between 1 and 28

---

### FundAccount

```
FundAccount
├── id:              UUID
├── organisationId:  UUID → Organisation
├── name:            String           "JJT General Education Fund"
├── type:            GENERAL | ZAKAT | SADAQAH | CAMPAIGN | EMERGENCY | GRANT | CORPORATE_CSR
├── description:     String
├── openedAt:        YearMonth
├── closedAt:        YearMonth?       null = open
├── targetAmount:    BigDecimal?      campaigns only
├── minReserve:      BigDecimal?      optional alert threshold
├── currency:        String           "PKR"
├── createdBy:       UUID → User
└── createdAt:       Instant

Derived (never stored):
├── balance:         SUM(credits) − SUM(debits) from fund_transactions
├── reservedAmount:  sum of active early-support commitments (forward-looking)
└── availableAmount: balance − reservedAmount
```

**Invariants:**
- A CLOSED fund account accepts no new transactions
- A CAMPAIGN fund must have a `targetAmount` and an associated `Campaign`
- `balance` must never go negative (enforced as a soft block with admin override)

---

### FundTransaction _(append-only)_

```
FundTransaction
├── id:              UUID
├── fundAccountId:   UUID → FundAccount
├── organisationId:  UUID → Organisation
├── type:            CREDIT | DEBIT
├── category:        DONATION | EARLY_SUPPORT | SPONSORSHIP_PAYMENT |
│                    CAMPAIGN_RECEIPT | MANUAL_CREDIT | MANUAL_DEBIT |
│                    FUND_TRANSFER | CORRECTION | GIFT_AID
├── amount:          BigDecimal       always positive
├── currency:        String
├── exchangeRateToBase: BigDecimal?   null if currency = base currency
├── amountInBaseCurrency: BigDecimal  = amount × exchangeRateToBase (or amount if same)
├── referenceDate:   LocalDate        date the money actually moved
├── month:           YearMonth?       for monthly education entries
├── childId:         UUID?            for child-specific entries
├── sponsorId:       UUID?            for sponsorship payments
├── donorId:         UUID?            for donations
├── donationId:      UUID?            FK to Donation
├── campaignId:      UUID?            for campaign-related entries
├── sponsorPaymentId: UUID?           FK to SponsorPayment
├── ledgerEntryId:   UUID?            FK to LedgerEntry
├── description:     String
├── externalReference: String?        bank reference, transaction ID
├── createdBy:       UUID → User
├── createdAt:       Instant
└── approvedBy:      UUID?            required if amount > org.largeTransactionThreshold
```

**Invariants:**
- Immutable after creation. No UPDATE or DELETE permitted.
- `amount` is always positive. The `type` (CREDIT/DEBIT) determines direction.
- Every DEBIT must have a business justification (captured in `category` + `description`)
- For transactions above threshold, `approvedBy` must be different from `createdBy`

---

### LedgerEntry _(already exists — extended)_

Phase 2 additions to existing `LedgerEntry`:

```
LedgerEntry (Phase 2 extensions)
├── [existing fields unchanged]
├── createdBy:       UUID → User      NEW — who recorded this entry
├── createdAt:       Instant          NEW
├── fundTransactionId: UUID → FundTransaction  NEW — links to the fund debit
├── sponsorPaymentId:  UUID?          NEW — links to the payment that funded this entry
├── expenseCategory: TUITION | BOOKS | UNIFORM | TRANSPORT | EXAM_FEES | MEALS | EMERGENCY | ADMIN  NEW
└── supersededBy:    UUID?            NEW — for CORRECTION entries; points to the original entry
```

---

### Donor

```
Donor
├── id:              UUID
├── organisationId:  UUID → Organisation
├── type:            INDIVIDUAL | CORPORATE | ANONYMOUS
├── displayName:     String           "The Khan Family"
├── firstName:       String?          for individuals
├── lastName:        String?          for individuals
├── email:           String?
├── phone:           String?
├── address:         String?
├── country:         String?          "GB", "AE", "PK"
├── isUkTaxpayer:    Boolean          for Gift Aid eligibility
├── giftAidDeclarationDate: LocalDate?
├── preferredCurrency: String         "GBP"
├── userId:          UUID?            if they have a self-service account
├── createdBy:       UUID → User
├── createdAt:       Instant
└── notes:           String?
```

**Invariants:**
- `ANONYMOUS` type: no personal information stored. `displayName` = "Anonymous". Email is null.
- If `isUkTaxpayer = true`, `address`, `firstName`, and `lastName` must be present for Gift Aid compliance
- `email` should be unique per organisation (soft constraint — warn on duplicate)

---

### Donation

```
Donation
├── id:              UUID
├── organisationId:  UUID → Organisation
├── donorId:         UUID → Donor
├── type:            SPONSORSHIP_PAYMENT | ONE_TIME | RECURRING_MONTHLY |
│                    CAMPAIGN | ZAKAT | SADAQAH | ANONYMOUS | GRANT |
│                    CORPORATE_CSR | GIFT_AID_RECLAIM
├── amount:          BigDecimal
├── currency:        String
├── exchangeRateApplied: BigDecimal?
├── amountInBaseCurrency: BigDecimal
├── paymentMethod:   BANK_TRANSFER | CASH | CHEQUE | ONLINE | STANDING_ORDER | DIRECT_DEBIT
├── receivedDate:    LocalDate
├── bankReferenceNumber: String?
├── fundAccountId:   UUID → FundAccount  which fund was credited
├── campaignId:      UUID?               if a campaign donation
├── sponsorPaymentId: UUID?              if a sponsorship payment
├── receiptStatus:   PENDING | ISSUED | SENT
├── receiptNumber:   String?             JJT-2026-0001
├── notes:           String?
├── isGiftAidEligible: Boolean
├── createdBy:       UUID → User
├── createdAt:       Instant
└── approvedBy:      UUID?               for large amounts
```

**Invariants:**
- `ZAKAT` type must route to the Zakat fund account — no other fund accepted
- `ANONYMOUS` type: `donorId` points to the organisation's anonymous donor placeholder record
- Receipt number is auto-generated sequentially per organisation per year: `{ORG_SLUG}-{YEAR}-{NNNN}`
- Once a receipt is issued, the donation record is immutable

---

### DonorReceipt

```
DonorReceipt
├── id:              UUID
├── donationId:      UUID → Donation
├── receiptNumber:   String           JJT-2026-0001
├── issuedAt:        Instant
├── issuedBy:        UUID → User
├── sentAt:          Instant?         null = not yet emailed
├── emailAddress:    String?          where it was sent
├── pdfStorageRef:   String?          storage path if PDF generated
└── notes:           String?
```

---

### SponsorPayment

```
SponsorPayment
├── id:              UUID
├── organisationId:  UUID → Organisation
├── sponsorshipId:   UUID → Sponsorship
├── sponsorId:       UUID → Sponsor
├── childId:         UUID → Child
├── month:           YearMonth        the month this payment covers
├── status:          EXPECTED | RECEIVED | PARTIAL | OVERDUE | WAIVED | PREPAID | CANCELLED
├── expectedAmount:  BigDecimal       the amount that should be received
├── expectedCurrency: String
├── receivedAmount:  BigDecimal?      null until received
├── receivedCurrency: String?
├── exchangeRateApplied: BigDecimal?
├── amountInBaseCurrency: BigDecimal?
├── paymentDueDate:  LocalDate        calculated from org.paymentDueDay
├── receivedDate:    LocalDate?
├── bankReferenceNumber: String?
├── donationId:      UUID?            FK to Donation record
├── fundTransactionId: UUID?          FK to the fund credit
├── ledgerEntryId:   UUID?            FK to the child's LedgerEntry
├── waivedReason:    String?          required if WAIVED
├── notes:           String?
├── createdBy:       UUID → User
├── createdAt:       Instant
├── updatedBy:       UUID?
└── updatedAt:       Instant?
```

**Invariants:**
- One `SponsorPayment` per sponsorship per month — enforced by unique constraint `(sponsorship_id, month)`
- `status` transitions are one-directional: EXPECTED → {RECEIVED, PARTIAL, OVERDUE, WAIVED, PREPAID}
- RECEIVED and WAIVED are terminal statuses
- When status = RECEIVED, `receivedAmount`, `receivedDate`, `bankReferenceNumber` are required
- When status = WAIVED, `waivedReason` is required

---

### Campaign

```
Campaign
├── id:              UUID
├── organisationId:  UUID → Organisation
├── name:            String           "Ramadan Appeal 2026"
├── description:     String
├── targetAmount:    BigDecimal
├── currency:        String
├── startDate:       LocalDate
├── endDate:         LocalDate
├── fundAccountId:   UUID → FundAccount  dedicated campaign fund
├── status:          DRAFT | ACTIVE | FUNDED | CLOSED | ARCHIVED
├── publicSlug:      String?          for public donation URL
├── imageUrl:        String?
├── childrenTargeted: Int?            how many children this campaign aims to fund
├── closedAt:        Instant?
├── closedReason:    String?
├── unspentDisposition: RETURN_TO_GENERAL | REFUND | ROLLOVER  (set at close)
├── createdBy:       UUID → User
└── createdAt:       Instant

Derived:
├── totalRaised:     SUM of all donations to this campaign's fund
├── progressPercent: (totalRaised / targetAmount) × 100
├── daysRemaining:   endDate − today
└── donorCount:      COUNT of distinct donors
```

**Invariants:**
- A DRAFT campaign cannot accept donations
- An ACTIVE campaign accepts donations until either `endDate` is reached or `status = FUNDED`
- Once CLOSED, no new donations accepted and fund disposition must be recorded
- `endDate` must be after `startDate`

---

### EducationCostHistory _(new — replaces scalar educationAmount)_

```
EducationCostHistory
├── id:              UUID
├── childId:         UUID → Child
├── amount:          BigDecimal
├── currency:        String
├── effectiveFrom:   YearMonth        this cost applies from this month
├── reason:          String?          "Annual fee increase 2026"
├── createdBy:       UUID → User
└── createdAt:       Instant
```

**Invariants:**
- No two records for the same child may have the same `effectiveFrom`
- Only the most recent record (highest `effectiveFrom` ≤ current month) is the active cost
- Creating a new record does not affect existing ledger entries

**Query:** `current cost for child` = SELECT * FROM education_cost_history WHERE child_id = ? AND effective_from <= current_month ORDER BY effective_from DESC LIMIT 1

---

### AuditEvent

```
AuditEvent
├── id:              UUID
├── organisationId:  UUID → Organisation
├── entityType:      FUND_TRANSACTION | DONATION | SPONSOR_PAYMENT | LEDGER_ENTRY |
│                    SPONSORSHIP | CHILD | SPONSOR | USER | CAMPAIGN | FUND_ACCOUNT
├── entityId:        UUID
├── action:          CREATED | STATUS_CHANGED | APPROVED | WAIVED | CANCELLED | EXPORTED
├── beforeState:     JSONB?           serialised prior state
├── afterState:      JSONB            serialised new state
├── userId:          UUID → User      who performed the action
├── userRole:        String           their role at time of action
├── ipAddress:       String?
├── userAgent:       String?
├── reason:          String?          required for status changes
├── timestamp:       Instant
└── checksum:        String           SHA-256 of (entityId + action + timestamp + userId) for tamper detection
```

**Invariants:**
- Immutable. No UPDATE or DELETE ever.
- Table is INSERT-only at the application user level (Postgres row-level permission)
- `checksum` allows an auditor to verify no records have been tampered with post-creation

---

## Entity Relationship Summary

```
Organisation (1) ──< FundAccount (many)
FundAccount (1) ──< FundTransaction (many, append-only)
FundTransaction >── LedgerEntry (optional 1:1)
FundTransaction >── SponsorPayment (optional 1:1)
FundTransaction >── Donation (optional 1:1)

Sponsor (1) ──< Sponsorship (many)
Sponsorship (1) ──< SponsorPayment (many, one per month)
SponsorPayment >── LedgerEntry (1:1 when received)
SponsorPayment >── Donation (1:1)
SponsorPayment >── FundTransaction (1:1)

Child (1) ── EducationSupportLedger (1)
EducationSupportLedger (1) ──< LedgerEntry (many, append-only)
LedgerEntry >── FundTransaction (1:1)
Child (1) ──< EducationCostHistory (many, one per effective date)

Donor (1) ──< Donation (many)
Donation (1) ──< DonorReceipt (0..1)
Donation >── FundTransaction (1:1)

Campaign (1) ── FundAccount (1, dedicated)
Campaign (1) ──< Donation (many, via campaignId)
```

---

## Key Financial Queries (Illustrative)

### Current Fund Balance
```sql
SELECT 
  SUM(CASE WHEN type = 'CREDIT' THEN amount_in_base_currency ELSE 0 END)
  - SUM(CASE WHEN type = 'DEBIT' THEN amount_in_base_currency ELSE 0 END) AS balance
FROM fund_transactions
WHERE fund_account_id = ? AND organisation_id = ?;
```

### Children At Financial Risk This Month
```sql
SELECT c.full_name, s.id as sponsorship_id, sp.status as payment_status
FROM children c
JOIN sponsorships s ON s.child_id = c.id AND s.status = 'ACTIVE'
LEFT JOIN sponsor_payments sp ON sp.sponsorship_id = s.id AND sp.month = current_month()
WHERE sp.status IN ('EXPECTED', 'OVERDUE') OR sp.id IS NULL
AND c.organisation_id = ?;
```

### Monthly Payment Collection Rate
```sql
SELECT 
  COUNT(*) as total_expected,
  SUM(CASE WHEN status = 'RECEIVED' THEN 1 ELSE 0 END) as received,
  SUM(CASE WHEN status = 'OVERDUE' THEN 1 ELSE 0 END) as overdue,
  SUM(CASE WHEN status = 'RECEIVED' THEN received_amount_in_base_currency ELSE 0 END) as total_received_pkr
FROM sponsor_payments
WHERE month = ? AND organisation_id = ?;
```
