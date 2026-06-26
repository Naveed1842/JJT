# JJT - Early Education Support Platform

JJT is a humanitarian platform focused on one problem: children's education must not stop because sponsorship is delayed.
This repository contains the Phase-1 backend (Spring Boot) and frontend (Angular) code.

## Mission
- Education continuity comes first
- Sponsorship joins later
- Transparency is mandatory

## Phase-1 scope (locked)
In scope:
- Child registration without sponsor dependency
- Monthly education cost per child
- Early Support Pool (temporary coverage)
- Immutable monthly education ledger
- Support status (EARLY_SUPPORTED, SPONSORED, AT_RISK)
- Sponsor read-only visibility
- Monthly progress updates
- Role-based access control
- Manual, human-controlled workflows
- Low-cost, simple deployment

Out of scope:
- Online payments
- Payment gateways
- Automated sponsor sourcing
- AI / analytics
- Mobile apps
- Government integrations
- Adoption / legal workflows
- Marketing automation

## Architecture (Phase-1)
- Spring Boot (Java 17+)
- Modular monolith
- Domain-first, ledger-first design
- Append-only financial data
- Explicit transactions
- No microservices in Phase-1

## Repository docs
Start here:
- `docs/README.md`
- `docs/current-state.md`
- `docs/phase-1-guardrails.md`
- `docs/architecture.md`
- `docs/domain-model.md`
- `docs/ledger-rules.md`
- `docs/workflows.md`
- `docs/api/README.md`
- `docs/adr/README.md`

## Wiki
For quick, Phase-1 summaries, see the wiki: https://github.com/Naveed1842/JJT/wiki

---

# Deployment (Heroku Backend + Firebase Frontend)

This section documents the exact deployment flow we used for Phase‑1.

## Backend: Heroku (Spring Boot + Heroku Postgres)

### 1) Prerequisites
- Heroku CLI installed and logged in.
- Java 17 set for Heroku.

### 2) Required repo settings (already applied in this repo)
- `system.properties`:
  - `java.runtime.version=17`
- `src/main/resources/application.yml` and `src/main/resources/application-postgres.yml`:
  - `server.port: ${PORT:8080}`
- Spring Boot Maven plugin has `repackage` enabled so the jar is executable.

### 3) Create app + add Postgres
```bash
heroku apps:create jjt-platform
heroku addons:create heroku-postgresql --app jjt-platform
```

### 4) Set config (safe method)
Use Heroku’s `DATABASE_URL` and convert it to JDBC:
```bash
heroku config:set \
  SPRING_DATASOURCE_URL="$(heroku config:get DATABASE_URL -a jjt-platform | sed -E 's/^postgres:\/\//jdbc:postgresql:\/\//')" \
  --app jjt-platform
```

Then set DB credentials from the CLI output (do not paste secrets into git or chat):
```bash
heroku pg:credentials:url --app jjt-platform
heroku config:set \
  SPRING_DATASOURCE_USERNAME="<USER_FROM_PG_CREDENTIALS>" \
  SPRING_DATASOURCE_PASSWORD="<PASSWORD_FROM_PG_CREDENTIALS>" \
  --app jjt-platform
```

Activate postgres profile:
```bash
heroku config:set SPRING_PROFILES_ACTIVE=postgres --app jjt-platform
```

### 5) Deploy
```bash
git push heroku dev:main
```

### 6) Verify
```bash
heroku logs --tail --app jjt-platform
```

Health check:
```bash
curl https://<heroku-app>.herokuapp.com/actuator/health
```

### 7) CORS for Firebase
Allow your Firebase domain in:
`src/main/java/com/jjt/platform/config/WebConfig.java`

Example origins:
- `https://<project>.web.app`
- `https://<project>.firebaseapp.com`

### 8) Admin endpoints
Admin endpoints require `X-ROLE: ORG_ADMIN`. Public endpoints are under `/api/public/**`.

---

## Frontend: Firebase Hosting (Angular)

### 1) Environment config
Development:
`jjt-angular/src/environments/environment.ts`

Production:
`jjt-angular/src/environments/environment.prod.ts`

Set `apiBaseUrl` to your deployed backend URL.

### 2) Build output location
Angular 18 outputs to:
`dist/jjt-angular/browser`

Ensure `jjt-angular/firebase.json`:
```json
{
  "hosting": {
    "public": "dist/jjt-angular/browser",
    "ignore": [
      "firebase.json",
      "**/.*",
      "**/node_modules/**"
    ],
    "rewrites": [
      { "source": "**", "destination": "/index.html" }
    ]
  }
}
```

### 3) Build + Deploy
```bash
cd jjt-angular
npm run build
firebase deploy --only hosting
```

---

## pgAdmin (View Production DB)

Get credentials:
```bash
heroku pg:credentials:url --app jjt-platform
```

Then create a pgAdmin server with:
- Host: value after `host=`
- Port: `5432`
- Database: value after `dbname=`
- User: value after `user=`
- Password: value after `password=`
- SSL mode: **Require**
