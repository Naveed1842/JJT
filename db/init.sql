-- check if jjt database exists, if not create it and the user
-- NOTE: This script requires the 'dblink' extension to be installed in PostgreSQL.
-- To enable it, connect as a superuser and run: CREATE EXTENSION IF NOT EXISTS dblink;
-- If dblink is not available or permissions are restricted, use the commented SQL below instead.

DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'jjt') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE jjt');
   END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'jjt') THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE USER jjt WITH PASSWORD ''jjt''');
        PERFORM dblink_exec('dbname=postgres', 'GRANT ALL PRIVILEGES ON DATABASE jjt TO jjt');
    END IF;
END
$$;

-- Alternative: If dblink is not available, run these commands directly as superuser:
-- CREATE DATABASE jjt;
-- CREATE USER jjt WITH PASSWORD 'jjt';
-- GRANT ALL PRIVILEGES ON DATABASE jjt TO jjt;
