# Docker-based local setup for AgendaMais

This document shows how to run a reproducible local DB (Postgres) for both dev and prod-like runs using Docker Compose.

## Prerequisites

- Docker Desktop installed and running on your machine
- Java 17+ installed
- Maven 3.8+ installed

## Files added

- `docker-compose.yml` - starts Postgres and Adminer (DB UI)
- `.env.docker` - environment values used by Postgres container
- `docker/init/*.sql` - initialization scripts to create `agendadb_dev` and `agendadb_prod` databases
- `src/main/resources/application-dev-docker.properties` - Spring profile for dev when using Docker Postgres
- `src/main/resources/application-prod-docker.properties` - Spring profile for prod-like when using Docker Postgres
- `run-dev-docker.bat` - convenience script to run dev profile
- `run-prod-docker.bat` - convenience script to run prod profile
- `stop-native-postgres.bat` - script to stop native PostgreSQL service (run as Administrator)

## Quick start

### 1. Stop native PostgreSQL (if installed)

**IMPORTANT:** If you have PostgreSQL installed natively on Windows, it will conflict with the Docker container on port 5432.

Run as **Administrator**:

```cmd
stop-native-postgres.bat
```

Or manually:

```cmd
net stop postgresql-x64-14
```

### 2. Start Docker containers

From the repository root:

```powershell
docker compose up -d
```

Verify containers are running:

```powershell
docker compose ps
```

You should see `agendamais-db` and `agendamais-adminer` both in "Up" state.

### 3. Build the application

```powershell
mvn clean package -DskipTests
```

### 4. Run the application

**Option A: Dev mode (with data seeding)**

```cmd
run-dev-docker.bat
```

Or manually:

```powershell
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev-docker
```

The app will start on http://localhost:8080

**Option B: Prod-like mode**

```cmd
run-prod-docker.bat
```

Or manually:

```powershell
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod-docker
```

The app will start on http://localhost:8080

### 5. Access Adminer (DB UI)

Open http://localhost:8083 and use:

- **System:** PostgreSQL
- **Server:** db (or localhost if connecting from host)
- **Username:** agenda
- **Password:** agenda
- **Database:** agendadb_dev (or agendadb_prod)

## Database credentials

- **Username:** `agenda`
- **Password:** `agenda`
- **Dev database:** `agendadb_dev` (port 5432)
- **Prod database:** `agendadb_prod` (port 5432)

These are defined in `.env.docker` and used by the Docker Postgres container.

## Troubleshooting

### Port 5432 already in use

Check if native Postgres is running:

```powershell
netstat -ano | findstr "5432"
```

If you see multiple processes, stop the native PostgreSQL service (as Administrator):

```cmd
net stop postgresql-x64-14
```

### Authentication failed errors

If you see "password authentication failed", ensure:

1. Native Postgres is stopped
2. The app is connecting to the Docker container, not a local instance
3. The credentials in `application-*-docker.properties` match `.env.docker`

To test Docker Postgres directly:

```powershell
docker exec -it agendamais-db psql -U agenda -d agendadb_dev -c "\conninfo"
```

### Clean start (reset database)

To completely reset the database:

```powershell
docker compose down -v
docker compose up -d
```

⚠️ **Warning:** This will delete all data in the databases.

## Notes & recommendations

- The Compose setup exposes Postgres on port **5432**. If you want to keep native Postgres running, edit `docker-compose.yml` to map Docker Postgres to a different host port (e.g., `"15432:5432"`).
- Adminer is included for quick DB browsing; you can remove it from `docker-compose.yml` if you prefer PgAdmin or DBeaver.
- `application-dev-docker.properties` enables `baseline-on-migrate=true` so Flyway won't fail on pre-existing schemas; **do not enable this in real production** without auditing.
- For production deployments, use proper secrets management instead of `.env.docker` files.
