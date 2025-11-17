@echo off
REM Temp start script for PROD (created by assistant)
set SPRING_DATASOURCE_URL=jdbc:h2:tcp://localhost/C:/DEV-IA2/agendamais/data/agendadb-prod;DB_CLOSE_DELAY=-1;MODE=MySQL
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --server.port=8080 > app-prod.log 2>&1
