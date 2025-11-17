@echo off
REM Start dev profile using separate H2 file DB to avoid locking prod DB.
REM Launch the java process in a new detached cmd window so it stays running.
set SPRING_DATASOURCE_URL=jdbc:h2:file:C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
echo Starting dev instance (port 8081) with SPRING_DATASOURCE_URL=%SPRING_DATASOURCE_URL%
start "AgendaMais-Dev" cmd /c "java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8081 > app-dev.log 2>&1"
exit /b 0
