-- Create production database when container initializes
-- In docker-compose down -v && up -d, this will run on a fresh volume
CREATE DATABASE agendadb_prod;

-- Grant privileges to the default user
GRANT ALL PRIVILEGES ON DATABASE agendadb_prod TO agenda;
