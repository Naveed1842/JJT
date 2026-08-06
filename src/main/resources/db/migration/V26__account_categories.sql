-- V26: Chart of accounts + charity pack seed
-- Reporting classes: INCOME, PROGRAMME, ADMIN, FUNDRAISING, COGS, OPEX, ASSET, LIABILITY, EQUITY, TRANSFER

CREATE TABLE IF NOT EXISTS account_categories (
    id              SERIAL PRIMARY KEY,
    org_id          UUID REFERENCES organisations(id) ON DELETE RESTRICT,
    code            VARCHAR(10)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    parent_id       INTEGER REFERENCES account_categories(id),
    reporting_class VARCHAR(20)  NOT NULL,
    is_system       BOOLEAN      NOT NULL DEFAULT false,
    active          BOOLEAN      NOT NULL DEFAULT true,
    UNIQUE (org_id, code)
);

-- Charity pack: system-level categories (org_id = NULL → available to all orgs)
INSERT INTO account_categories (code, name, parent_id, reporting_class, is_system)
VALUES
  -- 1xxx Assets
  ('1000', 'Assets',                     NULL, 'ASSET',       true),
  ('1100', 'Cash and Bank',               1,   'ASSET',       true),
  ('1200', 'Receivables',                 1,   'ASSET',       true),
  -- 2xxx Liabilities
  ('2000', 'Liabilities',                NULL, 'LIABILITY',   true),
  ('2100', 'Accounts Payable',            4,   'LIABILITY',   true),
  -- 3xxx Equity / Reserves
  ('3000', 'Reserves',                   NULL, 'EQUITY',      true),
  -- 4xxx Income
  ('4000', 'Income',                     NULL, 'INCOME',      true),
  ('4100', 'Sponsorship Income',          7,   'INCOME',      true),
  ('4200', 'Donation Income',             7,   'INCOME',      true),
  ('4300', 'Grant Income',                7,   'INCOME',      true),
  ('4400', 'Zakat Income',                7,   'INCOME',      true),
  -- 5xxx Programme
  ('5000', 'Programme Expenditure',      NULL, 'PROGRAMME',   true),
  ('5100', 'Education Costs',            12,   'PROGRAMME',   true),
  ('5200', 'School Supplies',            12,   'PROGRAMME',   true),
  ('5300', 'Child Welfare',              12,   'PROGRAMME',   true),
  -- 6xxx Admin
  ('6000', 'Administration',             NULL, 'ADMIN',       true),
  ('6100', 'Staff Salaries',             17,   'ADMIN',       true),
  ('6200', 'Office Costs',               17,   'ADMIN',       true),
  ('6300', 'Technology',                 17,   'ADMIN',       true),
  ('6400', 'Professional Fees',          17,   'ADMIN',       true),
  -- 7xxx Fundraising
  ('7000', 'Fundraising',                NULL, 'FUNDRAISING', true),
  ('7100', 'Marketing & Outreach',       23,   'FUNDRAISING', true),
  ('7200', 'Events',                     23,   'FUNDRAISING', true),
  -- 8xxx Transfers
  ('8000', 'Transfers',                  NULL, 'TRANSFER',    true)
ON CONFLICT DO NOTHING;
