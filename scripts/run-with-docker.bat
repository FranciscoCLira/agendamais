@echo off
REM Convenience script to bring up DB via Docker Compose and start the app
REM Usage: scripts\run-with-docker.bat dev|prod

setlocal
set ACTION=%1
if "%ACTION%"=="" (
  echo Usage: %~nx0 dev ^| prod
  exit /b 1
)

echo Bringing up Postgres and Adminer via Docker Compose...
docker compose up -d
if errorlevel 1 (
  echo ERROR: docker compose up failed. Ensure Docker Desktop is running and 'docker' is on PATH.
  exit /b 1
)

if /I "%ACTION%"=="dev" (
  echo Starting app in dev mode (use Maven for cleaner classpath)...
  mvn spring-boot:run -Dspring-boot.run.profiles=dev-docker
  exit /b %ERRORLEVEL%
)

if /I "%ACTION%"=="prod" (
  echo Building artifact and starting prod-like jar...
  mvn package -DskipTests
  if errorlevel 1 (
    echo mvn package failed
    exit /b 1
  )
  java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod-docker
  exit /b %ERRORLEVEL%
)

echo Unknown action: %ACTION%
endlocal
exit /b 1
