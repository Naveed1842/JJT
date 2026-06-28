# Phase 2 Business Requirements Document
## Junior Jinnah Trust — Financial Governance & Fundraising Platform

| | |
|---|---|
| **Document Version** | 1.0 |
| **Supersedes** | Phase 1 BRD v1.0 (27 June 2026) |
| **Prepared** | 2026-06-28 |
| **Status** | Draft for Review |
| **Classification** | Strategic — Internal |

---

## 1. Executive Summary

Phase 1 successfully transformed JJT from a spreadsheet-based operation into a structured, auditable commitment-tracking platform. Children are enrolled, sponsors are recorded, and an immutable ledger documents which months are covered. This was the right foundation.

Phase 2 transforms the platform from a **commitment-tracking system** into a **financial management and fundraising platform**. The distinction is fundamental: Phase 1 records *intent*; Phase 2 records *reality*.

After Phase 2, JJT will be able to answer questions it cannot currently answer:
- How much money does the organisation actually hold today?
- Which children are at financial risk right now?
- Has every sponsor paid this month?
- Can we afford to enroll 20 more children next quarter?
- How are Zakat funds being utilised, and can we demonstrate compliance?
- What is our 12-month cash forecast?

Phase 2 is not a feature list. It is an organisational capability upgrade.

---

## 2. Phase 1 → Phase 2 Transition

### What Phase 1 Delivered

| Capability | Status |
|-----------|--------|
| Child registration and ledger | ✓ Complete |
| Sponsorship commitment lifecycle (PENDING → ACTIVE → EXPIRED) | ✓ Complete |
| Sponsor portal (read-only) | ✓ Complete |
| Public sponsorship commitment form | ✓ Complete |
| JWT authentication and RBAC | ✓ Complete |
| Append-only ledger (audit trail for coverage) | ✓ Complete |
| Admin dashboard for operations | ✓ Complete |

### What Phase 1 Did Not Deliver (Phase 2 Scope)

| Capability | Gap Impact |
|-----------|-----------|
| Payment receipt recording | Organisation cannot confirm money was received |
| Central fund balance tracking | Organisation cannot know if it can afford more children |
| Monthly payment reconciliation | Sponsors who stop paying go undetected |
| Fund accounting (Zakat, Sadaqah, campaigns) | Restricted funds cannot be managed |
| Donation management | General donors have no pathway |
| Financial reporting | No reports exist |
| Audit attribution (created_by on records) | Cannot satisfy external audit requirement |
| Multi-organisation foundation | Single-tenant only; expensive to change later |
| Budget management | No forecast or variance capability |
| Email notifications | Manual follow-up for everything |

---

## 3. Phase 2 Business Objectives

| ID | Objective | Success Metric |
|----|-----------|---------------|
| BO2-01 | Enable real-time visibility into the organisation's financial position | Admin can view fund balances, committed funds, and available funds at any time |
| BO2-02 | Eliminate the risk of sponsorships remaining ACTIVE while payments have lapsed | Zero children marked ACTIVE with no payment received in 45+ days |
| BO2-03 | Create a compliant, auditable financial trail suitable for charity registration | Every financial event has creator, timestamp, and justification |
| BO2-04 | Support multiple fund types (Zakat, Sadaqah, Campaign) with appropriate restrictions | Zakat funds are never co-mingled with general funds |
| BO2-05 | Provide donors with transparent impact reporting | Every donor can see exactly how their money was used |
| BO2-06 | Enable fundraising campaigns with target tracking and performance reporting | Campaign performance measurable vs target in real time |
| BO2-07 | Lay the architectural foundation for multi-organisation support | Organisation ID on every entity; no schema migration required to add a second organisation |
| BO2-08 | Provide executive dashboards enabling board-level financial oversight | Board can review programme health without spreadsheet exports |
| BO2-09 | Automate routine administrative tasks (reminders, alerts, expected payments) | 30% reduction in manual admin for monthly reconciliation |
| BO2-10 | Support overseas donors with multi-currency payment recording | UK/UAE/US donors' payments recorded in their currency with PKR conversion |

---

## 4. Phase 2 Stakeholders

| Stakeholder | New Role in Phase 2 |
|------------|-------------------|
| **JJT Board** | Consumers of executive financial dashboards |
| **JJT Finance Team** | Operators of fund accounting, payment reconciliation, reporting |
| **JJT Admins** | Operators of enhanced admin panel with financial workflows |
| **Sponsors** | Recipients of payment receipts, monthly impact reports |
| **General Donors** | New persona: donors who give without sponsoring a specific child |
| **Corporate CSR Sponsors** | New persona: organisations funding multiple children |
| **Campaign Donors** | New persona: donors responding to specific appeals |
| **External Auditors** | Consumers of audit trail export |
| **Sharia Compliance Advisor** | Reviewer of Zakat fund management |

---

## 5. New User Roles

### Existing Roles (Expanded Permissions)

| Role | Phase 1 | Phase 2 Additions |
|------|---------|------------------|
| `JJT_ADMIN` | Full access | Approve large transactions; manage fund accounts; generate audit exports |
| `ORG_ADMIN` | Operations | Record payments; manage monthly reconciliation; view reports |
| `SPONSOR` | Read-only portal | Receive payment receipts; view payment history; pay online (Phase 2b) |

### New Roles

| Role | Permissions |
|------|-------------|
| `FINANCE_ADMIN` | Fund account management; all financial transactions; reporting; audit export. Cannot manage children or sponsorships directly. |
| `BOARD_MEMBER` | Read-only dashboard access; executive reports; fund balances. No operational access. |
| `CAMPAIGN_MANAGER` | Create and manage campaigns; view campaign fund; record campaign donations. |
| `DONOR` | Self-registered; view own donation history; download receipts. |

---

## 6. Phase 2 Functional Requirements

### Module 1 — Fund Account Management

**Purpose:** Introduce fund accounting as the financial backbone of the platform.

| ID | Requirement | Priority |
|----|-------------|----------|
| FM-01 | The system shall support the creation of named Fund Accounts with type: `GENERAL`, `ZAKAT`, `SADAQAH`, `CAMPAIGN`, `EMERGENCY`, `GRANT`, `CORPORATE_CSR` | Must Have |
| FM-02 | Each Fund Account shall maintain a running balance calculated from append-only `FundTransaction` entries (CREDIT/DEBIT) | Must Have |
| FM-03 | The system shall prevent a DEBIT transaction that would reduce a Fund Account balance below its configured minimum reserve, unless explicitly overridden with a recorded justification | Must Have |
| FM-04 | Fund Account balances shall be queryable at any point in time (historical balance at any date) | Must Have |
| FM-05 | The `ZAKAT` fund shall enforce restrictions: no transfers out to non-Zakat-eligible purposes; must be fully disbursed within the Islamic year of collection | Must Have |
| FM-06 | Campaign funds shall have a target amount and end date; the system shall close the campaign fund and flag unspent balance when the end date is reached | Must Have |
| FM-07 | Fund transfers between accounts shall require a recorded reason and, above a configurable threshold, a second approver | Should Have |
| FM-08 | Every `FundTransaction` shall record: created_by, created_at, linked entity (donation/payment/ledger entry), description, external reference | Must Have |
| FM-09 | The system shall provide a real-time "Available Funds" view: Total Fund Balance − Reserved (committed children months) | Must Have |

---

### Module 2 — Donation Management

**Purpose:** Capture all forms of financial contribution to the organisation.

| ID | Requirement | Priority |
|----|-------------|----------|
| DON-01 | The system shall support recording donations with the following types: `SPONSORSHIP_PAYMENT`, `ONE_TIME`, `RECURRING_MONTHLY`, `CAMPAIGN`, `ZAKAT`, `SADAQAH`, `ANONYMOUS`, `GRANT`, `CORPORATE_CSR`, `GIFT_AID_RECLAIM` | Must Have |
| DON-02 | Each donation shall record: donor identity (or ANONYMOUS), amount, currency, date received, payment method, bank reference, fund account credited, and receipt status | Must Have |
| DON-03 | The system shall generate a donor receipt (PDF exportable) for every donation, with a unique receipt number, JJT registration details, and the donation amount in both the donor's currency and PKR equivalent | Must Have |
| DON-04 | Anonymous donations shall increase the General Fund with no donor identity recorded. A receipt cannot be generated for anonymous donations | Must Have |
| DON-05 | Recurring monthly donation commitments shall create a `RecurringDonationSchedule` with expected monthly amounts and payment due dates | Should Have |
| DON-06 | The system shall alert finance admins when a recurring donation is overdue by more than 14 days | Should Have |
| DON-07 | Zakat donations shall be automatically routed to the Zakat Fund Account, not the General Fund | Must Have |
| DON-08 | Corporate donations shall be associated with a `CorporateAccount` entity, enabling aggregate reporting across multiple donations from one organisation | Should Have |
| DON-09 | General (unrestricted) donations shall increase the General Education Fund and be available immediately for early support expenditure | Must Have |
| DON-10 | Gift Aid eligibility shall be capturable for UK donors: first name, last name, full address, declaration date, and confirmation that they are a UK taxpayer | Should Have |

---

### Module 3 — Sponsorship Payment Lifecycle

**Purpose:** Close the gap between sponsorship commitment and actual verified payment.

| ID | Requirement | Priority |
|----|-------------|----------|
| PAY-01 | For every ACTIVE sponsorship, the system shall automatically create a `SponsorPayment` record at the start of each month with status `EXPECTED` | Must Have |
| PAY-02 | A `SponsorPayment` shall have statuses: `EXPECTED`, `RECEIVED`, `PARTIAL`, `OVERDUE`, `WAIVED`, `PREPAID` | Must Have |
| PAY-03 | When an admin records a payment as received, the system shall: (a) update `SponsorPayment` to RECEIVED, (b) create a `Donation` of type `SPONSORSHIP_PAYMENT`, (c) create a `FundTransaction` crediting the General Fund, (d) create a `LedgerEntry` of type `SPONSOR` for the child's month | Must Have |
| PAY-04 | If a `SponsorPayment` remains `EXPECTED` after the configured payment due date (default: 15th of the month), the system shall automatically transition it to `OVERDUE` | Must Have |
| PAY-05 | Two consecutive `OVERDUE` months shall trigger an admin alert recommending review of the sponsorship | Must Have |
| PAY-06 | Three consecutive `OVERDUE` months shall raise an escalation flag requiring explicit admin action (renew, waive, or expire the sponsorship) | Must Have |
| PAY-07 | A sponsor may prepay up to 12 months in advance. Prepaid months shall be recorded as individual `SponsorPayment` records with status `PREPAID` | Should Have |
| PAY-08 | Partial payments shall be recorded with the received amount; the shortfall shall be tracked and the `SponsorPayment` status set to `PARTIAL` | Should Have |
| PAY-09 | A `WAIVED` payment indicates the organisation chose not to pursue the payment for a recorded reason (e.g. sponsor hardship). Waived months shall be covered from the General Fund | Should Have |
| PAY-10 | The system shall produce a monthly reconciliation report showing all expected, received, partial, and overdue payments for the current month | Must Have |

---

### Module 4 — Child Financial Lifecycle

**Purpose:** Extend child records with full financial context.

| ID | Requirement | Priority |
|----|-------------|----------|
| CFL-01 | Each child shall have an annual education budget, approved at enrollment, covering all expected expense categories for the academic year | Should Have |
| CFL-02 | The system shall support expense categories on ledger entries: `TUITION`, `BOOKS`, `UNIFORM`, `TRANSPORT`, `EXAM_FEES`, `MEALS`, `EMERGENCY`, `ADMIN_OVERHEAD` | Should Have |
| CFL-03 | A child shall have an explicit lifecycle status: `ENROLLED`, `ACTIVE_SUPPORTED`, `SPONSOR_SOUGHT`, `GRADUATED`, `WITHDRAWN`, `TRANSFERRED`, `ARCHIVED` | Must Have |
| CFL-04 | When a child graduates, the system shall: expire all active sponsorships, produce a final impact report (years supported, total funds invested, progress summary), and archive the record | Should Have |
| CFL-05 | Children archived for more than 7 years shall be flagged for data retention review | Should Have |
| CFL-06 | The system shall track the education level of each child and alert when a child is approaching completion of their current level (enabling proactive programme decisions) | Could Have |
| CFL-07 | If a child's school fees change, the change shall apply from the recorded effective month. Historical ledger entries shall not be modified. | Must Have |
| CFL-08 | A "funding forecast" shall show for each child: how many months of committed coverage exist, the next uncovered month, and whether the general fund can cover that month | Must Have |

---

### Module 5 — Campaign Management

**Purpose:** Enable time-bound fundraising campaigns with financial tracking.

| ID | Requirement | Priority |
|----|-------------|----------|
| CAMP-01 | The system shall support creating Campaigns with: name, description, target amount, start date, end date, associated fund account, and eligible expense types | Must Have |
| CAMP-02 | A Campaign shall have statuses: `DRAFT`, `ACTIVE`, `FUNDED` (target met), `CLOSED`, `ARCHIVED` | Must Have |
| CAMP-03 | Donations made against an ACTIVE Campaign shall be credited to the Campaign's Fund Account | Must Have |
| CAMP-04 | A Campaign shall display a real-time progress bar: amount raised vs target | Must Have |
| CAMP-05 | When a Campaign reaches its target, it shall automatically transition to `FUNDED` and prevent further donations unless explicitly reopened | Should Have |
| CAMP-06 | At campaign close, the system shall produce a performance report: donations received, donors count, average donation, children funded, fund utilisation | Must Have |
| CAMP-07 | Unspent campaign funds at close shall be flagged for disposition: transfer to General Fund (with donor consent policy), return to donors, or roll over to next campaign | Must Have |
| CAMP-08 | A public-facing campaign page shall show the campaign goal, progress, and a donation form (anonymous or identified) | Should Have |

---

### Module 6 — Audit Trail (Full Implementation)

**Purpose:** Every financial event must be attributable, immutable, and exportable.

| ID | Requirement | Priority |
|----|-------------|----------|
| AUD-01 | All financial entities (FundTransaction, Donation, SponsorPayment, LedgerEntry, PaymentRecord) shall record `created_by` (user UUID) and `created_at` (timestamp) | Must Have |
| AUD-02 | All status transitions on sponsorships, payments, and fund accounts shall record the user who performed the transition and the reason | Must Have |
| AUD-03 | An immutable `AuditEvent` shall be created for every financial action, capturing: entity type, entity ID, action type, before state, after state, user, timestamp, IP address, reason | Must Have |
| AUD-04 | AuditEvents shall never be modified or deleted. A separate append-only audit table shall be maintained | Must Have |
| AUD-05 | The system shall produce a tamper-evident annual audit export in machine-readable format (JSON/CSV) for all financial transactions in a given calendar year | Must Have |
| AUD-06 | Supporting documents (bank statement screenshots, payment confirmations) shall be attachable to Donation and SponsorPayment records | Should Have |
| AUD-07 | A "Who did this?" query interface shall allow admins to search audit events by entity, user, date range, and action type | Should Have |
| AUD-08 | The four-eyes principle shall apply: any transaction above a configurable threshold (default PKR 50,000) shall require a second approver. The audit trail shall record both the creator and approver | Should Have |

---

### Module 7 — Multi-Organisation Foundation

**Purpose:** Lay the architectural groundwork for serving multiple organisations without a future rewrite.

| ID | Requirement | Priority |
|----|-------------|----------|
| ORG-01 | An `Organisation` entity shall be created. Every business entity (Children, Sponsors, Sponsorships, LedgerEntries, FundAccounts, etc.) shall carry an `organisation_id` foreign key | Must Have |
| ORG-02 | All API queries shall filter by the authenticated user's `organisation_id`. Cross-organisation data access shall be impossible for any role except a future `PLATFORM_ADMIN` | Must Have |
| ORG-03 | JJT shall be seeded as `organisation_id = "JJT-001"` in the initial migration | Must Have |
| ORG-04 | Organisation-level configuration shall be stored in the `organisations` table: name, country, base currency, Zakat year type (Hijri/Gregorian), minimum fund reserve, payment due day | Must Have |
| ORG-05 | An `organisations` API endpoint shall allow a `PLATFORM_ADMIN` to create new organisations (not exposed to JJT_ADMIN) | Should Have |
| ORG-06 | Fund accounts, campaigns, and reporting shall all be organisation-scoped | Must Have |

---

### Module 8 — Notifications

**Purpose:** Replace all manual follow-up with automated, targeted communications.

| ID | Requirement | Priority |
|----|-------------|----------|
| NOTIF-01 | The system shall send email notifications to sponsors: commitment confirmed, payment received, progress update published, sponsorship expiry approaching | Must Have |
| NOTIF-02 | The system shall send admin alerts: payment overdue (7 days), payment overdue (escalation at 60 days), fund balance below minimum reserve, child coverage lapsed | Must Have |
| NOTIF-03 | The system shall send campaign update notifications to campaign donors: target reached, campaign closed, fund utilisation report | Should Have |
| NOTIF-04 | Notification templates shall be configurable per organisation | Should Have |
| NOTIF-05 | An email delivery status log shall track sent, delivered, opened, and bounced emails for each notification | Should Have |
| NOTIF-06 | Sponsors shall be able to set notification preferences (frequency, types) | Could Have |

---

### Module 9 — Reporting & Dashboards

**Purpose:** Enable data-driven decision-making at every level of the organisation.

| ID | Requirement | Priority |
|----|-------------|----------|
| REP-01 | The system shall provide a real-time executive dashboard with: total fund balance, reserved vs available, children funded this month, payment collection rate, at-risk children count | Must Have |
| REP-02 | The system shall provide a monthly reconciliation report: all sponsorships, expected payments, received payments, outstanding balance | Must Have |
| REP-03 | The system shall provide a fund utilisation report: how each fund's money was allocated and spent | Must Have |
| REP-04 | The system shall provide a donor report: total donations by donor, by type, by period | Must Have |
| REP-05 | The system shall provide a cash flow report: monthly inflows and outflows with 3-month forecast | Should Have |
| REP-06 | The system shall provide a campaign performance report for each campaign | Should Have |
| REP-07 | The system shall provide a per-child financial summary: total invested, coverage months, sponsorship history, progress updates | Should Have |
| REP-08 | All reports shall be exportable to CSV and PDF | Should Have |
| REP-09 | A sponsor-facing impact report shall be generated annually for each sponsor showing: months funded, children supported, total contribution, child progress summary | Should Have |

---

## 7. Non-Functional Requirements (Phase 2)

### Security
| ID | Requirement |
|----|-------------|
| NFR2-S01 | Financial data access shall follow the principle of least privilege. SPONSOR role cannot access any financial aggregate data |
| NFR2-S02 | The four-eyes approval mechanism for large transactions shall be enforced server-side; client-side bypass shall be impossible |
| NFR2-S03 | Audit events shall be written to a separate, append-only database table. The application user shall have INSERT but not UPDATE or DELETE on this table |
| NFR2-S04 | All donor personal data shall be encrypted at rest using database-level encryption or field-level encryption for PII fields |

### Compliance
| ID | Requirement |
|----|-------------|
| NFR2-C01 | The platform shall support the production of financial statements compliant with Not-for-Profit accounting standards applicable in Pakistan (ICAP) |
| NFR2-C02 | Zakat fund management shall comply with published Sharia guidelines for Zakat disbursement. A Sharia compliance review shall be performed before Zakat fund goes live |
| NFR2-C03 | Gift Aid functionality (if implemented) shall comply with HMRC requirements for UK charity reclaims |
| NFR2-C04 | Data retention periods shall be enforced: financial records retained 7 years minimum |

### Performance
| ID | Requirement |
|----|-------------|
| NFR2-P01 | Fund balance calculations shall complete in under 200ms for any fund account with up to 100,000 transactions |
| NFR2-P02 | The executive dashboard shall load in under 2 seconds at the 95th percentile |
| NFR2-P03 | Monthly reconciliation reports shall generate in under 5 seconds for up to 1,000 active sponsorships |

---

## 8. Open Questions for Phase 2

| ID | Question | Owner | Impact |
|----|----------|-------|--------|
| OQ2-01 | What is JJT's official Islamic finance position on Zakat disbursement categories? Can Zakat fund education fees directly, or only personal assistance? | JJT Sharia Advisor | Determines Zakat fund eligible expense types |
| OQ2-02 | Does JJT intend to seek formal charity registration in Pakistan (SECP) or the UK (HMRC)? | JJT Executive | Determines compliance requirements for audit, Gift Aid |
| OQ2-03 | What is the intended public-facing donation pathway? Will there be an online payment gateway (JazzCash, EasyPaisa, Stripe)? | JJT Executive | Determines Phase 2b payment integration scope |
| OQ2-04 | Should the sponsor portal be enhanced to allow sponsors to self-record payment receipt, or remain admin-only? | JJT Admin Team | Affects admin workload and trust model |
| OQ2-05 | What is the policy for overfunded campaigns? | JJT Finance | Required before campaign module goes live |
| OQ2-06 | Should general donors be able to self-register accounts (not just sponsor accounts)? | JJT Executive | New persona requires new registration flow |
| OQ2-07 | What is the payment due date for monthly sponsors? | JJT Admin | Required for automated OVERDUE detection |
