# Deployment (Phase-1)

## Goals
- Low cost
- Simple operations
- Clear ownership

## Principles
- Single application deployment (modular monolith)
- Single PostgreSQL database
- Explicit backups and restore procedures
- No complex infrastructure requirements in Phase-1

## Operational notes
- Prefer one environment per stage (dev, staging, prod)
- Keep configuration minimal and documented

---

## Required Environment Variables (Heroku)

Set these config vars before going live. Missing or wrong values are security risks.

| Variable | Required | Description |
|---|---|---|
| `AUTH_MODE` | **Yes** | Must be `FIREBASE` in production. Default is `DUAL` which allows X-ROLE header bypass. |
| `FIREBASE_SERVICE_ACCOUNT_BASE64` | **Yes** | Base64-encoded Firebase Admin SDK service account JSON. Download from Firebase Console → Project Settings → Service Accounts → Generate New Private Key, then: `cat key.json \| base64` |
| `FIREBASE_PROJECT_ID` | **Yes** | Your Firebase project ID (e.g. `your-project`) |
| `SPRING_DATASOURCE_URL` | **Yes** | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | **Yes** | DB username |
| `SPRING_DATASOURCE_PASSWORD` | **Yes** | DB password |
| `PORT` | Auto-set by Heroku | HTTP port, defaults to 8080 |

### Setting config vars via Heroku CLI

```bash
heroku config:set AUTH_MODE=FIREBASE --app jjt-platform
heroku config:set FIREBASE_PROJECT_ID=your-project-id --app jjt-platform
heroku config:set FIREBASE_SERVICE_ACCOUNT_BASE64=$(cat firebase-service-account.json | base64) --app jjt-platform
```

---

## Frontend Deployment (Firebase Hosting)

Before running `firebase deploy`:

1. Fill in `src/environments/environment.prod.ts` with values from Firebase Console → Project Settings → Your Apps → Web app → SDK config:
   - `apiKey`
   - `authDomain`
   - `projectId`
   - `appId`

2. Build the app: `cd jjt-angular && ng build`

3. Deploy: `cd jjt-angular && firebase deploy --only hosting`

---

## Local Development

The default `AUTH_MODE=DUAL` allows `X-ROLE` header fallback, which is intentional for local development without Firebase credentials.

To run locally:
```bash
docker-compose -f docker-compose.postgres.yml up -d
JAVA_HOME=/path/to/jdk17 mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
