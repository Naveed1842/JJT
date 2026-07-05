-- M2.7: Notification System
-- Creates email_notifications log and admin_alerts tables.

CREATE TABLE email_notifications (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id      UUID NOT NULL REFERENCES organisations(id),
    recipient_email      VARCHAR(255) NOT NULL,
    recipient_name       VARCHAR(255),
    template             VARCHAR(50) NOT NULL
                             CHECK (template IN ('PAYMENT_RECEIVED','SPONSORSHIP_ACTIVATED',
                                                 'PROGRESS_UPDATE','PAYMENT_OVERDUE','DONATION_RECEIPT')),
    subject              VARCHAR(500) NOT NULL,
    status               VARCHAR(10) NOT NULL DEFAULT 'QUEUED'
                             CHECK (status IN ('QUEUED','SENT','FAILED')),
    error_message        TEXT,
    related_entity_id    UUID,
    related_entity_type  VARCHAR(50),
    sent_at              TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_notifications_org     ON email_notifications(organisation_id);
CREATE INDEX idx_email_notifications_status  ON email_notifications(status) WHERE status != 'SENT';
CREATE INDEX idx_email_notifications_created ON email_notifications(created_at DESC);

CREATE TABLE admin_alerts (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id      UUID NOT NULL REFERENCES organisations(id),
    alert_type           VARCHAR(30) NOT NULL
                             CHECK (alert_type IN ('PAYMENT_OVERDUE','FUND_BELOW_RESERVE',
                                                   'SPONSORSHIP_AT_RISK','GENERAL')),
    severity             VARCHAR(10) NOT NULL DEFAULT 'WARNING'
                             CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    title                VARCHAR(255) NOT NULL,
    message              TEXT NOT NULL,
    related_entity_id    UUID,
    related_entity_type  VARCHAR(50),
    dismissed_at         TIMESTAMP WITH TIME ZONE,
    dismissed_by         UUID REFERENCES users(id),
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_admin_alerts_org        ON admin_alerts(organisation_id);
CREATE INDEX idx_admin_alerts_active     ON admin_alerts(organisation_id, created_at DESC)
    WHERE dismissed_at IS NULL;
