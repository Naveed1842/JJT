CREATE TABLE audit_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    event_type      VARCHAR(60) NOT NULL,
    actor_id        UUID,
    actor_email     VARCHAR(255),
    entity_type     VARCHAR(50),
    entity_id       UUID,
    description     TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_org      ON audit_events(organisation_id, created_at DESC);
CREATE INDEX idx_audit_events_entity   ON audit_events(entity_type, entity_id) WHERE entity_id IS NOT NULL;
CREATE INDEX idx_audit_events_actor    ON audit_events(actor_id) WHERE actor_id IS NOT NULL;
