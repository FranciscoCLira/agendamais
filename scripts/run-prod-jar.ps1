# Run packaged jar in 'prod' profile using in-memory H2 (for local demo only)
$env:SPRING_DATASOURCE_URL = 'jdbc:h2:mem:agendadb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL'
$env:SPRING_DATASOURCE_USERNAME = 'sa'
$env:SPRING_DATASOURCE_PASSWORD = ''
$jar = Join-Path -Path (Get-Location) -ChildPath 'target\agenda-mais-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path $jar)) {
    Write-Error "Jar not found at $jar. Build first with 'mvn -DskipTests package'"
    exit 1
}
Write-Host "Running jar: $jar with SPRING_DATASOURCE_URL=$env:SPRING_DATASOURCE_URL"
Start-Process -NoNewWindow -FilePath java -ArgumentList "-jar", $jar, "--spring.profiles.active=prod" -WorkingDirectory (Get-Location).Path
Write-Host "Jar started (check logs)."