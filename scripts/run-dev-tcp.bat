@echo off
REM Launcher for AgendaMais (dev) using external H2 TCP server.
REM - Copies H2 jar from local Maven repo if missing in scripts\lib
REM - Starts the fat JAR in background and redirects logs to app-dev.log

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

set "LOG=app-dev.log"
set "URL=jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL"
set "DRIVER=org.h2.Driver"

echo Starting AgendaMais (dev) - logs -> %LOG%
if defined LOADERPATH (
  start "AgendaMais - dev" /B cmd /c "java -Dloader.path=\"%LOADERPATH%\" -jar \"%JAR%\" --spring.profiles.active=dev-noh2 --server.port=8081 --spring.datasource.url=\"%URL%\" --spring.datasource.driver-class-name=%DRIVER% --app.reload-data=true --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost > \"%LOG%\" 2>&1"
) else (
  start "AgendaMais - dev" /B cmd /c "java -jar \"%JAR%\" --spring.profiles.active=dev-noh2 --server.port=8081 --spring.datasource.url=\"%URL%\" --spring.datasource.driver-class-name=%DRIVER% --app.reload-data=true --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost > \"%LOG%\" 2>&1"
)

endlocal
echo Started (background). Use "type %LOG%" or PowerShell Get-Content to view logs.
@echo off
REM Start AgendaMais (dev) using external H2 TCP server on port 8081 and reload data
setlocal
set SCRIPT_DIR=%~dp0
set H2JAR=%SCRIPT_DIR%lib\h2-2.2.224.jar
if not exist "%H2JAR%" (
  echo H2 jar not found at %H2JAR% - please place h2-2.2.224.jar into scripts\lib
  exit /b 1
)
set JAR=target\agenda-mais-0.0.1-SNAPSHOT.jar
if not exist "%JAR%" (
  echo Application jar not found at %JAR% - build the project first (mvn package)
  exit /b 1
)
set LOG=app-dev.log
echo Starting AgendaMais (dev) on port 8081 - logging to %LOG%
start "AgendaMais Dev" cmd /c "java -Dloader.path=\"%H2JAR%\" -jar \"%JAR%\" --spring.profiles.active=dev --server.port=8081 --app.reload-data=true --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.url=\"jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL\" --spring.flyway.enabled=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost > \"%LOG%\" 2>&1"
endlocal
@echo off
rem Run AgendaMais in local "dev" profile using external H2 TCP DB
rem Usage: scripts\run-dev-tcp.bat

setlocal
set JAR=%~dp0..\target\agenda-mais-0.0.1-SNAPSHOT.jar
set LIB_H2=%~dp0lib\h2-2.2.224.jar

if exist "%LIB_H2%" (
  set LOADER_OPT=-Dloader.path="%LIB_H2%"
) else (
  set LOADER_OPT=
)

echo Starting agenda-mais (dev) with external H2 TCP DB...

java %LOADER_OPT% -jar "%JAR%" ^
  --spring.profiles.active=dev ^
  --server.port=8081 ^
  --spring.datasource.url="jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL" ^
  --spring.datasource.driver-class-name=org.h2.Driver ^
  --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect ^
  --spring.flyway.baseline-on-migrate=true ^
  --spring.mail.host=localhost

endlocal
@echo off
REM Start AgendaMais DEV against external H2 TCP server (background) on port 8081
setlocal
set H2JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar
if not exist "%H2JAR%" (
  REM fallback to a local extracted copy if present
  set H2JAR=%~dp0\tmpjar\BOOT-INF\lib\h2-2.2.224.jar
)
if not exist "%H2JAR%" (
  echo ERROR: H2 jar not found at %USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\ or %~dp0\tmpjar\BOOT-INF\lib
  exit /b 1
)

echo Starting AgendaMais (dev) in background on port 8081, logging to app-dev.log
start "AgendaMais-Dev" /B cmd /c "java -Dloader.path=%H2JAR% -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8081 --app.reload-data=true --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.url=jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL --spring.flyway.enabled=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect > app-dev.log 2>&1"

endlocal
@echo off
REM Start AgendaMais (dev) using external H2 TCP server and explicit H2 driver
setlocal
set "JAR=target\agenda-mais-0.0.1-SNAPSHOT.jar"
set "H2JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
set "LOG=app-dev.log"

echo Starting DEV (port 8081): writing logs to %LOG%
start "AgendaMais DEV" cmd /c "java -Dloader.path=\"%H2JAR%\" -jar \"%JAR%\" --spring.profiles.active=dev --server.port=8081 --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.url=jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL --spring.flyway.enabled=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --app.reload-data=true > \"%LOG%\" 2>&1"
endlocal
@echo off
rem Start dev with external H2 on TCP. Copies local maven H2 jar to scripts\lib if missing.
setlocal
set H2_VERSION=2.2.224
set H2_M2=%USERPROFILE%\.m2\repository\com\h2database\h2\%H2_VERSION%\h2-%H2_VERSION%.jar
set SCRIPTDIR=%~dp0
if not exist "%SCRIPTDIR%lib" mkdir "%SCRIPTDIR%lib"
if not exist "%SCRIPTDIR%lib\h2-%H2_VERSION%.jar" (
  if exist "%H2_M2%" (
    copy /Y "%H2_M2%" "%SCRIPTDIR%lib\" >nul
  ) else (
    echo H2 jar not found at %H2_M2% >&2
    exit /b 1
  )
)
set LOADERPATH=%SCRIPTDIR%lib\h2-%H2_VERSION%.jar
set JAR=target\agenda-mais-0.0.1-SNAPSHOT.jar
set URL=jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL
echo Starting DEV jar with loader.path=%LOADERPATH% and datasource url=%URL%
java -Dloader.path="%LOADERPATH%" -jar %JAR% --spring.profiles.active=dev --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.url="%URL%" --spring.flyway.enabled=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost --server.port=8081 --app.reload-data=true > ..\app-dev.log 2>&1
endlocal
@echo off
REM Start dev profile pointing at external H2 TCP server and reload dev data.
cd /d "%~dp0\.."

echo Starting AgendaMais (dev profile) using external H2 TCP server...

set "H2_TCP_HOST=192.168.0.3"
set "H2_TCP_PORT=9092"
set "SPRING_DATASOURCE_URL=jdbc:h2:tcp://%H2_TCP_HOST%/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL"
set "SPRING_DATASOURCE_DRIVER=org.h2.Driver"
set "SPRING_DATASOURCE_USERNAME=sa"
set "SPRING_DATASOURCE_PASSWORD="
set "SPRING_MAIL_HOST=localhost"
set "SPRING_MAIL_PORT=3025"
set "SPRING_MAIL_USERNAME=dummy"
set "SPRING_MAIL_PASSWORD=dummy"
set "SPRING_MAIL_FROM=local@localhost"
set "APP_RELOAD_DATA=true"

set "LOADER_H2_JAR=tmpjar\BOOT-INF\lib\h2-2.2.224.jar"

echo Using loader.path=%LOADER_H2_JAR%

echo Starting dev jar (logs -> app-dev.log)...
REM Use the dev-noh2 profile to avoid starting the embedded TCP server (external H2 already running)
REM Run dev on port 8081 to avoid conflict with prod
start "AgendaMais - dev" /B cmd /c "java -Dloader.path=%LOADER_H2_JAR% -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --server.port=8081 --spring.profiles.active=dev-noh2 --app.reload-data=true --spring.datasource.url=%SPRING_DATASOURCE_URL% --spring.datasource.driver-class-name=%SPRING_DATASOURCE_DRIVER% --spring.flyway.enabled=false --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=%SPRING_MAIL_HOST% > app-dev.log 2>&1"
echo dev started (background). Tail logs with: type app-dev.log
