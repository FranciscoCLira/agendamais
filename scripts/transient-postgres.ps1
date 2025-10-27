<#
Transient Postgres helper (PowerShell)

Usage: run from repo root in PowerShell (requires Docker installed/running and mvn available)
.
# Start: .\scripts\transient-postgres.ps1 start
# Migrate: .\scripts\transient-postgres.ps1 migrate
# Run app: .\scripts\transient-postgres.ps1 run-app
# Full flow: .\scripts\transient-postgres.ps1 full
# Tear down: .\scripts\transient-postgres.ps1 stop
#
# The script creates a container named 'agendamais-postgres' by default on port 5432.
# It uses user/password 'flyway' / 'flyway' and database 'agendadb'.
# The script will set environment variables for Flyway and the app when performing migrate/run-app.
#>
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("start", "migrate", "run-app", "full", "stop", "status")]
    [string]$Action
)

$containerName = "agendamais-postgres"
$pgUser = "flyway"
$pgPassword = "flyway"
$pgDb = "agendadb"
$pgPort = 15432

function Start-Container {
    Write-Host "Starting Postgres container: $containerName"
    # If container exists, remove it first to ensure a clean start
    $exists = (docker.exe ps -a --format "{{.Names}}" | Select-String -Pattern "^$containerName$") -ne $null
    if ($exists) {
        Write-Host "Removing existing container $containerName"
        & docker.exe rm -f $containerName > $null 2>&1
        if ($LASTEXITCODE -ne 0) { Write-Warning "docker rm exited with $LASTEXITCODE" }
    }

    $runArgs = @('run','-d','--name',$containerName,'-e',"POSTGRES_USER=$pgUser",'-e',"POSTGRES_PASSWORD=$pgPassword",'-e',"POSTGRES_DB=$pgDb",'-p',"$($pgPort):5432",'postgres:13')
    Write-Host "Running: docker.exe with arguments:" 
    $runArgs | ForEach-Object { Write-Host "  ARG: '$_'" }
    & docker.exe @runArgs
    Write-Host "docker run exit code: $LASTEXITCODE"
    if ($LASTEXITCODE -ne 0) { Write-Error "docker run failed (exit $LASTEXITCODE)"; exit 1 }

    Write-Host "Waiting for Postgres to be ready..."
    for ($i = 0; $i -lt 30; $i++) {
        & docker.exe exec $containerName pg_isready -U $pgUser -d $pgDb > $null 2>&1
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 2
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Postgres did not become ready in time"
        exit 1
    }
    Write-Host "Postgres is ready"
}

function Run-Flyway-Migrate {
    Write-Host "Running Flyway migrate against local Postgres"
    # Set env vars for Flyway and app
    $env:RUN_BOOTSTRAP = 'true'
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:$pgPort/$pgDb"
    $env:SPRING_DATASOURCE_USERNAME = $pgUser
    $env:SPRING_DATASOURCE_PASSWORD = $pgPassword

    # Optionally set SUPER_* / ADMIN_* env vars here if you want the accounts created
    if (-not $env:SUPER_USERNAME) {
        Write-Host "SUPER_USERNAME not set; skipping superuser creation unless you set SUPER_USERNAME/SUPER_PASSWORD/SUPER_EMAIL in the environment"
    }

    # Invoke the flyway maven plugin. The plugin is configured in pom.xml and includes the PostgreSQL JDBC driver
    $args = @("org.flywaydb:flyway-maven-plugin:10.10.0:migrate", "-DskipTests", "-Dflyway.url=$($env:SPRING_DATASOURCE_URL)", "-Dflyway.user=$($env:SPRING_DATASOURCE_USERNAME)", "-Dflyway.password=$($env:SPRING_DATASOURCE_PASSWORD)", "-Dflyway.locations=classpath:db/migration")
    Write-Host ("mvn " + ($args -join " "))
    $proc = Start-Process -FilePath "mvn" -ArgumentList $args -NoNewWindow -Wait -PassThru
    if ($proc.ExitCode -ne 0) { throw "Flyway migrate failed (exit $($proc.ExitCode))" }
}

function Run-App {
    Write-Host "Starting application in 'prod' profile against the transient Postgres DB"
    # Ensure envs are present
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:$pgPort/$pgDb"
    $env:SPRING_DATASOURCE_USERNAME = $pgUser
    $env:SPRING_DATASOURCE_PASSWORD = $pgPassword
    $env:RUN_BOOTSTRAP = 'true'

    # Run app in a separate process so this script can continue (start in background)
    # We'll launch via 'mvn spring-boot:run' which respects Spring profiles and env vars
    Start-Process -NoNewWindow -FilePath mvn -ArgumentList "-Dspring-boot.run.profiles=prod", "spring-boot:run" -WorkingDirectory (Get-Location).Path
    Write-Host "Application started (check console or logs)."
}

function Stop-Container {
    Write-Host "Stopping and removing container $containerName"
    & docker.exe rm -f $containerName > $null 2>&1
    if ($LASTEXITCODE -ne 0) { Write-Warning "docker rm exited with $LASTEXITCODE" }
}

switch ($Action) {
    'start' { Start-Container }
    'migrate' { Run-Flyway-Migrate }
    'run-app' { Run-App }
    'full' { Start-Container; Run-Flyway-Migrate; Run-App }
    'stop' { Stop-Container }
    'status' { docker.exe ps -a --filter "name=$containerName" }
}
