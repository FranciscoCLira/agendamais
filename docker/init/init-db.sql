-- Init script to create the two databases used for local dev and prod-like testing.
-- This script runs once when the Postgres image initializes (docker-entrypoint-initdb.d).

-- Create a user (if not present) and two databases owned by that user.
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'agenda') THEN
      CREATE USER agenda WITH PASSWORD 'agenda';
   END IF;
END
$$;

-- create databases if they don't exist
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'agendadb_dev') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE agendadb_dev OWNER agenda');
   END IF;
EXCEPTION WHEN undefined_function THEN
   -- dblink may not be available in init environment; create databases directly
   CREATE DATABASE agendadb_dev OWNER agenda;
END
$$;

DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'agendadb_prod') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE agendadb_prod OWNER agenda');
   END IF;
EXCEPTION WHEN undefined_function THEN
   CREATE DATABASE agendadb_prod OWNER agenda;
END
$$;

-- Note: simple approach above tolerates environments where dblink is missing by falling back.
