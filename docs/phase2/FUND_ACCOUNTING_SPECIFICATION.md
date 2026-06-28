# Fund Accounting Specification
## Junior Jinnah Trust — Phase 2 Complete Fund Design

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## 1. Why Fund Accounting (Not Commercial Accounting)

Commercial accounting tracks a single pool of capital and asks: "Did we make money?"

Charity fund accounting tracks multiple segregated pools and asks: "Did we spend donor money as they intended?"

A JJT donor who gives "for Zakat" is not giving the organisation a general asset. They are giving a restricted trust. If that money is spent on administrative costs, JJT has breached a fiduciary obligation — even if the organisation is financially healthy overall.

**This is not optional.** For any charity seeking formal registration (SECP Pakistan, or UK Charity Commission), fund accounting is a legal requirement. The platform must support it from Phase 2.

---

## 2. Fund Account Types

### 2.1 General Education Fund (Unrestricted)

**ACCOUNT_TYPE:** `GENERAL`

**Purpose:** The operational backbone. Covers all EARLY_SUPPORT months, bridges funding gaps, funds children awaiting a sponsor.

**Inflow Sources:**
- Unrestricted cash donations (individuals, collected at events, community drives)
- Bank transfers with no stated restriction
- General Sadaqah without specific purpose stated by donor
- Unspent campaign funds transferred after campaign close (with board approval)
- Gift Aid reclaims from HMRC (UK donors)
- Grants with broad education purpose

**Outflow Purposes:**
- `EARLY_SUPPORT` ledger entries — debited per child per month
- Coverage gap months when a sponsor's payment is waived
- Administrative overhead (Phase 4 — not Phase 2)

**Balance Rules:**
- Must not go negative (hard constraint — requires two-admin override to proceed)
- Should not fall below `org.minFundReserve` (soft alert at 80%, hard alert at threshold)
- "Available balance" = Total credits − Total debits − Forward-committed early support

**Typical Starting Balance:** Whatever cash JJT currently holds that is not restricted. The admin sets this by recording a `MANUAL_CREDIT` when the fund is first created, with a note "Opening balance — verified by [name] on [date]."

---

### 2.2 Zakat Fund (Restricted — Religious)

**ACCOUNT_TYPE:** `ZAKAT`

**Purpose:** Receives and disburses Zakat contributions in full compliance with Islamic obligation.

**Inflow Sources:**
- Donations explicitly designated as Zakat at time of giving
- Transferred Zakat from a parent organisation (if applicable)

**Outflow Purposes — What is Permissible:**

Zakat must be given to one of the eight eligible categories (asnaf) defined in the Quran (9:60). For JJT's education programme, the most relevant categories are:

- **Al-Fuqara (the poor):** Direct educational assistance to impoverished students
- **Al-Masaqeen (the destitute):** More severe poverty cases
- **Fii-sabilillah (in the way of God):** Education is broadly accepted under this category in modern Sharia scholarship

**What is NOT Permissible:**
- Administrative salaries (unless the recipient is itself a Zakat recipient)
- Building/infrastructure costs
- Loans or financial instruments
- Non-Muslim beneficiaries (disputed — requires specific Sharia ruling for JJT's context)

**Time Restriction:**
- Zakat collected must be disbursed within the same Islamic year (Hijri) it was collected
- Unspent balance at Hijri year-end: Flag for Sharia review. Cannot be carried forward indefinitely.

**Technical Rules:**
- `ZAKAT` fund has a strict no-transfer-out policy in the platform
- Any attempted transfer to General Fund is blocked with message: "Zakat funds cannot be transferred. Contact your Sharia compliance advisor."
- Annual Zakat report: Total collected, total disbursed, disbursement beneficiaries, balance, Hijri year

**NOTE:** Before the Zakat fund goes live in the UI, a formal Sharia compliance review must be completed to confirm:
1. The beneficiaries (enrolled children) meet eligibility criteria
2. The expense categories (tuition, books) are valid Zakat uses in JJT's specific context
3. The Hijri year calculation method is agreed

---

### 2.3 Sadaqah Fund (Purpose-Restricted)

**ACCOUNT_TYPE:** `SADAQAH`

**Purpose:** Receives Sadaqah (voluntary charity) donations where the donor has specified a use narrower than the general fund.

**Why Not Just Use the General Fund?**

If a donor gives "for books and stationery," they expect that money to buy books — not to be pooled with money used for school fees. The Sadaqah fund honours the donor's specific intent.

**Sadaqah Purpose Categories:**
- BOOKS_AND_STATIONERY
- UNIFORM
- TRANSPORT
- EXAM_FEES
- MEALS
- EMERGENCY_ASSISTANCE
- GENERAL_EDUCATION (can go to general fund)

**Inflow Sources:**
- Sadaqah donations where donor specifies a purpose

**Outflow Rules:**
- Spending must match the donor's stated purpose
- If the purpose cannot be matched (e.g. no transport costs this month), the balance carries forward
- At year-end, unspent balance: admin reviews and either returns to donor or confirms ongoing need

**Technical Notes:**
- Each Sadaqah donation should capture the donor's stated purpose
- Multiple Sadaqah purposes can co-exist in one account (tracked by category on `FundTransaction`)
- Or: each purpose gets its own Fund Account. Simpler operationally.

**Recommendation:** Use one Sadaqah fund account with purpose-category tagging on transactions. Adds one column, avoids creating 8 separate fund accounts.

---

### 2.4 Campaign Fund (Purpose-Restricted, Time-Limited)

**ACCOUNT_TYPE:** `CAMPAIGN`

**Purpose:** Receives all donations in response to a specific fundraising appeal, held separately until campaign objectives are achieved.

**Campaign Examples:**
- "Ramadan Appeal 2026 — Fund 10 Children's Education for 3 Months"
- "Back to School 2026 — Provide 50 Uniform Kits"
- "Emergency Winter Appeal 2026"

**Campaign Lifecycle:**

```
DRAFT → ACTIVE → FUNDED → CLOSED → ARCHIVED

DRAFT:   Created but not yet public. Accepts no donations.
ACTIVE:  Public. Accepts donations up to target.
FUNDED:  Target reached. Closed to new donations.
CLOSED:  End date reached or admin closes. Final accounting performed.
ARCHIVED: Historical record only.
```

**Funding Rules:**
- `ACTIVE` campaigns accept donations until `targetAmount` is reached or `endDate` passes
- If target is reached early, status transitions to `FUNDED`
- Admin can re-open a `FUNDED` campaign with justification (e.g. "stretch goal")
- `CLOSED` campaigns cannot receive donations

**Unspent Funds at Close:**

| Scenario | Disposition Options |
|---------|-------------------|
| Campaign fully funded and spent | Close with zero balance |
| Underfunded campaign (less than target) | Refund donors, OR apply to nearest matching purpose with donor consent |
| Overfunded (rare, if target raised mid-period) | Return excess to donors, OR apply to General Fund with public transparency note |
| Campaign cancelled | Refund all donations |

**The platform must record which disposition was chosen and by whom.**

---

### 2.5 Emergency Assistance Fund (Purpose-Restricted)

**ACCOUNT_TYPE:** `EMERGENCY`

**Purpose:** Non-education emergency support for enrolled children's families.

**Covers:**
- Medical emergencies
- Natural disaster assistance
- Family crisis support

**Does NOT cover:**
- Routine education expenses (these go through the education ledger)
- Administrative expenses

**Key Design Distinction:**

Emergency disbursements are NOT `LedgerEntry` records. They are separate `DisbursementRecord` entities (Phase 3). This keeps the education ledger pure: every entry in it represents an education month covered.

---

### 2.6 Grant Fund (Restricted — Purpose and/or Time)

**ACCOUNT_TYPE:** `GRANT`

**Purpose:** Receives grant funding from governments, foundations, or corporate CSR programmes.

**Differs from Campaign funds because:**
- Grantor specifies both purpose AND time boundaries (e.g. "must be spent within the 2026 financial year")
- Unspent grants may need to be returned to the grantor (unlike campaign funds)
- Grant reporting requirements may differ from donor reporting

**Each grant should have:**
- Grantor name
- Grant reference number
- Grant amount
- Eligible expense categories
- Start and end dates
- Reporting requirements
- Whether unspent funds must be returned

---

### 2.7 Corporate CSR Fund (Lightly Restricted)

**ACCOUNT_TYPE:** `CORPORATE_CSR`

**Purpose:** Receives donations from corporate sponsors under CSR programmes.

**Why Separate from General Fund?**
- Corporate donors typically require segregated reporting for their own CSR compliance
- JJT may need to provide impact reports broken down by corporate donor
- Some corporate programmes specify a geographic or demographic restriction

**Inflow:** Corporate donations from `CorporateAccount` donors.

**Outflow:** Education expenses for children covered by the corporate programme.

---

## 3. Fund Transfer Rules

| From Fund | To Fund | Permitted | Conditions |
|-----------|---------|-----------|-----------|
| General | Zakat | Never | Cannot retrospectively classify as Zakat |
| Zakat | General | Never | Sharia restriction |
| Zakat | Sadaqah | Never | Sharia restriction |
| General | Campaign | Never | Campaign money must be donated at source |
| Campaign | General | Yes, conditional | Only unspent after close, with board approval |
| Sadaqah (purpose met) | General | Yes, conditional | Only after purpose is fulfilled |
| Emergency | General | Yes, conditional | With governance approval |
| Grant | General | Never | Must be returned to grantor if unspent |

**Implementation:** All transfers require:
1. A recorded reason
2. The ID of the approving admin (`approvedBy`)
3. A `FUND_TRANSFER` category `FundTransaction` — DEBIT on source, CREDIT on destination

---

## 4. Fund Balance Calculation

**The golden rule: Fund balance is NEVER stored. It is always calculated.**

```
Balance at any moment T = 
  SUM(ft.amount WHERE type=CREDIT AND created_at <= T)
  - SUM(ft.amount WHERE type=DEBIT AND created_at <= T)
```

**Why this matters:**
- You can reconstruct the balance at any historical date (vital for audits)
- No risk of balance going out of sync with the transaction log
- Corrections are new transactions, not modifications

**Performance consideration:** For high-volume funds, a materialised view or periodic balance snapshot can be used to avoid full table scans. The snapshot is advisory — the true balance is always the sum.

---

## 5. Fund Account Opening Process

When a new fund account is created:

1. Admin creates fund account record (name, type, currency, description)
2. Admin optionally sets `minReserve` alert threshold
3. If the account has an opening balance (e.g. cash already held), admin records a `MANUAL_CREDIT` with:
   - Amount
   - Date
   - Description: "Opening balance — [verified by, date, source]"
   - External reference (e.g. bank statement ref)
4. Fund is now active and accepting transactions

**This process creates a complete audit trail from day one.** Even the opening balance has a `created_by` and timestamp.

---

## 6. Month-End Close Process

At the end of each month, the Finance Admin should:

1. **Review all EXPECTED payments** — confirm received, mark overdue as appropriate
2. **Reconcile fund balances** against bank statement
3. **Record any unmatched credits** in the bank statement as general donations
4. **Review children with coverage gaps** — add EARLY_SUPPORT entries from General Fund
5. **Generate monthly report** and save to file
6. **Confirm Zakat balance** is on track for the Hijri year

This process is currently entirely manual (not on the platform). Phase 2 should provide a guided "month-end close" workflow in the admin panel.

---

## 7. Annual Close and Audit Preparation

At year end:

1. **Generate annual financial statement** — all funds, all transactions, all balances
2. **Reconcile Zakat fund** — confirm disbursements match collection, address surplus per Sharia guidance
3. **Close or archive inactive campaign funds**
4. **Review Sadaqah fund purposes** — address any unspent purposes
5. **Generate donor receipts** for the tax year (for UK Gift Aid donors)
6. **Produce tamper-evident export** — all transactions as signed JSON, stored offline
7. **Board review and approval** of annual accounts

The platform should support steps 1, 3, 5, and 6 directly. Steps 2, 4, and 7 are governance processes that the platform supports but does not automate.
