Set-StrictMode -Version Latest

$testDir = Join-Path (Split-Path -Parent $PSScriptRoot) 'tests'
if (-Not (Test-Path $testDir)) { Write-Error "Test dir not found: $testDir"; exit 1 }

Write-Host "Running Pester tests in $testDir..."
Import-Module Pester -ErrorAction Stop
$result = Invoke-Pester -Script (Join-Path $testDir 'run-with-env.tests.ps1') -PassThru

if ($result.FailedCount -gt 0) {
    Write-Error "Pester tests failed: $($result.FailedCount) tests failing"
    exit 1
} else {
    Write-Host "Pester tests passed"
    exit 0
}
