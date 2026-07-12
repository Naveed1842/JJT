# Rollback Runbook

## When to Rollback

Roll back when a production deploy causes one or more of:
- `/actuator/health` returns non-200 or `"status":"DOWN"`
- Error rate spike visible in Sentry
- Critical business flow broken (login, sponsorship, payments)
- Data corruption detected

---

## Backend Rollback — Heroku

Heroku keeps the last 5 successful releases. Roll back to the previous one instantly:

```bash
# List recent releases
heroku releases --app jjt-platform

# Roll back to the previous release (v42 → v41)
heroku rollback --app jjt-platform

# Roll back to a specific release number
heroku rollback v38 --app jjt-platform

# Verify the rollback
heroku releases --app jjt-platform
curl https://jjt-platform-23fdba49b06d.herokuapp.com/actuator/health
```

**Time to rollback:** ~2 minutes. Heroku switches the slug without a new build.

> ⚠️ Heroku rollback does NOT revert database migrations. If the bad deploy included a schema migration, rolling back the app code while the new schema is active may cause errors. See "Rolling Back a Migration" below.

---

## Frontend Rollback — Firebase Hosting

Firebase Hosting keeps all previous deploys. Roll back via the console or CLI:

```bash
# List hosting releases
firebase hosting:releases:list --project sandbox-27e5d

# Roll back to the previous version (use the version ID from the list)
firebase hosting:clone sandbox-27e5d:live sandbox-27e5d:live --version <VERSION_ID>
```

Or via Firebase Console → Hosting → Release history → Roll back.

**Time to rollback:** ~30 seconds (CDN propagation may take up to 2 minutes globally).

---

## Rolling Back a Database Migration

If a migration in the bad deploy must be undone:

1. **Do NOT run Flyway repair or delete migration records** unless you fully understand the consequences.
2. Take a manual backup immediately: `heroku pg:backups:capture --app jjt-platform`
3. If the migration was additive (new table, new nullable column) and the old code is schema-compatible, rolling back the app code is sufficient.
4. If the migration was destructive (column dropped, data deleted), you must restore from backup — see [backup-restore.md](./backup-restore.md).

---

## Incident Checklist

```
[ ] Detect: Sentry alert / UptimeRobot alert / user report
[ ] Confirm: curl /actuator/health → not UP?
[ ] Decide: rollback or hotfix? (rollback if cause unknown)
[ ] Backend rollback: heroku rollback --app jjt-platform
[ ] Frontend rollback: firebase hosting rollback (if frontend change)
[ ] Verify: /actuator/health returns UP, smoke test login
[ ] Communicate: post in team channel with timeline
[ ] Post-mortem: within 48h — root cause, timeline, prevention
```

---

## Hotfix Process (alternative to rollback)

When rollback is not safe (e.g., migration already applied):

```bash
# 1. Create hotfix branch from main
git checkout -b hotfix/describe-the-fix main

# 2. Fix, test locally
./mvnw verify

# 3. Push and let CI run
git push origin hotfix/describe-the-fix

# 4. Fast-track PR review (single reviewer is sufficient for hotfix)
# 5. Merge to main → CD pipeline deploys to staging → promote to prod
```
