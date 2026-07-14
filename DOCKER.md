# Docker Setup Guide for JJT Platform

This guide provides step-by-step instructions to build and run the JJT platform (backend + database) using Docker.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose installed (usually comes with Docker Desktop)
- Git installed
- Project cloned locally

## Quick Start (Recommended)

### 1. Build the Backend Docker Image

```bash
docker build -t jjt-backend:1.0 .
```

**What this does:**
- Reads the `Dockerfile` in the project root
- Builds a multi-stage image:
  - **Stage 1 (builder)**: Uses Maven to compile and package the Spring Boot application
  - **Stage 2 (runtime)**: Creates a lightweight image with just the compiled JAR and Java runtime
- Tags the image as `jjt-backend:1.0`

**Note:** The build process takes 5-10 minutes on first run. Subsequent builds are faster due to Docker layer caching.

### 2. Start Both PostgreSQL and Backend with Docker Compose

```bash
docker compose -f docker-compose.postgres.yml up -d
```

**What this does:**
- Starts PostgreSQL container (`jjt-postgres`) on port `5432`
- Starts the backend container (`jjt-backend`) on port `8080`
- Both containers run on the same Docker network (`jjt-network`) so they can communicate
- The backend connects to PostgreSQL via the hostname `postgres:5432` (not `localhost`)
- Both services restart automatically if Docker daemon restarts (`unless-stopped` policy)

### 3. Verify Everything is Running

```bash
docker ps
```

You should see:
- `jjt-postgres` — PostgreSQL 15-alpine
- `jjt-backend` — JJT Backend (jjt-backend:1.0)

### 4. Test the Backend Health Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

If status is `UP`, the backend is healthy and connected to the database.

## Detailed Process Explanation

### Issue: CRLF Line Endings on Windows

**Problem:** The `mvnw` script was created on Windows with CRLF line endings (`\r\n`). When Docker runs the script in a Linux container, the shell can't find the interpreter (exit code 127: "not found").

**Solution:** The `Dockerfile` includes a `sed` command to strip carriage returns:
```dockerfile
RUN sed -i 's/\r//' mvnw && chmod +x mvnw
```

This ensures `mvnw` works correctly inside the Linux container.

### Issue: Container Network Communication

**Problem:** When running the backend container alone, it tries to connect to `localhost:5432`, which inside the container means "inside the container itself," not the host machine or other containers.

**Solution:** Docker Compose creates a shared network (`jjt-network`) where:
- PostgreSQL is reachable via the service name `postgres` (DNS resolution)
- The backend is configured to use `jdbc:postgresql://postgres:5432/jjt`
- Both containers can communicate on this isolated network

## Environment Variables

The backend container is configured with these environment variables (set in `docker-compose.postgres.yml`):

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/jjt
SPRING_DATASOURCE_USERNAME: jjt
SPRING_DATASOURCE_PASSWORD: jjt
```

These override the defaults in `application.yml`, allowing the backend to connect to PostgreSQL in Docker.

## PostgreSQL Details

- **Service Name:** `postgres` (used by backend for connection)
- **Host Port:** `5432` (exposed for local tools)
- **Database:** `jjt`
- **Username:** `jjt`
- **Password:** `jjt`
- **Image:** `postgres:15-alpine` (lightweight, ~200MB)
- **Data Volume:** `postgres-data` (persisted across container restarts)

## Useful Docker Commands

### View Logs

**Backend logs:**
```bash
docker logs jjt-backend -f
```

**PostgreSQL logs:**
```bash
docker logs jjt-postgres -f
```

(Use `-f` to follow live output. Press `Ctrl+C` to stop.)

### Stop Services

```bash
docker compose -f docker-compose.postgres.yml down
```

This stops and removes both containers but keeps the PostgreSQL data volume.

### Remove Everything (Including Data)

```bash
docker compose -f docker-compose.postgres.yml down -v
```

The `-v` flag removes named volumes, including `postgres-data`. **Use carefully!**

### Restart Services

```bash
docker compose -f docker-compose.postgres.yml restart
```

### Access PostgreSQL Shell

```bash
docker exec -it jjt-postgres psql -U jjt -d jjt
```

Then run SQL queries directly in the PostgreSQL shell. Type `\q` to exit.

### Rebuild Backend Image

If you make code changes:

```bash
docker build -t jjt-backend:1.0 .
docker compose -f docker-compose.postgres.yml down
docker compose -f docker-compose.postgres.yml up -d
```

## Troubleshooting

### Backend Fails to Start: "Connection refused" on localhost:5432

**Cause:** PostgreSQL container is not running.

**Fix:**
```bash
docker ps  # Check if jjt-postgres is running
docker compose -f docker-compose.postgres.yml up -d  # Start if not running
```

### Container Name Already in Use

**Error:** `Conflict. The container name "/jjt-backend" is already in use...`

**Fix:**
```bash
docker rm jjt-backend  # Remove the old container
docker compose -f docker-compose.postgres.yml up -d  # Restart
```

Or use `docker compose down` to cleanly stop and remove all services:
```bash
docker compose -f docker-compose.postgres.yml down
docker compose -f docker-compose.postgres.yml up -d
```

### Port Already in Use

**Error:** `bind: address already in use` or `Port 5432 is already allocated`

**Fix:** Another service is using the port. Either:
1. Stop the other service
2. Change the port mapping in `docker-compose.postgres.yml` (e.g., `"5433:5432"`)

### Build Timeout / Network Issues

**Error:** `i/o timeout` when pulling base images, `dial tcp ... connection refused`

**Cause:** Docker can't reach Docker Hub to pull images. Usually a firewall, proxy, or DNS issue.

**Fix:**
1. Check internet connection
2. Restart Docker Desktop
3. If behind a proxy, configure Docker proxy settings in Docker Desktop → Settings → Resources → Proxies

### Health Check Shows "DOWN"

**Cause:** Backend is running but database connection failed, or migrations didn't complete.

**Fix:**
```bash
docker logs jjt-backend  # Check for detailed errors
docker logs jjt-postgres  # Verify database is ready
```

## Database Migrations

The backend uses **Flyway** for database migrations. On startup:
1. Spring Boot automatically runs Flyway
2. SQL migration scripts in `src/main/resources/db/migration/` are applied to PostgreSQL
3. If migrations fail, Flyway logs an error and the app doesn't start

Check migration status:
```bash
docker logs jjt-backend | grep Flyway
```

To see applied migrations in the database:
```bash
docker exec -it jjt-postgres psql -U jjt -d jjt -c "SELECT * FROM flyway_schema_history;"
```

## Frontend (Angular)

To run the Angular frontend in development mode:

```bash
cd jjt-angular
npm install
npm start
```

Then navigate to `http://localhost:4200`.

The frontend will automatically proxy API requests to `http://localhost:8080` (configured in `angular.json`).

## Production Deployment

For production, use the JAR file directly with environment variables:

```bash
docker run -d \
  --name jjt-backend-prod \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db-host:5432/jjt-prod \
  -e SPRING_DATASOURCE_USERNAME=prod_user \
  -e SPRING_DATASOURCE_PASSWORD=prod_password \
  -e JWT_SECRET=your-production-secret \
  jjt-backend:1.0
```

Or use a separate `docker-compose.prod.yml` for production setup.

## Summary

| Step | Command |
|------|---------|
| Build backend image | `docker build -t jjt-backend:1.0 .` |
| Start all services | `docker compose -f docker-compose.postgres.yml up -d` |
| Check status | `docker ps` |
| Test backend | `curl http://localhost:8080/actuator/health` |
| View logs | `docker logs jjt-backend -f` |
| Stop services | `docker compose -f docker-compose.postgres.yml down` |

## Next Steps

1. ✅ Backend running on `http://localhost:8080`
2. ✅ PostgreSQL running on `localhost:5432`
3. 📝 Frontend: `cd jjt-angular && npm install && npm start`
4. 🌐 Navigate to `http://localhost:4200` to access the app

---

For additional help, check the main [README.md](README.md) or consult the [CLAUDE.md](CLAUDE.md) for architecture details.
