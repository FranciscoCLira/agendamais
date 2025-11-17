@echo off
REM Temp start script for DEV (created by assistant)
set SPRING_DATASOURCE_URL=jdbc:h2:tcp://localhost/C:/DEV-IA2/agendamais/data/agendadb-dev;DB_CLOSE_DELAY=-1;MODE=MySQL
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8081 --app.reload-data=true > app-dev.log 2>&1
