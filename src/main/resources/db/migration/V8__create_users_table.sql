CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    sponsor_id UUID,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_sponsor FOREIGN KEY (sponsor_id) REFERENCES sponsors(id) ON DELETE SET NULL
);

-- Create index for faster lookups
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- -- Insert default admin user (password: admin123)
-- -- Hash generated using: BCryptPasswordEncoder with strength 10
-- INSERT INTO users (id, username, password, email, role, enabled)
-- VALUES (
--     'a0000000-0000-0000-0000-000000000001',
--     'admin',
--     '$2a$10$slYQmyNdGzin7olVN.tf8OPST9/PgBkqquzi.Ss8KIUgO2t0mC9m6',
--     'admin@jjt.org',
--     'ADMIN',
--     true
-- );

