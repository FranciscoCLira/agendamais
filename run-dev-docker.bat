@echo off
REM Run AgendaMais in dev mode with Docker Postgres
REM Make sure Docker Desktop is running and containers are up (docker compose up -d)
REM Make sure native Postgres is stopped (run stop-native-postgres.bat as Admin)

echo Starting AgendaMais in DEV mode with Docker Postgres...
echo.
echo Logs will be written to app-dev.log
echo Press Ctrl+C to stop the application
echo.

java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev-docker
