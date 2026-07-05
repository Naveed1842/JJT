-- M2.1: Enforce uniqueness on sponsor contact_email.
-- Historical data may contain duplicate sponsor records with the same email.
-- Four-step deduplication:
--   1. Delete sponsorships from duplicates that conflict with the canonical's existing records.
--   2a. Reassign remaining sponsorships to the canonical sponsor.
--   2b. Reassign user accounts linked to duplicate sponsors to the canonical sponsor.
--   3. Delete the now-unreferenced non-canonical sponsor records.
-- Note: MIN(uuid) is not supported in PostgreSQL; id::text cast is used for deterministic ordering.

-- Step 1: Delete sponsorships from duplicate sponsors that would violate uk_sponsor_child_start
DELETE FROM sponsorships dup_sp
WHERE dup_sp.sponsor_id IN (
    SELECT id FROM sponsors
    WHERE contact_email IN (SELECT contact_email FROM sponsors GROUP BY contact_email HAVING COUNT(*) > 1)
    AND id::text NOT IN (SELECT MIN(id::text) FROM sponsors GROUP BY contact_email)
)
AND EXISTS (
    SELECT 1 FROM sponsorships canon_sp
    WHERE canon_sp.sponsor_id = (
        SELECT MIN(s.id::text)::uuid
        FROM sponsors s
        WHERE s.contact_email = (SELECT contact_email FROM sponsors WHERE id = dup_sp.sponsor_id)
    )
    AND canon_sp.child_id    = dup_sp.child_id
    AND canon_sp.start_month = dup_sp.start_month
);

-- Step 2a: Reassign remaining sponsorships from duplicate to canonical sponsor
UPDATE sponsorships s
SET sponsor_id = (
    SELECT MIN(s2.id::text)::uuid
    FROM sponsors s2
    WHERE s2.contact_email = (SELECT contact_email FROM sponsors WHERE id = s.sponsor_id)
)
WHERE sponsor_id IN (
    SELECT id FROM sponsors
    WHERE contact_email IN (SELECT contact_email FROM sponsors GROUP BY contact_email HAVING COUNT(*) > 1)
    AND id::text NOT IN (SELECT MIN(id::text) FROM sponsors GROUP BY contact_email)
);

-- Step 2b: Reassign user accounts linked to duplicate sponsors to the canonical sponsor
UPDATE users u
SET sponsor_id = (
    SELECT MIN(s.id::text)::uuid
    FROM sponsors s
    WHERE s.contact_email = (SELECT contact_email FROM sponsors WHERE id = u.sponsor_id)
)
WHERE u.sponsor_id IN (
    SELECT id FROM sponsors
    WHERE contact_email IN (SELECT contact_email FROM sponsors GROUP BY contact_email HAVING COUNT(*) > 1)
    AND id::text NOT IN (SELECT MIN(id::text) FROM sponsors GROUP BY contact_email)
);

-- Step 3: Delete the now-unreferenced non-canonical sponsor rows
DELETE FROM sponsors
WHERE contact_email IN (SELECT contact_email FROM sponsors GROUP BY contact_email HAVING COUNT(*) > 1)
AND id::text NOT IN (SELECT MIN(id::text) FROM sponsors GROUP BY contact_email);

-- Enforce uniqueness going forward
CREATE UNIQUE INDEX uk_sponsor_contact_email ON sponsors (contact_email);
