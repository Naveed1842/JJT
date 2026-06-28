# Business Rule Gap Analysis
## Junior Jinnah Trust — BRD Completeness Review

| | |
|---|---|
| **Document Version** | 1.0 |
| **Classification** | Strategic — Internal |
| **Prepared** | 2026-06-28 |
| **Status** | For Review |

---

## Methodology

The current BRD (v1.0, 27 June 2026) contains 12 business rules (BR-01 through BR-12). This analysis reviews each existing rule, identifies whether it is fully implemented, and then identifies business rules that are entirely absent from the BRD but should exist.

---

## Part 1 — Audit of Existing Business Rules

### BR-01: Child-Ledger Atomicity

> A child's Education Support Ledger is created atomically with the child record.

**Status:** ✓ Implemented. `CreateChildUseCase` creates both in one transaction.

**Gap:** The rule is correct but incomplete. It does not address what happens if a child is deleted (should the ledger be deleted? Currently no soft-delete exists). Recommend adding:

> BR-01b: A child record cannot be deleted while it has ledger entries. Archiving (soft delete) is the only permitted exit.

---

### BR-02: Future Start Month for Sponsorship

> A sponsorship's `startMonth` must be strictly later than the current calendar month.

**Status:** ✓ Implemented. Domain validates this.

**Gap:** The BRD does not say *how far in the future* a start month can be. Can an admin create a sponsorship starting 3 years from now? There is no upper bound. Recommend:

> BR-02b: A sponsorship start month may not be more than 12 calendar months in the future.

---

### BR-03: One Active Sponsorship Per Child

> Only one `ACTIVE` or `PENDING` sponsorship is permitted per child at any time.

**Status:** ✓ Implemented at application level.

**Gap 1:** This rule is not enforced at the database level (V4 migration dropped the unique index). A race condition between two simultaneous admin actions could create two ACTIVE sponsorships. A database-level unique partial index should be added:

```sql
CREATE UNIQUE INDEX idx_one_active_per_child
ON sponsorships (child_id)
WHERE status IN ('ACTIVE', 'PENDING');
```

**Gap 2:** The rule explicitly prevents co-sponsorship. This is a policy decision that should be documented as an explicit choice, not an oversight. If JJT ever wants two sponsors to share one child's cost, this constraint must be relaxed. Document the decision.

---

### BR-04: Ledger Entry Belongs to Own Child

> A ledger entry may only be appended to the ledger belonging to its own child.

**Status:** ✓ Implemented in domain (`EducationSupportLedger.appendEntry()`).

**Gap:** The validation error message if this rule fires is "ProgressUpdate month must match an existing ledger entry" — which is actually BR-06's error message. The two error paths are conflated. Recommend distinct error codes for each invariant violation.

---

### BR-05: One Ledger Entry Per Month Per Child

> No two ledger entries may share the same month for the same child.

**Status:** ✓ Implemented in domain + DB unique constraint.

**Gap:** The unique constraint only prevents exact duplicates. It does not prevent a CORRECTION entry for a month that already has a SPONSOR entry. Once `CORRECTION` coverage type is added (recommended in Phase 2), this rule needs revision:

> BR-05b: A `CORRECTION` entry may exist alongside a corrected entry for the same month, but the original entry must be marked as superseded.

---

### BR-06: Progress Requires Ledger Entry

> A progress update may only be recorded for a month that already has a ledger entry for that child.

**Status:** ✓ Implemented in domain.

**Missing Rule:** The BRD does not specify whether a CORRECTION entry (superseding a previous entry) still permits a progress update. If a month's ledger entry is corrected, the progress update for that month should remain valid (since the month was still funded, just at a different amount). The correction affects the financial record, not the educational record.

---

### BR-07: Append-Only Ledger

> Existing entries may not be modified or deleted.

**Status:** ✓ Implemented. No UPDATE or DELETE issued on ledger tables.

**Gap:** This rule is enforced by convention (no code that does it), not by a database-level constraint. There is nothing preventing a future developer from accidentally adding a DELETE to a use case. Recommend adding a Postgres trigger to enforce this at the database level:

```sql
CREATE RULE no_ledger_entry_update AS ON UPDATE TO ledger_entries DO INSTEAD NOTHING;
CREATE RULE no_ledger_entry_delete AS ON DELETE TO ledger_entries DO INSTEAD NOTHING;
```

---

### BR-08: Sponsors Are Read-Only

> Sponsors have read-only access; they cannot create, modify, or delete any records.

**Status:** ✓ Implemented via `@PreAuthorize`.

**Gap:** This rule is correct but does not address a related question: can a sponsor update their own profile information (display name, phone number, email)? Currently there is no mechanism for a sponsor to update anything, including their own contact details. A sponsor whose email changes has no self-service option — they must contact an admin.

> Recommended additional rule: A SPONSOR may update their own display name, phone number, and notification preferences. They may not update their email or password without admin involvement.

---

### BR-09: Sponsor Data Isolation

> Sponsor data access is scoped exclusively to children linked to their own `sponsorId` JWT claim.

**Status:** ✓ Implemented.

**Gap:** The JWT contains `sponsorId` as a claim. If an admin updates a user's `sponsor_id` in the database (as we did in this session for Nadeem), the user must re-authenticate to receive the updated claim. This is correct JWT behaviour but it is not documented as a known operational step. A user management log entry should be created when `sponsor_id` is changed.

> Recommended addition to business rules: Changing a user's `sponsor_id` association requires the affected user to log out and log back in. Admins performing this change must notify the affected sponsor.

---

### BR-10: Public Commitment Uses Next Month

> The start month for a public sponsorship commitment is always the next calendar month.

**Status:** ✓ Implemented.

**Gap:** What happens if a public commitment is submitted on the last day of the month at 11:59 PM? The "next month" calculation is based on server time in the default JVM timezone. This could produce unexpected results for sponsors in different time zones. Recommend:
- Explicitly document the server timezone (PKT, UTC+5)
- Clarify that "next calendar month" is calculated in PKT

---

### BR-11: Deactivated Users Cannot Authenticate

> A deactivated user account cannot authenticate.

**Status:** ✓ Implemented.

**Gap:** There is no rule about what happens to an authenticated user who is deactivated *mid-session*. Their existing access token remains valid until it expires (up to 15 minutes). For most deactivation scenarios this is acceptable. For emergency deactivation (account compromise), 15 minutes of continued access may be unacceptable. Recommend:
- Document this as a known limitation
- For emergency deactivation, consider a token revocation list checked on each request

---

### BR-12: User Management Restricted to JJT_ADMIN

> User management is restricted exclusively to `JJT_ADMIN`.

**Status:** ✓ Implemented.

**Gap:** Open Question OQ-05 in the BRD asks whether ORG_ADMIN should also be able to create sponsor accounts. This is an unresolved policy decision. As the organisation scales, having only JJT_ADMIN create sponsor accounts becomes a bottleneck. Recommend:

> Policy decision required: Should ORG_ADMIN be permitted to create SPONSOR user accounts (but not JJT_ADMIN or ORG_ADMIN accounts)? This is a reasonable delegation that reduces bottleneck without compromising security.

---

## Part 2 — Missing Business Rules (Not in BRD)

These rules govern real business scenarios that will definitely occur but are not documented anywhere in the BRD.

---

### MISSING BR-13: Sponsorship Payment Tracking

> **Proposed Rule:** For every calendar month of an ACTIVE sponsorship, there must be exactly one `SponsorPayment` record. A `SponsorPayment` may have status `EXPECTED`, `RECEIVED`, or `OVERDUE`.

**Business Justification:** Without this rule, the system has no way to detect a sponsor who has stopped paying. The monthly payment cycle is the core financial heartbeat of the organisation.

**Technical Impact:** New `sponsor_payments` table; monthly job or admin process to create expected records.

**Operational Impact:** Monthly admin task to confirm received payments reduces from "check all bank statements" to "confirm the records the system pre-populated".

---

### MISSING BR-14: General Fund Balance Protection

> **Proposed Rule:** An EARLY_SUPPORT ledger entry may not be created if it would reduce the General Education Fund balance below the minimum reserve threshold. The admin must be warned and must explicitly override with a justification.

**Business Justification:** The organisation cannot knowingly over-commit its central fund. This rule prevents the situation where JJT promises to support 100 children when it only has funds for 60.

**Financial Impact:** Prevents the central fund from going negative, which is a governance emergency for any registered charity.

**Alternative Approach:** Warn but allow (soft block vs hard block). A soft block (warning with override) is recommended over a hard block, to prevent the rare legitimate case where a trusted admin knowingly creates entries that will be funded by an incoming donation.

---

### MISSING BR-15: Commitment Type Financial Obligation

> **Proposed Rule:** A YEARLY commitment sponsor is financially committed for 12 months from the start month. An admin should not expire a YEARLY sponsorship before the commitment period ends without recording a reason and obtaining approval.

**Business Justification:** A sponsor who commits to YEARLY sponsorship has an obligation to pay for 12 months. Expiring their sponsorship after 3 months misrepresents the commitment. If the sponsor breaches the annual commitment, this should be recorded as a governance event.

**Operational Impact:** Adds an approval step to early expiry of YEARLY sponsorships. Minor operational friction for a significant governance benefit.

---

### MISSING BR-16: Duplicate Sponsor Prevention

> **Proposed Rule:** The system must warn an admin when creating a new Sponsor record if an existing Sponsor record has the same `contactEmail`. The admin must confirm whether they intend to create a second record or use the existing one.

**Business Justification:** This session demonstrated the real-world impact: Nadeem's account had two sponsor records with the same email, which caused the portal to show no children. This rule would have prevented the mistake.

**Technical Impact:** Unique constraint on `sponsors.contact_email` OR a server-side check with a confirmable warning.

**Risk of unique constraint:** May be too strict — a family might legitimately have two sponsor accounts under one email. A confirmable warning is safer than a hard block.

---

### MISSING BR-17: Progress Update Coverage Requirement

> **Proposed Rule:** For every child with an ACTIVE sponsorship, a progress update should be recorded for each month that has a ledger entry. If a month's ledger entry has no progress update after 45 days, the admin dashboard should display an overdue alert.

**Business Justification:** Sponsors commit their money because they want to see the child's progress. If progress updates are not recorded, sponsors receive no value from the portal. This directly affects sponsor retention.

**Operational Impact:** Admin workload increases — progress updates are required monthly, not optional. This is a feature, not a bug: it creates accountability for programme outcomes.

---

### MISSING BR-18: Child Education Cost Change

> **Proposed Rule:** A change to a child's `educationAmount` applies only from the month of the change forward. It does not retroactively alter any existing ledger entries.

**Business Justification:** School fees increase. When a child's monthly fee goes from PKR 2,000 to PKR 2,500, the existing ledger entries must not change (they are append-only and reflect what was actually paid). The new amount applies to future entries.

**Technical Impact:** The `educationAmount` on the child record should have a `validFrom` date. Historical amounts must be preserved.

**This gap exists today:** If an admin updates a child's `educationAmount`, all future operations use the new amount, but there is no history of what the old amount was, and there is no notification to the sponsor that their monthly commitment amount has changed.

---

### MISSING BR-19: Sponsorship Renewal

> **Proposed Rule:** When a YEARLY sponsorship reaches its 12th month, the system should alert the admin 30 days before expiry. The admin should be prompted to either renew (creating a new PENDING sponsorship for the same sponsor and child, starting the following month) or expire without renewal.

**Business Justification:** YEARLY sponsors whose commitment expires silently leave a child without coverage for the following month. This is exactly the problem the platform was built to solve (BRD Section 4: "no mechanism to determine which children were at risk").

**Operational Impact:** Proactive renewal workflow prevents coverage gaps. Sponsor retention is dramatically improved when the renewal conversation happens before expiry rather than after.

---

### MISSING BR-20: Orphaned Children Detection

> **Proposed Rule:** A child whose most recent ledger entry is more than 45 days in the past with no ACTIVE sponsorship is considered "coverage-lapsed." The admin dashboard must surface all coverage-lapsed children prominently.

**Business Justification:** A child can fall through the cracks if:
- An admin forgets to record a month's early support
- A sponsorship expires and no new sponsor is found
- A public commitment is submitted but never activated

Without this rule, a child could go months without any ledger entry — and the platform would simply show them as AVAILABLE while in reality their education is at risk.

---

### MISSING BR-21: Campaign Financial Integrity

> **Proposed Rule:** A campaign fund cannot exceed its stated target. If a campaign reaches its target, the public donation pathway for that campaign should close or display "target met." Campaign funds cannot be diverted to non-campaign purposes without explicit governance approval.

**Business Justification:** Donor trust is fundamental to fundraising. A donor who contributes to "Ramadan Appeal for 50 children" expects that money to fund exactly that, not to be absorbed into the general fund because the campaign was overfunded.

---

## Part 3 — Governance Rules (Absent From BRD Entirely)

These are not technical business rules but organisational governance rules that the platform should eventually enforce or support.

### MISSING GOV-01: Four-Eyes Principle for Large Disbursements

> Any single transaction above PKR 50,000 (configurable) should require a second admin to approve before the fund debit is committed.

**Business Justification:** Charities are vulnerable to insider fraud. A single admin who can both receive donations and disburse funds without oversight is a control weakness. The four-eyes principle requires two independent approvals for large transactions.

---

### MISSING GOV-02: Annual Programme Audit Export

> The system should be able to produce a machine-readable, signed export of all financial transactions for any given calendar year, suitable for submission to an external auditor.

**Business Justification:** If JJT seeks formal charity registration (with SECP in Pakistan, or the Charity Commission in the UK), audited accounts are a regulatory requirement. The export must be tamper-evident (digitally signed) and complete.

---

### MISSING GOV-03: Data Retention Policy

> Child personal data (name, school, city) must be retained for a minimum of 7 years after the child exits the programme, in accordance with financial record-keeping requirements. After the retention period, personal data must be anonymised or deleted in accordance with applicable data protection law.

**Business Justification:** Open Question OQ-01 in the BRD raises data privacy classification. This governance rule answers it operationally: whatever the privacy classification, financial records must be kept for 7 years (a standard accounting requirement).

---

## Summary Table

| Rule | Type | Priority | Phase |
|------|------|----------|-------|
| BR-01b: No delete with ledger entries | Data integrity | High | Phase 2 |
| BR-02b: Max 12-month future start | Business | Medium | Phase 2 |
| BR-03 DB constraint: unique active per child | Security | High | Phase 2 |
| BR-07 DB-level append-only enforcement | Security | High | Phase 2 |
| BR-13: Monthly payment tracking | Financial | Critical | Phase 2 |
| BR-14: Fund balance protection | Financial | Critical | Phase 2 |
| BR-15: YEARLY commitment protection | Governance | Medium | Phase 2 |
| BR-16: Duplicate sponsor email warning | Data quality | High | Phase 2 |
| BR-17: Progress update completeness | Operational | High | Phase 2 |
| BR-18: Education cost change history | Financial | Medium | Phase 2 |
| BR-19: Sponsorship renewal alerts | Operational | High | Phase 2 |
| BR-20: Orphaned children detection | Operational | Critical | Phase 2 |
| BR-21: Campaign financial integrity | Financial | Medium | Phase 3 |
| GOV-01: Four-eyes for large disbursements | Governance | Medium | Phase 3 |
| GOV-02: Annual audit export | Compliance | High | Phase 3 |
| GOV-03: Data retention policy | Compliance | Medium | Phase 3 |
