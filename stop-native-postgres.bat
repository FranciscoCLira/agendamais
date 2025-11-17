@echo off
REM Stop native PostgreSQL service to free up port 5432 for Docker
REM Run this script as Administrator

echo Stopping native PostgreSQL service...
net stop postgresql-x64-14

echo.
echo Native Postgres stopped. You can now run the Docker-based app.
echo Run: java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev-docker
pause
