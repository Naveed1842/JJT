# JJT Platform — C4 Architecture Diagrams

> C4 model: Level 1 (System Context), Level 2 (Container), Level 3 (Component).
> Architecture frozen 2026-07-11.

---

## Level 1 — System Context

Who uses the system and what external systems does it touch?

```mermaid
graph TB
    subgraph Users["External Actors"]
        SPONSOR["🧑 Sponsor\n(pays monthly,\nviews child progress)"]
        ADMIN["🏢 Org Admin / JJT Admin\n(manages children, payments,\nfund accounts, programmes)"]
        PUBLIC["🌐 Public Visitor\n(makes one-off donations,\nviews campaigns)"]
    end

    subgraph JJT["JJT Platform"]
        SYSTEM["JJT Sponsorship &\nFinance Platform\n\n• Child sponsorship lifecycle\n• Payment reconciliation\n• Fund & programme management\n• Donor receipting\n• Reporting & audit"]
    end

    subgraph External["External Systems"]
        EMAIL["📧 Email Provider\n(SMTP / SendGrid)\nPayment confirmations,\noverdue notices"]
        FIREBASE["🔥 Firebase Hosting\nAngular app delivery\nCDN"]
        HEROKU["☁️ Heroku\nSpring Boot runtime"]
        POSTGRES["🗄️ PostgreSQL 16\nPrimary data store"]
    end

    SPONSOR -->|"views progress, sees\npayment history"| JJT
    ADMIN -->|"records payments,\nmanages all data"| JJT
    PUBLIC -->|"donates, views\ncampaigns"| JJT

    JJT -->|"sends transactional\nemails"| EMAIL
    JJT -->|"served via"| FIREBASE
    JJT -->|"hosted on"| HEROKU
    JJT -->|"stores all data"| POSTGRES

    style JJT fill:#1C352C,color:#F5EFE3,stroke:#2F5D4F
    style Users fill:#F5EFE3,stroke:#C9942A
    style External fill:#f0f0f0,stroke:#999
```

---

## Level 2 — Container Diagram

What are the deployable units and how do they communicate?

```mermaid
graph TB
    subgraph Actors["Actors"]
        ADMIN["🏢 Admin / Org Admin"]
        SPONSOR["🧑 Sponsor"]
        PUBLIC["🌐 Public"]
    end

    subgraph Frontend["Frontend — Firebase Hosting"]
        ANGULAR["Angular 19 SPA\n\nStandalone components\nSignals-based state\nLazy-loaded routes\nJWT in memory\nRefresh token in localStorage\n\n→ sponsorone.app"]
    end

    subgraph Backend["Backend — Heroku"]
        SPRING["Spring Boot 3.2 API\n(Java 17)\n\nClean Architecture\nJWT HS512 auth\nFlyway migrations\n76 REST endpoints\n\n→ jjt-platform.herokuapp.com"]
    end

    subgraph Data["Data Layer"]
        POSTGRES["PostgreSQL 16\n\n24 tables\n25 Flyway migrations\nAppend-only rules\nPartial unique indexes\n\njjt database"]
    end

    subgraph Infra["Infrastructure"]
        SMTP["SMTP / Email\nSendGrid or configured\nMail server"]
    end

    ADMIN -->|"HTTPS"| ANGULAR
    SPONSOR -->|"HTTPS"| ANGULAR
    PUBLIC -->|"HTTPS"| ANGULAR

    ANGULAR -->|"REST API\nBearer JWT\nHTTPS"| SPRING
    SPRING -->|"JDBC / HikariCP\nSQL"| POSTGRES
    SPRING -->|"SMTP"| SMTP

    style Frontend fill:#2F5D4F,color:#F5EFE3,stroke:#1C352C
    style Backend fill:#1C352C,color:#F5EFE3,stroke:#2F5D4F
    style Data fill:#C9942A,color:#fff,stroke:#8B6510
    style Infra fill:#f0f0f0,stroke:#999
```

---

## Level 3 — Backend Component Diagram

How are the Spring Boot components structured internally?

```mermaid
graph TB
    subgraph API["API Layer (HTTP only — no business logic)"]
        AUTH_C["AuthController\n/api/auth/*\nLogin, refresh, logout,\n/me, change-password"]
        ADMIN_C["AdminControllers (10)\n/api/admin/*\nChildren, Sponsors, Payments,\nFunds, Programmes, Donations,\nAlerts, Users, Campaigns, Audit"]
        ORG_C["OrgControllers\n/api/org/*\nRead-only child, ledger,\nprogress (ORG_ADMIN)"]
        SPONSOR_C["SponsorControllers\n/api/sponsor/*\nScoped by sponsorId\nextracted from JWT"]
        PUBLIC_C["PublicController\n/api/public/*\nUnauthenticated:\ndonations, campaigns"]
        EX_HANDLER["GlobalExceptionHandler\nCentralised error mapping\n→ RFC 7807 ProblemDetail"]
    end

    subgraph AppLayer["Application Layer (Use Cases — own transactions)"]
        UC_AUTH["AuthUseCase\nLogin, token rotation,\nsession restore"]
        UC_CHILD["ChildManagementUseCase\nRegister, update, archive"]
        UC_SPONSOR["SponsorshipUseCase\nCreate, activate, suspend,\nterminate sponsorships"]
        UC_PAYMENT["PaymentUseCase\nRecord received, waive,\ngenerate expected, mark overdue"]
        UC_FUND["FundUseCase\nCredit, debit, assertSufficient\nFunds, balance query"]
    end

    subgraph Domain["Domain Layer (Zero Spring / JPA)"]
        ENT["Domain Entities\nChild, Sponsor, Sponsorship\nEducationSupportLedger\nLedgerEntry, FundTransaction\nFundAccount, SponsorPayment\nProgramme, ProgrammeExpense"]
        VO["Value Objects\nMoney (RoundingMode.UNNECESSARY)\nYearMonthValue"]
        EX["Domain Exceptions\nDomainException\nLedgerInvariantViolation\nSponsorshipInvariantViolation\nInsufficientFundsException"]
    end

    subgraph Infra["Infrastructure Layer"]
        REPO["24 Spring Data JPA\nRepositories"]
        MAPPER["DomainMapper\nJPA Entity ↔ Domain Object\nboundary conversion"]
        JPA["24 JPA Entities\n(@Entity classes,\nnever cross into domain)"]
        FLYWAY["Flyway Migrations\nV1–V25 (+ V26, V27 Phase 2.5)\nhibermate.ddl-auto=none"]
        NOTIF["NotificationService\nSMTP email dispatch\ntemplates per event type"]
        SEED["AdminUserInitializer\nDefault admin on first boot"]
    end

    subgraph Security["Security (Cross-cutting)"]
        JWT_PROV["JwtTokenProvider\nHS512, 15min access\n7-day opaque refresh"]
        JWT_FILTER["JwtAuthenticationFilter\nper-request token validation"]
        SEC_CONF["SecurityConfig\nCORS: localhost:4200\nsponsorone.app\nwww.sponsorone.app"]
    end

    AUTH_C --> UC_AUTH
    ADMIN_C --> UC_CHILD
    ADMIN_C --> UC_SPONSOR
    ADMIN_C --> UC_PAYMENT
    ADMIN_C --> UC_FUND
    ORG_C --> UC_CHILD
    SPONSOR_C --> UC_SPONSOR
    PUBLIC_C --> UC_FUND

    UC_AUTH --> ENT
    UC_CHILD --> ENT
    UC_SPONSOR --> ENT
    UC_PAYMENT --> ENT
    UC_FUND --> ENT

    ENT --> VO
    ENT --> EX

    UC_AUTH --> REPO
    UC_CHILD --> REPO
    UC_SPONSOR --> REPO
    UC_PAYMENT --> REPO
    UC_FUND --> REPO

    REPO --> JPA
    JPA --> MAPPER
    MAPPER --> ENT

    JWT_FILTER --> JWT_PROV
    SEC_CONF --> JWT_FILTER

    style API fill:#2F5D4F,color:#F5EFE3,stroke:#1C352C
    style AppLayer fill:#1C352C,color:#F5EFE3,stroke:#2F5D4F
    style Domain fill:#C9942A,color:#fff,stroke:#8B6510
    style Infra fill:#4a5568,color:#F5EFE3,stroke:#2d3748
    style Security fill:#6B7C6B,color:#F5EFE3,stroke:#4a5568
```

---

## Level 3 — Frontend Component Diagram

```mermaid
graph TB
    subgraph Bootstrap["Bootstrap (main.ts)"]
        INIT["APP_INITIALIZER\nrestoreSession()\nSilent JWT refresh on load"]
    end

    subgraph Services["Services (Singletons)"]
        AUTH_SVC["AuthService\nBehaviorSubject<CurrentUser|null>\nJWT in memory\nRefresh token in localStorage (rt)"]
        SPONSOR_SVC["SponsorService\nChildren, ledger, public commitment"]
        ADMIN_SVC["AdminService\nAll admin API calls"]
    end

    subgraph Interceptors["HTTP Interceptors"]
        AUTH_INT["AuthInterceptor\nAttaches Bearer token\nHandles 401 → refresh retry"]
        ERR_INT["HttpErrorInterceptor\nGlobal error mapping"]
    end

    subgraph Guards["Route Guards"]
        ADMIN_G["adminAuthGuard\nRole: JJT_ADMIN | ORG_ADMIN"]
        SPONSOR_G["sponsorAuthGuard\nRole: SPONSOR"]
    end

    subgraph State["Signal Stores"]
        CHILD_STORE["ChildrenStore\nSignals-based cache\nTTL invalidation"]
        ALERT_STORE["AlertsStore\nUnread count signal"]
    end

    subgraph Pages["Pages (Lazy-loaded)"]
        PUBLIC_P["Public Pages\nHome, Campaigns\nDonate, Sponsor Portal"]
        ADMIN_P["Admin Pages (18 sections)\nChildren, Sponsors, Payments\nFunds, Programmes, Donations\nAlerts, Users, Reports, Audit"]
        SPONSOR_P["Sponsor Portal\nDashboard, Child Profile\nLedger, Payments"]
    end

    subgraph Components["Shared Components"]
        HEADER["Header / Nav"]
        FOOTER["Footer"]
        CARDS["Child Cards\nPayment Tables\nAlert Badges"]
    end

    INIT --> AUTH_SVC
    AUTH_SVC --> INIT

    AUTH_INT --> AUTH_SVC
    ADMIN_G --> AUTH_SVC
    SPONSOR_G --> AUTH_SVC

    ADMIN_P --> ADMIN_SVC
    ADMIN_P --> CHILD_STORE
    ADMIN_P --> ALERT_STORE
    SPONSOR_P --> SPONSOR_SVC
    PUBLIC_P --> SPONSOR_SVC

    ADMIN_SVC --> AUTH_INT
    SPONSOR_SVC --> AUTH_INT
    AUTH_INT --> ERR_INT

    style Bootstrap fill:#1C352C,color:#F5EFE3
    style Services fill:#2F5D4F,color:#F5EFE3
    style State fill:#C9942A,color:#fff
    style Interceptors fill:#6B7C6B,color:#F5EFE3
    style Guards fill:#6B7C6B,color:#F5EFE3
    style Pages fill:#4a5568,color:#F5EFE3
    style Components fill:#4a5568,color:#F5EFE3
```

---

## Authentication Flow

```mermaid
sequenceDiagram
    participant Browser
    participant Angular
    participant SpringBoot
    participant PostgreSQL

    Note over Browser,PostgreSQL: Login
    Browser->>Angular: User submits credentials
    Angular->>SpringBoot: POST /api/auth/login {email, password}
    SpringBoot->>PostgreSQL: Lookup user, verify BCrypt hash
    PostgreSQL-->>SpringBoot: UserEntity
    SpringBoot->>PostgreSQL: INSERT refresh_token (SHA-256 hash, 7 days)
    SpringBoot-->>Angular: {accessToken (JWT 15min), refreshToken (UUID)}
    Angular->>Angular: Store accessToken in memory
    Angular->>Browser: Store refreshToken in localStorage (rt)

    Note over Browser,PostgreSQL: Authenticated Request
    Browser->>Angular: User action
    Angular->>SpringBoot: GET /api/... Bearer {accessToken}
    SpringBoot->>SpringBoot: Validate JWT (HS512, expiry, claims)
    SpringBoot->>PostgreSQL: Query data
    PostgreSQL-->>SpringBoot: Result
    SpringBoot-->>Angular: 200 + data

    Note over Browser,PostgreSQL: Token Refresh
    Angular->>SpringBoot: POST /api/auth/refresh {refreshToken}
    SpringBoot->>PostgreSQL: Lookup token by SHA-256 hash
    PostgreSQL-->>SpringBoot: TokenEntity (validate not revoked, not expired)
    SpringBoot->>PostgreSQL: UPDATE revoked_at = NOW() (old token)
    SpringBoot->>PostgreSQL: INSERT new refresh_token
    SpringBoot-->>Angular: {new accessToken, new refreshToken}

    Note over Browser,PostgreSQL: Session Restore (APP_INITIALIZER)
    Angular->>Angular: Read rt from localStorage
    Angular->>SpringBoot: POST /api/auth/refresh {rt}
    SpringBoot-->>Angular: {accessToken, refreshToken}
    Angular->>Angular: Restore CurrentUser in BehaviorSubject
```

---

## Finance Engine Data Flow (Phase 2.5)

```mermaid
graph LR
    subgraph Income["Income Side (unchanged)"]
        SP["Sponsor Payment\nPKR 2,000"]
        FT_CR["fund_transactions\ntype: CREDIT\nreason: SPONSOR_PAYMENT\nprogramme_id: nullable"]
        LE["ledger_entries\ncoverage_type: SPONSOR\nyear_month: 2025-07"]
    end

    subgraph Expense["Expense Side (NEW Phase 2.5)"]
        PE["programme_expenses\namount: PKR 800\ncategory: PERSONNEL\nstatus: APPROVED"]
        FT_DB["fund_transactions\ntype: DEBIT\nreason: PROGRAMME_EXPENSE\nprogramme_id: education-prog-id"]
        BC["budget_lines\nplanned: PKR 10,000/month\ncategory: PERSONNEL"]
    end

    subgraph Reports["Reports"]
        PL["Programme P&L\nRevenue − Expenses"]
        TR["Transparency Ratio\nEducation % of total spend"]
        BUDGET_VAR["Budget Variance\nPlanned vs Actual"]
    end

    SP -->|"recordPaymentReceived()"| FT_CR
    SP -->|"recordSponsorLedgerEntry()"| LE
    FT_CR -.->|"optional attribution"| PE

    PE -->|"backs the expense"| FT_DB
    BC -->|"plans the spend"| PE

    FT_CR --> PL
    FT_DB --> PL
    FT_DB --> TR
    BC --> BUDGET_VAR
    PE --> BUDGET_VAR
    PE --> TR

    style Income fill:#2F5D4F,color:#F5EFE3
    style Expense fill:#C9942A,color:#fff
    style Reports fill:#1C352C,color:#F5EFE3
```
