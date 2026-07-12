# Database Backup & Restore Runbook

## Scheduled Backups

Heroku Postgres automatically retains daily backups for 7 days on Standard tier and above.
Enable scheduled backups on the free/mini tier manually:

```bash
heroku pg:backups:schedule DATABASE_URL --at "02:00 UTC" --app jjt-platform
heroku pg:backups:schedule DATABASE_URL --at "02:00 UTC" --app jjt-platform-staging
```

Verify the schedule is active:
```bash
heroku pg:backups:schedules --app jjt-platform
```

---

## Manual Backup (before any risky migration or deploy)

```bash
# Capture a named backup
heroku pg:backups:capture --app jjt-platform

# List all backups
heroku pg:backups --app jjt-platform

# Download the latest backup locally
heroku pg:backups:download --app jjt-platform
# → writes b001.dump (or the latest backup number) to the current directory
```

---

## Restore Production Backup to Staging (for testing)

```bash
# Get the public URL of the latest production backup
BACKUP_URL=$(heroku pg:backups:url --app jjt-platform)

# Restore to staging (DESTRUCTIVE — wipes staging DB first)
heroku pg:backups:restore "$BACKUP_URL" DATABASE_URL --app jjt-platform-staging --confirm jjt-platform-staging
```

---

## Full Restore (disaster recovery)

```bash
# 1. Download the last known-good backup
heroku pg:backups:download b003 --app jjt-platform   # replace b003 with the target backup

# 2. Restore from the local dump file
heroku pg:backups:restore b003.dump DATABASE_URL \
  --app jjt-platform \
  --confirm jjt-platform

# 3. Restart dynos to clear any in-memory state
heroku restart --app jjt-platform

# 4. Verify health
curl https://jjt-platform-23fdba49b06d.herokuapp.com/actuator/health
```

**Recovery time objective:** ~15 minutes from incident detection to restored service.
**Recovery point objective:** Up to 24 hours of data loss (daily backups). For zero-RPO, enable continuous WAL archiving via Heroku Postgres Standard+.

---

## Restore to Local PostgreSQL (for debugging)

```bash
# Download dump
heroku pg:backups:download --app jjt-platform

# Restore to local DB
pg_restore --verbose --clean --no-acl --no-owner \
  -h localhost -U jjt -d jjt latest.dump
```

---

## Verify Backup Integrity

```bash
# List backups with size and status
heroku pg:backups --app jjt-platform

# A healthy backup shows status "Completed" and a non-zero size.
# If any backup shows "Failed", investigate immediately and capture a fresh one.
```

---

## Migration Rollback Policy

Flyway Community does not support undo migrations. All schema changes follow a **three-phase forward-only** approach:

| Phase | What | When |
|---|---|---|
| V{n} | Add new column/table (nullable, no constraints) | Deploy A |
| V{n+1} | Backfill data; add NOT NULL / constraints | Deploy B (after A is stable) |
| V{n+2} | Drop old column/table | Deploy C (after B is stable) |

For truly destructive changes with no rollback path: take a manual backup before deploying, and have the restore procedure ready.
