@echo off
REM cmd helper to run V4 bootstrap via Flyway
REM Usage: open cmd.exe in repo root, set environment variables described in docs\bootstrap-local.md, then run:
REM   scripts\run-bootstrap.cmd

nIF "%RUN_BOOTSTRAP%"=="" (
  echo RUN_BOOTSTRAP not set. Aborting.
  exit /b 1
)

nIF "%FLYWAY_URL%"=="" (
  echo FLYWAY_URL not set. Please set FLYWAY_URL, FLYWAY_USER and FLYWAY_PASSWORD before running.
  exit /b 1
)

necho Running Flyway migrate against: %FLYWAY_URL%
mvn -DskipTests org.flywaydb:flyway-maven-plugin:9.16.0:migrate -Dflyway.url=%FLYWAY_URL% -Dflyway.user=%FLYWAY_USER% -Dflyway.password=%FLYWAY_PASSWORD% -Dflyway.locations=classpath:db/migration
IF ERRORLEVEL 1 (
  echo Flyway migrate failed (exit code %ERRORLEVEL%).
  exit /b %ERRORLEVEL%
)
echo Flyway migrate finished.
exit /b 0