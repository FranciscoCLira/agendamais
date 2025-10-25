@echo off
REM Transient Postgres helper (cmd)
REM Usage: scripts\transient-postgres.cmd start|migrate|run-app|full|stop|status

nSET containerName=agendamais-postgres
SET pgUser=flyway
SET pgPassword=flyway
SET pgDb=agendadb
SET pgPort=5432

nIF "%1"=="start" GOTO start
IF "%1"=="migrate" GOTO migrate
IF "%1"=="run-app" GOTO runapp
IF "%1"=="full" GOTO full
IF "%1"=="stop" GOTO stop
IF "%1"=="status" GOTO status

necho Usage: %0 start|migrate|run-app|full|stop|status
goto :eof

n:start
necho Starting Postgres container %containerName%
ndocker ps -a --format "{{.Names}}" | findstr /R /C:"^%containerName%$" >nul 2>&1
nIF %ERRORLEVEL%==0 (
  echo Removing existing container %containerName%
  docker rm -f %containerName%
)
docker run -d --name %containerName% -e POSTGRES_USER=%pgUser% -e POSTGRES_PASSWORD=%pgPassword% -e POSTGRES_DB=%pgDb% -p %pgPort%:5432 postgres:15
necho Waiting for Postgres to be ready...
nfor /L %%i in (1,1,30) do (
  docker exec %containerName% pg_isready -U %pgUser% -d %pgDb% >nul 2>&1
  if %ERRORLEVEL%==0 goto ready
  timeout /t 2 >nul
)
necho Postgres did not become ready in time
nexit /b 1
:ready
necho Postgres is ready
ngoto :eof

n:migrate
necho Running Flyway migrate against local Postgres
nset RUN_BOOTSTRAP=true
nset SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:%pgPort%/%pgDb%
set SPRING_DATASOURCE_USERNAME=%pgUser%
set SPRING_DATASOURCE_PASSWORD=%pgPassword%
rem Optionally set SUPER_/ADMIN_ env vars before calling this script to create accounts
mvn -DskipTests org.flywaydb:flyway-maven-plugin:9.16.0:migrate -Dflyway.url=%SPRING_DATASOURCE_URL% -Dflyway.user=%SPRING_DATASOURCE_USERNAME% -Dflyway.password=%SPRING_DATASOURCE_PASSWORD% -Dflyway.locations=classpath:db/migration
nif %ERRORLEVEL% NEQ 0 (
  echo Flyway migrate failed (exit %ERRORLEVEL%)
  exit /b %ERRORLEVEL%
)
ngoto :eof

n:runapp
necho Starting application in 'prod' profile against the transient Postgres DB
nset SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:%pgPort%/%pgDb%
set SPRING_DATASOURCE_USERNAME=%pgUser%
set SPRING_DATASOURCE_PASSWORD=%pgPassword%
set RUN_BOOTSTRAP=true
mvn -Dspring-boot.run.profiles=prod spring-boot:run
ngoto :eof

n:full
ncall %0 start
ncall %0 migrate
ncall %0 run-app
ngoto :eof

n:stop
necho Stopping and removing container %containerName%
docker rm -f %containerName%
ngoto :eof

n:status
ndocker ps -a --filter "name=%containerName%"
ngoto :eof