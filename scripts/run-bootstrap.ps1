# PowerShell helper to run V4 bootstrap via Flyway
# Usage: open PowerShell in repo root, set the environment variables described in docs/bootstrap-local.md, then run:
#   .\scripts\run-bootstrap.ps1

if (-not ($env:RUN_BOOTSTRAP -and $env:RUN_BOOTSTRAP.ToLower() -eq 'true')) {
    Write-Host "RUN_BOOTSTRAP not set to 'true'. Aborting." -ForegroundColor Red
    exit 1
}

if (-not $env:FLYWAY_URL) {
    Write-Host "FLYWAY_URL not set. Please set FLYWAY_URL, FLYWAY_USER and FLYWAY_PASSWORD before running." -ForegroundColor Red
    exit 1
}

$flywayUrl = $env:FLYWAY_URL
$flywayUser = $env:FLYWAY_USER
$flywayPassword = $env:FLYWAY_PASSWORD

Write-Host "Running Flyway migrate against: $flywayUrl"

$mvnCmd = "mvn -DskipTests org.flywaydb:flyway-maven-plugin:9.16.0:migrate -Dflyway.url=$flywayUrl -Dflyway.user=$flywayUser -Dflyway.password=$flywayPassword -Dflyway.locations=classpath:db/migration"
Write-Host $mvnCmd

# Execute
Invoke-Expression $mvnCmd

if ($LASTEXITCODE -ne 0) {
    Write-Host "Flyway migrate failed (exit code $LASTEXITCODE)." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Flyway migrate finished." -ForegroundColor Green