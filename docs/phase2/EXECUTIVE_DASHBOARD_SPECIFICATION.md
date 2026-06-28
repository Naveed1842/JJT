# Executive Dashboard Specification
## Junior Jinnah Trust — Phase 2 Dashboard Design

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |

---

## Principles

1. **Dashboard = decision support, not data dump.** Every metric shown should help someone make a decision or take an action. If it doesn't change behaviour, remove it.
2. **Traffic light indicators over raw numbers.** A number without context is noise. "PKR 145,000 fund balance" is meaningless; "PKR 145,000 — 3 months runway" is actionable.
3. **Each persona sees only their relevant view.** A board member does not need to see individual child records. A sponsor does not need to see organisation-wide financials.
4. **Actions from dashboards.** Where possible, the user should be able to act directly from the dashboard without navigating to another page.

---

## Dashboard 1 — JJT_ADMIN / Finance Admin (Operations View)

**URL:** `/admin/dashboard`
**Audience:** Day-to-day platform administrators, finance team

### Section 1.1 — Top-Line KPIs (always visible, top of page)

```
┌────────────────────────┬────────────────────────┬────────────────────────┬────────────────────────┐
│  GENERAL FUND          │  CHILDREN ACTIVE        │  PAYMENTS THIS MONTH   │  AT-RISK CHILDREN      │
│  PKR 145,000           │  47 / 52 enrolled       │  16 / 20 received      │  3                     │
│  ▲ 3 months runway     │  5 seeking sponsor       │  4 overdue             │  ! Needs attention     │
│  ▼ Near reserve        │  [+ Add child]           │  [View reconciliation] │  [View at-risk list]   │
└────────────────────────┴────────────────────────┴────────────────────────┴────────────────────────┘
```

Each KPI card links to the relevant detailed view. The "At-Risk Children" card is red if > 0.

### Section 1.2 — Fund Balance Detail

```
Fund Accounts                          This Month
─────────────────────────────────────────────────
General Education Fund    PKR 145,000  ↓ 8,000 (early support)
Zakat Fund                PKR  22,000  → 0 movement
Sadaqah Fund (Books)      PKR   3,500  → 0 movement
Ramadan Appeal 2026       PKR  15,000  ↑ 12,000 (8 donations)  [ACTIVE]

Available for new commitments:  PKR  89,000
Reserved (current early support):  PKR  56,000
```

### Section 1.3 — Monthly Reconciliation Snapshot

```
July 2026 Payments                     Status
─────────────────────────────────────────────
Muhammad Nadeem  → Zainab Khalid       RECEIVED   PKR 2,000  ✓
Adnan Malik      → Bilal Ahmed         OVERDUE    PKR 2,500  ⚠ 12 days
Tariq Hussain    → Fatima Sheikh       EXPECTED   PKR 2,000
Sara Ahmed       → Hassan Raza         RECEIVED   PKR 2,000  ✓
...                                               [View full reconciliation]
```

### Section 1.4 — At-Risk Children

```
Children Requiring Attention
───────────────────────────────────────────────────────────────────
Bilal Ahmed       ACTIVE sponsorship  Payment 12 days overdue    [Record payment] [Contact sponsor]
Hira Khan         ACTIVE sponsorship  Payment 45 days overdue    [Review]
Usman Tariq       Early support       4 consecutive months       [Find sponsor]
───────────────────────────────────────────────────────────────────
```

### Section 1.5 — Recent Activity Feed

```
Today
  14:32  Fatima Sheikh — Progress update added (July 2026)
  11:15  General Fund — PKR 5,000 donation recorded [admin@jjt.org]
  09:00  Monthly SponsorPayments auto-created (20 records)

Yesterday
  17:45  Sponsorship activated — Nadeem → Zainab Khalid
  16:20  Zainab Khalid — Ledger entry created (July 2026, SPONSOR)
```

### Section 1.6 — Quick Actions

```
[+ Record Payment]  [+ Add Donation]  [+ Add Child]  [+ Add Sponsor]
[Generate Monthly Report]  [Month-End Close Checklist]
```

---

## Dashboard 2 — Board Member View

**URL:** `/board/dashboard`
**Role:** `BOARD_MEMBER` (read-only, no operational controls)
**Audience:** JJT trustees and board members

### Section 2.1 — Programme Health Summary (Quarterly View)

```
Programme Health — Q2 2026 (April–June)
────────────────────────────────────────────────────────
Children Enrolled:          52
Children Actively Funded:   47  (90.4%)
Children Seeking Sponsor:    5  (9.6%)
New Enrollments This Quarter: 8
Graduations This Quarter:     2
────────────────────────────────────────────────────────
```

### Section 2.2 — Financial Position

```
Financial Position — June 2026
────────────────────────────────────────────────────────
Total Funds Held:            PKR 185,500
  General Education Fund:    PKR 145,000
  Zakat Fund:                PKR  22,000
  Sadaqah Fund:              PKR   3,500
  Ramadan Appeal:            PKR  15,000

Monthly Programme Cost:      PKR  94,000  (47 children × avg PKR 2,000)
Fund Runway (at current spend):  1.97 months

Quarterly Donations Received:  PKR 280,000
Quarterly Expenditure:         PKR 282,000
Quarterly Surplus/(Deficit):  (PKR 2,000)
────────────────────────────────────────────────────────
```

**Runway indicator:** Prominently shows how many months the current fund can sustain the programme at current run rate. Red if < 2 months. Amber if < 4 months. Green if > 4 months.

### Section 2.3 — Sponsorship Health

```
Sponsorship Portfolio
────────────────────────────────────────────────────────
Active Sponsorships:   38  (81% of enrolled children)
Pending (awaiting payment): 3
Children on Early Support:  9  (JJT-funded)
Payment Collection Rate (June): 80%  (16 of 20 payments received)
Overdue Payments Outstanding: 4 (PKR 9,500 total)
────────────────────────────────────────────────────────
```

### Section 2.4 — 12-Month Trend Chart

Line chart showing:
- Monthly donations received
- Monthly programme expenditure
- Fund balance over time

This gives the board visibility into the funding trajectory — are donations keeping pace with enrolment growth?

### Section 2.5 — Key Decisions Required

```
Items Requiring Board Attention
────────────────────────────────────────────────────────
⚠  Fund runway below 2 months (immediate fundraising action needed)
ℹ  Ramadan Appeal 2026 closes 30 June — disposition of PKR 15,000 unspent funds needed
ℹ  3 children on early support for 4+ months — sponsor acquisition needed
────────────────────────────────────────────────────────
```

---

## Dashboard 3 — Finance Admin View

**URL:** `/admin/finance`
**Role:** `FINANCE_ADMIN`
**Audience:** Finance team responsible for reconciliation and reporting

### Section 3.1 — Month-End Close Checklist

```
Month-End Close — July 2026
────────────────────────────────────────────────────────
[✓] SponsorPayments auto-created for all active sponsorships (20 records)
[ ] Reconcile received payments against bank statement
[ ] Record any unmatched bank credits
[ ] Review overdue payments (4 outstanding)
[ ] Cover coverage gaps with early support entries
[ ] Generate and save monthly report
[ ] Confirm Zakat fund balance vs Hijri year target
────────────────────────────────────────────────────────
Progress: 1/7 complete
```

### Section 3.2 — Reconciliation Workspace

Full table of all active sponsorships with:
- Sponsor name, child name
- Month
- Expected amount and currency
- Amount received (fill-in field)
- Bank reference (fill-in field)
- Date received (date picker)
- Status badge

Inline editing allows recording payments without navigating away from the reconciliation view.

### Section 3.3 — Unmatched Bank Credits

```
Bank credits without a matching payment record:

2026-07-03  PKR 2,000   REF: TXN-20260703-001   [Match to SponsorPayment] [Record as Donation]
2026-07-05  PKR 10,000  REF: TXN-20260705-002   [Record as Donation]
2026-07-08  PKR 500     REF: TXN-20260708-003   [Record as Donation]
```

This section prompts the finance admin to account for every bank transaction, preventing unrecorded income.

---

## Dashboard 4 — Sponsor Portal

**URL:** `/sponsor/portal`
**Role:** `SPONSOR`
**Audience:** Individual sponsors

### Section 4.1 — My Sponsorship(s)

```
Welcome back, Muhammad Nadeem

Your Supported Child
────────────────────────────────────────────────────────
Zainab Khalid  •  Grade 5  •  Al-Huda School, Karachi

Status:  ACTIVE ✓
Sponsorship Since:  April 2026
Total Months Covered:  4
Your Total Contribution:  PKR 8,000

Latest Progress:  June 2026
  "Zainab completed her mid-term exams with excellent results. 
   Her performance in Mathematics has improved significantly."
                                              [Read full update]
────────────────────────────────────────────────────────
```

### Section 4.2 — Payment History

```
Payment History
────────────────────────────────────────────────────────
July 2026      EXPECTED     Due: 15 July 2026
June 2026      RECEIVED     PKR 2,000   3 June 2026   [View Receipt]
May 2026       RECEIVED     PKR 2,000   2 May 2026    [View Receipt]
April 2026     RECEIVED     PKR 2,000   5 April 2026  [View Receipt]
────────────────────────────────────────────────────────
```

### Section 4.3 — Impact Summary

```
Your Impact
────────────────────────────────────────────────────────
4 months of continuous education funded
PKR 8,000 invested in Zainab's future
3 progress reports received
────────────────────────────────────────────────────────
```

### Section 4.4 — Annual Impact Report (downloadable)

Generates a PDF showing:
- Child details (name, school, grade level — no address)
- Months funded during the year
- Total contribution
- Progress highlights
- JJT programme summary

---

## Dashboard 5 — Campaign Manager View

**URL:** `/admin/campaigns/{id}/dashboard`
**Role:** `CAMPAIGN_MANAGER`

### Section 5.1 — Campaign Progress

```
Ramadan Appeal 2026
────────────────────────────────────────────────────────
Status:  ACTIVE
End Date:  30 June 2026
Days Remaining:  2

Target:     PKR 50,000
Raised:     PKR 35,000  [████████░░] 70%
Gap:        PKR 15,000

Donors:     28
Avg. Donation:  PKR 1,250
────────────────────────────────────────────────────────
```

### Section 5.2 — Recent Donations

Paginated list of donations to this campaign with donor name (or Anonymous), amount, date.

### Section 5.3 — Campaign Disposition (at close)

Shown when campaign reaches endDate or FUNDED status:
```
Campaign Closing
────────────────────────────────────────────────────────
Total Raised:    PKR 35,000
Total Spent:     PKR 20,000
Unspent Balance: PKR 15,000

Choose disposition for unspent funds:
  ○ Transfer to General Education Fund (recommended)
  ○ Refund to donors (manual process)
  ○ Roll over to next campaign
  
Justification: ___________________________
[Submit and Close Campaign]
────────────────────────────────────────────────────────
```

---

## Dashboard Design Tokens

All dashboards use the established Phase 1 design system:
- Background: `#fffdf9` (light cream)
- Primary text: `#1c352c` (dark green)
- Accent: `#2f5d4f` (forest green)
- Alert red: `#c0392b`
- Alert amber: `#d68910`
- Success green: `#27ae60`
- KPI card background: `#f5f0e8`
- Table row hover: `#eef5f2`

Badges:
- RECEIVED / ACTIVE / FUNDED: Forest green background, white text
- EXPECTED / PENDING / DRAFT: Amber background, dark text
- OVERDUE / AT_RISK / CLOSED: Red background, white text
- WAIVED / ARCHIVED: Grey background, dark text
