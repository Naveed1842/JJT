# Reporting Requirements
## Junior Jinnah Trust — Phase 2 Report Catalogue

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Reporting Principles

1. **Reports answer questions, not expose data.** Each report solves a specific decision-making need, not just "show everything."
2. **All reports are point-in-time snapshots.** A report generated today for "June 2026" will always produce the same output regardless of when it is run.
3. **Exportable.** Every report can be downloaded as CSV (for analysis) and PDF (for distribution).
4. **Accessible by role.** Each report specifies which roles can access it. Finance data is not available to SPONSOR.

---

## Report Catalogue

### R-01: Monthly Reconciliation Report

**Purpose:** Show the complete payment status for every active sponsorship in a given month.

**Audience:** Finance Admin, JJT Admin
**Frequency:** Monthly (generated after month-end close)
**Format:** Table + PDF

**Columns:**
- Sponsor Name
- Child Name
- Roll Number
- Campus
- Month
- Expected Amount (PKR)
- Expected Amount (Sponsor Currency)
- Payment Status (RECEIVED / OVERDUE / PARTIAL / WAIVED)
- Amount Received (PKR)
- Amount Received (Sponsor Currency)
- Exchange Rate Applied
- Bank Reference Number
- Date Received
- Shortfall (if PARTIAL)

**Totals:**
- Total expected
- Total received
- Total overdue
- Collection rate %
- Fund impact (net: credits − debits)

**Filters:** Year, Month, Sponsor, Status

**Trigger:** Admin generates manually after month-end close.

---

### R-02: Fund Balance Report

**Purpose:** Show the current balance of all fund accounts and the composition of recent transactions.

**Audience:** Finance Admin, JJT Admin, Board Member
**Frequency:** Real-time (generated on demand)
**Format:** Summary + detail table

**Sections:**

**Summary:**
- Fund Name | Type | Balance (PKR) | Balance (Fund Currency) | Change This Month

**Detail (per fund):**
- Date | Type | Category | Amount | Description | Created By | External Reference

**Derived metrics:**
- Available for new commitments (General Fund balance − forward-committed early support)
- Fund runway at current monthly spend rate (months)
- Minimum reserve status

---

### R-03: Donor Summary Report

**Purpose:** List all donors and their contribution history for a given period.

**Audience:** Finance Admin, JJT Admin
**Frequency:** Monthly, quarterly, annual
**Format:** Table + PDF

**Columns:**
- Donor Name (or "Anonymous")
- Donor Type (Individual, Corporate, Anonymous)
- Country
- Total Donated (period)
- Currency Breakdown
- PKR Equivalent
- Number of Donations
- Receipt Status (all issued / outstanding)
- Gift Aid Eligible? (Y/N)

**Filters:** Date range, Donor type, Fund, Country, Gift Aid status

---

### R-04: Cash Flow Report

**Purpose:** Show actual inflows and outflows for the past 3 months and forecast the next 3 months.

**Audience:** Finance Admin, Board
**Frequency:** Monthly
**Format:** Table + line chart

**Structure:**

| Month | Opening Balance | Inflows | Outflows | Closing Balance | Forecast? |
|-------|----------------|---------|---------|-----------------|-----------|
| Apr 2026 | PKR X | PKR Y | PKR Z | PKR W | No (actual) |
| May 2026 | PKR W | PKR Y | PKR Z | PKR V | No (actual) |
| Jun 2026 | PKR V | PKR Y | PKR Z | PKR U | No (actual) |
| Jul 2026 | PKR U | PKR F | PKR F | PKR T | Yes (forecast) |
| Aug 2026 | ... | | | | Yes |
| Sep 2026 | ... | | | | Yes |

**Forecast methodology:**
- Projected inflows = active sponsorships × monthly rate + recurring donations
- Projected outflows = enrolled children × average monthly cost
- Forecast confidence note: "Based on current active sponsorships and enrollments"

---

### R-05: Sponsorship Portfolio Report

**Purpose:** Overview of all sponsorships, their status, and financial performance.

**Audience:** Finance Admin, JJT Admin, Board
**Frequency:** Monthly
**Format:** Summary + detail

**Summary:**
- Total sponsorships: active, pending, expired (this year)
- Total value of active sponsorships per month (PKR)
- Breakdown by commitment type (Monthly vs Yearly)
- Breakdown by sponsor country (UK, AE, PK, other)
- Average sponsorship duration

**Detail:**
- Sponsor | Child | Commitment Type | Start Month | Status | Months Active | Total Received | Overdue Months

---

### R-06: Per-Child Financial Summary

**Purpose:** Complete financial history for a single child.

**Audience:** Finance Admin, JJT Admin
**Frequency:** On-demand
**Format:** PDF (for records, donor reporting, graduation package)

**Sections:**
1. Child Details (name, campus, enrolment date)
2. Coverage History (month-by-month: source of funding, amount)
3. Sponsorship History (each sponsor, period, total contributed)
4. Progress Update Summary
5. Totals: months funded, total investment by JJT, total contributed by sponsors

---

### R-07: At-Risk Children Report

**Purpose:** Immediate operational alert for children whose education continuity is threatened.

**Audience:** JJT Admin, Finance Admin
**Frequency:** Daily (auto-generated; alerts sent when count > 0)
**Format:** Table with action buttons (in-platform)

**Shows children where ANY of:**
- ACTIVE sponsorship with payment OVERDUE > 14 days
- ACTIVE sponsorship with payment OVERDUE for 2+ consecutive months
- No ledger entry for the current month (after the 10th)
- On early support for 4+ consecutive months
- General Fund balance below minimum reserve (organisation-level risk)

**Columns:**
- Child Name | Status | Risk Type | Days At Risk | Recommended Action | [Act]

---

### R-08: Campaign Performance Report

**Purpose:** Complete financial and engagement analysis for a campaign.

**Audience:** Campaign Manager, Finance Admin, Board
**Frequency:** At campaign close; also available during ACTIVE campaigns
**Format:** Summary + detail + PDF for public release

**Sections:**
1. Campaign Goals vs Actuals
2. Donation Timeline (chart: daily donations vs cumulative target)
3. Donor Breakdown (by type, by country, by amount bracket)
4. Fund Utilisation (how the money was spent)
5. Children Funded (list with education months covered)
6. Final Balance and Disposition

---

### R-09: Annual Financial Statement

**Purpose:** Full-year financial summary for governance, compliance, and public transparency.

**Audience:** Board, External Auditors, Public (redacted version)
**Frequency:** Annual (January for prior calendar year)
**Format:** PDF (formal document with JJT letterhead)

**Sections:**
1. **Statement of Financial Activities (SOFA)** — NGO standard format
   - Income: donations by type, campaigns, grants, gift aid
   - Expenditure: education support (by campus), fundraising costs, admin
   - Net movement in funds
   - Opening and closing fund balances

2. **Balance Sheet equivalent (Statement of Financial Position)**
   - Assets: cash held per fund account
   - Liabilities: commitments (forward early support, pending sponsorship activations)

3. **Fund Account Analysis** — each fund's inflows, outflows, and closing balance

4. **Programme Impact Summary** — children funded, education months delivered, graduations

5. **Notes to Accounts** — accounting policies, significant judgements

---

### R-10: Zakat Compliance Report

**Purpose:** Demonstrate that Zakat funds were collected and disbursed in compliance with Islamic requirements.

**Audience:** JJT Admin, Sharia Compliance Advisor, Donors
**Frequency:** Annual (at Hijri year end)
**Format:** PDF

**Sections:**
1. Total Zakat received (with Hijri year designation)
2. Beneficiary categories and eligibility basis
3. Disbursements made (by beneficiary category, by month)
4. Closing balance and status (0 = fully disbursed as required; >0 = pending action)
5. Sharia compliance declaration (to be signed by advisor)

---

### R-11: Sponsor Impact Report (Individual)

**Purpose:** Annual report sent to each sponsor showing the impact of their contribution.

**Audience:** Individual sponsors
**Frequency:** Annual (generated for each sponsor once per year)
**Format:** Personalised PDF, emailed to sponsor

**Content:**
- "Dear [Sponsor Name], thank you for supporting [Child Name] in [Year]"
- Child's photo (if consent given) and school
- Months funded by the sponsor in the year
- Total contribution
- Progress highlights for the year (excerpts from progress updates)
- JJT programme-level impact for the year
- Invitation to renew or sponsor another child

---

### R-12: Gift Aid Claim Export

**Purpose:** Generate the HMRC-required data for UK Gift Aid reclaim.

**Audience:** Finance Admin
**Frequency:** Quarterly or annual
**Format:** HMRC-compatible CSV (matching the Schedule spreadsheet format)

**Includes:**
- Donor first name, last name, full address
- Gift Aid declaration date
- Donation date, amount
- Total Gift Aid claimable (25% of donation amount)

**Only includes** donations where `isGiftAidEligible = true` and donor has a valid declaration on file.

---

## Report Access Matrix

| Report | JJT Admin | Finance Admin | Board Member | Campaign Mgr | Sponsor |
|--------|:---------:|:-------------:|:------------:|:------------:|:-------:|
| R-01: Monthly Reconciliation | ✓ | ✓ | | | |
| R-02: Fund Balance | ✓ | ✓ | ✓ (summary) | | |
| R-03: Donor Summary | ✓ | ✓ | | | |
| R-04: Cash Flow | ✓ | ✓ | ✓ | | |
| R-05: Sponsorship Portfolio | ✓ | ✓ | ✓ (summary) | | |
| R-06: Per-Child Summary | ✓ | ✓ | | | |
| R-07: At-Risk Children | ✓ | ✓ | | | |
| R-08: Campaign Performance | ✓ | ✓ | ✓ | ✓ | |
| R-09: Annual Statement | ✓ | ✓ | ✓ | | |
| R-10: Zakat Compliance | ✓ | ✓ | ✓ | | |
| R-11: Sponsor Impact | ✓ | ✓ | | | ✓ (own only) |
| R-12: Gift Aid Export | ✓ | ✓ | | | |

---

## Report Technical Requirements

| Requirement | Specification |
|-------------|--------------|
| Generation time (standard reports) | < 5 seconds for up to 1,000 sponsorships |
| Generation time (annual statement) | < 30 seconds |
| Export formats | CSV (data analysis), PDF (distribution) |
| Historical accuracy | A report run today for June 2026 returns the same data as one run on 30 June 2026 |
| Point-in-time balance | Fund balances at any past date must be queryable |
| Pagination | All tabular reports support pagination (max 200 rows per page) |
| Locale | PKR amounts formatted as "PKR 1,50,000" (Pakistani lakh/crore) or "PKR 150,000" (international) — configurable per org |
