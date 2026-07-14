-- Media Management Platform — core tables
-- Replaces the simpler 'attachments' table from Roadmap 5 V30

CREATE TABLE media_files (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id            UUID NOT NULL REFERENCES organisations(id),
    storage_ref       VARCHAR(500) NOT NULL,
    storage_provider  VARCHAR(20) NOT NULL,
    original_name     VARCHAR(255) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    media_type        VARCHAR(20) NOT NULL,
    size_bytes        BIGINT NOT NULL DEFAULT 0,
    width_px          INTEGER,
    height_px         INTEGER,
    duration_secs     INTEGER,
    visibility        VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    status            VARCHAR(20) NOT NULL DEFAULT 'UPLOADING',
    content_hash      VARCHAR(64),
    alt_text          VARCHAR(500),
    metadata          JSONB NOT NULL DEFAULT '{}',
    uploaded_by       UUID NOT NULL REFERENCES users(id),
    uploaded_at       TIMESTAMP NOT NULL DEFAULT now(),
    deleted_at        TIMESTAMP
);

CREATE INDEX idx_media_files_org      ON media_files(org_id);
CREATE INDEX idx_media_files_status   ON media_files(org_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_media_files_type     ON media_files(org_id, media_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_media_files_hash     ON media_files(org_id, content_hash) WHERE content_hash IS NOT NULL;
CREATE INDEX idx_media_files_metadata ON media_files USING GIN(metadata);

CREATE TABLE media_variants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id        UUID NOT NULL REFERENCES media_files(id) ON DELETE CASCADE,
    variant_type    VARCHAR(30) NOT NULL,
    storage_ref     VARCHAR(500) NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    size_bytes      BIGINT NOT NULL DEFAULT 0,
    width_px        INTEGER,
    height_px       INTEGER,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (media_id, variant_type)
);

CREATE INDEX idx_media_variants_media ON media_variants(media_id);

CREATE TABLE media_attachments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id        UUID NOT NULL REFERENCES media_files(id),
    owner_type      VARCHAR(50) NOT NULL,
    owner_id        UUID NOT NULL,
    attachment_role VARCHAR(50) NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    attached_by     UUID NOT NULL REFERENCES users(id),
    attached_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_media_att_owner ON media_attachments(owner_type, owner_id);
CREATE INDEX idx_media_att_role   ON media_attachments(owner_type, owner_id, attachment_role);
CREATE INDEX idx_media_att_media  ON media_attachments(media_id);
