@echo off
REM Start prod-like profile using H2 TCP DB agendadb-prod in a detached window
set SPRING_DATASOURCE_URL=jdbc:h2:tcp://localhost:9092/C:/DEV-IA2/agendamais/data/agendadb-prod;DB_CLOSE_DELAY=-1;MODE=MySQL
echo Starting prod-like instance (port 8080) with SPRING_DATASOURCE_URL=%SPRING_DATASOURCE_URL%
start "AgendaMais-Prod" cmd /c "java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod,prod-local --server.port=8080 --app.reload-data=false > app-prod.log 2>&1"
exit /b 0
