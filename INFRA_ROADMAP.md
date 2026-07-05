# JJT Platform — Production Infrastructure & Deployment Roadmap

**Organisation:** Junior Jinnah Trust · `sponsorone.app`
**Prepared:** 2026-07-02
**Status:** Planning — Phase 0 in progress
**Stack baseline:** Spring Boot 3 / Java 17 · Angular 18 · PostgreSQL 16 · Heroku + Firebase Hosting

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Assessment](#current-state-assessment)
3. [Target Architecture](#target-architecture)
4. [Milestone Roadmap](#milestone-roadmap)
   - [Phase 0 — Immediate Hardening](#phase-0--immediate-hardening-weeks-12)
   - [Phase 1 — CI/CD Pipeline](#phase-1--cicd-pipeline-weeks-25)
   - [Phase 2 — GCP Infrastructure Migration](#phase-2--gcp-infrastructure-migration-weeks-510)
   - [Phase 3 — Production Hardening](#phase-3--production-hardening-weeks-1016)
5. [Cloud Infrastructure](#cloud-infrastructure)
6. [CI/CD Pipeline](#cicd-pipeline)
7. [Environment Management](#environment-management)
8. [Database Strategy](#database-strategy)
9. [Monitoring & Alerting](#monitoring--alerting)
10. [Secrets & Configuration Management](#secrets--configuration-management)
11. [Security Architecture](#security-architecture)
12. [File Storage Strategy](#file-storage-strategy)
13. [Scalability Plan](#scalability-plan)
14. [Cost Analysis](#cost-analysis)
15. [Operational Procedures](#operational-procedures)
16. [Risk Register](#risk-register)

---

## Executive Summary

The platform currently runs a single-dyno Heroku backend with no CI/CD pipeline, no staging environment, no monitoring, no file persistence, and no disaster recovery. This is acceptable for early development but creates existential risk as real children, donors, and sponsors depend on the data.

This roadmap migrates JJT to a hardened, cost-optimised **Google Cloud Platform** stack in three phases over 16 weeks, leveraging the **Google for Nonprofits** programme (up to $20,000/year in free GCP credits). The target monthly infrastructure cost is **$0** after NPO credit approval.

**Key outcomes by end of Phase 3:**
- Zero-downtime deployments with one-command rollback
- Point-in-time database recovery (RPO < 5 minutes)
- Automated CI/CD from git push to production
- Full observability: structured logs, metrics, alerts
- WAF protection with rate limiting on all public endpoints
- Isolated staging environment that mirrors production exactly

---

## Current State Assessment

| Area | Current State | Risk |
|---|---|---|
| Hosting | Heroku single dyno ($7/month) | Medium — limited scaling, vendor risk |
| Database | Heroku PostgreSQL Essential-0 | **Critical** — no automatic backups |
| CI/CD | Manual `git push heroku main` | High — no quality gates, no rollback |
| Staging | None | High — untested changes go straight to production |
| Secrets | Heroku config vars (not rotated) | Medium — no audit trail, no rotation policy |
| Monitoring | `/actuator/health` only | High — blind to errors and performance issues |
| File storage | In-memory generation, not persisted | Medium — receipts and reports lost after response |
| Disaster recovery | None | **Critical** — data loss = mission failure |
| Security | JWT auth in place; no WAF, no rate limiting | Medium |

---

## Target Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                          Internet                               │
└───────────────┬──────────────────────────┬─────────────────────┘
                │                          │
      ┌─────────▼──────────┐    ┌──────────▼──────────┐
      │   Firebase CDN     │    │   Cloud Armor WAF    │
      │   sponsorone.app   │    │   Rate limit · DDoS  │
      │   (Angular SPA)    │    │   OWASP rules        │
      └────────────────────┘    └──────────┬───────────┘
                                           │
                                 ┌─────────▼──────────┐
                                 │    Cloud Run        │
                                 │    jjt-api          │
                                 │    Spring Boot JAR  │
                                 │    min=1 · max=10   │
                                 └─────────┬───────────┘
                          ┌────────────────┼────────────────┐
                          │                │                │
               ┌──────────▼──┐  ┌──────────▼──┐  ┌────────▼──────┐
               │  Cloud SQL  │  │   Cloud     │  │   Secret      │
               │  PostgreSQL │  │   Storage   │  │   Manager     │
               │  (VPC only) │  │  PDFs·XLSX  │  │   env vars    │
               └─────────────┘  └─────────────┘  └───────────────┘
```

---

## Milestone Roadmap

### Overview

| Phase | Milestone | Weeks | Priority | Status |
|---|---|---|---|---|
| **0** | Rotate secrets + enable backups | 1–2 | CRITICAL | ⬜ Not started |
| **0** | Flyway validation hardening | 1–2 | HIGH | ⬜ Not started |
| **1** | GitHub Actions CI pipeline | 2–4 | HIGH | ⬜ Not started |
| **1** | Branch strategy + protected branches | 3–4 | HIGH | ⬜ Not started |
| **1** | Dockerfile + container build | 4–5 | HIGH | ⬜ Not started |
| **2** | Apply for Google for Nonprofits | 5 | HIGH | ⬜ Not started |
| **2** | GCP project + Cloud SQL setup | 5–6 | HIGH | ⬜ Not started |
| **2** | Backend: Heroku → Cloud Run | 6–7 | HIGH | ⬜ Not started |
| **2** | Database: Heroku PG → Cloud SQL | 7 | HIGH | ⬜ Not started |
| **2** | Cloud Armor WAF + rate limiting | 7–8 | MEDIUM | ⬜ Not started |
| **2** | Staging environment + auto-deploy | 8–9 | HIGH | ⬜ Not started |
| **3** | Secret Manager integration | 9–10 | HIGH | ⬜ Not started |
| **3** | Structured logging + Cloud Monitoring | 10–11 | MEDIUM | ⬜ Not started |
| **3** | Alerting + email/SMS integration | 11–12 | MEDIUM | ⬜ Not started |
| **3** | Cloud Storage for file persistence | 12–13 | MEDIUM | ⬜ Not started |
| **3** | Database index migrations (V24+) | 13–14 | MEDIUM | ⬜ Not started |
| **3** | PITR verification + restore drills | 14–15 | HIGH | ⬜ Not started |
| **3** | Security headers + OWASP CI scan | 15–16 | MEDIUM | ⬜ Not started |

---

### Phase 0 — Immediate Hardening (Weeks 1–2)

> These steps apply to the current Heroku deployment today. They protect live data before any migration begins.

#### M0.1 — Enable Database Backups

**Risk without this:** Complete data loss is possible at any time. Heroku Essential-tier has no automatic backups.

```bash
# Schedule daily backup at 02:00 UTC
heroku pg:backups:schedule DATABASE_URL --at "02:00 UTC" --app jjt-platform

# Capture a manual snapshot right now
heroku pg:backups:capture --app jjt-platform

# Download and store offsite
heroku pg:backups:download --app jjt-platform
```

**Acceptance criteria:** Daily backup schedule confirmed in `heroku pg:backups --app jjt-platform`.

---

#### M0.2 — Rotate All Secrets

```bash
# Generate a new 64+ character JWT secret
openssl rand -base64 64

# Set in Heroku (never commit to git)
heroku config:set JWT_SECRET="<new-value>" --app jjt-platform
heroku config:set ADMIN_PASSWORD="<new-strong-password>" --app jjt-platform
```

Audit git history for any committed secrets:
```bash
git log --all -S "JWT_SECRET" --oneline
git log --all -S "Admin@JJT" --oneline
```

Purge if found using `git filter-repo`.

**Acceptance criteria:** All secrets set via config vars only. No plaintext secrets in any committed file.

---

#### M0.3 — Flyway Migration Lock

Add to `application.yml`:
```yaml
spring:
  flyway:
    out-of-order: false
    validate-on-migrate: true
    baseline-on-migrate: false
```

**Acceptance criteria:** Application fails to start if migrations are out of sequence.

---

#### M0.4 — Actuator Endpoint Lock

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
```

**Acceptance criteria:** `/actuator/env`, `/actuator/beans`, `/actuator/heapdump` return 404 in production.

---

### Phase 1 — CI/CD Pipeline (Weeks 2–5)

#### M1.1 — Branch Strategy

```
main        ← production only · protected · PR + CI required
staging     ← pre-production · auto-deploys to staging environment
develop     ← integration branch · auto-deploys to dev
feature/*   ← short-lived · PR into develop
hotfix/*    ← branches from main · PR into main + develop
```

**Protection rules (GitHub Settings → Branches):**
- `main`: Require 1 approving review + all status checks passing. No direct push, no force push.
- `staging`: Require all status checks passing.

---

#### M1.2 — GitHub Actions CI Pipeline

**File:** `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [develop, staging, main]
  pull_request:
    branches: [develop, staging, main]

jobs:
  backend:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: jjt_test
          POSTGRES_USER: jjt
          POSTGRES_PASSWORD: jjt
        ports: ['5432:5432']
        options: --health-cmd pg_isready --health-interval 10s --health-timeout 5s --health-retries 5

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-maven-
      - name: Run tests
        run: ./mvnw verify
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/jjt_test
          SPRING_DATASOURCE_USERNAME: jjt
          SPRING_DATASOURCE_PASSWORD: jjt
          JWT_SECRET: ci-only-test-secret-minimum-64-characters-long-not-for-production
      - name: Upload test results
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: jjt-angular/package-lock.json
      - name: Install dependencies
        run: npm ci
        working-directory: jjt-angular
      - name: Production build
        run: npm run build -- --configuration=production
        working-directory: jjt-angular

  deploy-staging:
    needs: [backend, frontend]
    if: github.ref == 'refs/heads/staging'
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - name: Build JAR
        run: ./mvnw clean package -DskipTests
      - name: Build and push container
        run: |
          docker build -t gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA .
          docker push gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA
      - name: Deploy to Cloud Run (staging)
        run: |
          gcloud run deploy jjt-api-staging \
            --image gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA \
            --region us-central1 \
            --platform managed
      - name: Deploy Angular to Firebase preview channel
        run: firebase hosting:channel:deploy staging --expires 30d
        working-directory: jjt-angular

  deploy-production:
    needs: [backend, frontend]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment: production   # requires manual approval in GitHub UI
    steps:
      - uses: actions/checkout@v4
      - name: Build JAR
        run: ./mvnw clean package -DskipTests
      - name: Build and push container
        run: |
          docker build -t gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA .
          docker push gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA
      - name: Run Flyway migrations (prod DB — before traffic switch)
        run: |
          gcloud sql connect jjt-prod --user=jjt_app < /dev/null
          ./mvnw flyway:migrate -Dflyway.url=$PROD_DB_URL
      - name: Deploy to Cloud Run (production)
        run: |
          gcloud run deploy jjt-api \
            --image gcr.io/$GCP_PROJECT/jjt-api:$GITHUB_SHA \
            --region us-central1 \
            --platform managed \
            --tag stable
      - name: Smoke test
        run: |
          curl -sf https://api.sponsorone.app/actuator/health | grep '"status":"UP"'
      - name: Deploy Angular to Firebase Hosting (production)
        run: firebase deploy --only hosting
        working-directory: jjt-angular
```

---

#### M1.3 — Quality Gates

Every PR must pass all of the following before merging:

| Gate | Tool | Failure threshold |
|---|---|---|
| Backend tests | Maven Surefire + Testcontainers | Any failure |
| Code coverage | JaCoCo | < 70% on `application/` and `core/domain/` |
| Static analysis | SpotBugs + PMD | Any HIGH or CRITICAL finding |
| Dependency CVEs | OWASP `dependency-check` | Any CVSS ≥ 7.0 unresolved |
| Angular build | `ng build --configuration=production` | Any error |
| Secret scan | TruffleHog / git-secrets | Any credential in diff |

---

#### M1.4 — Rollback Strategy

**Backend (Cloud Run):** Keeps last 10 container revisions. One command, under 60 seconds, zero downtime.

```bash
# Identify previous revision
gcloud run revisions list --service=jjt-api --region=us-central1

# Route 100% traffic to previous revision
gcloud run services update-traffic jjt-api \
  --to-revisions=PREVIOUS_REVISION=100 \
  --region=us-central1
```

**Frontend (Firebase Hosting):** Full deployment history available.

```bash
# List recent deploys
firebase hosting:versions:list

# Roll back to a specific version
firebase hosting:clone SOURCE_VERSION_ID:jjt-platform:live
```

**Database:** Schema rollback is not supported (Flyway is forward-only by design). Mitigation:
1. Every migration PR includes a documented undo procedure in the description
2. Pre-migration snapshot taken automatically by CI before `main` deploy
3. Breaking changes use a three-PR sequence (add → migrate → remove)

---

#### M1.5 — Dockerfile

```dockerfile
# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build
COPY . .
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user
RUN addgroup -S jjt && adduser -S jjt -G jjt

COPY --from=build /build/target/jjt-platform-*.jar app.jar
RUN chown jjt:jjt app.jar

USER jjt
EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

**Acceptance criteria:** `docker build` produces an image under 250MB. Container starts in < 10 seconds.

---

### Phase 2 — GCP Infrastructure Migration (Weeks 5–10)

#### M2.1 — Google for Nonprofits Application

1. Visit [google.com/nonprofits](https://www.google.com/nonprofits/)
2. Verify JJT as a registered charitable trust (501(c)(3) equivalent)
3. Processing time: 2–4 weeks
4. Credits awarded: up to $20,000/year in GCP credits

**Fallback:** While awaiting credits, run staging at `minScale: 0` (shuts down when idle, < $5/month). Production remains on Heroku until credits are confirmed.

---

#### M2.2 — GCP Project Setup

```bash
# Create separate projects for isolation
gcloud projects create jjt-platform-prod --name="JJT Platform Production"
gcloud projects create jjt-platform-staging --name="JJT Platform Staging"

# Enable required APIs
gcloud services enable \
  run.googleapis.com \
  sqladmin.googleapis.com \
  storage.googleapis.com \
  secretmanager.googleapis.com \
  cloudbuild.googleapis.com \
  cloudarmor.googleapis.com \
  logging.googleapis.com \
  monitoring.googleapis.com \
  --project=jjt-platform-prod
```

---

#### M2.3 — Cloud SQL Provisioning

```bash
# Production instance
gcloud sql instances create jjt-prod \
  --database-version=POSTGRES_16 \
  --tier=db-g1-small \
  --region=us-central1 \
  --storage-auto-increase \
  --storage-size=20GB \
  --backup-start-time=02:00 \
  --retained-backups-count=30 \
  --retained-transaction-log-days=7 \
  --no-assign-ip \
  --network=default \
  --project=jjt-platform-prod

# Staging instance (smaller tier, scales to zero is not possible for SQL but micro suffices)
gcloud sql instances create jjt-staging \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --no-assign-ip \
  --network=default \
  --project=jjt-platform-staging

# Create database and application user
gcloud sql databases create jjt --instance=jjt-prod --project=jjt-platform-prod
gcloud sql users create jjt_app --instance=jjt-prod \
  --password=$(openssl rand -base64 32) --project=jjt-platform-prod
```

**Cloud SQL connection from Cloud Run:** Uses the Unix socket via the existing `postgres-socket-factory` dependency already present in `pom.xml`. No public IP, no firewall rules needed.

```
SPRING_DATASOURCE_URL=jdbc:postgresql:///jjt?cloudSqlInstance=jjt-platform-prod:us-central1:jjt-prod&socketFactory=com.google.cloud.sql.postgres.SocketFactory
```

---

#### M2.4 — Cloud Run Service

```yaml
# service.yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: jjt-api
  annotations:
    run.googleapis.com/ingress: all
spec:
  template:
    metadata:
      annotations:
        autoscaling.knative.dev/minScale: "1"
        autoscaling.knative.dev/maxScale: "10"
        run.googleapis.com/cloudsql-instances: jjt-platform-prod:us-central1:jjt-prod
        run.googleapis.com/cpu-throttling: "false"
    spec:
      serviceAccountName: jjt-api-sa@jjt-platform-prod.iam.gserviceaccount.com
      containers:
        - image: gcr.io/jjt-platform-prod/jjt-api:latest
          resources:
            limits:
              cpu: "2"
              memory: 512Mi
          ports:
            - containerPort: 8080
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
            initialDelaySeconds: 30
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
            initialDelaySeconds: 10
```

---

#### M2.5 — Database Migration from Heroku to Cloud SQL

```bash
# 1. Capture final Heroku backup
heroku pg:backups:capture --app jjt-platform

# 2. Download backup
heroku pg:backups:download --app jjt-platform -o latest.dump

# 3. Import into Cloud SQL
gcloud sql import sql jjt-prod gs://jjt-migration/latest.dump \
  --database=jjt --project=jjt-platform-prod

# 4. Verify row counts
# Run FirstTimeQueries.sql against both to confirm parity

# 5. Update DNS / environment to point at Cloud SQL
# 6. Decommission Heroku PostgreSQL addon
```

**Maintenance window required:** 15–30 minutes. Notify users in advance via email.

---

#### M2.6 — Cloud Armor WAF

```bash
# Create security policy
gcloud compute security-policies create jjt-waf \
  --description="JJT Platform WAF" \
  --project=jjt-platform-prod

# OWASP Top 10 managed rules
gcloud compute security-policies rules create 1000 \
  --security-policy=jjt-waf \
  --expression="evaluatePreconfiguredExpr('xss-v33-stable')" \
  --action=deny-403

gcloud compute security-policies rules create 1001 \
  --security-policy=jjt-waf \
  --expression="evaluatePreconfiguredExpr('sqli-v33-stable')" \
  --action=deny-403

# Rate limit: auth endpoint — 100 requests/minute per IP
gcloud compute security-policies rules create 2000 \
  --security-policy=jjt-waf \
  --expression="request.path.matches('/api/auth/login')" \
  --action=rate-based-ban \
  --rate-limit-threshold-count=100 \
  --rate-limit-threshold-interval-sec=60 \
  --ban-duration-sec=600

# Rate limit: all other endpoints — 1000 requests/minute per IP
gcloud compute security-policies rules create 2001 \
  --security-policy=jjt-waf \
  --action=rate-based-ban \
  --rate-limit-threshold-count=1000 \
  --rate-limit-threshold-interval-sec=60
```

---

### Phase 3 — Production Hardening (Weeks 10–16)

#### M3.1 — Secret Manager Integration

```bash
# Store all production secrets
echo -n "$JDBC_URL"      | gcloud secrets create db-url          --data-file=-
echo -n "$JWT_SECRET"    | gcloud secrets create jwt-secret      --data-file=-
echo -n "$DB_PASSWORD"   | gcloud secrets create db-password     --data-file=-
echo -n "$MAIL_PASSWORD" | gcloud secrets create mail-password   --data-file=-

# Grant Cloud Run service account access (read-only)
gcloud secrets add-iam-policy-binding jwt-secret \
  --member="serviceAccount:jjt-api-sa@jjt-platform-prod.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

Update Cloud Run to pull secrets as environment variables — no application code changes required.

**Rotation schedule:**

| Secret | Rotation frequency | Method |
|---|---|---|
| `jwt-secret` | Every 6 months | Rolling — old + new both valid during transition |
| `db-password` | Every 3 months | Cloud SQL user rotation |
| `admin-password` | On any personnel change | Immediate |
| GCP service account keys | Every 90 days | GCP policy enforcement |

---

#### M3.2 — Structured Logging

Add to `pom.xml`:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

Add `logback-spring.xml` for production profile — emits JSON to stdout, parsed automatically by Cloud Logging.

Every log line includes: `traceId`, `userId`, `orgId`, `requestPath`, `durationMs`, `status`.

---

#### M3.3 — Monitoring Dashboards

Add Micrometer GCP exporter:
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-metrics</artifactId>
</dependency>
```

**Key metrics tracked:**

| Metric | Alert threshold | Severity |
|---|---|---|
| HTTP 5xx error rate | > 1% over 5 minutes | P2 |
| HTTP P99 latency | > 2,000ms | P2 |
| JVM heap usage | > 80% | P3 |
| DB connection pool utilisation | > 90% | P2 |
| Auth failure rate (`/api/auth/login`) | > 10 failures/minute | P2 (brute force) |
| Cloud Run instance count | > 8 of 10 max | P3 (scaling pressure) |
| Flyway migration failure | Any | P1 |
| Daily backup failure | Any | P1 |

---

#### M3.4 — Alerting

| Severity | Trigger | Channel | Response SLA |
|---|---|---|---|
| P1 — Production down | Health check fails 2× in 5 min | Email + SMS | 15 minutes |
| P2 — High error rate | > 1% 5xx over 5 min | Email | 1 hour |
| P3 — Performance | P99 > 2s | Email | Next business day |
| Security | Auth anomaly > 10 failures/min | Email | 30 minutes |

---

#### M3.5 — Cloud Storage for File Persistence

**Bucket layout:**
```
jjt-files-prod/
├── imports/     {orgId}/{timestamp}-upload.xlsx          30-day retention
├── reports/     {orgId}/{childId}/{month}-report.pdf      1-year retention
├── exports/     {orgId}/{date}-children.xlsx              7-day retention
└── receipts/    {orgId}/{receiptNumber}.pdf               7-year retention (tax/legal)
```

**Access pattern:** Application generates file → uploads to GCS → returns signed URL with 15-minute expiry. Bucket is never public.

---

#### M3.6 — Database Index Migration (V24)

```sql
-- V24__performance_indexes.sql
CREATE INDEX CONCURRENTLY idx_children_org_city ON children (organisation_id, city);
CREATE INDEX CONCURRENTLY idx_children_org_campus ON children (organisation_id, campus_name);
CREATE INDEX CONCURRENTLY idx_sponsorships_org_status ON sponsorships (organisation_id, status);
CREATE INDEX CONCURRENTLY idx_ledger_entries_month_desc ON ledger_entries (ledger_id, entry_month DESC);
CREATE INDEX CONCURRENTLY idx_donations_org_date ON donations (organisation_id, donation_date DESC);
CREATE INDEX CONCURRENTLY idx_audit_events_org_created ON audit_events (organisation_id, created_at DESC);
```

---

#### M3.7 — Security Headers

Add to `SecurityConfig.java`:
```java
http.headers(headers -> headers
    .httpStrictTransportSecurity(hsts ->
        hsts.maxAgeInSeconds(31536000).includeSubDomains(true))
    .frameOptions(fo -> fo.deny())
    .contentTypeOptions(Customizer.withDefaults())
    .xssProtection(Customizer.withDefaults())
);
```

Add `Content-Security-Policy` response header via Cloud Run or a Spring filter.

---

#### M3.8 — PITR Verification + Monthly Restore Drills

**Monthly drill procedure (first Sunday of each month):**
```bash
# 1. Create PITR clone to 24 hours ago
gcloud sql instances clone jjt-prod jjt-restore-$(date +%Y%m%d) \
  --point-in-time=$(date -u -d '24 hours ago' '+%Y-%m-%dT%H:%M:%SZ')

# 2. Connect and verify row counts
gcloud sql connect jjt-restore-$(date +%Y%m%d) --user=jjt_app

# 3. Confirm children, sponsorships, donations counts match expectation
# 4. Delete restore instance
gcloud sql instances delete jjt-restore-$(date +%Y%m%d) --quiet
```

**Results:** Log outcome in `docs/restore-drills/` with timestamp and row counts.

---

## Cloud Infrastructure

### GCP Services

| Service | Purpose | Tier | Est. retail cost/month |
|---|---|---|---|
| Cloud Run | Backend compute (Spring Boot) | Serverless | ~$15 |
| Cloud SQL PostgreSQL 16 | Primary database | `db-g1-small` (prod) / `db-f1-micro` (staging) | ~$33 |
| Cloud Storage | File persistence (PDFs, XLSX, receipts) | Standard | ~$3 |
| Secret Manager | Secrets storage and rotation | — | ~$1 |
| Cloud Armor | WAF + DDoS + rate limiting | Managed rules | ~$5 |
| Cloud Monitoring + Logging | Observability | Free tier | ~$2 |
| Firebase Hosting | Angular SPA CDN | Spark (free) | $0 |
| **Total retail** | | | **~$59/month** |
| **After NPO credits** | | | **~$0/month** |

---

## CI/CD Pipeline

### Pipeline Flow

```
Developer pushes feature branch
        │
        ▼
GitHub PR created → CI runs:
  ├── Backend tests (Testcontainers + PostgreSQL)
  ├── Frontend build (ng build --prod)
  ├── JaCoCo coverage gate (≥ 70%)
  ├── SpotBugs static analysis
  ├── OWASP dependency audit
  └── Secret scan (TruffleHog)
        │
        ▼ All gates green
PR merged to develop → auto-deploys to dev (local/optional)
        │
        ▼
PR to staging branch → auto-deploys to Cloud Run staging
        │
        ▼
PR to main → requires manual approval → deploys to production
  ├── Flyway migrations run FIRST (before traffic switch)
  ├── Cloud Run rolling deploy (zero downtime)
  └── Smoke test: /actuator/health returns UP
```

### Deployment Time Targets

| Stage | Target duration |
|---|---|
| CI (tests + build) | < 8 minutes |
| Container build + push | < 3 minutes |
| Flyway migrations | < 1 minute |
| Cloud Run rolling deploy | < 2 minutes |
| Firebase deploy | < 1 minute |
| **End-to-end** | **< 15 minutes** |

---

## Environment Management

| Environment | Purpose | Branch | Database | Auto-deploy | Min instances |
|---|---|---|---|---|---|
| Development | Local dev + PR previews | `feature/*` | Docker Compose | No | N/A |
| Staging | Pre-production validation | `staging` | Cloud SQL `db-f1-micro` | Yes | 0 (scales to zero) |
| Production | Live data | `main` | Cloud SQL `db-g1-small` | After manual approval | 1 |

**Environment parity principle:** Staging uses the identical Docker image, identical Cloud Run configuration, and identical secret structure as production. Only instance sizes differ. This eliminates environment-specific bugs.

---

## Database Strategy

### Backup Policy

| Backup type | Frequency | Retention | Storage |
|---|---|---|---|
| Automated daily backup | 02:00 UTC | 30 days | Cloud SQL managed |
| Pre-deployment snapshot | On every `main` deploy | 90 days | Cloud Storage bucket |
| Weekly full export | Sundays 03:00 UTC | 1 year | `jjt-db-exports` GCS bucket |
| Point-in-time recovery (PITR) | Continuous transaction logs | 7 days | Cloud SQL managed |

### Disaster Recovery Targets

| Metric | Target |
|---|---|
| RTO (Recovery Time Objective) | < 2 hours |
| RPO (Recovery Point Objective) | < 5 minutes (via PITR) |
| Monthly restore drill | First Sunday of each month |
| Backup verification | Automated row-count check after each drill |

### Restore Scenarios

**Scenario A — Data corruption (within last 7 days):**
Use PITR clone to restore to any point within 7 days. Verify, then promote or selectively restore tables.

**Scenario B — Full instance loss:**
Restore from latest automated backup via Cloud SQL restore API. Target RTO < 1 hour.

**Scenario C — Accidental row deletion:**
PITR clone → pg_dump affected table → pg_restore into production. Target RTO < 2 hours.

### Migration Management

- All schema changes via `V{n}__description.sql` — no exceptions, no manual DDL
- Migrations run as a separate CI step before the new binary starts
- Staging always receives migrations 24 hours before production
- Breaking migrations use the three-PR sequence:
  1. PR 1: Add new column (backward-compatible)
  2. PR 2: Migrate data + update application to use new column
  3. PR 3: Drop old column

---

## Monitoring & Alerting

### Health Check Endpoints

| Endpoint | Used by | Purpose |
|---|---|---|
| `/actuator/health/liveness` | Cloud Run | Restart container if JVM hangs |
| `/actuator/health/readiness` | Cloud Run | Remove from traffic if DB unreachable |
| `/actuator/health` | Monitoring | Overall status for dashboards |

### Key Dashboards (Cloud Monitoring)

1. **API Health** — request rate, error rate, P50/P95/P99 latency
2. **Database** — query latency, connection pool usage, active connections
3. **JVM** — heap usage, GC pause time, thread count
4. **Business** — sponsorships created/day, donations recorded/day, active children count
5. **Security** — auth failures/minute, WAF rule triggers, 4xx/5xx by path

---

## Secrets & Configuration Management

### Secret Inventory

| Secret | Location | Rotation |
|---|---|---|
| `jwt-secret` | GCP Secret Manager | 6 months |
| `db-url` | GCP Secret Manager | On DB change |
| `db-password` | GCP Secret Manager | 3 months |
| `mail-password` | GCP Secret Manager | 6 months |
| `admin-password` | GCP Secret Manager | On personnel change |
| GCP service account key | GCP IAM (no key — Workload Identity) | N/A |

### Configuration Hierarchy

```
application.yml              ← shared defaults (no secrets, committed to git)
application-dev.yml          ← local dev overrides (gitignored)
application-staging.yml      ← staging non-secret config (committed)
application-prod.yml         ← prod non-secret config (committed)
GCP Secret Manager           ← all secrets for staging + production
```

---

## Security Architecture

### Layers of Defence

| Layer | Control | Implementation |
|---|---|---|
| Edge | DDoS protection | Google Cloud Armor managed rules |
| Edge | Rate limiting | 100 req/min on auth, 1000 req/min elsewhere |
| Edge | WAF | OWASP Top 10 managed rule set |
| Transport | TLS enforcement | Cloud Run + Firebase — TLS-only, HSTS |
| Network | Private database | Cloud SQL on VPC, no public IP |
| Application | Authentication | JWT HS512, 15-min access, 7-day refresh (revocable) |
| Application | Authorisation | Spring `@PreAuthorize` RBAC on every endpoint |
| Application | Input validation | `@Valid` on all DTOs, parameterised JPA queries |
| Application | Password security | BCrypt cost 12 |
| Application | Audit trail | Every write operation logged to `audit_events` |
| Dependencies | CVE scanning | OWASP dependency-check in CI weekly |
| Secrets | Rotation | GCP Secret Manager with rotation schedule |

### Security Headers (Target)

```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'; script-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
```

---

## File Storage Strategy

### Bucket Structure

```
jjt-files-prod/
├── imports/
│   └── {orgId}/{yyyy-MM-dd-HHmmss}-upload.xlsx
├── reports/
│   └── {orgId}/{childId}/{yyyy-MM}-progress.pdf
├── exports/
│   └── {orgId}/{yyyy-MM-dd}-{type}.xlsx
└── receipts/
    └── {orgId}/{receiptNumber}.pdf
```

### Access & Retention

| Prefix | Lifecycle rule | Reason |
|---|---|---|
| `imports/` | Delete after 30 days | Temporary — data already imported |
| `exports/` | Delete after 7 days | On-demand admin exports |
| `reports/` | Delete after 1 year | Historical but replaceable |
| `receipts/` | Delete after 7 years | Tax / legal retention requirement |

All access via **signed URLs** with 15-minute expiry. Bucket is never public. Application service account has `objectCreator` + `objectViewer` only — no `objectAdmin`.

---

## Scalability Plan

### Current Capacity (db-g1-small + Cloud Run max 10)

| Metric | Estimated capacity |
|---|---|
| Concurrent API requests | ~500/second |
| Children records | ~50,000 (comfortable) |
| Donations per year | ~100,000 |
| Organisations | ~50 |
| Ledger entries | ~500,000 (before partitioning needed) |

### Scaling Thresholds & Actions

| Signal | Threshold | Action |
|---|---|---|
| Cloud Run P99 > 1s consistently | 3 days | Profile and add DB indexes |
| Cloud Run instance count > 8/10 | Regularly | Upgrade to `db-n1-standard-1`, increase pool size |
| Donations table > 500K rows | — | Partition by `(organisation_id, year)` |
| Ledger entries > 1M rows | — | Monthly pre-aggregation table |
| Multiple orgs with heavy usage | — | Add pgBouncer connection pooler |

### HikariCP Tuning for Cloud Run

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10       # 10 instances × 10 = 100 max connections
      minimum-idle: 2
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

---

## Cost Analysis

### Current State (Heroku)

| Item | Monthly cost |
|---|---|
| Heroku Basic dyno | $7.00 |
| Heroku PostgreSQL Essential-0 | $5.00 |
| Firebase Hosting | $0.00 |
| **Total** | **$12.00** |

### Target State (GCP — retail pricing)

| Service | Monthly cost |
|---|---|
| Cloud Run (prod + staging) | ~$15.00 |
| Cloud SQL db-g1-small (prod) | ~$25.00 |
| Cloud SQL db-f1-micro (staging) | ~$8.00 |
| Cloud Storage | ~$3.00 |
| Cloud Armor | ~$5.00 |
| Secret Manager | ~$1.00 |
| Cloud Monitoring / Logging | ~$2.00 |
| Firebase Hosting | $0.00 |
| **Total retail** | **~$59.00** |
| **After Google for Nonprofits credits** | **~$0.00** |

**Break-even:** GCP at retail is ~$47/month more than Heroku, but $0 after NPO credits with significantly better reliability, scalability, and security.

---

## Operational Procedures

### Production Deployment Runbook

1. Merge PR into `main` — CI pipeline triggers automatically
2. All quality gates pass (tests · coverage · OWASP · secret scan)
3. GitHub Actions requests manual approval from designated approver
4. Approver reviews the deploy diff and clicks **Approve and deploy**
5. CI builds JAR → builds Docker image → pushes to `gcr.io/jjt-platform-prod/jjt-api:{git-sha}`
6. CI runs Flyway migrations against production DB (separate step — before traffic switch)
7. Cloud Run performs zero-downtime rolling deploy
8. Smoke test: `GET /actuator/health` returns `{"status":"UP"}`
9. Firebase Hosting deploy runs in parallel
10. Deploy complete — total wall-clock time under 15 minutes

### Incident Response

| Severity | Definition | First response | Escalation |
|---|---|---|---|
| P1 | Production completely down | Immediate rollback (< 60s) | All hands |
| P2 | > 5% error rate or partial outage | Investigate logs; rollback if root cause not clear in 30 min | Lead dev |
| P3 | Performance degraded, no errors | Investigate; fix in next deploy | Dev on-call |
| P4 | Minor functional issue | Log ticket; normal sprint | — |

**P1 rollback command:**
```bash
# Get previous revision name
PREV=$(gcloud run revisions list --service=jjt-api \
  --region=us-central1 --format="value(metadata.name)" --limit=2 | tail -1)

# Route 100% traffic back
gcloud run services update-traffic jjt-api \
  --to-revisions=$PREV=100 --region=us-central1

# Confirm
echo "Rollback complete. Traffic now routed to: $PREV"
```

### Maintenance Windows

- **Scheduled maintenance:** Sundays 02:00–04:00 UTC (lowest traffic for PKR/UK timezone donors)
- **Cloud SQL minor version updates:** Configured to maintenance window above
- **Dependency updates:** Monthly PR cycle — `mvn versions:display-dependency-updates` + `npm audit`
- **Certificate renewal:** Automatic via Firebase / Cloud Run managed TLS

---

## Risk Register

| Risk | Likelihood | Impact | Mitigation | Owner |
|---|---|---|---|---|
| Data loss before backups enabled | **High** (current) | **Critical** | Do Phase 0 today — enable Heroku backups immediately | Dev lead |
| GCP NPO credits delayed | Medium | Low | Staging at `minScale: 0`; prod cost < $60/month retail during wait | — |
| Flyway migration failure in production | Low | High | Staging deploys migrations 24h before prod; pre-migration DB snapshot | CI pipeline |
| Cold start latency on Cloud Run | Medium | Medium | `minScale: 1` in production; JVM startup ~3s is acceptable | Infra |
| Secret rotation causing auth downtime | Low | High | Rolling rotation — old + new JWT secrets valid during 30-min transition window | Dev lead |
| Database connection exhaustion | Low | High | HikariCP pool sized to 10/instance; Cloud SQL supports 100 connections | Infra |
| Heroku PostgreSQL data loss before migration | **High** (current) | **Critical** | Enable backups NOW (Phase 0); migrate within 8 weeks | Dev lead |

---

## Quick Reference — Key Commands

```bash
# Trigger manual backup
heroku pg:backups:capture --app jjt-platform

# Cloud Run rollback (production)
gcloud run services update-traffic jjt-api \
  --to-revisions=REVISION_NAME=100 --region=us-central1

# Cloud Run rollback (staging)
gcloud run services update-traffic jjt-api-staging \
  --to-revisions=REVISION_NAME=100 --region=us-central1

# Firebase rollback
firebase hosting:versions:list
firebase hosting:clone VERSION_ID:jjt-platform:live

# PITR clone (restore to specific point in time)
gcloud sql instances clone jjt-prod jjt-restore-tmp \
  --point-in-time="YYYY-MM-DDTHH:MM:SSZ"

# View Cloud Run logs
gcloud run services logs read jjt-api --region=us-central1 --limit=100

# Check active Cloud Run revision
gcloud run services describe jjt-api --region=us-central1 \
  --format="value(status.traffic[].revisionName,status.traffic[].percent)"

# Secret Manager — update a secret
echo -n "new-value" | gcloud secrets versions add jwt-secret --data-file=-
```

---

*This document should be reviewed and updated at the start of each phase. Archive completed phase sections to `docs/infra-history/` once delivered.*
