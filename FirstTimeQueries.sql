SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

SELECT relname AS table_name, n_live_tup AS approx_rows
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;

SELECT * FROM children ORDER BY id DESC LIMIT 50;


SELECT * FROM sponsorships ORDER BY id DESC LIMIT 50;

SELECT * FROM sponsorships ORDER BY id DESC LIMIT 50;

SELECT * FROM ledger_entries ORDER BY id DESC LIMIT 50;




