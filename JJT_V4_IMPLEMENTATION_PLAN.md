# JJT V4 Lite — Implementation Plan

> Scope: Evolve the current public website into the V4 experience using **existing backend and data model only**.
> Rule: No new backend modules. Existing data. Maximum visual and UX gain, minimum engineering effort.
> Date: 2026-07-11

---

## Summary

After comparing V4 (8 screens) against the current Angular implementation, there are **11 high-value improvements** achievable with no or minimal backend changes, **3 improvements** requiring small backend work, and **6 future features** to defer.

The biggest gaps are:
1. Status terminology ("Bridged", "Seeking") is internal jargon — V4 speaks in plain English
2. The give flow is 4 steps where V4 achieves the same outcome in 2
3. The trust page is missing a traceability steps section and document shelf
4. The child detail story section is near-empty — no dream, no journey preview

---

## Page-by-Page Analysis

### 01 · Home

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Live impact strip (4 stats in a dedicated band after hero) | Stats embedded in hero copy | Separate strip: children in programme, 87% ratio, available count, "Updated X min ago" label | None — all data already loaded | Small — new section, rearrange existing stats | High — trust at first scroll | **High** |
| Sadaqah pill badge copy | "Sadaqah Jariyah · ongoing reward through education" | "3 children found a sponsor today" (dynamic social proof) | Small — new public endpoint: `/api/public/stats/recent-sponsorships-count` | Trivial once endpoint exists | High — immediate urgency | Medium |
| Hero overlay card | Static first available child | Same child card, but badge shows "Sponsored X hours ago" instead of availability status | None for current card; small for timestamps | Small | Medium | Low |
| Islamic giving door strip | Absent | A strip between featured children and How It Works: "Giving as an act of worship? →" links to `/why-give` | None | Trivial — 10 lines of HTML | High — opens Zakat/Sadaqah audience path | **High** |
| Featured children: card copy | Badge shows "Seeking" / "Bridged" / "Sponsored" | V4 shows "Waiting X months" or "Covered by the fund" | Small — add `enrolledAt` date to `ChildDto` | Small — compute months from date | High — plain language converts better | **High** |
| Fund card copy | "Can't choose just one?" | "Overwhelmed by X faces? That's human." + copy about the fund being the no-choice path | None | Trivial | Medium | Medium |
| Stats: graduates count | Not shown | V4 shows 412 graduates, 52 schools | Large — no graduate tracking in DB | n/a | Low | **Defer** |

---

### 02 · Children Listing (`/children`)

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Status label rename: "Bridged" → "Covered by the fund" | "Bridged" (internal jargon) | "Covered by the fund" | None | Trivial — 3 string constants in `.ts` file | **Critical** — "Bridged" means nothing to a donor | **High** |
| Status label rename: "Seeking" → "Waiting for a sponsor" | "Seeking" | "Waiting for a sponsor" | None | Trivial | High | **High** |
| Status tooltip explanation | None | Inline tooltip: "Covered by the fund = donors' pooled gifts are paying their fees until a personal sponsor arrives" | None | Small — add a tooltip chip on the filter | High — removes confusion | **High** |
| Match-me banner | Absent | "Overwhelmed by X faces?" banner above the grid with link to fund | None — banner is static copy linking to `/why-give` | Small | Medium — catches indecisive visitors | Medium |
| Header: "X found a sponsor this month" | Absent | "34 found a sponsor this month" next to total count | Small — same endpoint as home stats | Trivial once endpoint exists | High | Medium |
| "Waiting X months" badge on cards | Absent | Amber badge on each card: "Waiting 4 mo" | Small — `enrolledAt` on ChildDto | Small | High — urgency signal | **High** |
| Filter: "Give with intention" section | Status filter only | Zakat-eligible filter, Girls' education filter | Large — requires new DB fields | n/a | Future | **Defer** |
| Province / school level filter | Search + status only | Province chips, school level | Large — no province field in current ChildDto | n/a | Future | **Defer** |

---

### 03 · Child Detail (`/children/:id`)

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Story section copy | "X studies at Y in Z. Roll number: Z." — very thin | Keep school/city; add lead paragraph about what sponsorship covers specifically | None — content is editorial | Small | High — the current story is a data dump, not a story | **High** |
| "What happens after you sponsor" journey | 3 numbered bullets: confirm, photo, reports | 5-stage timeline: Day 1, 60 days, Each term, Each year, One day | None | Small — better layout, same content | High — sells the relationship, not a transaction | **High** |
| Continuity Fund notice | Amber chip in sidebar: good | Same (already implemented identically to V4) | None | Done | Done | — |
| "If already sponsored while you decide" message | Absent | Gentle note below sponsor card | None | Trivial | Medium | Medium |
| Dream / quote banner | Absent | Italic pull quote with left border | Large — no `dream` field in ChildDto or DB | n/a | High | **Defer** |
| Interest chips (subject, hobby, walk time, attendance) | Absent | Tags: "Favourite subject · Mathematics", "96% attendance" | Large — no structured fields | n/a | Future | **Defer** |
| Teacher's note / Mother's message | Absent | Verified quotes from home visit | Large — no `teacherNote`, `parentMessage` fields | n/a | High (once data exists) | **Defer** |
| Artwork section | Absent | 3 photo slots labelled drawing/exam/classroom | Medium — requires photo upload backend | n/a | Future | **Defer** |
| Waiting time on sidebar status badge | "Seeking sponsor" | "Waiting 4 months for a sponsor" | Small — `enrolledAt` on ChildDto | Small | High | **High** |
| Share button | Absent | "← Share this child's story" text link | None — just a Web Share API call | Trivial | Medium | Medium |

---

### 04 · Sponsorship Flow (`/children/:id/sponsor`)

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| **Collapse 4 steps to 2** | Step 1: Intention → Step 2: Plan → Step 3: Details → Step 4: Payment | Step 1: Gift (frequency + intention combined) → Step 2: Payment (details + bank transfer) | None — same form fields, same API call | Medium — refactor step logic, merge Step 1+2 and Step 3+4 | **Critical** — every extra step loses sponsors | **High** |
| Preset amount buttons | Absent (amount is fixed by child) | Visual preset chips showing the child's amount + context | None | Trivial | Medium — makes amount feel chosen | Medium |
| Intention selector | Separate step | Collapsed accordion within Step 1: "Giving as Zakat or Sadaqah?" | None | Small — accordion component | High — keeps Islamic intent without forcing it on everyone | **High** |
| Post-gift account prompt | Absent | "After you give: create a password to follow X's progress" dark card in sidebar | None | Small | High — converts anonymous donors to portal users | **High** |
| Trust signals in checkout sidebar | Continuity Fund amber notice | Add: "87 of every 100 rupees reaches a child", "Cancel anytime", "256-bit encryption" | None | Trivial | High — reduces payment hesitation | **High** |
| Order summary: child thumbnail | Name + campus text only | Small thumbnail image slot (uses same placeholder as elsewhere) | None | Trivial | Medium | Medium |
| Confirmation: "Browse more" → next child | Returns to home | Offer specific sibling child from same campus | None | Small | Medium | Low |
| Apple Pay / payment method visuals | Card, Bank, JazzCash, Easypaisa shown as text chips | Same chips, already present — cosmetic alignment only | None | Trivial | Low | Low |

---

### 05 · Trust / Impact Page (`/trust`)

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Traceability steps (5-step money trail) | Absent | "Your gift → Allocation → School payment → Progress → Audit trail" with numbered columns | None | Small — new section with 5 column layout | **High** — the single most persuasive trust element in V4 | **High** |
| Live activity feed | Absent | "Latest activity": Ayesha found a sponsor, fees paid for 214 children, etc. | Large — requires real-time event log or public audit stream | n/a | High when live | **Defer** |
| Document shelf | Absent | 4 document cards: Annual Report PDF, Audit Letter, Registration Certificate, Zakat Policy | None — static links | Small — 4 cards with PDF links | **Critical** — these are the trust artifacts donors actually want | **High** |
| Governance: named people | Absent | 4 person cards: Board Chair, Trustee, Zakat Scholar, External Auditor | None — static content | Small | High | **High** |
| Page rename: "Transparency" → "Your impact" | Page is titled "Trust" | Rename to "Your impact" — reframe proof as pride | None | Trivial | Medium | Medium |
| Dashboard stats on this page | Absent | 4 big-number cards (children in school, waiting, graduates, active sponsors) | None for existing stats; large for new ones | Small | High | Medium |
| "Where every 100 rupees goes" bars | Present — already matches V4 | Same | Done | Done | Done | — |
| Continuity Fund section | Present — matches V4 | Same | Done | Done | Done | — |

---

### 06 · Stories Page

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Dedicated stories page | Absent | Graduate journey (4 chapters), sponsor voices, parent voices, teacher voices, community CTA | Large — no graduate tracking, no voice content | n/a | High long-term | **Defer** |

---

### 07 · Zakat & Sadaqah Page

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Dedicated `/zakat` page | Absent (content split across trust + why-give) | Full page: opening verse, 3 rewards, Zakat mechanics, calculator | None — static content + simple calculator | Medium — new page, new route | High for Muslim donor audience | **Defer** |
| Zakat calculator | Absent | Client-side only: cash + gold → 2.5% → children count | None | Medium — calculator widget | High | **Defer** (but low effort when ready) |

---

### 08 · Corporate Giving Page

| Feature | Current | V4 Proposed | Backend Impact | Frontend Effort | Business Value | Priority |
|---------|---------|-------------|----------------|-----------------|----------------|----------|
| Dedicated `/corporate` page | Absent | 4 units of giving, CSR reporting promise, contact CTA | None — static content | Medium — new page | Medium — opens new donor class | **Defer** |

---

## Future Features (Do Not Include in V4 Lite)

These require either major backend development or data that does not exist:

| Feature | Reason to Defer |
|---------|----------------|
| Stories / graduate profiles | No graduate entity or timeline data in DB |
| Live activity feed | Requires public event stream or WebSocket; no `audit_events` public endpoint |
| "X sponsored today" live counter | Requires a new public `/stats` endpoint with daily aggregation |
| Currency toggle (USD/GBP/AED/PKR) | No exchange rate data; no multi-currency display logic |
| AI / match-me recommendation engine | No affinity data |
| Photo management | Requires media storage architecture (Roadmap 2.5) |
| Child dream / interests / teacher notes | No structured fields in DB or `ChildDto` |
| Province / school level filter | No `province` field on `children` table |
| Graduation count | No graduate status in sponsorship lifecycle |
| Zakat-eligible filter | No `isZakatEligible` or `isOrphan` flag on `children` table |

---

## V4 Lite — Prioritized Implementation Plan

Ordered by highest impact, lowest effort. All items marked **[BACKEND]** require the one small migration described below.

### Phase A — Pure Frontend (0 backend changes, ~3–4 days)

| # | Page | Change | Notes |
|---|------|--------|-------|
| A1 | All | Rename "Bridged" → "Covered by the fund", "Seeking" → "Waiting for a sponsor" | 3 string changes in `one-child-at-a-time.component.ts`, `home.component.ts`, `child-detail.component.ts` |
| A2 | Trust | Add 5-step traceability section above allocation bars | New section: "Your gift → Allocation → School payment → Progress → Audit trail" |
| A3 | Trust | Add document shelf (4 static PDF cards) | Requires actual PDF URLs from JJT; slots are static |
| A4 | Trust | Add governance cards (4 named people) | Static content — board chair, trustee, scholar, auditor names needed from JJT |
| A5 | Sponsor flow | Collapse 4 steps → 2: merge Intention+Plan into Step 1, Details+Payment into Step 2 | Refactor `sponsor-commit.component.ts` step logic; same API payload unchanged |
| A6 | Sponsor flow | Move intention selector to collapsed accordion in Step 1 | "Giving as Zakat or Sadaqah?" accordion under frequency selector |
| A7 | Sponsor flow | Add trust signals to checkout sidebar | "87% reaches a child", "Cancel anytime", "Secure" — 3 bullet points below Continuity Fund |
| A8 | Sponsor flow | Add post-gift account prompt to sidebar | Dark card: "After you give, create a password to follow [child]'s progress" |
| A9 | Home | Add Islamic giving door strip between featured children and How It Works | Banner: "Giving as an act of worship? →" links to `/why-give` |
| A10 | Home | Restructure hero stats into a dedicated impact strip | Move 3 stats out of hero into a separate band below hero; add "Updated live" label |
| A11 | Child detail | Rewrite story section copy | Replace data dump with editorial paragraph about what sponsorship provides |
| A12 | Child detail | Upgrade "What happens after" to 5-stage timeline | Day 1 / 60 days / Each term / Each year / One day — same content, better layout |
| A13 | Children listing | Add tooltip explanation for "Covered by the fund" status | `<small>` block below the filter: explains what "Covered" means |
| A14 | Children listing | Add match-me banner above grid | Static banner: "Overwhelmed by X faces?" → link to `/why-give` (no AI needed) |
| A15 | Footer | Upgrade to 4-column layout matching V4 | Give / Proof / JJT columns + registration number strip |

---

### Phase B — One Small Backend Change (1 migration, 1 DTO field, ~0.5 days backend + 1 day frontend)

**Backend work required:**

```java
// 1. Add to ChildDto (public response):
LocalDate enrolledAt;  // populated from children.created_at

// 2. Add to PublicChildController response mapping:
// DtoMapper: .enrolledAt(entity.getCreatedAt().atZone(ZoneId.of("UTC")).toLocalDate())
```

No new table. No new migration. One field added to an existing public DTO.

| # | Page | Change | Notes |
|---|------|--------|-------|
| B1 | Home featured cards | Compute and display "Waiting X months" badge on child cards | `Math.floor((today - enrolledAt) / 30)` months |
| B2 | Children listing | "Waiting X months" amber badge on each card | Same computation as B1 |
| B3 | Child detail | Waiting time in sidebar status badge: "Waiting 4 months for a sponsor" | Same field |
| B4 | Home hero | Pill badge: "X children found a sponsor this month" | Use `DashboardChildrenStats.enrolledCount - availableCount` or keep static for now |

---

### Phase C — Deferred (Next Quarter)

| # | What |
|---|------|
| C1 | Stories page with graduate journey |
| C2 | Dedicated Zakat & Sadaqah page with calculator |
| C3 | Corporate giving page |
| C4 | Live activity feed (requires public audit endpoint) |
| C5 | Currency toggle (requires exchange rate API) |
| C6 | Child dream / interests / notes fields (requires DB schema + admin upload UI) |
| C7 | "X sponsored today" live counter (requires new aggregation query) |

---

## V4 Lite Scope Summary

| Phase | Days | Backend changes | Frontend changes | Business outcome |
|-------|------|----------------|-----------------|-----------------|
| A | 3–4 | 0 | 15 targeted improvements across 6 pages | Language clarity, trust credibility, checkout conversion |
| B | 1.5 | 1 DTO field | 3–4 components | Urgency signals on every child card |
| **Total** | **~5 days** | **minimal** | **complete V4 public experience** | |

---

## Key Decisions

**1. Keep the 4-step flow or collapse to 2?**
Collapse to 2. The V4 reasoning is correct: every step loses people. Intention + plan belong together (they are both "how do you want to give?"). Details + bank transfer belong together (they are both "complete the transaction"). The current 4-step flow asks for payment method before details, which creates friction.

**2. "Bridged" or "Covered by the fund"?**
Replace everywhere. "Bridged" is internal accounting language. A sponsor seeing "Bridged" has no idea what it means. "Covered by the fund" tells a story: someone else's pooled gift is keeping this child in school right now, and a personal sponsor is still needed.

**3. Dream/quote fields — wait for data?**
Yes. Do not add empty placeholder UI ("This child's dream will appear here"). The V4 child story section is powerful because the content is real. Until JJT collects structured dream/interest data, keep the existing story copy but rewrite the editorial framing. The field can be added as a Phase C enhancement once the admin UI has an input for it.

**4. Document shelf — block on PDFs?**
No. Add the document shelf section with 4 cards now. If PDFs aren't ready, the cards link to `#pending` with a "Coming soon" chip. The shelf structure itself is a trust signal; the PDFs make it actionable.

**5. Stories and Zakat pages — build or wait?**
Wait for stories (requires graduate data). The Zakat page is mostly static content + a client-side calculator — worth building independently in Phase C, but not a blocker for V4 Lite.
