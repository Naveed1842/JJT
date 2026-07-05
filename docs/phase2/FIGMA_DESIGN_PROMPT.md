# Figma Design Brief — JJT Platform Phase 2
## Junior Jinnah Trust — Financial Governance & Fundraising Platform

---

## Project Context

You are designing Phase 2 of a charity management platform for **Junior Jinnah Trust (JJT)**, a Pakistan-based education charity that sponsors underprivileged children's schooling through donor contributions.

Phase 1 already exists as a working product. It has an established design system (cream backgrounds, dark green sidebar, IBM Plex Mono for numbers). Phase 2 **extends** the platform with financial management, fund accounting, payment reconciliation, donor management, and executive reporting — without breaking the existing visual language.

---

## Established Design System (Phase 1 — Do Not Break)

Use these exact values for all Phase 2 screens.

**Colour tokens:**
```
Sidebar/dark green:    #1c352c
Forest green (primary):#2f5d4f
Forest light bg:       #e6f0ed
Cream background:      #fffdf9
Cream alt:             #f4f1ea
Border:                #e3dccd
Text primary:          #1a1a1a
Text secondary:        #5a6360
Text muted:            #9aa79f
Success green:         #2f7a5e
Success green bg:      #e6f4ef
Amber:                 #92600a
Amber bg:              #fef3c7
Red (danger):          #b91c1c
Red bg:                #fee2e2
```

**Typography:**
- Body / UI labels: **Hanken Grotesk**
- Numbers / IDs / codes / amounts: **IBM Plex Mono**
- Section display headings: **Newsreader** (serif, used sparingly)

**Layout shell (Phase 1 admin — already built):**
- Full-viewport two-column: fixed dark green sidebar (252px) + scrollable main content
- Top header: 64px, cream background, breadcrumb left, user avatar right
- Content area: 24px padding, cream-alt background
- All Phase 2 screens live inside this same shell

**Status badge system:**
- RECEIVED / ACTIVE / FUNDED / ALLOCATED → green bg `#e6f4ef`, text `#2f7a5e`
- EXPECTED / PENDING / DRAFT / RESERVED → amber bg `#fef3c7`, text `#92600a`
- OVERDUE / AT_RISK / EXPIRED / REJECTED → red bg `#fee2e2`, text `#b91c1c`
- WAIVED / ARCHIVED / CLOSED / CANCELLED → grey bg `#f4f1ea`, text `#5a6360`, 1px border `#e3dccd`
- PARTIAL / PREPAID → distinct amber variant, slightly darker

---

## Screens to Design

Design the following **16 screens** across 5 user personas. Each screen is a full-page view inside the admin shell unless marked otherwise.

---

### PERSONA 1 — JJT Admin / Finance Admin (Operations)

---

#### Screen 1: Admin Dashboard (Enhanced)

This replaces the current simple dashboard. It is the first screen seen on login.

**Top KPI row — 4 cards:**
1. **General Fund Balance** — Large PKR amount in IBM Plex Mono. Sub-line: "X months runway" with colour coding (red < 2 months, amber 2–4, green > 4). CTA: "View fund detail"
2. **Children Active** — "47 / 52 enrolled". Sub: "5 seeking sponsor". CTA: "+ Add child"
3. **Payments This Month** — "16 / 20 received". Sub: "4 overdue ⚠". CTA: "View reconciliation"
4. **At-Risk Children** — Red card if > 0. Count with "! Needs attention". CTA: "View at-risk list"

**Second row — 2 columns:**
Left: **Fund Accounts panel** — table listing each fund (General, Zakat, Sadaqah, Ramadan Appeal), its balance, and month-on-month movement arrow (↑↓→). Tag badges for ACTIVE / CLOSED campaigns. At the bottom: "Available for new commitments: PKR X" vs "Reserved: PKR Y".

Right: **At-Risk Children panel** — list of max 5 children requiring action. Each row: child avatar initial, child name, status badge (ACTIVE), issue description ("Payment 12 days overdue"), two inline action buttons ("Record payment" primary, "Contact sponsor" ghost).

**Third row — full width:**
**Monthly Reconciliation Snapshot** — horizontal scrollable table showing current month's expected payments. Columns: Sponsor, Child, Expected (PKR), Status badge, Received, Actions. Each OVERDUE row has a subtle red-tinted background. Table has a "View full reconciliation →" link.

**Bottom row — full width:**
**Activity feed** — date-grouped list. Each entry: time (IBM Plex Mono), icon, description, user tag. Today's entries first.

**Quick actions toolbar** — fixed at the bottom of the sidebar, 2×3 grid of icon+label buttons: Record Payment, Add Donation, Add Child, Add Sponsor, Generate Report, Month-End Close.

---

#### Screen 2: Fund Accounts — List View

Sidebar: "Funds" is the active nav item under a new "Finance" nav group.

**Toolbar:** "Fund Accounts" heading + description + "New fund account" primary button (right).

**Fund cards grid (2 columns):**
Each card shows:
- Fund type pill (GENERAL / ZAKAT / SADAQAH / CAMPAIGN / EMERGENCY)
- Fund name (large, Newsreader)
- Current balance (IBM Plex Mono, large, 28px)
- "Available: PKR X" sub-line (balance minus reserved)
- Bar showing reserved vs available as a proportion
- Month-on-month delta: "↑ PKR 5,000 this month" in green / "↓ PKR 2,000" in red
- If CAMPAIGN: progress bar showing raised vs target (e.g. "70% of target reached"), days remaining chip
- "View transactions →" link

**At the bottom of the page:**
"Available for new early support commitments" — summary box (cream-alt background, border, shows the net available from General Fund only).

---

#### Screen 3: Fund Account — Transaction History

Accessed by clicking a fund card.

**Header:** Fund name, type badge, current balance (large IBM Plex Mono), opened date, created-by.

**Filter bar:** Date range picker, Transaction type chips (ALL / CREDIT / DEBIT), Category dropdown, Search by reference.

**Transaction table:**
- Date (IBM Plex Mono)
- Type badge (CREDIT green / DEBIT red)
- Category (DONATION / EARLY_SUPPORT / SPONSORSHIP_PAYMENT / etc.)
- Amount (IBM Plex Mono, green for credit, red for debit)
- Running balance (IBM Plex Mono)
- Description (truncated)
- External reference (monospace, muted)
- Created by (user avatar + name)
- If requires approval: "Approved by" field, or amber "Awaiting approval" badge

**Summary footer row:** Total credits | Total debits | Net movement | Closing balance.

---

#### Screen 4: Monthly Reconciliation Workspace

This is a dedicated full-page workspace for Finance Admins to reconcile a month's payments.

**Header:** Month selector (e.g. "July 2026 ▾") + "Generate PDF report" button + progress indicator ("16 / 20 reconciled").

**Month-End Close Checklist (collapsible panel, top of page):**
Numbered checklist with checkboxes:
1. SponsorPayments auto-created ✓
2. Reconcile received payments (in progress)
3. Record unmatched bank credits
4. Review overdue payments
5. Cover gaps with early support
6. Generate and save monthly report
7. Confirm Zakat fund balance

**Reconciliation table (main content):**
Each row = one active sponsorship for the month.
Columns:
- Sponsor (avatar + name)
- Child (avatar + name + roll number in mono)
- Expected PKR (mono)
- Status badge
- Amount received (editable input if EXPECTED/PARTIAL)
- Bank reference (text input)
- Date received (date input)
- [Save] button per row (activates when inputs filled)

Rows grouped by status: OVERDUE first (red tint), then EXPECTED, then RECEIVED (green tint, read-only).

**Bottom: Unmatched Bank Credits panel**
Amber-bordered box. List of unaccounted transactions with [Match to sponsor] and [Record as donation] actions on each row.

---

#### Screen 5: Record Payment — Modal

Triggered from the reconciliation table row or the dashboard "Record payment" button.

**Modal (560px wide, centered):**
- Header: "Record Payment Received"
- Sponsor name + child name shown as context (read-only chips, not editable)
- Month (pre-filled, read-only)
- Expected amount (read-only, shown for reference)
- **Amount received** (input, pre-filled with expected, editable for partial)
- **Currency** (dropdown: PKR default, USD, GBP, AED)
- **Exchange rate** (shows only if non-PKR, auto-calculated PKR equivalent shown below)
- **Bank reference** (text input, required)
- **Date received** (date picker, defaults today)
- **Notes** (optional textarea)
- If amount < expected: amber warning "This will be recorded as PARTIAL — shortfall of PKR X"
- Footer: Cancel ghost + "Record Payment" primary

---

#### Screen 6: Donation — Record New Donation

Accessed from "+ Add Donation" quick action.

**Full-page form (not modal, as it has many fields):**

**Section 1: Donor**
- Donor type toggle: Individual / Corporate / Anonymous
- If Individual/Corporate: searchable dropdown (existing donors) or "New donor" inline expand
  - New donor fields: Name, Email, Country, Phone (optional)
  - For UK donors: Gift Aid declaration toggle → expands to First name, Last name, Full address, Declaration date, "I am a UK taxpayer" checkbox
- If Anonymous: no donor fields, amber note "No receipt can be generated for anonymous donations"

**Section 2: Donation Details**
- Donation type dropdown (ONE_TIME / RECURRING_MONTHLY / CAMPAIGN / ZAKAT / SADAQAH / etc.)
- Amount + Currency
- Exchange rate field (if non-PKR) + PKR equivalent auto-calculated
- Fund routing: auto-set by type (Zakat → Zakat Fund, Campaign → Campaign Fund dropdown, else → General Fund), with override toggle for admins
- Bank reference (required)
- Date received
- Notes

**Section 3: Recurring (shows only if RECURRING_MONTHLY)**
- Expected monthly amount
- First expected payment date
- End date (optional, "Indefinite" default)

**Preview box (right column sticky):**
"This donation will:
- Credit General Fund by PKR X
- Create a Donation record for [Donor Name]
- Generate receipt [JJT-2026-0042]"

**Footer:** Cancel + "Record Donation & Generate Receipt" primary.

---

#### Screen 7: Donor — Profile Page

Accessed by clicking a donor from the Donors list.

**Header:** Donor avatar initial (large, forest green circle) + full name + donor type badge + country flag emoji.

**KPI row:**
- Total donated (all time)
- Number of donations
- First donation date
- Gift Aid eligible badge (Y/N)

**Tabs:**
1. **Donation History** — table with date, type, amount, fund, bank ref, receipt status ([Download] link per row)
2. **Recurring Schedules** — any RECURRING_MONTHLY commitments with their status
3. **Receipts** — all generated receipts with status (ISSUED / OUTSTANDING) and download link

**Action buttons (top right):** "Record new donation" primary, "Download donor summary" ghost.

---

### PERSONA 2 — Board Member (Read-Only)

---

#### Screen 8: Board Dashboard

URL: `/board/dashboard`. Read-only. No action buttons anywhere.

**Programme Health banner (full width, dark green background, white text):**
"Q2 2026 · 52 children enrolled · 47 actively funded · PKR 185,500 total funds · 1.97 months runway"
Runway traffic light is the dominant visual: big coloured arc/gauge showing runway months.

**Three-column layout below banner:**

Column 1: **Financial Position card**
- Fund breakdown table (fund name, type, balance)
- "Monthly programme cost: PKR 94,000"
- "Quarterly surplus/(deficit): (PKR 2,000)" — red if deficit
- "Fund runway: 1.97 months" — prominent, colour coded

Column 2: **Sponsorship Health card**
- Donut chart: Active / Pending / Early Support / Seeking proportions
- Below chart: table of counts and percentages
- "Payment collection rate (this month): 80%"

Column 3: **Key Decisions Required** — amber card
- Bullet list of items requiring board attention
- Each item has a severity dot (red/amber/green)
- Read-only, no action buttons

**Full-width bottom section:**
12-month trend area chart showing:
- Blue line: Monthly donations received
- Green line: Fund balance over time
- Red dashed line: Monthly programme cost (constant)
- Legend below

---

#### Screen 9: Board — Annual Financial Statement (Report View)

Read-only document-style layout (white background within the main content area to simulate a formal report).

**Page header:** "Annual Financial Statement 2025–26 · Junior Jinnah Trust"

Sections (styled like a formal document):
1. **Income Summary** — table of all donation types with totals
2. **Expenditure Summary** — education costs by month
3. **Fund Movements** — opening balance → credits → debits → closing balance per fund
4. **Programme Statistics** — children supported, months covered, graduations
5. **Signature block** — "Prepared by: [name] · Approved by: [name] · Date: [date]"

Footer: "Download PDF" button.

---

### PERSONA 3 — Sponsor (Enhanced Portal)

---

#### Screen 10: Sponsor Portal — Enhanced Home

The existing sponsor portal extended with payment history and receipts.

**Hero card (full width, dark green background):**
Left: Child avatar large (circle with initials) + "Zainab Khalid · Grade 5 · Al-Huda School, Karachi"
Right: Status badge ACTIVE + "Sponsored since April 2026 · 4 months · PKR 8,000 contributed"

**Latest Progress panel:**
Month label chip (e.g. "June 2026"), 2–3 sentence progress excerpt, "[Read full update →]" link.

**Payment History table:**
- Month (IBM Plex Mono)
- Status badge
- Amount (IBM Plex Mono)
- Date received (mono)
- [Download Receipt] link per received row
- Current month row highlighted with amber EXPECTED badge

**Impact Summary row (3 stats):**
"4 months funded · PKR 8,000 invested · 3 progress reports"

**Annual Impact Report download button** (only shows if year has passed).

---

### PERSONA 4 — Campaign Manager

---

#### Screen 11: Campaign List

Sidebar: "Campaigns" nav item under "Finance" group.

**Toolbar:** "Campaigns" heading + "New campaign" button.

**Campaign cards (list, full width):**
Each card:
- Campaign name (Newsreader, large) + status badge (DRAFT / ACTIVE / FUNDED / CLOSED)
- Description (2 lines, truncated)
- Progress bar: "PKR 35,000 raised of PKR 50,000 target" with percentage label
- Key stats row: Donors count · Average donation · Days remaining (or closed date)
- "View dashboard →" link

---

#### Screen 12: Campaign Dashboard

Header: Campaign name + status badge + "Edit" ghost button.

**Stats row:** Target / Raised / Gap / Days remaining / Donors / Avg donation

**Progress bar** (large, prominent, 12px height, with percentage label and target marker line).

**Donation feed** (right column): Paginated list of recent donations. Each row: date, donor name (or "Anonymous"), amount, currency, fund routing tag.

**Campaign Disposition panel** (shows only when status = CLOSED or near end date):
- Amber-bordered card
- "Unspent balance: PKR X"
- Radio buttons: Transfer to General Fund / Refund donors / Roll to next campaign
- Reason textarea
- "Submit and close campaign" primary button

---

### PERSONA 5 — Shared / Cross-cutting Screens

---

#### Screen 13: Child — Enhanced Profile (Admin View)

Extension of the existing child detail page with financial panel added.

The existing layout is: left column (child details + progress timeline) + right column (ledger / sponsor info).

**Add to right column — new "Financial Summary" section:**
- Funding status badge (ENROLLED / ACTIVE_SUPPORTED / SPONSOR_SOUGHT / GRADUATED)
- "Months covered: X · Total invested: PKR Y"
- Coverage forecast: next 3 months listed with coverage status (COVERED by sponsor / COVERED by early support / UNCOVERED — red)
- Education cost history (table: effective month, amount, change reason — if ever changed)
- CTA: "Record early support for this child" link

---

#### Screen 14: Notifications & Alerts Centre

Sidebar: "Alerts" nav item with red badge showing count.

**Tabs:** All / Payment Overdue / At-Risk Children / Fund Alerts / System

**Alert list:**
Each item:
- Severity dot (red / amber / green)
- Icon (payment / child / fund / system)
- Title (bold, e.g. "Payment overdue — Adnan Malik → Bilal Ahmed")
- Sub-line (e.g. "Expected July 2026 · PKR 2,500 · 12 days overdue")
- Timestamp (mono, muted)
- Action buttons (contextual: "Record payment" / "View child" / "Review sponsorship")
- "Mark resolved" ghost button

---

#### Screen 15: Audit Trail — Search Interface

Sidebar: "Audit Log" nav item under "System" group. JJT_ADMIN only.

**Filter bar (top):**
- Entity type dropdown (FundTransaction / Donation / Sponsorship / LedgerEntry / User)
- Action type dropdown (CREATED / STATUS_CHANGED / APPROVED / DELETED_ATTEMPT)
- Date range
- User dropdown (filter by which user performed the action)
- Search by entity ID

**Results table:**
- Timestamp (IBM Plex Mono, precise to seconds)
- Entity type badge
- Action badge (CREATED green / STATUS_CHANGED amber / APPROVED teal)
- Description ("Sponsorship PENDING → ACTIVE")
- Entity ID (truncated UUID, mono)
- User (avatar + email)
- IP address (mono, muted)
- "View full record →" link

---

#### Screen 16: Four-Eyes Approval Queue

Shown when a transaction > PKR 50,000 is submitted and awaiting a second approver.

**Header:** "Pending Approvals (3)" with red badge.

**Approval cards list:**
Each card (cream background, amber left border):
- Transaction type + amount (IBM Plex Mono, large)
- Submitted by (avatar + name + timestamp)
- Description + fund + external reference
- "Why this requires approval" chip (e.g. "Amount exceeds PKR 50,000 threshold")
- Two buttons: "Approve" (green primary) + "Reject with reason" (red ghost)

Clicking Approve → confirmation modal: "You are approving [description] for PKR X. This cannot be undone. Your name will be recorded as approver." → "Confirm Approval" button.

---

## Component Library (Design alongside screens)

Design these reusable components as a separate Figma page named "Components":

1. **KPI Card** — label, value (mono), sub-line, optional CTA link. Variants: default / alert-red / alert-amber
2. **Fund Balance Card** — name, type pill, balance (mono large), available sub-line, bar, month delta
3. **Status Badge** — all 8 status variants listed above
4. **Reconciliation Row** — sponsor cell, child cell, expected amount, status badge, amount input, bank ref input, date input, save button
5. **Alert Item** — severity dot, icon, title, sub-line, timestamp, actions. Variants: unread / read / resolved
6. **Progress Bar** — label, percentage, amount labels, variants: campaign / fund / coverage
7. **Activity Feed Item** — time, icon, description, user tag
8. **Approval Card** — amber left border, transaction details, approve/reject buttons
9. **Donor Receipt Card** — receipt number, donor name, amount, date, [Download] button
10. **Month Selector** — dropdown style, shows YYYY-MM format in IBM Plex Mono

---

## Navigation — New Sidebar Items

Add these items to the existing admin sidebar:

```
Overview
  Dashboard (existing, enhanced)

Management (existing group)
  Children (existing)
  Sponsors (existing)
  Commitments (existing)

Finance (NEW group)
  Fund Accounts
  Reconciliation
  Donations
  Donors
  Campaigns

Operations (existing group)
  Early Support (existing)
  Add Progress (existing)
  Alerts ← new, with red badge

System (existing group)
  Users (existing)
  Audit Log ← new
  Reports ← new
  Approvals ← new, with amber badge
  How It Works (existing)
```

---

## Responsive Breakpoints

- **Desktop (≥ 1280px):** Full sidebar + multi-column content grids
- **Tablet (768px – 1279px):** Sidebar collapses to icon-only (44px wide), content goes single-column
- **Mobile (< 768px):** Sidebar becomes bottom nav bar (5 most-used items), full-width content

---

## Tone and Feel

- **Professional but warm.** This is a charity managing real children's education. Numbers should feel serious (monospace, precision), but the overall aesthetic should feel trustworthy, not cold.
- **Action-oriented dashboards.** Every section should have a visible next action. No data for the sake of data.
- **Financial precision.** All monetary values in IBM Plex Mono with thousands separator (PKR 1,45,000 Pakistani format or PKR 145,000 international — designer's discretion, keep consistent).
- **Traffic-light awareness.** Financial health indicators (fund runway, overdue payments, at-risk children) must use colour as a primary signal — the user should understand the severity at a glance without reading text.
- **Minimal decoration.** No gradients, no heavy illustrations. Clean lines, ample whitespace, borders for structure.
