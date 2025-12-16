#Requires -Version 5.1
<#
.SYNOPSIS
    Rollback script for AgendaMais PROD deployment
.DESCRIPTION
    - Stops current Java process
    - Restores JAR from last backup or git tag
    - Restarts application with previous stable version
.PARAMETER Version
    Git tag to rollback to (default: v2025.12.15)
.PARAMETER BackupJar
    Path to backup JAR file to restore
.EXAMPLE
    .\rollback-prod.ps1
    .\rollback-prod.ps1 -Version v2025.12.14
    .\rollback-prod.ps1 -BackupJar "C:\backups\agenda-mais-old.jar"
#>

param(
    [string]$Version = "v2025.12.15",
    [string]$BackupJar
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$JarPath = Join-Path $ProjectRoot "target\agenda-mais-0.0.1-SNAPSHOT.jar"
$LogFile = Join-Path $ProjectRoot "app-prod.log"

Write-Host "╔════════════════════════════════════════════════════════════════╗"
Write-Host "║         AgendaMais PROD Rollback Script                        ║"
Write-Host "║         Started at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')                  ║"
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan

# ============================================================================
# STOP JAVA PROCESS
# ============================================================================

Write-Host "`n[1/3] Stopping current Java process..." -ForegroundColor Yellow

$javaProcs = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "✓ Java process stopped" -ForegroundColor Green
} else {
    Write-Host "ℹ No Java process running" -ForegroundColor Cyan
}

# ============================================================================
# RESTORE JAR
# ============================================================================

Write-Host "`n[2/3] Restoring JAR from version: $Version..." -ForegroundColor Yellow

if ($BackupJar -and (Test-Path $BackupJar)) {
    # Restore from backup file
    Copy-Item -Path $BackupJar -Destination $JarPath -Force
    Write-Host "✓ JAR restored from backup: $BackupJar" -ForegroundColor Green
} else {
    # Rebuild from git tag
    Push-Location $ProjectRoot
    try {
        git checkout $Version -- . 2>&1 | Out-Null
        Write-Host "✓ Checked out tag: $Version" -ForegroundColor Green
        
        Write-Host "Building JAR from tag $Version..." -ForegroundColor Cyan
        mvn clean package -DskipTests 2>&1 | Out-Null
        Write-Host "✓ JAR rebuilt successfully" -ForegroundColor Green
    } catch {
        Write-Host "✗ Failed to restore from tag" -ForegroundColor Red
        throw $_
    } finally {
        Pop-Location
    }
}

# ============================================================================
# RESTART APPLICATION
# ============================================================================

Write-Host "`n[3/3] Restarting application..." -ForegroundColor Yellow

Push-Location $ProjectRoot
try {
    $startCmd = "java -jar `"$JarPath`" --spring.profiles.active=prod --app.reload-data=false"
    Start-Process -FilePath "powershell.exe" `
                  -ArgumentList "-NoProfile -Command `"$startCmd >> '$LogFile' 2>&1`"" `
                  -NoNewWindow -RedirectStandardOutput $null -RedirectStandardError $null
    
    Write-Host "✓ Application restarted" -ForegroundColor Green
    Write-Host "`nℹ Check health at: http://localhost:8080/acesso" -ForegroundColor Cyan
    Write-Host "ℹ Logs at: $LogFile" -ForegroundColor Cyan
} finally {
    Pop-Location
}

Write-Host "`n✓ Rollback complete" -ForegroundColor Green
Write-Host "Version: $Version" -ForegroundColor Cyan
