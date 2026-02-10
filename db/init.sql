-- check if jjt database exists, if not create it and the user
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

-- CREATE DATABASE jjt;
-- CREATE USER jjt WITH PASSWORD 'jjt';
-- GRANT ALL PRIVILEGES ON DATABASE jjt TO jjt;
