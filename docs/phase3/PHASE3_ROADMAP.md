# Phase 3 Roadmap
## Junior Jinnah Trust — Agentic Automation Platform

| | |
|---|---|
| **Document Version** | 1.0 |
| **Prepared** | 2026-07-02 |
| **Supersedes** | Phase 2 Roadmap v1.0 |
| **Status** | Draft for Review |
| **Classification** | Strategic — Internal |

---

## Context: What Phase 3 Is For

Phase 1 replaced spreadsheets with structured data. Phase 2 replaced untracked commitments with real financial accounting. Both phases still require a human to initiate every action. Every ledger entry, every payment recording, every progress update, every report — a person has to open the admin panel and do it manually.

At 50 children that is manageable. At 500 it is painful. At 5,000 it breaks entirely.

**Phase 3 introduces autonomous agents** — software components that observe the system state, decide what action is needed, act (or propose action), and report what they did. The goal is to reduce recurring admin work by 80% so the JJT team can focus on field operations and sponsor relationships rather than data entry.

This is not AI for the sake of AI. Every agent in this roadmap eliminates a specific, named manual task that the team currently performs by hand each month.

---

## Guiding Principles

1. **Every agent has a name, a trigger, and a clear scope.** An agent that does too much is a liability. Each agent owns exactly one area of responsibility.
2. **Agents propose; humans approve anything financially material.** Auto-marking a payment received requires the bank statement upload as evidence. Auto-expiring a sponsorship requires one-click admin confirmation. No autonomous mutation of financial records without audit provenance.
3. **Every agent run is logged.** The `agent_runs` table records: what agent ran, when, what it read, what it wrote, whether it succeeded. Agents are fully observable.
4. **Graceful degradation.** If an agent fails, the manual workflow in Phase 2 still works. Agents augment the UI — they do not replace it as the sole path to action.
5. **Intelligence only where it adds value.** Scheduled jobs and pattern-matching are not AI. Reserve LLM calls for tasks that require language understanding, synthesis, or ranking — not for tasks that are deterministic.
6. **Event-driven first.** Agents react to domain events (payment received, child added, sponsorship expired) before they run on schedules. Reactive agents are faster and more reliable than polled schedules.

---

## Manual Workflows Being Automated

This section maps every Phase 2 manual task to the agent that will replace it.

| Manual Task | Frequency | Time Cost | Replacing Agent |
|-------------|-----------|-----------|----------------|
| Generate expected payment records | Monthly | 5 min | M3.2 Monthly Cycle Agent |
| Send payment reminders to sponsors | Monthly | 30–60 min | M3.4 Communication Agent |
| Mark payments OVERDUE at due date | Monthly | 10 min | M3.2 Monthly Cycle Agent |
| Match bank transfers to expected payments | Monthly | 60–120 min | M3.3 Reconciliation Agent |
| Record payments in the platform | Monthly | 30 min | M3.3 Reconciliation Agent |
| Collect progress updates from field staff | Monthly | 60–90 min | M3.5 Progress Agent |
| Type progress updates into the platform | Monthly | 45 min | M3.5 Progress Agent |
| Generate monthly board report | Monthly | 90 min | M3.7 Report Agent |
| Send sponsor acknowledgement emails | Ad hoc | 20 min | M3.4 Communication Agent |
| Identify at-risk children | Weekly | 30 min | M3.6 Risk Agent |
| Review data quality issues | Weekly | 45 min | M3.9 Data Quality Agent |
| Campaign progress monitoring | Daily | 15 min | M3.10 Campaign Agent |
| Match new available children to sponsors | Ad hoc | 2–3 hrs | M3.8 Sponsor Matching Agent |
| Annual donor impact report | Annual | Full day | M3.4 Communication Agent |
| Export financial data for audit | Annual | 2–3 hrs | M3.9 Data Quality Agent |

**Total estimated manual effort eliminated per month: 8–10 hours of admin time per 50 children.** At 500 children this scales to 80–100 hours — effectively a full-time role — that agents handle instead.

---

## Phase 3 Milestones Overview

| Milestone | Name | Focus | Priority |
|-----------|------|-------|----------|
| M3.1 | Agent Infrastructure | Event bus, agent registry, approval queue | Critical |
| M3.2 | Monthly Cycle Agent | Automate full monthly payment lifecycle | Critical |
| M3.3 | Reconciliation Agent | Bank statement upload → auto-match payments | Critical |
| M3.4 | Communication Agent | All sponsor and donor emails | High |
| M3.5 | Progress Collection Agent | Field data → draft progress updates | High |
| M3.6 | Risk Detection Agent | AI portfolio health analysis | High |
| M3.7 | Board Report Agent | Auto-generated monthly narrative reports | High |
| M3.8 | Sponsor Matching Agent | AI-powered child-to-sponsor matching | Medium |
| M3.9 | Data Quality Agent | Integrity sweeps + anomaly detection | Medium |
| M3.10 | Campaign Automation Agent | Campaign lifecycle + donor acknowledgement | Medium |

---

## Milestone M3.1 — Agent Infrastructure

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes — no visible change to users, foundational plumbing only

This milestone builds the infrastructure all other agents depend on. No agent in M3.2–M3.10 should be built without this in place.

### Domain Event Bus

Replace direct method calls with domain events published after transaction commit. Uses Spring's `ApplicationEventPublisher` with `@TransactionalEventListener(phase = AFTER_COMMIT)` so events only fire on successful persistence.

**Events to define:**

| Event | Published When |
|-------|---------------|
| `ChildEnrolledEvent` | New child created |
| `SponsorshipActivatedEvent` | Sponsorship status → ACTIVE |
| `SponsorshipExpiredEvent` | Sponsorship status → EXPIRED |
| `PaymentReceivedEvent` | SponsorPayment marked RECEIVED |
| `PaymentOverdueEvent` | SponsorPayment auto-transitioned to OVERDUE |
| `LedgerEntryCreatedEvent` | Any new ledger entry |
| `ProgressUpdatePostedEvent` | New progress update saved |
| `DonationRecordedEvent` | New donation saved |
| `CampaignStatusChangedEvent` | Campaign opened, closed, or funded |
| `FundBalanceLowEvent` | Fund balance crosses below minimum reserve |

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `DomainEvent` base class | Abstract base with `eventId`, `occurredAt`, `organisationId` | Critical |
| `DomainEventPublisher` | Wraps `ApplicationEventPublisher`, enforces post-commit semantics | Critical |
| `V24__agent_infrastructure.sql` | `agent_runs` table, `agent_tasks` table, `human_approval_queue` table | Critical |
| `AgentRun` JPA entity | Records: agentName, triggeredBy, startedAt, completedAt, status (SUCCESS/FAILED/PARTIAL), summary, errorDetail, recordsRead, recordsWritten | Critical |
| `HumanApprovalTask` JPA entity | Records: proposingAgent, action, payload (JSONB), proposedAt, reviewedBy, reviewedAt, decision (APPROVED/REJECTED), rejectionReason | Critical |
| `AgentRegistry` | Spring `@Component` that maintains a catalogue of all registered agents with metadata | High |
| `AgentExecutionService` | Service that runs any registered agent with logging, timeout, error isolation | Critical |
| `AdminAgentController` | `GET /api/admin/agents` (list all agents + last run status), `POST /api/admin/agents/{name}/run` (manual trigger) | High |
| `AdminApprovalController` | `GET /api/admin/approvals` (pending tasks), `POST /api/admin/approvals/{id}/approve`, `POST /api/admin/approvals/{id}/reject` | Critical |

**Database schema:**

```sql
-- V24__agent_infrastructure.sql

CREATE TABLE agent_runs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    agent_name      VARCHAR(60) NOT NULL,
    triggered_by    VARCHAR(30) NOT NULL CHECK (triggered_by IN ('SCHEDULED','EVENT','MANUAL','APPROVAL')),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL DEFAULT 'RUNNING'
                        CHECK (status IN ('RUNNING','SUCCESS','FAILED','PARTIAL')),
    records_read    INT NOT NULL DEFAULT 0,
    records_written INT NOT NULL DEFAULT 0,
    summary         TEXT,
    error_detail    TEXT
);

CREATE TABLE human_approval_queue (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    agent_name      VARCHAR(60) NOT NULL,
    action_type     VARCHAR(60) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    payload         JSONB NOT NULL,
    proposed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    reviewed_by     UUID REFERENCES users(id),
    reviewed_at     TIMESTAMPTZ,
    decision        VARCHAR(20) CHECK (decision IN ('APPROVED','REJECTED','EXPIRED')),
    rejection_reason TEXT
);

CREATE INDEX idx_agent_runs_org_name     ON agent_runs(organisation_id, agent_name, started_at DESC);
CREATE INDEX idx_approvals_pending       ON human_approval_queue(organisation_id, decision)
    WHERE decision IS NULL;
```

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Admin nav: "Agents" section | New nav group in admin panel below System | Critical |
| Agent registry panel | List of all agents: name, description, last run, last status, "Run Now" button | Critical |
| Approval queue panel | List of pending agent proposals with Approve / Reject actions and payload preview | Critical |
| Agent run log panel | Paginated history of all agent executions with status, records processed, summary | High |
| Agent run badge on dashboard | Count of pending approvals shown as alert badge | High |

### Acceptance Criteria

- [ ] All domain events are published after transaction commit, not before
- [ ] Every agent execution creates an `agent_runs` row with outcome
- [ ] Admin can see all pending approval tasks and act on them
- [ ] Admin can manually trigger any registered agent from the UI
- [ ] A failing agent is caught, logged, and does not crash the application

---

## Milestone M3.2 — Monthly Cycle Agent

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes — critical operational automation
**Depends on:** M3.1, M2.3 (payment infrastructure)

This agent runs the entire monthly payment cycle without human initiation. Currently the admin must manually trigger payment generation, manually check for overdues, and manually review the reconciliation view each month.

### What the Agent Does

**Step 1 — Generate Expected Payments** (1st of each month, 00:05 UTC)
- For every ACTIVE sponsorship, creates one `SponsorPayment` record with status `EXPECTED` for the current month
- Idempotent: if record already exists for the month, skips it
- Logs: N payments generated for month YYYY-MM

**Step 2 — Send Payment Reminders** (triggered by schedule)
- T-5 days before due date: sends "Payment due soon" email to sponsor
- T+0 (due date): sends "Payment due today" email if not yet RECEIVED
- T+7 days overdue: sends "Payment overdue" email, creates admin alert
- T+30 days overdue: sends escalation to admin, proposes sponsorship review to human approval queue
- All reminder sends are idempotent (checks `communication_log` to avoid duplicate emails)

**Step 3 — Auto-Transition OVERDUE** (daily, 02:00 UTC)
- Queries all `EXPECTED` payments where `dueDate < today`
- Transitions each to `OVERDUE`
- Publishes `PaymentOverdueEvent` for each
- Creates admin alert for each newly overdue payment

**Step 4 — Flag At-Risk Sponsorships** (monthly, after Step 3)
- Any sponsorship with 2 consecutive OVERDUE months → creates HIGH alert
- Any sponsorship with 3 consecutive OVERDUE months → creates proposal in `human_approval_queue`: "Propose sponsoring {child} as at-risk: expire and return to available pool?"
- Admin approves → sponsorship transitioned to EXPIRED
- Admin rejects → adds note, sends personalised sponsor outreach

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `MonthlyCycleAgent` | Spring `@Component` implementing `Agent` interface | Critical |
| `@Scheduled` configuration | Quartz or Spring `@EnableScheduling` with cron expressions | Critical |
| Consecutive overdue detection | Query counting consecutive OVERDUE months per sponsorship | Critical |
| Idempotency guard | `communication_log` check before every reminder email send | Critical |
| `V25__communication_log.sql` | `communication_log` table: templateId, recipientEmail, sentAt, sponsorshipId, paymentId | Critical |

**Cron schedule:**

```
Payment generation:   0  5  0  1 * *     (1st of every month, 00:05)
Overdue transition:   0  0  2  * * *     (every day, 02:00)
Reminder T-5:         0  0  9  * * *     (every day, 09:00 — filtered by due date)
At-risk sweep:        0  0  8  5 * *     (5th of every month, 08:00)
```

### Acceptance Criteria

- [ ] On the 1st of each month, all active sponsorships have EXPECTED payment records — with zero admin action
- [ ] Sponsors receive reminder emails at T-5, T+0, T+7, and T+30 without admin involvement
- [ ] Duplicate reminder emails are never sent (idempotency enforced via `communication_log`)
- [ ] A sponsorship with 3 consecutive OVERDUE months appears in the admin approval queue
- [ ] Admin approves the proposal → sponsorship expires and child returns to AVAILABLE
- [ ] Every step is recorded in `agent_runs` with counts of records processed

---

## Milestone M3.3 — Payment Reconciliation Agent

**Duration estimate:** 3 sprints (6 weeks)
**Deploy:** Yes — highest time-saving agent in the platform
**Depends on:** M3.1, M2.3

Bank reconciliation is the single largest monthly admin time cost. Admin downloads a CSV from the bank, opens the reconciliation view, and manually matches each transaction line to an expected payment. This agent does the matching.

### What the Agent Does

1. Admin uploads a bank statement CSV (or PDF) to the platform
2. The agent parses every credit transaction from the statement
3. For each credit, the agent runs a matching algorithm against all EXPECTED and OVERDUE payments:
   - **Exact match** (amount + sponsor name): confidence 98% → auto-record as RECEIVED
   - **Amount match** (amount within PKR 50 of expected, within same month): confidence 85% → propose to admin
   - **Partial match** (amount < expected, sponsor name matches): confidence 75% → propose as PARTIAL payment
   - **No match**: queued as unrecognised transaction for manual review
4. Auto-recorded payments go through the same flow as manual recording: LedgerEntry created, FundTransaction CREDIT created
5. Proposed matches are surfaced to the admin in the approval queue with "Accept" / "Reject / Reassign" actions
6. Admin reviews the proposed list in under 5 minutes instead of manually matching 30–50 entries

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `V26__bank_statements.sql` | `bank_statement_imports` table + `bank_statement_lines` table | Critical |
| `BankStatementParser` | Parses CSV formats from Pakistan's major banks (HBL, UBL, MCB, Meezan). Extensible via bank-specific `StatementFormat` implementations | Critical |
| `PaymentMatchingService` | Fuzzy matching algorithm: normalised amount comparison + sponsor name distance + date window | Critical |
| `ReconciliationAgent` | `Agent` implementation that processes an uploaded statement | Critical |
| `POST /api/admin/statements/upload` | Accepts multipart file upload, creates import record, triggers agent | Critical |
| `GET /api/admin/statements` | Lists past imports with match statistics | High |
| Confidence threshold config | Org-level config: auto-approve threshold (default 95%), propose threshold (default 70%) | High |

**Database schema:**

```sql
-- V26__bank_statements.sql

CREATE TABLE bank_statement_imports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    bank_name       VARCHAR(60),
    statement_month VARCHAR(7) NOT NULL,  -- YYYY-MM
    filename        VARCHAR(255) NOT NULL,
    uploaded_by     UUID NOT NULL REFERENCES users(id),
    uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING'
                        CHECK (status IN ('PROCESSING','COMPLETED','FAILED')),
    total_lines     INT NOT NULL DEFAULT 0,
    matched_auto    INT NOT NULL DEFAULT 0,
    matched_manual  INT NOT NULL DEFAULT 0,
    unmatched       INT NOT NULL DEFAULT 0
);

CREATE TABLE bank_statement_lines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    import_id       UUID NOT NULL REFERENCES bank_statement_imports(id),
    transaction_date DATE NOT NULL,
    description     TEXT NOT NULL,
    amount          NUMERIC(19,4) NOT NULL,
    currency        VARCHAR(10) NOT NULL DEFAULT 'PKR',
    match_status    VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (match_status IN ('PENDING','AUTO_MATCHED','PROPOSED','REJECTED','UNMATCHED')),
    matched_payment_id UUID REFERENCES sponsor_payments(id),
    confidence      NUMERIC(5,2),
    agent_run_id    UUID REFERENCES agent_runs(id)
);
```

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Statement upload UI | Drag-and-drop upload in Reconciliation section | Critical |
| Match review panel | Table of proposed matches: bank line ↔ expected payment, confidence badge, Accept / Reassign buttons | Critical |
| Statement history | List of past uploads with match statistics | High |
| Unmatched transactions | Panel showing unrecognised bank lines for manual resolution | High |

### Acceptance Criteria

- [ ] Admin can upload a bank statement CSV and see match results within 30 seconds
- [ ] Transactions with ≥ 95% confidence are auto-recorded with no admin action
- [ ] Transactions with 70–95% confidence appear in the approval queue
- [ ] Each auto-recorded payment creates the correct LedgerEntry and FundTransaction
- [ ] Unmatched transactions are surfaced for manual resolution, not silently dropped
- [ ] The entire monthly reconciliation workflow can be completed in under 10 minutes

---

## Milestone M3.4 — Communication Agent

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M2.7 (email service), M3.2 (payment events)

Every sponsor email is currently sent manually. The Communication Agent handles all outbound communication, triggered by domain events. Critically, it is not a bulk-sender — every email is contextual, triggered by something that happened to that specific sponsor or donor.

### What the Agent Does

**Event-triggered emails:**

| Trigger Event | Recipient | Template | Timing |
|--------------|-----------|----------|--------|
| `PaymentReceivedEvent` | Sponsor | Payment acknowledgement + month covered | Immediate |
| `ProgressUpdatePostedEvent` | Sponsor | Child progress update for {month} | Immediate |
| `SponsorshipActivatedEvent` | Sponsor | Welcome — your sponsorship is now active | Immediate |
| `SponsorshipExpiredEvent` | Sponsor | Your sponsorship has ended — impact summary | Next business day |
| `PaymentOverdueEvent` (T+7) | Sponsor | Overdue payment reminder | On event |
| `DonationRecordedEvent` | Donor | Donation receipt + thank you | Immediate |
| `CampaignStatusChangedEvent` (FUNDED) | All campaign donors | Campaign goal reached! | Immediate |
| `FundBalanceLowEvent` | JJT Admin | Fund balance alert | Immediate |

**Scheduled stewardship emails:**

| Schedule | Recipient | Email |
|----------|-----------|-------|
| 1 year anniversary of sponsorship start | Sponsor | "One year of impact" — annual summary |
| 6 months with no contact from sponsor | Sponsor | Engagement touchpoint with child update |
| Donor who gave 6+ months ago with no repeat | Donor | Impact update from last donation |

**Annual impact report** (1st January each year):
- For each active sponsor: generates a personalised annual impact document (PDF data) covering all months funded, progress updates for the year, photos (when attached to progress updates), fund utilisation summary
- Sent to sponsor as a structured email (no PDF dependency — structured HTML)

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `CommunicationAgent` | Event listener implementing all triggered sends | Critical |
| `EmailTemplateService` | Resolves template by name, merges data, sends via configured provider | Critical |
| `V27__email_templates.sql` | `email_templates` table: templateKey, subject, htmlBody, textBody, lastUpdated | High |
| Template admin endpoint | `GET/PUT /api/admin/email-templates/{key}` — JJT_ADMIN can edit templates without redeployment | High |
| Unsubscribe management | One-click unsubscribe token on every email; `POST /api/public/unsubscribe/{token}` | Critical |
| Stewardship scheduler | `@Scheduled` monthly job: checks anniversary dates, 6-month no-contact, lapsed donors | High |
| `communication_log` | Already defined in M3.2 — all sends recorded here with deliveryStatus | Critical |

### Acceptance Criteria

- [ ] Recording a payment triggers a sponsor acknowledgement email within 60 seconds
- [ ] A new progress update triggers a sponsor notification within 60 seconds
- [ ] No duplicate emails are ever sent (idempotency on `communication_log`)
- [ ] Every email contains a valid one-click unsubscribe link
- [ ] Admin can edit email templates from the admin panel without a code deployment
- [ ] Annual impact emails are sent to all active sponsors on 1 January

---

## Milestone M3.5 — Progress Collection Agent

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M3.4 (communication infrastructure)

Progress updates are the highest-friction monthly task. A staff member or field coordinator visits each school, collects notes, then types every update into the admin platform. The Progress Collection Agent turns this into a structured, mobile-friendly data collection flow.

### What the Agent Does

1. **Monthly trigger** (10th of each month): The agent identifies all children who have a ledger entry for the current month but no progress update
2. **Data collection link generated**: For each school/campus, a unique, time-limited link is generated pointing to a public form that field staff can fill in on their phone
3. **Form submitted by field staff**: One form per child. Fields: summary text, attendance indicator (PRESENT/ABSENT), optional photo
4. **Drafts created**: Each submission creates a `ProgressDraft` record with status `PENDING_REVIEW`
5. **Admin notified**: "10 progress updates are ready for your review"
6. **Admin reviews**: Opens the Progress Drafts panel, reads each draft, clicks Approve or Edit+Approve
7. **On approval**: Draft is promoted to a real `ProgressUpdate` record in the main table

This reduces the admin's role from data-entry operator to quality reviewer.

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `V28__progress_drafts.sql` | `progress_drafts` table, `data_collection_links` table | Critical |
| `ProgressCollectionAgent` | Monthly scheduled agent: identifies gaps, generates collection links | Critical |
| `POST /api/public/progress/{token}` | Public endpoint accepting field staff submissions (no auth — token is the auth) | Critical |
| `ProgressDraft` JPA entity | childId, month, rawSummary, sourceToken, submittedAt, status (PENDING/APPROVED/REJECTED) | Critical |
| `GET /api/admin/progress/drafts` | Lists all pending drafts for review | Critical |
| `POST /api/admin/progress/drafts/{id}/approve` | Promotes draft → real ProgressUpdate | Critical |
| `POST /api/admin/progress/drafts/{id}/reject` | Marks rejected, optionally notifies submitter | High |
| Token security | Collection links are single-use, expire after 72 hours, scoped to one child+month | Critical |

**Database schema:**

```sql
-- V28__progress_drafts.sql

CREATE TABLE data_collection_links (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    child_id        UUID NOT NULL REFERENCES children(id),
    month           VARCHAR(7) NOT NULL,
    token           VARCHAR(64) NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NOT NULL,
    used_at         TIMESTAMPTZ,
    agent_run_id    UUID REFERENCES agent_runs(id)
);

CREATE TABLE progress_drafts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    child_id        UUID NOT NULL REFERENCES children(id),
    month           VARCHAR(7) NOT NULL,
    raw_summary     TEXT NOT NULL,
    source_token    UUID REFERENCES data_collection_links(id),
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    reviewed_by     UUID REFERENCES users(id),
    reviewed_at     TIMESTAMPTZ,
    promoted_to_id  UUID REFERENCES progress_updates(id)
);
```

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Public field data form | Mobile-friendly page at `/collect/{token}` — no login required | Critical |
| Progress Drafts panel | Admin panel section listing all pending drafts with Approve/Edit+Approve/Reject | Critical |
| Collection links panel | Admin view of generated links, their expiry, and submission status | High |
| Manual trigger | "Generate collection links now" button for ad hoc collection outside the scheduled run | High |

### Acceptance Criteria

- [ ] On the 10th of each month, collection links are automatically generated for all children missing a progress update
- [ ] Field staff can submit updates on a mobile browser without creating an account
- [ ] Submitted updates appear in the admin draft review panel within 60 seconds
- [ ] Approved drafts become real progress updates visible to sponsors in the portal
- [ ] Expired or used tokens are rejected with a clear message
- [ ] Admin can manually trigger link generation for any child and month

---

## Milestone M3.6 — Risk Detection Agent

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M2.9 (dashboard data), Spring AI

This is the platform's first LLM-powered agent. The Risk Detection Agent runs a portfolio-wide health analysis weekly and produces a natural language "Intelligence Brief" for the admin. It combines rule-based pattern detection with LLM synthesis to explain what is happening, why it matters, and what to do.

### What the Agent Does

**Weekly sweep** (every Monday, 07:00 PKT):

**Rule-based detections:**

| Rule | Severity | Description |
|------|----------|-------------|
| Child with ACTIVE sponsorship but no ledger entry this month | HIGH | Money is expected to be committed but isn't |
| Sponsorship ACTIVE but last payment > 45 days ago | HIGH | Sponsor may have stopped paying without formal expiry |
| Fund balance < 60-day runway at current burn rate | CRITICAL | Organisation cannot cover next 2 months from fund |
| Child enrolled > 90 days with no sponsorship of any kind | MEDIUM | Falling through the cracks |
| Progress update missing for a child for 3+ consecutive months | MEDIUM | Sponsor engagement risk |
| Sponsorship PENDING for > 45 days (not yet activated) | MEDIUM | Stuck commitment, possibly no payment received |
| Donor with no contact in 6+ months | LOW | Lapse risk |

**LLM synthesis (Spring AI → Claude API):**

After the rule-based sweep, the agent sends the structured findings to the Claude API with a prompt that:
1. Translates the raw risk findings into a narrative paragraph the board can read
2. Prioritises the three most urgent items needing admin action this week
3. Notes any positive trends (e.g. fund balance improved, coverage rate up)
4. Flags whether the organisation is operationally healthy or stressed

The LLM output is stored as the agent's `summary` and rendered in the admin dashboard as the "Weekly Intelligence Brief" card.

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Spring AI dependency | Add `spring-ai-anthropic-spring-boot-starter` to `pom.xml` | Critical |
| `RiskDetectionAgent` | Weekly scheduled agent + event subscriber for immediate high-severity risks | Critical |
| `RiskFinding` record | ruleId, childId (nullable), sponsorshipId (nullable), severity, description, detectedAt | Critical |
| `IntelligenceBriefService` | Builds structured prompt from findings, calls Claude API, returns narrative | Critical |
| `GET /api/admin/intelligence/brief` | Returns the latest intelligence brief + raw findings | High |
| Immediate escalation | When `FundBalanceLowEvent` fires, RiskDetectionAgent runs immediately, not waiting for Monday | Critical |
| Config | `jjt.ai.anthropic.model`, `jjt.ai.anthropic.max-tokens`, `jjt.risk.fund-runway-days-threshold` | High |

**LLM prompt structure:**

```
You are the operations analyst for Junior Jinnah Trust, a children's education charity in Pakistan.
Analyse the following risk findings from this week's portfolio sweep and write a brief (under 200 words)
executive summary for the admin team. Prioritise the three most urgent items. Be direct and specific.
If the organisation is broadly healthy, say so.

Organisation: {orgName}
Report week: {weekEnding}
Children enrolled: {totalChildren}
Active sponsorships: {activeCount}
Fund balance: PKR {fundBalance} ({runwayDays} days runway)

FINDINGS:
{structuredFindings}

Write the summary now:
```

### Frontend Changes

| Task | Description | Priority |
|------|-------------|----------|
| Intelligence Brief card | Admin dashboard card: "Weekly Intelligence Brief" — rendered markdown, sourced from latest brief | Critical |
| Raw findings panel | Expandable list of all rule-based findings with severity badges | High |
| Risk indicator on child cards | Red indicator on children in the admin list who appear in HIGH/CRITICAL findings | High |

### Acceptance Criteria

- [ ] Every Monday, the agent runs and produces an intelligence brief
- [ ] The brief is visible on the admin dashboard without navigating away
- [ ] CRITICAL findings (fund balance < 60-day runway) trigger an immediate run, not waiting for Monday
- [ ] The LLM prompt never sends personally identifiable sponsor data to the API — only aggregate statistics and IDs
- [ ] If the Claude API call fails, the agent still logs rule-based findings without an LLM summary

---

## Milestone M3.7 — Board Report Agent

**Duration estimate:** 2 sprints (4 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M2.8 (report data), M3.6 (Spring AI already wired)

Monthly board reports are currently assembled by hand: the admin screenshots dashboards, copies numbers into a Word document, and emails it to the board. This agent generates a complete, data-accurate board report every month and delivers it automatically.

### What the Agent Does

**Scheduled** (15th of each month, 08:00):

Gathers the following data for the previous calendar month:
- Fund balance: opening, credits, debits, closing
- Payment reconciliation: expected, received, overdue, waived
- Portfolio: total children, coverage rate, new enrollments, exits
- Donations: total received, by type, campaign progress
- Top 3 risks from the intelligence brief

Calls Claude API to generate a structured board report in three sections:
1. **Financial Position** — fund health, payment performance, 3-month trend
2. **Programme Status** — children by status, sponsorship pipeline, geographic summary
3. **Management Actions Required** — items needing board awareness or approval

The report is:
- Stored in `board_reports` table as structured JSON + rendered HTML
- Emailed to all `BOARD` role users (new role in M3.7)
- Accessible in the admin panel under Reports → Board Reports

**Natural language query** (always available):

Admin types a question in plain English: "How many children have been without a sponsor for more than 60 days?"

The agent translates this to a database query (using a structured prompt with the schema), executes it, and returns the answer in plain English with the supporting data.

This is **not** an unrestricted SQL execution — the agent uses a predefined set of query templates that it selects and parameterises based on the question. The LLM determines which template to use and what the parameters should be.

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `V29__board_reports.sql` | `board_reports` table, `BOARD` role added to Role enum | Critical |
| `BoardReportAgent` | Monthly scheduled agent, report generator | Critical |
| `BoardReportDataService` | Aggregates all data needed for the report (fund, payments, portfolio, donations) | Critical |
| `NaturalLanguageQueryService` | Template-based NLQ: maps user questions to query templates, executes, narrates result | High |
| `POST /api/admin/reports/query` | `{ "question": "..." }` → `{ "answer": "...", "data": {...} }` | High |
| `GET /api/admin/reports/board` | Lists past board reports | High |
| `GET /api/admin/reports/board/{id}` | Returns rendered HTML + raw data for a board report | High |

**NLQ template examples:**

```java
// Maps to: "Which children have been available for more than N days?"
TEMPLATE_AVAILABLE_CHILDREN_BY_AGE,

// Maps to: "What is the total overdue amount for month YYYY-MM?"
TEMPLATE_OVERDUE_AMOUNT_BY_MONTH,

// Maps to: "Which sponsors have not paid in N+ months?"
TEMPLATE_DELINQUENT_SPONSORS,

// Maps to: "What is the fund balance trend over the last N months?"
TEMPLATE_FUND_TREND
```

### Acceptance Criteria

- [ ] On the 15th of each month, a board report is generated and emailed automatically
- [ ] Admin can query portfolio data in plain English from the admin panel
- [ ] NLQ responses include the underlying data, not just text answers
- [ ] Board reports are stored and retrievable from the Reports section
- [ ] The NLQ service rejects questions that do not map to any known template (no free-form SQL risk)

---

## Milestone M3.8 — Sponsor Matching Agent

**Duration estimate:** 3 sprints (6 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M3.6 (Spring AI), public commitment form data

When a child becomes available — either newly enrolled or their sponsor has lapsed — the organisation must identify a new sponsor. Today this is done by memory and phone calls. The Matching Agent makes it systematic.

### What the Agent Does

**Triggered by:**
- `ChildEnrolledEvent` (new child, available)
- `SponsorshipExpiredEvent` (child back to AVAILABLE)

**Step 1 — Source candidates:**
- Expired sponsors who previously sponsored a child in the same city
- Public commitment form submissions with status `PENDING` (not yet linked to a child)
- Donors who have donated ≥ 3 times to the general fund (high engagement, potential sponsors)

**Step 2 — Score candidates (LLM-assisted):**
The agent sends the child's profile and the candidate list to Claude API. The prompt asks the model to rank candidates by match quality, considering:
- Geographic proximity (same city preference)
- Historical commitment (previously sponsored → more reliable)
- Recency of last engagement
- Commitment type preference (public form submissions specify MONTHLY/YEARLY)

Claude returns a ranked list with a brief explanation for each top-3 candidate.

**Step 3 — Proposal surfaced to admin:**
A `human_approval_queue` task is created: "Potential sponsors identified for {child}. Review and reach out."

Admin opens the proposal, sees the ranked list with LLM explanations, and clicks:
- "Contact this sponsor" → generates a personalised outreach email draft
- "Assign and create sponsorship" → directly creates a PENDING sponsorship

**Step 4 — Outreach email draft (LLM):**
Claude API generates a personalised outreach email for the selected sponsor candidate, referencing their prior engagement and introducing the child with appropriate anonymised detail (name, school level, city, monthly cost).

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `SponsorMatchingAgent` | Event-driven agent triggered on availability events | Critical |
| `CandidateSourceService` | Queries expired sponsors, pending public commitments, active donors | Critical |
| `MatchScoringService` | Sends structured prompt to Claude API, receives ranked results | Critical |
| `OutreachDraftService` | Generates personalised email draft for selected candidate | High |
| `GET /api/admin/matching/proposals` | Lists all pending match proposals with candidates | High |
| `POST /api/admin/matching/proposals/{id}/select/{candidateId}` | Admin selects a candidate → creates sponsorship or outreach | High |
| PII policy | Child full names are never sent to Claude API. Only: city, school level, monthly cost, tenure in months | Critical |

### Acceptance Criteria

- [ ] When a child becomes AVAILABLE, a match proposal appears in the admin panel within 5 minutes
- [ ] The proposal lists ranked candidates with rationale
- [ ] Admin can generate an outreach email draft for any candidate with one click
- [ ] Child PII is never transmitted to the Claude API
- [ ] If no candidates exist, the agent creates an alert rather than an empty proposal

---

## Milestone M3.9 — Data Quality Agent

**Duration estimate:** 1 sprint (2 weeks)
**Deploy:** Yes
**Depends on:** M3.1

A silent agent that runs weekly and sweeps the database for integrity issues that accumulate over time. These are the kinds of issues that are invisible until they cause a report discrepancy or an angry sponsor.

### What the Agent Detects

| Check | Severity | Example |
|-------|----------|---------|
| Ledger month gap | HIGH | Child has entry for March and May but not April — a missing month |
| Active sponsorship with no payment records | HIGH | Sponsorship is ACTIVE but `sponsor_payments` table has no rows for it |
| Progress update without ledger entry | MEDIUM | Progress update exists for a month when no ledger entry was recorded |
| Orphaned sponsorship | MEDIUM | Sponsorship references a sponsor UUID that doesn't exist |
| PENDING sponsorship older than 60 days | MEDIUM | Commitment was never activated — may be abandoned |
| Duplicate donor email | LOW | Two donor records with same email address (case-insensitive) |
| Fund transaction with no associated ledger entry | HIGH | Fund was debited but no child ledger entry was created |
| Child with `educationAmount = 0` | MEDIUM | Likely data entry error |

**Actions:**
- HIGH findings → creates admin alert immediately
- MEDIUM findings → surfaced in "Data Quality" panel in admin panel
- LOW findings → batched into weekly data quality digest email to admin

**Annual export:**
- On 31 December each year, generates a JSONB + CSV export of all financial records for the calendar year
- Includes SHA-256 checksum of the export file for audit integrity
- Stored in `annual_exports` table; admin can download from Reports section

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `DataQualityAgent` | Weekly scheduled sweep + annual export | Critical |
| `DataQualityFinding` record | ruleId, severity, entityType, entityId, description, detectedAt, resolvedAt | Critical |
| `GET /api/admin/data-quality` | Returns current open findings grouped by severity | High |
| `POST /api/admin/data-quality/{id}/resolve` | Admin marks a finding as reviewed/resolved | High |
| `V30__data_quality.sql` | `data_quality_findings` table, `annual_exports` table | Critical |
| Annual export service | Generates export, computes checksum, stores in DB | High |
| `GET /api/admin/exports` | Lists available annual exports for download | High |

### Acceptance Criteria

- [ ] Weekly sweep runs every Sunday at 03:00 and produces a finding list
- [ ] HIGH findings trigger admin alerts visible on the dashboard
- [ ] Admin can mark findings as resolved with a note
- [ ] Annual export runs on 31 December and produces a downloadable file with checksum
- [ ] The agent never modifies data — it only reads and reports

---

## Milestone M3.10 — Campaign Automation Agent

**Duration estimate:** 1 sprint (2 weeks)
**Deploy:** Yes
**Depends on:** M3.1, M2.6 (campaign module), M3.4 (communication)

Campaigns require continuous monitoring to detect when milestones are hit and when they should close. The Campaign Automation Agent handles the full lifecycle from opening to completion acknowledgement.

### What the Agent Does

**Event-driven monitoring:**

| Trigger | Action |
|---------|--------|
| Donation recorded against a campaign | Recalculate campaign total; check milestone thresholds |
| Campaign total reaches 50% of target | Create admin notification: "Campaign at 50% — consider promoting" |
| Campaign total reaches 75% of target | Email all campaign donors: "We're 75% of the way there!" |
| Campaign total reaches 90% of target | Email all campaign donors + admin alert |
| Campaign total reaches 100% of target | Propose closing campaign to admin via approval queue |
| Campaign `endDate` reached with status ACTIVE | Propose closing campaign (regardless of whether target was met) |

**On campaign close (approved by admin):**
- Campaign status → CLOSED
- Generates campaign completion report: total raised, number of donors, average donation, duration
- Emails all campaign donors: "Campaign complete — thank you" with impact statement
- The impact statement is LLM-generated: "Your donations collectively funded X months of education for Y children"

**Dormant campaign detection:**
- If an ACTIVE campaign has received no donations in 30 days and is < 50% funded, creates admin alert: "Campaign stalled — consider extending or archiving"

### Backend Changes

| Task | Description | Priority |
|------|-------------|----------|
| `CampaignAutomationAgent` | Event listener on `DonationRecordedEvent` + daily schedule for end-date checks | Critical |
| `CampaignMetricsService` | Computes current total, progress %, donor count for any campaign | Critical |
| `CampaignMilestone` enum | FIFTY_PCT, SEVENTY_FIVE_PCT, NINETY_PCT, FULLY_FUNDED | High |
| `campaign_milestone_log` column | Add `milestones_notified JSONB` to campaigns table (tracks which milestones already communicated) | High |
| Completion report generator | Data structure for campaign completion; LLM generates impact narrative | High |

### Acceptance Criteria

- [ ] When a campaign reaches 75%, donors automatically receive a progress email
- [ ] When a campaign is fully funded, admin sees a "Close Campaign" proposal in the approval queue
- [ ] On admin approval, all campaign donors receive a completion email within 5 minutes
- [ ] Stalled campaigns (30 days, < 50%) generate admin alerts
- [ ] All milestone notifications are idempotent — a donor never receives the same milestone email twice

---

## Agent Architecture Reference

```
┌─────────────────────────────────────────────────────────┐
│                   HTTP API (Spring MVC)                  │
│  Admin triggers: /api/admin/agents/{name}/run           │
│  Approvals:      /api/admin/approvals/{id}/approve      │
│  Uploads:        /api/admin/statements/upload           │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│              AgentExecutionService                       │
│  - Wraps every agent call in try/catch                  │
│  - Creates agent_runs row before and after              │
│  - Enforces per-agent timeout                           │
│  - Publishes AgentCompletedEvent                        │
└──────┬────────────────────────────────────────┬─────────┘
       │                                        │
┌──────▼──────────┐                  ┌──────────▼─────────┐
│  Scheduled      │                  │  Event-Driven       │
│  Agents         │                  │  Agents             │
│                 │                  │                     │
│ @Scheduled      │                  │ @TransactionalEvent │
│ MonthlyCycle    │                  │ Listener(AFTER_COMMIT│
│ DataQuality     │                  │ Communication       │
│ RiskDetection   │                  │ CampaignAutomation  │
│ BoardReport     │                  │ SponsorMatching     │
└──────┬──────────┘                  └──────────┬──────────┘
       │                                        │
┌──────▼────────────────────────────────────────▼─────────┐
│              Domain Services (existing)                  │
│  PaymentService | LedgerService | FundService            │
│  SponsorService | CampaignService | NotificationService  │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                  AI Layer (Spring AI)                    │
│  IntelligenceBriefService (RiskDetection)               │
│  NaturalLanguageQueryService (BoardReport)              │
│  MatchScoringService (SponsorMatching)                  │
│  OutreachDraftService (SponsorMatching)                 │
│  All calls: claude-sonnet-4-6, max 1024 tokens          │
│  PII policy: no personal names/emails sent to API       │
└─────────────────────────────────────────────────────────┘
```

---

## Database Migrations Summary

| Migration | Milestone | Contents |
|-----------|-----------|----------|
| V24 | M3.1 | `agent_runs`, `human_approval_queue` |
| V25 | M3.2 | `communication_log` |
| V26 | M3.3 | `bank_statement_imports`, `bank_statement_lines` |
| V27 | M3.4 | `email_templates` |
| V28 | M3.5 | `data_collection_links`, `progress_drafts` |
| V29 | M3.7 | `board_reports`, BOARD role |
| V30 | M3.9 | `data_quality_findings`, `annual_exports` |

---

## Dependencies Map

```
M3.1 (Agent Infrastructure)
 └── required by ALL other milestones

M3.2 (Monthly Cycle Agent)
 └── M3.1 required
 └── M2.3 required (sponsor_payments table)
 └── enables M3.3 (has EXPECTED records to match against)

M3.3 (Reconciliation Agent)
 └── M3.1 required
 └── M3.2 required (expected payment records must exist)

M3.4 (Communication Agent)
 └── M3.1 required
 └── M2.7 required (email service)
 └── M3.2 required (provides PaymentOverdueEvent)

M3.5 (Progress Collection Agent)
 └── M3.1 required
 └── M3.4 required (uses email infrastructure)

M3.6 (Risk Detection Agent)
 └── M3.1 required
 └── Spring AI required (adds LLM calls)
 └── Recommended before M3.7 (shares AI infrastructure)

M3.7 (Board Report Agent)
 └── M3.1 required
 └── M3.6 required (Spring AI already wired)
 └── M2.8 required (report data)

M3.8 (Sponsor Matching Agent)
 └── M3.1 required
 └── M3.6 required (Spring AI)
 └── M3.4 required (outreach emails)

M3.9 (Data Quality Agent)
 └── M3.1 required

M3.10 (Campaign Automation Agent)
 └── M3.1 required
 └── M2.6 required (campaigns)
 └── M3.4 required (campaign emails)
```

---

## New Configuration Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | (required in production) | Claude API key for AI agents |
| `JJT_AI_MODEL` | `claude-sonnet-4-6` | Model used for all LLM calls |
| `JJT_AI_MAX_TOKENS` | `1024` | Max response tokens per LLM call |
| `JJT_RISK_FUND_RUNWAY_DAYS` | `60` | Days of runway below which CRITICAL alert fires |
| `JJT_PAYMENT_DUE_DAY` | `15` | Day of month payments are due (already in org config) |
| `JJT_RECONCILIATION_CONFIDENCE_AUTO` | `0.95` | Bank match confidence above which auto-record |
| `JJT_RECONCILIATION_CONFIDENCE_PROPOSE` | `0.70` | Bank match confidence above which propose to admin |
| `JJT_COLLECTION_LINK_TTL_HOURS` | `72` | Hours before a progress collection link expires |
| `JJT_AGENTS_ENABLED` | `true` | Master switch to disable all agents (for maintenance) |

---

## Delivery Risk Register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Claude API latency degrades agent response time | Medium | Medium | Async agent execution; LLM calls run off the HTTP request thread |
| Claude API cost exceeds budget at scale | Low | Medium | Token limits enforced per call; LLM only used where deterministic logic cannot substitute |
| Bank statement CSV formats vary across banks | High | High | Build parser as a pluggable interface; start with HBL format; expand on request |
| Field staff do not use progress collection links | Medium | High | Fallback: admin can still enter progress manually; collection links are additive, not blocking |
| Scheduled agents fail silently on Heroku dyno restart | Medium | High | Use database-persisted job state; agent is idempotent and re-runnable; alert on missing run |
| PII accidentally sent to Claude API | Low | Critical | Code review gate: every LLM prompt template must pass PII audit before merge |
| Agent writes incorrect data (reconciliation mismatch) | Low | Critical | Auto-record only at ≥ 95% confidence; all records include `agent_run_id` for traceback and reversal |
| Approval queue becomes overloaded and ignored | Medium | Medium | Maximum 10 pending proposals per organisation; new proposals replace lowest-severity when full |

---

## Phase 3 — Definition of Done

Phase 3 is complete when an admin can handle the full monthly operational cycle — from payment reminders to reconciliation to progress collection to board reporting — by responding to proposals in an approval queue rather than initiating each action manually.

**Quantified target:**

| Metric | Phase 2 (manual) | Phase 3 (agents) |
|--------|-----------------|-----------------|
| Monthly admin hours per 50 children | ~10 hours | ~2 hours |
| Time to complete reconciliation | 90–120 min | 10 min (review only) |
| Time for progress updates to reach platform | 2–4 days | Same day |
| Board report preparation | 90 min | 0 min (auto-generated) |
| Sponsor outreach for new available child | 2–3 hours | 20 min (review + approve) |
| Detection time for overdue payment | Next manual review | < 24 hours (automated) |
