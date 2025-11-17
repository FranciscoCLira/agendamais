@echo off
cd /d "%~dp0\.."
set "LOADERPATH=%~dp0lib\h2-2.2.224.jar"
set "JAR=target\agenda-mais-0.0.1-SNAPSHOT.jar"
if not exist "%JAR%" (
  echo ERROR: %JAR% not found. Run mvn package
  exit /b 1
)

echo Running prod jar in foreground (to capture startup output)...
java -Dloader.path="%LOADERPATH%" -jar "%JAR%" --spring.profiles.active=prod --server.port=8080 --spring.datasource.url="jdbc:h2:tcp://192.168.0.3/C:/DEV-IA2/agendamais/data/agendadb-prod;DB_CLOSE_DELAY=-1;MODE=MySQL" --spring.datasource.driver-class-name=org.h2.Driver --spring.flyway.baseline-on-migrate=true --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect --spring.mail.host=localhost
