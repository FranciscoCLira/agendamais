# Run application in 'prod' profile using in-memory H2 (for local demo only)
$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:agendadb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL'
$env:SPRING_DATASOURCE_USERNAME = 'sa'
$env:SPRING_DATASOURCE_PASSWORD = ''
Write-Host "Starting app with SPRING_DATASOURCE_URL=$env:SPRING_DATASOURCE_URL"
mvn -Dspring-boot.run.profiles=prod spring-boot:run
