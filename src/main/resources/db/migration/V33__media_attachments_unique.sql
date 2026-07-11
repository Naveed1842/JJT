-- Prevent duplicate attachment rows for the same file+owner+role combination.
-- ON CONFLICT DO UPDATE in BulkImportService makes this a safe dedup gate.

ALTER TABLE media_attachments
    ADD CONSTRAINT uq_media_attachment_per_role
        UNIQUE (media_id, owner_type, owner_id, attachment_role);
