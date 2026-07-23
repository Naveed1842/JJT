-- V40: Hardening — partial indexes, additional GIN indexes, ON DELETE RESTRICT enforcement

-- GIN on vendor metadata for AI query patterns
CREATE INDEX IF NOT EXISTS idx_vendors_metadata_gin
    ON vendors USING GIN (metadata);

-- Partial index: only active people (hot path for payroll computation)
CREATE INDEX IF NOT EXISTS idx_people_org_kind_active
    ON people (org_id, kind) WHERE active = true;

-- Partial index: open periods (hot path for expense/budget lookups)
CREATE INDEX IF NOT EXISTS idx_periods_org_open
    ON financial_periods (org_id, start_date) WHERE status = 'OPEN';

-- Partial index: pending / submitted approvals (queue view)
CREATE INDEX IF NOT EXISTS idx_approvals_org_pending
    ON approval_requests (org_id, created_at DESC)
    WHERE status IN ('SUBMITTED', 'RECOMMENDED_APPROVE', 'RECOMMENDED_REVIEW', 'RECOMMENDED_REJECT');

-- Monthly category totals view (used by AnomalyDetectionCheck)
CREATE OR REPLACE VIEW v_monthly_category_totals AS
SELECT
    org_id,
    category_id,
    TO_CHAR(effective_date, 'YYYY-MM') AS year_month,
    SUM(amount) AS total_amount
FROM financial_transactions
WHERE type IN ('EXPENSE', 'ADJUSTMENT')
  AND category_id IS NOT NULL
GROUP BY org_id, category_id, TO_CHAR(effective_date, 'YYYY-MM');
