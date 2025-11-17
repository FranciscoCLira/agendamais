@echo off
REM Simple, robust launcher for AgendaMais (prod) using an external H2 TCP server.
REM - Ensures an H2 jar is available in scripts\lib (copies from local Maven repo if found)
REM - Starts the fat JAR in background and redirects logs to app-prod.log

setlocal
cd /d "%~dp0\.."
set "SCRIPTDIR=%~dp0"
set "LIB_DIR=%SCRIPTDIR%lib"
set "H2_VERSION=2.2.224"
set "M2_H2=%USERPROFILE%\.m2\repository\com\h2database\h2\%H2_VERSION%\h2-%H2_VERSION%.jar"

if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
if exist "%LIB_DIR%\h2-%H2_VERSION%.jar" (
  set "LOADERPATH=%LIB_DIR%\h2-%H2_VERSION%.jar"
) else if exist "%M2_H2%" (
  copy /Y "%M2_H2%" "%LIB_DIR%\" >nul
  set "LOADERPATH=%LIB_DIR%\h2-%H2_VERSION%.jar"
) else (
  set "LOADERPATH="
)

set "JAR=target\agenda-mais-0.0.1-SNAPSHOT.jar"
if not exist "%JAR%" (
  echo ERROR: application jar not found at %JAR% - run "mvn package" first
  endlocal
  exit /b 1
)

set "LOG=app-prod.log"
set "URL=jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-prod;DB_CLOSE_DELAY=-1;MODE=MySQL"
set "DRIVER=org.h2.Driver"
set "FLYWAY_BASELINE=true"

echo Starting AgendaMais (prod) - logs -> %LOG%
if defined LOADERPATH (
  start "AgendaMais - prod" /B cmd /c "java -Dloader.path=\"%LOADERPATH%\" -jar \"%JAR%\" --spring.profiles.active=prod --server.port=8080 --spring.datasource.url=\"%URL%\" --spring.datasource.driver-class-name=%DRIVER% --spring.flyway.baseline-on-migrate=%FLYWAY_BASELINE% --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost > \"%LOG%\" 2>&1"
) else (
  start "AgendaMais - prod" /B cmd /c "java -jar \"%JAR%\" --spring.profiles.active=prod --server.port=8080 --spring.datasource.url=\"%URL%\" --spring.datasource.driver-class-name=%DRIVER% --spring.flyway.baseline-on-migrate=%FLYWAY_BASELINE% --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost > \"%LOG%\" 2>&1"
)

endlocal
echo Started (background). Use "type %LOG%" or PowerShell Get-Content to view logs.
