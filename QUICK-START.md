# AgendaMais - Quick Reference Card

## 🚀 Current Running Services

- **Dev App**: http://localhost:8080 (PID: 37444)
- **Prod App**: http://localhost:8081 (PID: 16712)
- **Adminer**: http://localhost:8083
- **PostgreSQL**: localhost:5432

## 🔑 Login Credentials

| Username | Password   | Role          |
| -------- | ---------- | ------------- |
| `superu` | `superu1$` | Super User    |
| `admin1` | `admin1$`  | Administrator |
| `autor1` | `autor1$`  | Author        |
| `parti1` | `parti1$`  | Participant   |

## 💾 Database Credentials

- **Host**: localhost
- **Port**: 5432
- **Username**: agenda
- **Password**: agenda
- **Databases**:
  - `agendadb_dev` (Dev)
  - `agendadb_prod` (Prod)

## 💤 Windows Suspend Mode

**✅ Recommended: Leave everything running**

- Docker and Java apps will automatically resume when Windows wakes up
- No action needed before suspending Windows

## 🔄 After Windows Restart

```powershell
# 1. Start Docker
docker compose up -d

# 2. Start Dev App (background)
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev-docker" -RedirectStandardOutput "app-dev.log" -RedirectStandardError "app-dev.err"

# 3. Start Prod App (background)
Start-Process -NoNewWindow -FilePath "java" -ArgumentList "-jar","target\agenda-mais-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod-docker" -RedirectStandardOutput "app-prod.log" -RedirectStandardError "app-prod.err"

# 4. Verify
netstat -ano | findstr "8080 8081"
```

## 🗄️ DBeaver Configuration

1. **New Connection** → PostgreSQL
2. **Settings**:
   - Host: `localhost`
   - Port: `5432`
   - Database: `agendadb_dev` or `agendadb_prod`
   - Username: `agenda`
   - Password: `agenda`
3. **Driver Properties** (if needed):
   - ssl: `false`
   - sslmode: `disable`
4. **Test Connection** and **Finish**

## 🛑 Stop Everything

```powershell
# Find PIDs
netstat -ano | findstr "8080 8081"

# Stop apps
taskkill /F /PID <dev-pid>
taskkill /F /PID <prod-pid>

# Stop Docker (keeps data)
docker compose stop
```

## 📝 Check Logs

```powershell
# View last 50 lines
Get-Content app-dev.log -Tail 50
Get-Content app-prod.log -Tail 50

# Follow in real-time
Get-Content app-dev.log -Wait -Tail 50
```

## 🔍 Check Status

```powershell
# Docker status
docker compose ps

# App ports
netstat -ano | findstr "8080 8081"

# Java processes
Get-Process java
```

## 📚 Full Documentation

See `README-docker.md` for complete instructions including:

- First-time setup
- Troubleshooting
- Clean restart procedures
- Advanced configurations
