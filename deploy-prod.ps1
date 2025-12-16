#Requires -Version 5.1
<#
.SYNOPSIS
    Automated deployment script for AgendaMais PROD environment
.DESCRIPTION
    - Stops existing Java processes
    - Rebuilds JAR (mvn clean package -DskipTests)
    - Starts Spring Boot app with prod profile
    - Monitors health check for startup validation
.PARAMETER Force
    Skip backup confirmation (use with caution)
.PARAMETER HealthCheckUrl
    Custom health check URL (default: http://localhost:8080/acesso)
.PARAMETER HealthCheckTimeout
    Max wait time for health check in seconds (default: 120)
.EXAMPLE
    .\deploy-prod.ps1
    .\deploy-prod.ps1 -Force
    .\deploy-prod.ps1 -HealthCheckUrl "http://localhost:8080/administrador" -HealthCheckTimeout 180
#>

param(
    [switch]$Force,
    [string]$HealthCheckUrl = "http://localhost:8080/acesso",
    [int]$HealthCheckTimeout = 120
)

$ErrorActionPreference = "Stop"
$WarningPreference = "Continue"

# ============================================================================
# CONFIG
# ============================================================================
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$JarPath = Join-Path $ProjectRoot "target\agenda-mais-0.0.1-SNAPSHOT.jar"
$LogFile = Join-Path $ProjectRoot "app-prod.log"
$BackupDir = Join-Path $ProjectRoot "backup-tools"
$StartTime = Get-Date

Write-Host "╔════════════════════════════════════════════════════════════════╗"
Write-Host "║         AgendaMais PROD Automated Deployment                   ║"
Write-Host "║         Started at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')                  ║"
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

# ============================================================================
# PRE-DEPLOYMENT CHECKS
# ============================================================================

Write-Host "`n[1/5] Validating environment..." -ForegroundColor Yellow

if (-not (Test-Path $ProjectRoot)) {
    throw "Project root not found: $ProjectRoot"
}

if (-not (Test-Path (Join-Path $ProjectRoot "pom.xml"))) {
    throw "Maven pom.xml not found. Invalid project root."
}

Write-Host "✓ Project structure valid" -ForegroundColor Green

# ============================================================================
# BACKUP CONFIRMATION
# ============================================================================

if (-not $Force) {
    Write-Host "`n[2/5] Backup confirmation..." -ForegroundColor Yellow
    if (Test-Path $BackupDir) {
        Write-Host "ℹ Backup tools found at: $BackupDir" -ForegroundColor Cyan
        $response = Read-Host "Have you completed a backup? (y/n)"
        if ($response -ne 'y' -and $response -ne 'Y') {
            Write-Warning "Deployment cancelled. Please run backup first."
            exit 1
        }
    }
    Write-Host "✓ Backup confirmed" -ForegroundColor Green
}
else {
    Write-Host "`n[2/5] Skipping backup confirmation (-Force flag set)" -ForegroundColor Yellow
}

# ============================================================================
# STOP EXISTING JAVA PROCESSES
# ============================================================================

Write-Host "`n[3/5] Stopping existing Java processes..." -ForegroundColor Yellow

$javaProcs = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -eq "java" }

if ($javaProcs) {
    Write-Host "Found $($javaProcs.Count) Java process(es). Stopping..." -ForegroundColor Cyan
    $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 3
    Write-Host "✓ Java processes stopped" -ForegroundColor Green
}
else {
    Write-Host "ℹ No Java processes running" -ForegroundColor Cyan
}

# ============================================================================
# BUILD JAR
# ============================================================================

Write-Host "`n[4/5] Building JAR (mvn clean package -DskipTests)..." -ForegroundColor Yellow

Push-Location $ProjectRoot
try {
    $buildOutput = mvn clean package -DskipTests 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Build failed" -ForegroundColor Red
        Write-Host ($buildOutput -join "`n") -ForegroundColor Red
        throw "Maven build failed with exit code $LASTEXITCODE"
    }
    
    if (-not (Test-Path $JarPath)) {
        throw "JAR file not created at: $JarPath"
    }
    
    Write-Host "✓ JAR built successfully" -ForegroundColor Green
    Write-Host "  JAR: $JarPath" -ForegroundColor Cyan
}
finally {
    Pop-Location
}

# ============================================================================
# START APPLICATION
# ============================================================================

Write-Host "`n[5/5] Starting AgendaMais PROD..." -ForegroundColor Yellow

Push-Location $ProjectRoot
try {
    # Start java process in background
    $startCmd = "java -jar `"$JarPath`" --spring.profiles.active=prod --app.reload-data=false"
    Write-Host "Command: $startCmd" -ForegroundColor Cyan
    
    # Redirect output to log file
    Start-Process -FilePath "powershell.exe" `
        -ArgumentList "-NoProfile -Command `"$startCmd >> '$LogFile' 2>&1`"" `
        -NoNewWindow -RedirectStandardOutput $null -RedirectStandardError $null
    
    Write-Host "✓ Application started (PID in background)" -ForegroundColor Green
    Write-Host "  Log file: $LogFile" -ForegroundColor Cyan
    
    # ========================================================================
    # HEALTH CHECK
    # ========================================================================
    
    Write-Host "`n[HEALTH CHECK] Waiting for application startup..." -ForegroundColor Yellow
    Write-Host "  URL: $HealthCheckUrl" -ForegroundColor Cyan
    Write-Host "  Timeout: ${HealthCheckTimeout}s" -ForegroundColor Cyan
    
    $startTime = Get-Date
    $healthy = $false
    $attempts = 0
    $maxAttempts = [Math]::Ceiling($HealthCheckTimeout / 5)
    
    while ((New-TimeSpan -Start $startTime).TotalSeconds -lt $HealthCheckTimeout -and $attempts -lt $maxAttempts) {
        $attempts++
        Start-Sleep -Seconds 5
        
        try {
            $response = Invoke-WebRequest -Uri $HealthCheckUrl -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $healthy = $true
                break
            }
        }
        catch {
            Write-Host "  [$attempts/$maxAttempts] Waiting for startup... (${$([Math]::Floor((New-TimeSpan -Start $startTime).TotalSeconds))}s)" -ForegroundColor Gray
        }
    }
    
    if ($healthy) {
        $totalTime = New-TimeSpan -Start $startTime
        Write-Host "✓ Application is HEALTHY" -ForegroundColor Green
        Write-Host "  Response time: $([Math]::Round($totalTime.TotalSeconds, 2))s" -ForegroundColor Cyan
        Write-Host "`n  🎉 Deployment successful!" -ForegroundColor Green
        Write-Host "  Access at: http://localhost:8080" -ForegroundColor Green
    }
    else {
        Write-Host "✗ Application health check FAILED after ${HealthCheckTimeout}s" -ForegroundColor Red
        Write-Host "  Check logs at: $LogFile" -ForegroundColor Red
        Write-Host "`n  Possible issues:" -ForegroundColor Yellow
        Write-Host "  - Database not accessible" -ForegroundColor Yellow
        Write-Host "  - Port 8080 already in use" -ForegroundColor Yellow
        Write-Host "  - Application startup error" -ForegroundColor Yellow
        Write-Host "`n  Last 20 lines of log:" -ForegroundColor Yellow
        if (Test-Path $LogFile) {
            Get-Content $LogFile -Tail 20 | Write-Host
        }
        exit 1
    }
    
}
finally {
    Pop-Location
}

# ============================================================================
# SUMMARY
# ============================================================================

$totalTime = New-TimeSpan -Start $StartTime
Write-Host "`n╔════════════════════════════════════════════════════════════════╗"
Write-Host "║ DEPLOYMENT COMPLETE                                            ║"
Write-Host "║ Total time: $([Math]::Round($totalTime.TotalSeconds, 2))s" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
