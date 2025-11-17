# Local Docker Setup for AgendaMais (PostgreSQL)

This repository includes a Docker Compose setup to run local PostgreSQL instances and Adminer (DB UI) for development and prod-like testing.

## Files Overview

- `docker-compose.yml` — PostgreSQL + Adminer containers
- `.env.docker` — Environment variables for database credentials
- `src/main/resources/application-dev-docker.properties` — Dev profile with data seeding enabled
- `src/main/resources/application-prod-docker.properties` — Prod-like profile with initial data loading
- `run-dev-docker.bat` — Convenience script to start dev environment (port 8080)
- `run-prod-docker.bat` — Convenience script to start prod environment (port 8081)

## Quick Start (First Time Setup)

### Prerequisites
- Docker Desktop installed and running
- Java 17+ and Maven 3.8+
- No native PostgreSQL service running on port 5432 (stop it with `stop-native-postgres.bat` if needed)

### 1. Start Docker Containers

```powershell
docker compose up -d
```

This starts:
- PostgreSQL 15 on port 5432 (databases: `agendadb_dev` and `agendadb_prod`)
- Adminer on port 8083 (database web UI)

### 2. Build the Application

```powershell
mvn clean package -DskipTests
```

### 3. Start the Applications

**Dev Environment (port 8080):**
```powershell
.\run-dev-docker.bat
```

**Prod Environment (port 8081) - in another terminal:**
```powershell
.\run-prod-docker.bat
```

Or start them in background mode:
```powershell
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev-docker" -RedirectStandardOutput "app-dev.log" -RedirectStandardError "app-dev.err"

Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod-docker" -RedirectStandardOutput "app-prod.log" -RedirectStandardError "app-prod.err"
```

### 4. Access the Applications

- **Dev App**: http://localhost:8080
- **Prod App**: http://localhost:8081
- **Adminer**: http://localhost:8083

## Sample Login Credentials

Both environments have the following test users:

| Username | Password   | Level | Role          |
|----------|------------|-------|---------------|
| `parti1` | `parti1$`  | 1     | Participant   |
| `autor1` | `autor1$`  | 2     | Author        |
| `admin1` | `admin1$`  | 5     | Administrator |
| `superu` | `superu1$` | 9     | Super User    |

## Suspend and Resume

### Option 1: Leave Everything Running (Recommended for Windows Suspend)

If you're putting Windows in suspend/sleep mode without restarting:
- **Docker containers and apps can stay running** — they will automatically resume when Windows wakes up
- Docker containers consume minimal resources when idle
- Java apps will also resume normally

**No action needed** — just put Windows to sleep and wake it up later.

### Option 2: Suspend Manually (Save Resources)

If you want to stop everything to save resources:

**Stop the Java applications:**
```powershell
# Find the process IDs
netstat -ano | findstr "8080 8081"

# Kill the processes (replace PID with actual process ID)
taskkill /F /PID <dev-pid>
taskkill /F /PID <prod-pid>
```

**Stop Docker containers (keeps data):**
```powershell
docker compose stop
```

**To resume later:**
```powershell
# Restart containers
docker compose start

# Restart applications (in background mode)
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev-docker" -RedirectStandardOutput "app-dev.log" -RedirectStandardError "app-dev.err"

Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod-docker" -RedirectStandardOutput "app-prod.log" -RedirectStandardError "app-prod.err"
```

## After Windows Restart

After a Windows restart, Docker containers will NOT auto-start. Follow these steps:

### 1. Verify Docker Desktop is Running

Check the system tray for Docker Desktop icon and ensure it's started.

### 2. Start Docker Containers

```powershell
docker compose up -d
```

### 3. Verify Containers are Running

```powershell
docker compose ps
```

Expected output:
```
NAME                          STATUS          PORTS
agendamais-adminer-1          Up              0.0.0.0:8083->8080/tcp
agendamais-postgres-1         Up              0.0.0.0:5432->5432/tcp
```

### 4. Start Applications

**Quick method (batch files):**
```powershell
.\run-dev-docker.bat    # In one terminal
.\run-prod-docker.bat   # In another terminal
```

**Background mode (recommended):**
```powershell
# Dev on port 8080
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev-docker" -RedirectStandardOutput "app-dev.log" -RedirectStandardError "app-dev.err"

# Prod on port 8081
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod-docker" -RedirectStandardOutput "app-prod.log" -RedirectStandardError "app-prod.err"
```

### 5. Verify Applications are Running

```powershell
netstat -ano | findstr "8080 8081"
```

You should see both ports listening.

## DBeaver Configuration for PostgreSQL

### Connection Settings

1. **Open DBeaver** and create a new connection (Database → New Database Connection)

2. **Select PostgreSQL** from the database list

3. **Configure the connection:**

   **Main Tab:**
   - **Host**: `localhost`
   - **Port**: `5432`
   - **Database**: Choose one:
     - `agendadb_dev` (for development database)
     - `agendadb_prod` (for production database)
   - **Username**: `agenda`
   - **Password**: `agenda`

   **PostgreSQL Tab:**
   - **Show all databases**: Unchecked (optional)
   
   **Driver Properties Tab** (if connection fails):
   - Add property: `ssl` = `false`
   - Add property: `sslmode` = `disable`

4. **Test Connection** — Click the "Test Connection" button

5. **Download Driver** — If prompted, let DBeaver download the PostgreSQL JDBC driver

6. **Finish** — Save the connection

### Troubleshooting DBeaver Connection Issues

**Issue: "Connection refused" or "Connection timed out"**
- Verify Docker containers are running: `docker compose ps`
- Verify port 5432 is listening: `netstat -ano | findstr 5432`
- Restart Docker containers: `docker compose restart`

**Issue: "FATAL: password authentication failed"**
- Double-check credentials: username `agenda`, password `agenda`
- Verify `.env.docker` file has correct credentials

**Issue: "Database does not exist"**
- The databases are created automatically by Docker
- Verify in Adminer: http://localhost:8083
  - System: `PostgreSQL`
  - Server: `postgres`
  - Username: `agenda`
  - Password: `agenda`
  - Database: `agendadb_dev` or `agendadb_prod`

**Issue: Driver not found**
- In DBeaver, go to Database → Driver Manager
- Find PostgreSQL, click Edit Driver
- Click "Download/Update" to get the latest JDBC driver

### Quick DBeaver Connection Summary

```
Connection Type: PostgreSQL
Host:            localhost
Port:            5432
Database:        agendadb_dev  (or agendadb_prod)
Username:        agenda
Password:        agenda
SSL:             Disabled
```

## Useful Commands

### Check Status

```powershell
# Docker containers
docker compose ps

# Application ports
netstat -ano | findstr "8080 8081 5432 8083"

# Java processes
Get-Process java | Select-Object Id,StartTime
```

### View Logs

```powershell
# Docker logs
docker compose logs postgres
docker compose logs adminer

# Application logs (if running in background)
Get-Content app-dev.log -Tail 50
Get-Content app-prod.log -Tail 50
Get-Content app-dev.err -Tail 50
Get-Content app-prod.err -Tail 50

# Follow logs in real-time
Get-Content app-dev.log -Wait -Tail 50
```

### Stop Everything

```powershell
# Stop applications
taskkill /F /PID <pid-dev>
taskkill /F /PID <pid-prod>

# Stop containers (keeps data)
docker compose stop

# Stop and remove containers (keeps data in volumes)
docker compose down

# Stop and remove everything including data (DESTRUCTIVE)
docker compose down -v
```

### Clean Restart

If you need to start fresh with empty databases:

```powershell
# Stop everything
docker compose down -v

# Start fresh
docker compose up -d

# Rebuild application
mvn clean package -DskipTests

# Start applications
# (use batch files or background commands from above)
```

## Database Credentials Reference

**PostgreSQL Connection:**
- Host: `localhost:5432`
- Dev Database: `agendadb_dev`
- Prod Database: `agendadb_prod`
- Username: `agenda`
- Password: `agenda`

**Adminer Web UI:**
- URL: http://localhost:8083
- System: PostgreSQL
- Server: `postgres`
- Username: `agenda`
- Password: `agenda`

## Notes

- Dev profile (`dev-docker`) has `app.reload-data=true` — automatically seeds data on startup
- Prod profile (`prod-docker`) has `app.reload-data=true` for initial setup — loads sample users and institutions
- Both environments use separate databases to avoid conflicts
- Flyway migrations run automatically on startup
- The applications use different ports (8080 for dev, 8081 for prod) so they can run simultaneously
- Docker containers consume minimal resources when idle, so it's safe to leave them running
- After Windows restart, you need to manually start containers and applications
