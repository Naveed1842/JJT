-- Add child identity/location fields with backfill

ALTER TABLE children ADD COLUMN roll_number VARCHAR(255);
ALTER TABLE children ADD COLUMN city VARCHAR(255);
ALTER TABLE children ADD COLUMN campus_name VARCHAR(255);
ALTER TABLE children ADD COLUMN school_name VARCHAR(255);

-- Backfill existing rows with deterministic placeholders
UPDATE children
SET roll_number = CONCAT('ROLL-', SUBSTRING(CAST(id AS VARCHAR), 1, 8)),
    city = 'UNKNOWN',
    campus_name = 'UNKNOWN'
WHERE roll_number IS NULL;

-- Enforce constraints
ALTER TABLE children ALTER COLUMN roll_number SET NOT NULL;
ALTER TABLE children ALTER COLUMN city SET NOT NULL;
ALTER TABLE children ALTER COLUMN campus_name SET NOT NULL;

-- Unique roll number
CREATE UNIQUE INDEX IF NOT EXISTS uk_child_roll_number ON children(roll_number);
