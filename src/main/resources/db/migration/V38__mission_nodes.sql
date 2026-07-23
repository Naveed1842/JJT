-- V38: Mission hierarchy tree + impact metrics + FK from campaigns to mission nodes

CREATE TABLE IF NOT EXISTS mission_nodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID         NOT NULL REFERENCES organisations(id) ON DELETE RESTRICT,
    parent_id       UUID         REFERENCES mission_nodes(id),
    kind            VARCHAR(20)  NOT NULL,  -- MISSION | PROGRAMME | PROJECT | CAMPAIGN | ACTIVITY
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',  -- DRAFT | ACTIVE | COMPLETED | ARCHIVED
    start_date      DATE,
    end_date        DATE,
    target_amount   NUMERIC(15,2),
    metadata        JSONB NOT NULL DEFAULT '{}',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mission_nodes_org    ON mission_nodes (org_id);
CREATE INDEX IF NOT EXISTS idx_mission_nodes_parent ON mission_nodes (parent_id) WHERE parent_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS impact_metrics (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mission_node_id  UUID         NOT NULL REFERENCES mission_nodes(id) ON DELETE CASCADE,
    metric_key       VARCHAR(100) NOT NULL,
    unit             VARCHAR(50),
    period_id        UUID         REFERENCES financial_periods(id),
    value_source     VARCHAR(20)  NOT NULL DEFAULT 'MANUAL',  -- MANUAL | COUNT_QUERY
    count_query_key  VARCHAR(100),
    manual_value     NUMERIC,
    recorded_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- Wire deferred FKs from earlier tables to mission_nodes
ALTER TABLE expenses         ADD CONSTRAINT IF NOT EXISTS fk_expenses_mission
    FOREIGN KEY (mission_node_id) REFERENCES mission_nodes(id);

ALTER TABLE financial_transactions ADD CONSTRAINT IF NOT EXISTS fk_fin_tx_mission
    FOREIGN KEY (mission_node_id) REFERENCES mission_nodes(id);

ALTER TABLE budgets          ADD CONSTRAINT IF NOT EXISTS fk_budgets_mission
    FOREIGN KEY (mission_node_id) REFERENCES mission_nodes(id);

-- Link existing campaigns to mission nodes (nullable seam)
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS mission_node_id UUID REFERENCES mission_nodes(id);
