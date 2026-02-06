-- Add child identity/location fields with backfill

ALTER TABLE children
    ADD COLUMN roll_number VARCHAR(255),
    ADD COLUMN city VARCHAR(255),
    ADD COLUMN campus_name VARCHAR(255),
    ADD COLUMN school_name VARCHAR(255);

-- Backfill existing rows with deterministic placeholders
UPDATE children
SET roll_number = COALESCE(roll_number, 'ROLL-' || SUBSTRING(id::text, 1, 8)),
    city = COALESCE(city, 'UNKNOWN'),
    campus_name = COALESCE(campus_name, 'UNKNOWN'),
    school_name = school_name;

-- Enforce constraints
ALTER TABLE children
    ALTER COLUMN roll_number SET NOT NULL,
    ALTER COLUMN city SET NOT NULL,
    ALTER COLUMN campus_name SET NOT NULL;

-- Unique roll number
CREATE UNIQUE INDEX IF NOT EXISTS uk_child_roll_number ON children(roll_number);
