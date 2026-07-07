-- Media bulk import job tracking

CREATE TABLE media_import_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID NOT NULL REFERENCES organisations(id),
    job_type        VARCHAR(30) NOT NULL DEFAULT 'BULK_CHILD_PHOTOS',
    status          VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    manifest_ref    VARCHAR(500),
    archive_ref     VARCHAR(500),
    total_files     INTEGER NOT NULL DEFAULT 0,
    matched         INTEGER NOT NULL DEFAULT 0,
    uploaded        INTEGER NOT NULL DEFAULT 0,
    skipped         INTEGER NOT NULL DEFAULT 0,
    failed          INTEGER NOT NULL DEFAULT 0,
    error_report    JSONB NOT NULL DEFAULT '[]',
    created_by      UUID NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    completed_at    TIMESTAMP
);

CREATE INDEX idx_import_jobs_org ON media_import_jobs(org_id, status);

CREATE TABLE media_import_items (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id                UUID NOT NULL REFERENCES media_import_jobs(id) ON DELETE CASCADE,
    source_filename       VARCHAR(255) NOT NULL,
    matched_entity_type   VARCHAR(50),
    matched_entity_id     UUID,
    match_key             VARCHAR(100),
    status                VARCHAR(20) NOT NULL,
    media_id              UUID REFERENCES media_files(id),
    error_detail          TEXT
);

CREATE INDEX idx_import_items_job ON media_import_items(job_id, status);
