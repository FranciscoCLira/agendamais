@echo off
REM Start an H2 TCP server that exposes the DBs in data/ so apps and tools (DBeaver) can connect.
cd /d "%~dp0\.."
set "H2JAR=%~dp0lib\h2-2.2.224.jar"
if not exist "%H2JAR%" (
  echo ERROR: H2 jar not found at %H2JAR% - ensure scripts\lib\h2-2.2.224.jar exists or run mvn to populate your local repo
  exit /b 1
)

echo Starting H2 TCP server (background), writing logs to h2-server.log
start "H2 Server" /B cmd /c "java -cp \"%H2JAR%\" org.h2.tools.Server -tcp -tcpPort 9092 -tcpAllowOthers -baseDir \"C:/DEV-IA2/agendamais/data\" > h2-server.log 2>&1"
echo H2 server start requested.
