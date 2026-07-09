-- Sponsor users created via UserManagementService before this release were saved
-- with org_id = NULL (the service passed null instead of the sponsor's organisation).
-- Backfill from the linked sponsor row; fall back to the default org for any
-- remaining NULL rows so org-scoped queries (alerts, audit, dashboards) match.

UPDATE users u
SET org_id = s.organisation_id
FROM sponsors s
WHERE u.sponsor_id = s.id
  AND u.org_id IS NULL;

UPDATE users
SET org_id = (SELECT id FROM organisations WHERE slug = 'jjt')
WHERE org_id IS NULL;
