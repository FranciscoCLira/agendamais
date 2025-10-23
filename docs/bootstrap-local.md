# Local bootstrap (Flyway V4) — safe instructions

This short guide shows how to run the V4 bootstrap migration locally in a controlled way.

Important safety notes
- This migration can create privileged accounts. Always run on a disposable or staging database first.
- The migration requires an explicit guard: set `RUN_BOOTSTRAP=true` before running.
- Prefer using Flyway (mvn flyway:migrate) with a dedicated DB URL for staging or a local Postgres container.

Environment variables used by the migration
- RUN_BOOTSTRAP=true  (required to actually run)
- INSTITUTION_NAME (optional)
- INSTITUTION_EMAIL (optional)

Superuser (optional)
- SUPER_USERNAME
- SUPER_PASSWORD
- SUPER_EMAIL

Institution admin (optional)
- ADMIN_USERNAME
- ADMIN_PASSWORD
- ADMIN_EMAIL

How to run (PowerShell)
1. Start a local test Postgres (or ensure you have a DB for testing).
2. Export env vars and run the Flyway migrate goal from the repository root:

Example (PowerShell):

```powershell
$env:RUN_BOOTSTRAP = 'true'
$env:FLYWAY_URL = 'jdbc:postgresql://127.0.0.1:5432/agendadb'
$env:FLYWAY_USER = 'flyway'
$env:FLYWAY_PASSWORD = 'flyway'
# optional:
$env:SUPER_USERNAME = 'super'
$env:SUPER_PASSWORD = 'S3cur3P@ss!'
$env:SUPER_EMAIL = 'super@example.com'

# Run Flyway migrate (this will execute Java migrations in src/main/java/db/migration)
mvn -DskipTests org.flywaydb:flyway-maven-plugin:9.16.0:migrate -Dflyway.url=$env:FLYWAY_URL -Dflyway.user=$env:FLYWAY_USER -Dflyway.password=$env:FLYWAY_PASSWORD -Dflyway.locations=classpath:db/migration
```

How to run (cmd.exe)

```bat
REM Example for cmd.exe
set RUN_BOOTSTRAP=true
set FLYWAY_URL=jdbc:postgresql://127.0.0.1:5432/agendadb
set FLYWAY_USER=flyway
set FLYWAY_PASSWORD=flyway
REM optional:
set SUPER_USERNAME=super
set SUPER_PASSWORD=S3cur3P@ss!
set SUPER_EMAIL=super@example.com

mvn -DskipTests org.flywaydb:flyway-maven-plugin:9.16.0:migrate -Dflyway.url=%FLYWAY_URL% -Dflyway.user=%FLYWAY_USER% -Dflyway.password=%FLYWAY_PASSWORD% -Dflyway.locations=classpath:db/migration
```

What to expect
- Flyway will run migrations (including the Java migration V4). The migration prints informational messages to stdout about skipped/created accounts.
- If you need to re-run the bootstrap, delete the test DB or use a fresh database; the migration is idempotent (it checks for existing records before insert) but running in production requires caution.

If you'd like, I can add a small script to start a temporary Postgres container, run the migrate, and tear it down automatically. Let me know if you want that convenience script added.