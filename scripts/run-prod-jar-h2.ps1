# Run packaged jar in 'prod' profile using in-memory H2 (for local demo only)
$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:agendadb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL'
$env:SPRING_DATASOURCE_USERNAME = 'sa'
$env:SPRING_DATASOURCE_PASSWORD = ''
$env:SPRING_DATASOURCE_DRIVER = 'org.h2.Driver'
$env:SPRING_MAIL_HOST = 'localhost'
$env:SPRING_MAIL_PORT = '1025'
$env:SPRING_MAIL_USERNAME = 'foo'
$env:SPRING_MAIL_PASSWORD = 'bar'
$jar = Join-Path -Path (Get-Location) -ChildPath 'target\agenda-mais-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path $jar)) {
    Write-Error "Jar not found at $jar. Build first with 'mvn -DskipTests package'"
    exit 1
}
Write-Host "Running jar: $jar with SPRING_DATASOURCE_URL=$env:SPRING_DATASOURCE_URL"
java -jar $jar --spring.profiles.active=prod
