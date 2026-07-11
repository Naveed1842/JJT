# JJT Platform — Complete Presentation Design Brief
### For Claude Design · Single Source of Truth

---

## 1. EXECUTIVE SUMMARY

**Product:** Junior Jinnah Trust (JJT) — a complete digital platform for managing child education sponsorships, donations, and financial accountability for NGOs and charitable organisations.

**Tagline:** *One platform. Every child. Full transparency.*

**What it does in one sentence:** JJT replaces spreadsheets, WhatsApp coordination, and manual receipts with a professional, end-to-end sponsorship management system — giving NGOs, sponsors, donors, and regulators complete visibility from a child's first enrolment to every monthly payment received.

**Audience for this presentation:**
- NGO Owners / Charity Founders
- School Owners / Campus Managers
- Corporate CSR Teams
- Investors / Board Members
- Government Representatives
- Potential Sponsors
- Zakat Boards and Islamic Finance Bodies

**Tone:** Trusted. Warm. Professional. Accountable. Impact-driven.

**Presentation length:** 22 slides

---

## 2. FEATURE AUDIT

### ✅ FULLY IMPLEMENTED (may appear in presentation)

#### Public Website (sponsorone.app)
- **Home Page** — Hero with featured children, live count of total enrolled and available children, CTAs to browse and give
- **Children Listing** — Full searchable, filterable, paginated gallery of all children with real-time availability status (Seeking / Bridged / Sponsored)
- **Child Profile Page** — Individual child detail: education cost, campus, city, availability badge
- **5-Step Public Sponsorship Flow:**
  1. Set Intention (Sadaqah / Zakat / General giving)
  2. Choose commitment plan (Monthly or Yearly with 10% discount)
  3. Enter personal details (name, email, phone)
  4. Bank transfer payment information (account/IBAN/bank name displayed with one-tap copy)
  5. Confirmation with sponsorship start month
- **Trust & Accountability Page** — Static transparency content
- **Why Give Page** — Static impact and motivation content
- **Login Page** — Secure JWT-based authentication

#### Admin Console (Role: JJT_ADMIN / ORG_ADMIN)
- **Dashboard** — Live fund balances, children stats (total / available / enrolled), monthly payment stats (expected / received / overdue / waived), active alert badge
- **Children Management** — Add children individually or bulk-import from Excel; searchable list with current sponsor name; drill-down detail panel with full sponsorship history, ledger, and progress
- **Sponsor Management** — Add and manage sponsors with contact details; searchable list
- **Commitments / Sponsorships** — Commit sponsor to child; Activate (admin approval step); Expire; Filter by Pending/Active/All
- **Early Support** — Bridge a child's education cost before a sponsor is found; recorded as a ledger entry
- **Progress Updates** — Add monthly written progress summaries per child; email notification sent to active sponsor automatically
- **User Management** — Create sponsor-linked users and org admin users; activate / deactivate accounts
- **Fund Accounts** — Track organisational fund balances; credit fund on cash receipt; view full transaction history; minimum reserve warnings
- **Payment Reconciliation** — Month-by-month payment view; record received payments with bank reference; waive payments; detect at-risk sponsorships (consecutive overdue months); generate expected payment records
- **Alerts System** — Severity-tagged system alerts (Critical / High / Medium / Low) with sidebar badge; dismissible
- **Donors** — Create and manage donors (Individual / Corporate / Trust / Anonymous) with contact details and notes
- **Donations** — Record one-time donations (General / Zakat / Sadaqah / Sponsorship Top-Up / Corporate / In-Kind); mark received; reverse; printable receipts
- **Zakat Module** — Dedicated section for Zakat tracking with headline stats (total received, this year, this month); filterable Zakat-only donation ledger
- **Recurring Donations** — Schedule recurring giving (Monthly / Quarterly / Annual); pause or cancel schedules; auto-generate expected donations
- **Campaigns** — Create fundraising campaigns with name, target, dates; Open / Close lifecycle
- **Reports** — Cash flow report (6-month opening/closing balance history); Portfolio report (active/pending/expired sponsorship counts, total monthly value, commitment type breakdown)
- **Audit Log** — Full paginated system-wide audit trail: who did what, when, to which record
- **Settings** — Organisation name, base currency, payment due day; change password
- **Exports** — Children to Excel; Donations to Excel; Reconciliation to Excel; Individual child PDF report
- **Bulk Import** — Upload Excel for batch child enrolment with row-by-row result report and template download

#### Sponsor Portal (Role: SPONSOR)
- View own sponsored children
- Expand each child to see month-by-month education ledger and progress updates
- View and update own profile (display name, phone)

#### Platform Infrastructure
- Secure JWT authentication with automatic token refresh
- Role-based access: JJT_ADMIN, ORG_ADMIN, SPONSOR
- Email notifications: sponsor welcome on activation, progress update delivery
- Append-only financial ledger (immutable records, no deletions)
- Full audit trail on every action
- Cloud-deployed (Heroku backend, Firebase Hosting frontend)

---

### ⚠️ PARTIALLY IMPLEMENTED (do NOT include in presentation)
- **Child Photos / Media** — Backend storage infrastructure built; UI not yet integrated
- **Visual Dashboard Charts** — Data exists; charts not rendered in UI (tables only)
- **Campaign donation linking** — Campaign records exist; no donation-to-campaign tracking in UI

### ❌ NOT IMPLEMENTED (do NOT include in presentation)
- Online payment gateway (only bank transfer)
- Mobile app
- Donor self-service portal
- Public campaign browse pages
- Two-factor authentication
- Dark mode

---

## 3. PRESENTATION STORY

The presentation tells **one continuous story** organised around the human journey — not software modules.

```
ACT 1: THE PROBLEM
  Slide 01 · Title / Opener
  Slide 02 · The Problem — How charities operate today (manual chaos)
  Slide 03 · What breaks when processes are manual
  Slide 04 · What modern donors and regulators now demand

ACT 2: THE SOLUTION
  Slide 05 · Introducing JJT — The Platform
  Slide 06 · Three audiences, one platform
  Slide 07 · How JJT works — The journey map (overview)

ACT 3: THE PUBLIC EXPERIENCE
  Slide 08 · A sponsor discovers a child
  Slide 09 · They commit in 5 steps — no account required
  Slide 10 · Bank transfer with instant confirmation

ACT 4: THE ADMIN EXPERIENCE
  Slide 11 · The Admin Console — Command centre
  Slide 12 · Enrolling children (individual + bulk import)
  Slide 13 · Activating and managing sponsorships
  Slide 14 · Payments — Reconcile every dirham/rupee
  Slide 15 · Donations + Zakat tracked separately

ACT 5: TRANSPARENCY & ACCOUNTABILITY
  Slide 16 · The Sponsor Portal — What sponsors see
  Slide 17 · Progress updates delivered automatically
  Slide 18 · Audit Log — Nothing is hidden

ACT 6: FINANCIAL INTELLIGENCE
  Slide 19 · Fund Accounts and Cash Flow
  Slide 20 · Alerts — Proactive risk detection
  Slide 21 · Reports + Exports

ACT 7: THE CLOSE
  Slide 22 · Why organisations choose JJT
```

---

## 4. SLIDE-BY-SLIDE BREAKDOWN

---

### SLIDE 01 — Title / Opener

**Title:** Junior Jinnah Trust
**Subtitle:** Education. Accountability. Impact.

**Business Message:** This is a platform for charities that take transparency seriously.

**Visual Direction:**
- Full bleed warm cream background (#F5EFE3)
- Large centred logotype "JJT" in deep forest green (#1C352C)
- Subtitle in muted olive (#6B7C6B), weight 300
- Very subtle texture overlay (linen grain)
- Bottom right: small "sponsorone.app" in grey

**Speaker Notes:** Open with silence. Let the slide breathe. "The problem we're solving today is not technology — it is trust."

**Animation:** Fade in logo → Fade in subtitle → Subtle upward text entrance

---

### SLIDE 02 — The Problem

**Title:** Managing a charity today means managing chaos

**Business Message:** Most NGOs run on WhatsApp groups, spreadsheets, and paper receipts. When money moves, no one can trace it.

**Visual Direction:**
- Four pain-point icons in a 2×2 grid
- Each icon: simple line illustration, warm red tint (#C0392B at 10% opacity background)
- Pain points:
  1. 📋 Sponsor commitments tracked in Excel — version confusion, no single source of truth
  2. 💸 Payments received by cash — no formal receipts, no audit trail
  3. 📱 Updates shared on WhatsApp — no formal record, not searchable
  4. 📊 Board meetings with printed sheets — data always stale

**Suggested Diagram:** 4-cell grid with icon + 5-word headline + 1-line description each

**Animation:** Each cell fades in sequentially (0.3s delay between each)

---

### SLIDE 03 — What Breaks

**Title:** The cost of manual operations

**Business Message:** Donors lose trust. Children fall through gaps. Organisations face compliance risk.

**Visual Direction:**
- Three consequence statements in large type, left-aligned
- Each preceded by a red dot / warning icon
- Statements:
  - "Sponsors don't know their money arrived"
  - "Children lose funding with no warning"
  - "Regulators find no records to audit"
- Bottom: subtle divider + "There is a better way."

**Animation:** Statements slide in from left, one by one. Final line fades in last.

---

### SLIDE 04 — What Modern Donors Demand

**Title:** Expectations have changed

**Business Message:** Today's sponsors and donors expect real-time visibility, formal receipts, and proof of impact — not promises.

**Visual Direction:**
- Three demand pillars as vertical cards (dark green #1C352C background, cream text)
- Card 1: 👁️ **Visibility** — "I want to see my child's progress"
- Card 2: 🧾 **Receipts** — "I need formal documentation for tax/zakat"
- Card 3: 🔒 **Trust** — "I need to know my money is used correctly"

**Animation:** Cards slide up from bottom with stagger

---

### SLIDE 05 — Introducing JJT

**Title:** One platform. Every child. Full transparency.

**Business Message:** JJT is a complete digital operations platform for child education sponsorship — from the moment a child enrols to every payment received.

**Visual Direction:**
- Full-bleed forest green (#1C352C) background
- White headline in large display type
- Tagline below in cream
- Subtle background: faint geometric pattern (arches or Islamic-inspired motif in lighter green)
- Bottom: three feature pillars in cream mini-cards
  - Public Website · Admin Console · Sponsor Portal

**Animation:** Background loads first, text fades in centre, three cards rise from bottom

---

### SLIDE 06 — Three Audiences, One Platform

**Title:** Built for everyone in the equation

**Business Message:** JJT serves three distinct users — the public sponsor, the organisation administrator, and the sponsor with an account — each with their own experience.

**Visual Direction:**
- Three large icons / illustrated figures in a horizontal row
- Separated by subtle vertical dividers
- Left: 🌐 **Public Visitor** — Browses children, commits a sponsorship online
- Centre: 🛡️ **Organisation Admin** — Manages everything: children, money, reports
- Right: 💚 **Verified Sponsor** — Logs in to see their child's progress

**Callout Boxes:**
- Under each figure: role label in bold + 3-bullet feature summary

**Animation:** Figures appear one by one with upward entrance

---

### SLIDE 07 — How JJT Works (Journey Map)

**Title:** The journey from enrolment to impact

**Business Message:** Every step of the child's journey — from first enrolment to monthly education payment — is tracked, audited, and visible.

**Visual Direction:**
- Horizontal flow diagram (left to right)
- 6 connected stages with icons:
  1. 📋 Child Enrolled (admin)
  2. 🌐 Listed Online (public)
  3. 🤝 Sponsor Commits (public)
  4. ✅ Admin Activates (admin)
  5. 💳 Monthly Payment (reconciliation)
  6. 📊 Progress Reported (sponsor portal)
- Arrows connecting each stage
- Colour: stages alternate between cream and light green fills

**Animation:** Stages reveal left-to-right, one per beat

---

### SLIDE 08 — A Sponsor Discovers a Child

**Title:** Every available child is visible online

**Business Message:** Sponsors browse a live list of children seeking education support — with name, city, campus, and monthly cost displayed openly.

**Screenshot Required:**
- Route: `/children`
- Capture: Full children listing page
- Crop: Show 6–8 child cards in the grid
- Highlight: Status badges (Seeking / Bridged / Sponsored) — circle them
- Highlight: Search bar and filter buttons at top
- Remove/blur: Any real names if privacy needed

**Annotation Ideas:**
- Arrow pointing to status badge: "Real-time availability"
- Arrow pointing to search: "Search by name, city, or campus"
- Arrow pointing to card: "Education cost shown clearly"

**Animation:** Screenshot fades in, then callout arrows appear one by one

---

### SLIDE 09 — They Commit in 5 Steps

**Title:** Sponsoring a child takes five minutes

**Business Message:** No account needed. No friction. A sponsor can commit online, declare their intention (Sadaqah or Zakat), and choose monthly or yearly giving — all from their phone.

**Screenshot Required:**
- Route: `/children/:childId/sponsor`
- Capture: Step 1 (Intention), Step 2 (Plan), Step 3 (Details) — show 3 screenshots side by side
- Crop: Step progress bar at top + step content
- Highlight: Step indicator breadcrumb, Monthly/Yearly toggle with price

**Suggested Diagram:**
- 5-step horizontal breadcrumb illustration:
  Intention → Plan → Account → Payment → Confirmed
  (step 5 shown in green as "done")

**Annotation Ideas:**
- "No login required"
- "10% discount for annual commitment"
- "Sadaqah or Zakat — sponsor declares their intention"

---

### SLIDE 10 — Bank Transfer Made Simple

**Title:** Payment instructions, one tap to copy

**Business Message:** JJT displays full bank details — account title, number, IBAN, bank name — with copy-to-clipboard for each field. No errors, no confusion.

**Screenshot Required:**
- Route: `/children/:childId/sponsor` — Step 4 (Payment screen)
- Capture: The payment info card with account details
- Highlight: Copy buttons next to each field

**Annotation Ideas:**
- "One tap copies the IBAN"
- "Sponsor knows exactly where to send"
- Callout box: "After transfer — admin confirms receipt on the platform"

**Animation:** Screenshot loads, then arrow points to copy button with pulse

---

### SLIDE 11 — The Admin Console

**Title:** Everything your organisation needs, in one place

**Business Message:** The admin console is the operational nerve centre — children, sponsors, payments, alerts, donations, reports, and audit logs — all in one secure, role-based interface.

**Screenshot Required:**
- Route: `/admin/dashboard`
- Capture: Full dashboard view
- Highlight: Fund balance cards, children stats, payment summary, alert badge in sidebar
- Crop: Show full dashboard without truncation

**Annotation Ideas:**
- Arrow to fund balance: "Live fund balance"
- Arrow to children stats: "Enrolled vs. available"
- Arrow to payment stats: "This month's collection status"
- Arrow to sidebar alert badge: "Active alerts"

**Callout Box:** "20+ modules. One login."

---

### SLIDE 12 — Enrolling Children

**Title:** From zero to enrolled in seconds

**Business Message:** Admins add children individually or import hundreds from an Excel spreadsheet. Every child gets a unique roll number, education cost, and ledger — ready to be sponsored.

**Screenshot Required:**
- Route: `/admin/children`
- Capture 1: Children list with admin detail panel open
- Capture 2: Add Child modal form
- Crop: Show list + detail panel side-by-side

**Highlight areas:**
- Detail panel showing sponsorship history, ledger, progress
- "Import from Excel" button
- Child's current sponsor name in the list row

**Annotation Ideas:**
- "Full sponsorship history per child"
- "Month-by-month education ledger"
- "Bulk Excel import for 100s of children"

---

### SLIDE 13 — Activating and Managing Sponsorships

**Title:** Every commitment requires admin approval

**Business Message:** Sponsors commit online, but activation is admin-controlled — ensuring every sponsorship is verified before education funding begins. Admins can also expire sponsorships when needed.

**Visual Direction:**
- Three-state flow diagram:
  🟡 PENDING (sponsor committed) → ✅ ACTIVE (admin approved) → ⬛ EXPIRED (ended)
- Use colour-coded badges matching the UI (amber, green, grey)

**Screenshot Required:**
- Route: `/admin/commitments`
- Capture: Sponsorship list showing PENDING and ACTIVE rows with Activate/Expire action buttons

**Annotation Ideas:**
- "Admin reviews every commitment before activation"
- "Activate button — single click"
- "Expiry closes the sponsorship cleanly, child becomes available again"

---

### SLIDE 14 — Payments — Reconcile Every Rupee

**Title:** Know exactly who paid, who hasn't, and who is at risk

**Business Message:** Every month, JJT auto-generates expected payment records for every active sponsorship. Admins record receipts against bank references. Overdue accounts are flagged automatically.

**Screenshot Required:**
- Route: `/admin/reconciliation`
- Capture: Monthly reconciliation view showing summary cards (expected/received/overdue/waived) + payment list
- Highlight: Summary cards at top, status badges in the list (Received/Overdue/Pending)

**Suggested Diagram:**
- 5-state payment lifecycle badge strip:
  PENDING → RECEIVED (green) / OVERDUE (red) / PARTIAL (amber) / WAIVED (grey)

**Annotation Ideas:**
- Arrow to summary: "Total expected vs. actually received this month"
- Arrow to overdue badge: "At-risk flagged automatically"
- "Bank reference recorded with every payment"

**Callout Box:** "At-risk sponsorships identified after consecutive missed payments"

---

### SLIDE 15 — Donations and Zakat Tracked Separately

**Title:** Every type of giving is recorded and receipted

**Business Message:** JJT separates Zakat from general donations — giving Islamic-focused organisations a clean, auditable record for Zakat distribution compliance. Printable receipts are generated for every donor.

**Screenshot Required:**
- Route: `/admin/zakat`
- Capture: Zakat section showing headline stats (total received, this year, this month) + donation table

**Suggested Diagram:**
- Donation type hierarchy visual:
  GENERAL → ZAKAT → SADAQAH → CORPORATE → IN-KIND → SPONSORSHIP TOP-UP
  (Each with different shade of green)

**Annotation Ideas:**
- Arrow to Zakat stats: "Total Zakat received — fully separate from general funds"
- "Printable PDF receipt for every donation"
- "Recurring schedules: monthly, quarterly, annual"

**Callout Box:** "Donation type: General · Zakat · Sadaqah · Corporate · In-Kind"

---

### SLIDE 16 — The Sponsor Portal

**Title:** Sponsors see their impact, not your spreadsheets

**Business Message:** Every sponsor with a login can see exactly which children they support, how much has been paid each month, and receive progress updates — no emails, no PDFs, no WhatsApp.

**Screenshot Required:**
- Route: `/sponsor/portal`
- Capture: Portal showing sponsored child card expanded with ledger entries and progress updates

**Highlight areas:**
- Ledger table (month / amount / coverage type)
- Progress update entries
- Child name and availability badge

**Annotation Ideas:**
- "Month-by-month education ledger"
- "Progress updates delivered here — and by email"
- "Sponsor sees only their own children — nobody else's data"

**Callout Box:** "Private. Secure. Always up to date."

---

### SLIDE 17 — Progress Updates Delivered Automatically

**Title:** Sponsors receive progress reports automatically

**Business Message:** When an admin adds a monthly progress update, JJT sends an email to the child's sponsor automatically — no manual follow-up, no forgotten messages.

**Visual Direction:**
- Left panel: Admin adding progress update (simple form mockup)
- Right panel: Email received by sponsor (illustrated email preview)
- Arrow in the middle: "Automatic"

**Annotation Ideas:**
- "Admin enters one paragraph"
- "Sponsor receives a formal email notification"
- "Full update history stored in the portal"

**Callout Box:** "No WhatsApp. No manual email. Done automatically."

---

### SLIDE 18 — Audit Log — Nothing Is Hidden

**Title:** A complete record of every action, ever taken

**Business Message:** JJT records every action on the platform — who created what, who approved a sponsorship, who marked a payment received — with actor, timestamp, and entity. Built for regulatory and board review.

**Screenshot Required:**
- Route: `/admin/audit`
- Capture: Audit log table with event type, actor email, description, time ago columns

**Highlight areas:**
- Actor email column
- Event type (PUBLIC_SPONSORSHIP_COMMITTED, CHILD_CREATED, etc.)
- Timestamp

**Annotation Ideas:**
- "Actor: who did it"
- "Timestamp: when it happened"
- "Entity: what was changed"

**Callout Box:** "Full compliance-ready audit trail. Nothing is deleted. Nothing is hidden."

---

### SLIDE 19 — Fund Accounts and Cash Flow

**Title:** Know your financial position at all times

**Business Message:** JJT maintains live fund account balances, tracks every credit and debit, and produces a 6-month cash flow history — giving finance teams and boards a clear picture of organisational health.

**Screenshot Required:**
- Route: `/admin/funds`
- Capture: Fund accounts list showing balance, currency, min reserve indicator

**Suggested Diagram:**
- 6-month cash flow bar chart (horizontal bars or stepped line):
  Months on x-axis, Opening/Closing balance on y-axis
  Green = healthy, Amber = near reserve, Red = below reserve
  (Illustrative — not from actual data screenshot)

**Annotation Ideas:**
- "Live balance — updated on every receipt"
- "Minimum reserve threshold — system warns when breached"
- "Full transaction history exportable"

**Callout Box:** "PKR 2,45,000 available · PKR 20,000 minimum reserve · ✅ Healthy"

---

### SLIDE 20 — Alerts — Proactive Risk Detection

**Title:** Problems are flagged before they become crises

**Business Message:** JJT raises automatic alerts when sponsorships fall behind, funds drop below reserves, or system events require attention — so administrators don't need to search for problems.

**Screenshot Required:**
- Route: `/admin/alerts`
- Capture: Alert list showing severity badges (Critical/High/Medium/Low) and alert titles

**Highlight areas:**
- Severity colour coding (red/amber/grey)
- Dismiss button
- Sidebar badge showing count

**Annotation Ideas:**
- Arrow to red badge: "Critical — requires immediate action"
- Arrow to amber: "Medium — worth monitoring"
- Arrow to sidebar: "Badge count always visible"

**Callout Box:**
- "Alerts include: overdue payments, low fund balance, new public commitment awaiting review"

---

### SLIDE 21 — Reports and Exports

**Title:** Your data, your way

**Business Message:** JJT produces ready-to-share reports: a 6-month cash flow statement, a complete sponsorship portfolio summary, and one-click Excel or PDF exports for every module.

**Visual Direction:**
- Three export cards in a row:
  - 📊 **Portfolio Report** — Active/Pending/Expired counts, total monthly value
  - 💰 **Cash Flow Report** — 6-month balance history
  - 📥 **Exports** — Children (Excel), Donations (Excel), Reconciliation (Excel), Child Report (PDF)

**Suggested Diagram:**
- Portfolio report visualised as three circle/donut indicators:
  Active (green) · Pending (amber) · Expired (grey)
  Centre label: "PKR X,XX,XXX / month total commitment"

**Annotation Ideas:**
- "Download as Excel with one click"
- "Individual child PDF reports for sponsor packs"
- "Board-ready reconciliation report"

---

### SLIDE 22 — Why Organisations Choose JJT

**Title:** Replace chaos with confidence

**Business Message:** JJT gives organisations, sponsors, donors, and regulators exactly what they need — trust, accountability, and clarity — without replacing your team, just empowering them.

**Visual Direction:**
- Full-bleed forest green background (#1C352C)
- Six impact statements in large cream type, one per row, centred:
  - ✓ Every child enrolled, tracked, and accounted for
  - ✓ Every donation receipted and categorised
  - ✓ Every sponsorship approved, activated, monitored
  - ✓ Every payment reconciled against bank records
  - ✓ Every sponsor informed of their child's progress
  - ✓ Every action audited and available for review
- Bottom: JJT logo + "sponsorone.app" + gentle CTA: "Ready to run your organisation on JJT?"

**Animation:** Statements appear one by one from top, each with checkmark fade-in. Logo pulses softly at end.

---

## 5. SCREENSHOT PLAN

| Slide | Route | What to Capture | Crop / Focus |
|---|---|---|---|
| 08 | `/children` | Children grid | 6-8 cards, status badges visible |
| 09 | `/children/:childId/sponsor` | Steps 1, 2, 3 | Step progress bar + content area |
| 10 | `/children/:childId/sponsor` | Step 4 payment | Bank account card with copy buttons |
| 11 | `/admin/dashboard` | Full dashboard | Sidebar + main content, no truncation |
| 12a | `/admin/children` | Children list | List rows with sponsor info visible |
| 12b | `/admin/children` | Add child modal | Modal open, form fields visible |
| 12c | `/admin/children` | Detail panel | Sponsorship history + ledger expanded |
| 13 | `/admin/commitments` | Commitment list | PENDING and ACTIVE rows, action buttons |
| 14 | `/admin/reconciliation` | Recon view | Summary cards + payment rows with status badges |
| 15 | `/admin/zakat` | Zakat section | Headline stats + donation table |
| 16 | `/sponsor/portal` | Sponsor portal | Child expanded showing ledger + progress |
| 18 | `/admin/audit` | Audit log | Event list with actor, type, time |
| 19 | `/admin/funds` | Fund accounts | Balance cards with reserve indicator |
| 20 | `/admin/alerts` | Alert list | Severity badges visible, sidebar badge |

**Screenshot Notes:**
- All screenshots should be taken on a 1440px wide browser
- Use Chrome DevTools to set 1440×900 viewport for consistency
- Ensure test data is populated (at least 3 children, 2 sponsors, 2 sponsorships, some donations)
- Blur or anonymise any real personal data (names can be replaced with "Ahmed K.", "Sara M." etc.)
- Capture at 2x for retina quality

---

## 6. DESIGN GUIDELINES

### Brand Identity
- **Primary Name:** Junior Jinnah Trust (JJT)
- **Digital Brand:** sponsorone.app
- **Brand Voice:** Warm. Authoritative. Transparent. Purposeful.

### Colour Palette

| Role | Name | Hex | Usage |
|---|---|---|---|
| Primary Dark | Forest Green | `#1C352C` | Headlines, backgrounds, CTA buttons |
| Primary Mid | Sage Green | `#2F5D4F` | Section accents, icons, active states |
| Background Warm | Cream | `#F5EFE3` | Page backgrounds, slide backgrounds |
| Background Light | Linen | `#FAF7F2` | Card backgrounds, panel fills |
| Text Primary | Charcoal | `#1A1A1A` | Body text |
| Text Secondary | Olive | `#6B7C6B` | Captions, labels |
| Accent Amber | Warm Amber | `#C9942A` | Warnings, overdue badges |
| Accent Red | Soft Red | `#B03A2E` | Critical alerts, danger states |
| Accent Grey | Stone | `#8A958D` | Neutral badges, disabled states |
| White | Pure White | `#FFFFFF` | Text on dark backgrounds, card fills |

### Typography

| Role | Font | Weight | Size |
|---|---|---|---|
| Display / Hero | Playfair Display | 700 | 64–80px |
| Slide Title | Inter or DM Sans | 600 | 36–48px |
| Body / Caption | Inter | 400 | 16–20px |
| Monospace / Data | JetBrains Mono | 400 | 14px |
| Label / Badge | Inter | 700 | 11–13px, uppercase |

**Fallback font stack:** Georgia (display) / -apple-system, Helvetica Neue (body)

### Spacing & Layout
- Slides: 1920×1080px (16:9)
- Margin: 80px on all sides (safe zone)
- Grid: 12-column, 24px gutters
- Content width: max 1280px centred
- White space is intentional — never fill for the sake of filling

### Icon Style
- Line icons, 1.5px stroke weight
- Rounded corners on icon containers
- Use Phosphor Icons or Heroicons (available in Figma)
- Icon size in slides: 40–64px
- Never use emoji as primary visual elements

### Illustration Style
- Flat, minimal, geometric
- Islamic-inspired geometric motifs for background texture (subtle, 5% opacity)
- Human figures: simple silhouette / abstract (no photorealistic people)
- Data visualisations: clean bar or donut charts, no 3D effects

---

## 7. DIAGRAM SPECIFICATIONS

### Diagram 1 — Platform Journey Map (Slide 07)
**Type:** Horizontal flow / pipeline
**Stages:** 6 nodes connected by arrows
**Colours:** Alternate cream/sage green fill per node
**Icons:** Per stage (child, screen, handshake, checkmark, currency, chart)
**Arrows:** Solid, 2px, forest green
**Labels:** Bold stage name above, 1-line description below

### Diagram 2 — Three Audiences (Slide 06)
**Type:** Three-column card layout
**Each card:** Large icon (64px), role name (24px bold), 3-bullet feature list
**Background:** Light linen (#FAF7F2)
**Divider:** 1px solid #E0D8CC between cards

### Diagram 3 — Sponsorship Lifecycle (Slide 13)
**Type:** Three-state badge flow
**States:** PENDING (amber) → ACTIVE (green) → EXPIRED (grey)
**Layout:** Horizontal with right-pointing arrows
**Width:** Full content width

### Diagram 4 — Payment States (Slide 14)
**Type:** 5-badge colour strip
**Badges:** PENDING / RECEIVED / OVERDUE / PARTIAL / WAIVED
**Colours:** Grey / Green / Red / Amber / Stone
**Layout:** Horizontal, equal spacing

### Diagram 5 — Donation Types (Slide 15)
**Type:** Tag/pill group
**Tags:** 6 donation types as rounded pills in different green shades
**Layout:** Wrapped row, centred

### Diagram 6 — Cash Flow Chart (Slide 19)
**Type:** Grouped bar chart (illustrative only)
**X-axis:** Last 6 months (Jan–Jun)
**Y-axis:** PKR amount
**Bars:** Opening balance (sage) + Closing balance (forest green)
**Note:** Use illustrative sample values, not real data

### Diagram 7 — Portfolio Summary (Slide 21)
**Type:** Three donut/ring indicators side by side
**Left ring:** Active sponsorships (green fill)
**Centre ring:** Pending sponsorships (amber fill)
**Right ring:** Expired (grey fill)
**Centre label:** Count + percentage

---

## 8. ICON RECOMMENDATIONS

| Concept | Icon Name (Phosphor) | Usage |
|---|---|---|
| Child / Education | `GraduationCap` | Children module, enrolment |
| Sponsor / Handshake | `Handshake` | Commitment, partnerships |
| Money / Payment | `CurrencyDollar` or `Money` | Payments, fund accounts |
| Receipt / Document | `Receipt` | Donations, receipts |
| Audit / History | `ClockCounterClockwise` | Audit log |
| Alert / Warning | `Warning` or `BellRinging` | Alerts section |
| Shield / Trust | `ShieldCheck` | Security, transparency |
| Chart / Report | `ChartBar` | Reports |
| Users / Team | `UsersThree` | User management |
| Campaign | `Megaphone` | Campaigns |
| Zakat / Crescent | `MoonStars` | Zakat module |
| Export / Download | `DownloadSimple` | Exports |
| Settings / Config | `Gear` | Settings |
| Checkmark / Done | `CheckCircle` | Success states |
| Lock / Security | `Lock` | Authentication |
| Globe / Public | `Globe` | Public website |

---

## 9. VISUAL HIERARCHY

### Slide Layout Hierarchy (in order of visual weight)

1. **The ONE BIG IDEA** — Set in 48–80px display type. One sentence maximum. Top-left or centred.
2. **The Supporting Visual** — Screenshot or diagram. Takes 60–70% of the slide area.
3. **The Business Message** — Short paragraph (2–3 sentences) in 18–20px. Bottom-left panel or sidebar.
4. **Annotations / Callout Boxes** — Small labelled arrows pointing to key UI elements. 12–14px.
5. **Speaker Notes** — Not shown on slides. Detailed notes in notes pane for presenter.

### Do Not
- Use more than 3 typeface weights per slide
- Exceed 30 words of body text per slide
- Use gradients on text
- Use drop shadows on screenshots (subtle border or 2px frame only)
- Use red for anything other than critical alerts / errors
- Show technical URLs, JSON, or code

### Screenshot Framing
- All screenshots: Browser chrome removed (headless screenshot)
- Rounded corners: 12px on all screenshot frames
- Border: 1px solid rgba(0,0,0,0.08)
- Drop shadow: 0 4px 24px rgba(0,0,0,0.08)
- Scale: Full width or 60% width with diagram/text alongside

---

## 10. FINAL CLAUDE DESIGN PROMPT

---

```
PRESENTATION DESIGN REQUEST — Junior Jinnah Trust (JJT Platform)

You are designing a 22-slide investor/stakeholder presentation for Junior Jinnah Trust (JJT),
a complete digital platform for managing child education sponsorships. The platform is live at
sponsorone.app.

────────────────────────────────────────────────────────────────────
OBJECTIVE
────────────────────────────────────────────────────────────────────
Create a professional, minimal, story-driven presentation that explains
the JJT platform to a completely non-technical audience including NGO
owners, corporate CSR teams, investors, government representatives,
school owners, and charity board members.

The presentation must communicate BUSINESS VALUE and HUMAN IMPACT,
not technical implementation. A person who has never used software
should understand the entire system after viewing it.

────────────────────────────────────────────────────────────────────
TONE AND STYLE
────────────────────────────────────────────────────────────────────
- Warm, trustworthy, and professional
- Minimal and elegant (think: Apple keynote meets UNICEF annual report)
- Premium whitespace — never fill for the sake of filling
- One idea per slide, always
- Large visuals, small amounts of text
- No technical jargon, no code, no system architecture

Reference presentations: Apple product reveals, Stripe annual letter,
Linear product updates, UNICEF fundraising decks, World Vision impact reports.

────────────────────────────────────────────────────────────────────
SLIDE FORMAT
────────────────────────────────────────────────────────────────────
22 slides · 1920×1080px (16:9) · Widescreen

────────────────────────────────────────────────────────────────────
BRAND AND COLOUR PALETTE
────────────────────────────────────────────────────────────────────
Primary Dark (Forest Green): #1C352C — headlines, dark backgrounds, CTAs
Primary Mid (Sage Green): #2F5D4F — accents, icons, active states
Background Warm (Cream): #F5EFE3 — primary slide backgrounds
Background Light (Linen): #FAF7F2 — cards, panels
Text Primary (Charcoal): #1A1A1A — body text
Text Secondary (Olive): #6B7C6B — captions, labels
Accent Amber: #C9942A — warnings, pending states
Accent Red: #B03A2E — critical alerts only
Neutral Grey: #8A958D — expired, inactive states
White: #FFFFFF — text on dark slides, card fills

────────────────────────────────────────────────────────────────────
TYPOGRAPHY
────────────────────────────────────────────────────────────────────
Display: Playfair Display Bold (64–80px) — hero slides and title slides
Headline: Inter SemiBold (36–48px) — slide titles
Body: Inter Regular (16–20px) — descriptions and bullet points
Data: JetBrains Mono (14px) — numbers, amounts, codes only
Badge/Label: Inter Bold Uppercase (11–13px) — status badges

────────────────────────────────────────────────────────────────────
ICON STYLE
────────────────────────────────────────────────────────────────────
Phosphor Icons — line style, 1.5px stroke, rounded joints
Icon sizes: 40–64px in slides
Container: Light fill (10% primary green opacity) with 8px rounded corners
Do NOT use emoji as primary visual elements

────────────────────────────────────────────────────────────────────
ILLUSTRATION STYLE
────────────────────────────────────────────────────────────────────
Flat geometric shapes — no 3D effects
Subtle Islamic-inspired geometric pattern for backgrounds (arches, tessellation)
at 5–8% opacity maximum
Human figures: simple silhouette abstracts only
Data visualisations: clean, flat bars or donuts — no gradients, no shadows

────────────────────────────────────────────────────────────────────
SCREENSHOT TREATMENT
────────────────────────────────────────────────────────────────────
No browser chrome visible (pure app screenshots)
Frame: 12px rounded corners, 1px solid rgba(0,0,0,0.08) border
Shadow: 0 4px 24px rgba(0,0,0,0.08) — subtle, not dramatic
Scale: Full width for hero shots; 55–60% width when paired with annotation text

────────────────────────────────────────────────────────────────────
ANIMATION GUIDANCE (for animated versions)
────────────────────────────────────────────────────────────────────
- Fade + slight upward rise (12px): Default entrance for all elements
- Stagger delay: 0.25–0.35s between sequential items
- Duration: 0.4–0.6s per element
- Ease: ease-out cubic
- No bouncing, spinning, or flying elements
- Annotations (arrows, callout boxes): appear AFTER the screenshot settles
- Final slide: checkmarks appear one by one, logo pulses once at end

────────────────────────────────────────────────────────────────────
SLIDE DESCRIPTIONS
────────────────────────────────────────────────────────────────────

SLIDE 01 — TITLE / OPENER
Background: Full-bleed warm cream (#F5EFE3)
Centre: Large JJT logotype in Forest Green
Subtitle: "Education. Accountability. Impact." in Olive
Bottom right corner: "sponsorone.app" in Stone Grey, small
Subtle linen texture overlay

SLIDE 02 — THE PROBLEM
Title: "Managing a charity today means managing chaos"
Layout: 2×2 icon grid, each cell with icon + headline + 1-line description
Pain points: (1) Spreadsheet tracking, (2) Cash receipts, (3) WhatsApp updates, (4) Printed reports
Each cell: Soft warm-red background (#C0392B at 8% opacity)
Icons: Clipboard, Money, Chat, Document

SLIDE 03 — WHAT BREAKS
Title: "The cost of manual operations"
Layout: Three left-aligned statements in large type (28px)
Each preceded by a red warning dot (●)
Statements: "Sponsors don't know their money arrived" / "Children lose funding with no warning" / "Regulators find no records to audit"
Bottom: Subtle divider + "There is a better way." in Forest Green italic

SLIDE 04 — EXPECTATIONS CHANGED
Title: "Expectations have changed"
Layout: Three tall vertical cards on Forest Green (#1C352C) background
Card 1 (Cream text): Eye icon + "Visibility" + "I want to see my child's progress"
Card 2: Receipt icon + "Receipts" + "I need formal documentation for Zakat"
Card 3: Shield icon + "Trust" + "I need to know my money is used correctly"

SLIDE 05 — INTRODUCING JJT
Background: Full-bleed Forest Green (#1C352C)
Headline (white, Playfair Display 72px): "One platform. Every child. Full transparency."
Tagline (cream, Inter 22px): "Junior Jinnah Trust"
Background decoration: Subtle geometric Islamic pattern at 8% white opacity
Bottom: Three pill cards in cream — "Public Website · Admin Console · Sponsor Portal"

SLIDE 06 — THREE AUDIENCES
Title: "Built for everyone in the equation"
Layout: Three columns, separated by 1px dividers
Left: Globe icon + "Public Visitor" + bullets: Browse children / Commit online / No account needed
Centre (slightly elevated/featured): Shield icon + "Organisation Admin" + bullets: Manage children / Track money / Generate reports
Right: Heart icon + "Verified Sponsor" + bullets: See sponsored child / View ledger / Read progress updates

SLIDE 07 — HOW IT WORKS (JOURNEY MAP)
Title: "The journey from enrolment to impact"
Horizontal flow diagram (6 stages, left to right):
Stage 1: Child icon + "Child Enrolled" (cream fill)
Stage 2: Globe icon + "Listed Online" (light green fill)
Stage 3: Handshake icon + "Sponsor Commits" (cream fill)
Stage 4: Checkmark icon + "Admin Activates" (light green fill)
Stage 5: Money icon + "Payment Received" (cream fill)
Stage 6: Chart icon + "Progress Reported" (forest green fill, white text)
Connecting arrows: Forest green, 2px, solid

SLIDE 08 — CHILDREN LISTING (SCREENSHOT SLIDE)
Title: "Every available child is visible online"
Left 65%: Screenshot of /children page showing child card grid
Right 35%: 3 annotation callout boxes with arrows pointing to screenshot:
  → Status badge: "Real-time availability"
  → Search bar: "Search by name, city, or campus"
  → Monthly cost on card: "Education cost shown clearly"
Bottom message (small italic): "No account required to browse"

SLIDE 09 — 5-STEP FLOW (SCREENSHOT SLIDE)
Title: "Sponsoring a child takes five minutes"
Top: Step breadcrumb illustration showing 5 steps (Intention / Plan / Account / Payment / Confirmed)
Main area: Three side-by-side screenshots (Steps 1, 2, 3)
Annotation callouts:
  → "No login required"
  → "10% discount for annual commitment"
  → "Declare intention: Sadaqah or Zakat"

SLIDE 10 — BANK TRANSFER (SCREENSHOT SLIDE)
Title: "Payment instructions, one tap to copy"
Centre: Screenshot of payment step (Step 4) with bank details card
Annotation arrows:
  → Copy button: "One tap copies the IBAN"
  → Account details block: "Full bank details shown clearly"
Callout box (bottom): "After transfer — admin confirms receipt on the platform"

SLIDE 11 — ADMIN DASHBOARD (SCREENSHOT SLIDE)
Title: "Everything your organisation needs, in one place"
Full-width screenshot of /admin/dashboard
Annotation overlays (coloured boxes on screenshot):
  Green box: Fund balance card — "Live fund balance"
  Blue box: Children stats — "Enrolled vs. available"
  Amber box: Payment stats — "This month's collection status"
  Red dot: Sidebar alert badge — "Active alerts"
Callout box (bottom right): "20+ modules. One login."

SLIDE 12 — ENROLLING CHILDREN (SCREENSHOT SLIDE)
Title: "From zero to enrolled in seconds"
Left 55%: Screenshot of /admin/children with detail panel open
Right 45%: Three annotation boxes:
  Box 1: "Add individually or import from Excel"
  Box 2: "Full sponsorship history per child"
  Box 3: "Month-by-month education ledger"
Small screenshot inset (bottom left): Add Child modal form

SLIDE 13 — SPONSORSHIP LIFECYCLE
Title: "Every commitment requires admin approval"
Centre: Large sponsorship lifecycle diagram:
  PENDING (amber pill) → [arrow] → ACTIVE (green pill) → [arrow] → EXPIRED (grey pill)
Left panel (40%): Screenshot of /admin/commitments showing pending and active rows
Right panel (60%): Lifecycle diagram + annotation:
  "Sponsors commit online — admin approves"
  "One click to activate or expire"
  "Child becomes available again after expiry"

SLIDE 14 — PAYMENT RECONCILIATION (SCREENSHOT SLIDE)
Title: "Know exactly who paid, who hasn't, and who is at risk"
Screenshot of /admin/reconciliation showing summary cards + payment list
Annotation callouts:
  → Summary cards at top: "Total expected vs. actually received"
  → Overdue badge in list: "At-risk flagged automatically"
  → Bank reference column: "Bank reference recorded per payment"
Bottom callout box: "At-risk detection: consecutive overdue months trigger alerts"
Small diagram: 5-state payment badge strip: PENDING / RECEIVED / OVERDUE / PARTIAL / WAIVED

SLIDE 15 — DONATIONS AND ZAKAT (SCREENSHOT SLIDE)
Title: "Every type of giving is recorded and receipted"
Left 55%: Screenshot of /admin/zakat showing headline stats + donation table
Right 45%: 
  - Donation type pills (6 types in shades of green)
  - Three feature bullets:
    ✓ Zakat tracked separately from general funds
    ✓ Printable PDF receipt for every donation
    ✓ Recurring schedules: monthly, quarterly, annual

SLIDE 16 — SPONSOR PORTAL (SCREENSHOT SLIDE)
Title: "Sponsors see their impact, not your spreadsheets"
Screenshot of /sponsor/portal with child expanded (showing ledger + progress)
Annotation callouts:
  → Ledger table: "Month-by-month education ledger"
  → Progress updates: "Progress updates stored here — and emailed"
  → Header: "Scoped to their children only"
Callout box: "Private. Secure. Always up to date."

SLIDE 17 — AUTOMATIC PROGRESS UPDATES
Title: "Sponsors receive progress reports automatically"
Layout: Two-panel split with arrow between
Left panel (Forest Green background):
  Admin form mockup: Child selector + Month + Summary text area
  Label: "Admin enters update"
Centre: Large right arrow with label "Automatic"
Right panel (Cream background):
  Illustrated email preview mockup showing:
  "Progress Update: [Child Name]" subject line
  Short paragraph of progress text
  JJT branding in header
  Label: "Sponsor receives email"
Bottom small note: "Full history stored in sponsor portal"

SLIDE 18 — AUDIT LOG (SCREENSHOT SLIDE)
Title: "A complete record of every action, ever taken"
Screenshot of /admin/audit showing audit event table
Annotation callouts:
  → Actor column: "Who did it"
  → Event type column: "What action was taken"
  → Timestamp column: "When it happened"
Callout box (prominent, bottom): "Full compliance-ready audit trail. Nothing is deleted. Nothing is hidden."

SLIDE 19 — FUND ACCOUNTS AND CASH FLOW
Title: "Know your financial position at all times"
Left 50%: Screenshot of /admin/funds showing fund balance cards
Right 50%: Illustrative 6-month cash flow bar chart
  X-axis: Last 6 months (abbreviated)
  Y-axis: Fund balance (PKR)
  Bars: Opening (light sage) vs Closing (forest green) per month
  Amber horizontal line: Minimum reserve threshold
Annotation: "Warning when balance approaches minimum reserve"

SLIDE 20 — ALERTS (SCREENSHOT SLIDE)
Title: "Problems are flagged before they become crises"
Screenshot of /admin/alerts showing alert list with severity badges
Annotation callouts:
  → Red (CRITICAL/HIGH) badge: "Requires immediate action"
  → Amber (MEDIUM) badge: "Monitoring recommended"
  → Sidebar badge in corner: "Always visible — wherever you are in the system"
Callout box with three alert examples:
  "New sponsorship commitment awaiting review"
  "Fund balance below minimum reserve"
  "Sponsor payment 30 days overdue"

SLIDE 21 — REPORTS AND EXPORTS
Title: "Your data, your way"
Layout: Three cards in a row
Card 1 (light green fill): ChartBar icon + "Portfolio Report"
  Bullets: Active/Pending/Expired counts · Total monthly value · Commitment type breakdown
  Mini donut chart illustration (3 segments: green/amber/grey)
Card 2 (cream fill): TrendUp icon + "Cash Flow Report"
  Bullets: 6-month balance history · Credits vs debits · Current balance
  Mini bar chart illustration
Card 3 (white fill, green border): DownloadSimple icon + "One-Click Exports"
  Bullets: Children list (Excel) · Donations (Excel) · Reconciliation (Excel) · Child PDF report

SLIDE 22 — CLOSING / WHY JJT
Background: Full-bleed Forest Green (#1C352C)
Six impact statements in cream, large type (28px), centred, full width:
  ✓ Every child enrolled, tracked, and accounted for
  ✓ Every donation receipted and categorised
  ✓ Every sponsorship approved, activated, monitored
  ✓ Every payment reconciled against bank records
  ✓ Every sponsor informed of their child's progress
  ✓ Every action audited and available for review
Bottom: JJT logotype in cream + "sponsorone.app" in Sage Green
Final CTA (small, bottom centre): "Ready to run your organisation on JJT?"

────────────────────────────────────────────────────────────────────
LAYOUT RULES
────────────────────────────────────────────────────────────────────
- Safe margin: 80px all sides
- Content max-width: 1280px centred
- Grid: 12 columns, 24px gutters
- Screenshot frames: 12px border-radius, subtle shadow
- Never exceed 30 words of text per slide (excluding annotation callouts)
- Callout boxes: cream background, 1px sage green border, 8px radius, 14px text, Forest Green label
- Annotation arrows: 1.5px Forest Green, with small circular endpoint

────────────────────────────────────────────────────────────────────
WHAT NOT TO INCLUDE
────────────────────────────────────────────────────────────────────
- No technical terms (API, JWT, microservices, database, etc.)
- No code snippets
- No URL paths or route strings
- No real personal data (use placeholder names like "Ahmed K.", "Sara M.")
- No features not yet implemented (no photos/media uploads, no online payment, no mobile app)
- No competitor comparisons
- No roadmap or future features

────────────────────────────────────────────────────────────────────
FINAL DELIVERABLE
────────────────────────────────────────────────────────────────────
22 slides, designed at 1920×1080px, suitable for:
- PowerPoint / Google Slides export
- PDF for board packets
- Keynote for live presentation
- Web-based slide format

The presentation should feel like one continuous story — not a
feature list — building from problem to solution to impact.
```

---

*End of JJT Presentation Design Brief*
*Document version: 1.0 · Based on codebase audit of feat/firebase-auth-jjt branch*
*Prepared by: Claude Code (Architecture + Code Audit)*
