@echo off
REM Run the prod profile against a file-backed H2 DB for local testing.
REM Place this file at repository root under scripts\run-prod-h2.bat and run from project root.

cd /d "%~dp0\.."

echo Starting AgendaMais (prod profile) using local H2 file DB...

set "SPRING_DATASOURCE_URL=jdbc:h2:file:./data/prod;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
set "SPRING_DATASOURCE_USERNAME=sa"
set "SPRING_DATASOURCE_PASSWORD="
set "SPRING_MAIL_HOST=localhost"
set "SPRING_MAIL_PORT=3025"
set "SPRING_MAIL_USERNAME=dummy"
set "SPRING_MAIL_PASSWORD=dummy"
set "SPRING_MAIL_FROM=local@localhost"
set "APP_RELOAD_DATA=true"

java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.flyway.enabled=false --spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.url="jdbc:h2:file:./data/prod;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
@echo off
REM Run prod-profile app using a local file-backed H2 DB for testing
cd /d %~dp0\..\
set "SPRING_DATASOURCE_URL=jdbc:h2:file:./data/prod;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
set "SPRING_DATASOURCE_DRIVER=org.h2.Driver"
set "SPRING_DATASOURCE_USERNAME=sa"
set "SPRING_DATASOURCE_PASSWORD="
set "SPRING_MAIL_HOST=localhost"
set "SPRING_MAIL_PORT=3025"
set "SPRING_MAIL_USERNAME=dummy"
set "SPRING_MAIL_PASSWORD=dummy"
set "SPRING_MAIL_FROM=local@localhost"
set "APP_RELOAD_DATA=true"
echo Starting prod jar with H2 (data in .\data\prod)...
java -jar target\agenda-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --app.reload-data=true --spring.flyway.enabled=false --spring.jpa.hibernate.ddl-auto=update --spring.sql.init.mode=never --spring.datasource.url=%SPRING_DATASOURCE_URL% --spring.datasource.driver-class-name=%SPRING_DATASOURCE_DRIVER% --spring.datasource.username=%SPRING_DATASOURCE_USERNAME% --spring.datasource.password=%SPRING_DATASOURCE_PASSWORD%
pause
